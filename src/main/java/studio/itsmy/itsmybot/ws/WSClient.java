package studio.itsmy.itsmybot.ws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.util.PluginLogger;
import studio.itsmy.itsmybot.ws.handler.PlaceholderUtil;
import studio.itsmy.itsmybot.ws.handler.role.RoleSyncUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * WebSocket client responsible for connecting the plugin to the backend gateway.
 * <p>
 * Features:
 * <ul>
 *   <li>Secure connection (WSS) with optional trust-all strategy for self-signed certificates</li>
 *   <li>JWT-based authentication on {@link #onOpen(WebSocket, Response)}</li>
 *   <li>Automatic reconnection with backoff (fixed rate) when failures occur</li>
 *   <li>Request/response correlation via {@code id} and {@link CompletableFuture}</li>
 *   <li>Dispatching of server messages to domain handlers (role sync, placeholder)</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #connect()} establishes a new connection and requests auth.</li>
 *   <li>On {@link #onOpen(WebSocket, Response)}, a short-lived JWT is created and sent.</li>
 *   <li>On {@code AUTH_SUCCESS}, the client becomes {@code authenticated} and {@link #isReady()} returns true.</li>
 *   <li>On any failure/close, {@link #scheduleReconnect()} triggers periodic attempts (if {@code shouldReconnect}).</li>
 *   <li>{@link #disconnect()} stops reconnection and closes the socket.</li>
 *   <li>{@link #shutdown()} releases executors and OkHttp resources.</li>
 * </ol>
 *
 * <h2>Security</h2>
 * The client can be configured to trust all certificates (see {@link #createClientAllowingSelfSigned()}).
 * This simplifies development/self-signed deployments but reduces transport security guarantees.
 * In production, prefer a properly signed certificate and a strict {@link HostnameVerifier}.
 *
 * <h2>Thread-safety</h2>
 * - Pending requests are stored in a {@link ConcurrentHashMap}.<br>
 * - Reconnect scheduling is guarded by {@code synchronized} methods ({@link #scheduleReconnect()}, {@link #cancelReconnect()}).
 * - Connection state flags ({@code connected}, {@code authenticated}) are {@code volatile}.
 */
public class WSClient extends WebSocketListener {

    private final ItsMyBotPlugin plugin;

    /** In-flight request futures, keyed by correlation id. */
    private final Map<String, CompletableFuture<JsonObject>> pendingRequests = new ConcurrentHashMap<>();

    /** Single-thread scheduler for timeouts and reconnect attempts. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** JSON codec. */
    private final Gson gson = new Gson();

    private WebSocket webSocket;
    private final OkHttpClient client;
    private ScheduledFuture<?> reconnectTask;
    private volatile boolean shouldReconnect;

    private volatile boolean connected = false;
    private volatile boolean authenticated = false;

    /**
     * Creates a new WebSocket client instance.
     *
     * @param plugin main plugin instance
     */
    public WSClient(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
        this.client = createClientAllowingSelfSigned();
        this.shouldReconnect = false;
    }

    /**
     * Indicates whether the client is both connected and authenticated.
     *
     * @return {@code true} if the WebSocket is open and JWT auth succeeded
     */
    public boolean isReady() {
        return connected && authenticated;
    }

    /**
     * Initiates (or re-initiates) a WebSocket connection.
     * <p>
     * Validates host/port from {@link WSConfig}, closes any previous socket, and starts a fresh connection.
     * Also enables automatic reconnection until {@link #disconnect()} is called.
     */
    public void connect() {
        shouldReconnect = true;
        cancelReconnect();

        if (webSocket != null) {
            webSocket.close(1000, "Reconnecting");
        }

        final String host = WSConfig.getHost();
        final int port = WSConfig.getPort();

        if (host == null || host.isEmpty()) {
            PluginLogger.error("WebSocket host is not configured. Please check your settings.");
            return;
        }

        if (port <= 0 || port > 65535) {
            PluginLogger.error("WebSocket port is not valid. Please check your settings.");
            return;
        }

        final Request request = new Request.Builder()
                .url("wss://" + WSConfig.getHost() + ":" + WSConfig.getPort())
                .build();

        client.newWebSocket(request, this);
    }

    /**
     * Explicitly disconnects the WebSocket and disables automatic reconnection.
     * <p>
     * Resets {@code connected/authenticated} flags and cancels any scheduled reconnect task.
     */
    public void disconnect() {
        shouldReconnect = false;
        cancelReconnect();

        this.connected = false;
        this.authenticated = false;

        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
    }

    /**
     * Shuts down internal resources (scheduler and OkHttp executor/connection pool).
     * <p>
     * Call this once on plugin disable to avoid thread leaks.
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        cancelReconnect();

        if (client != null) {
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
        }
    }

    /**
     * Called by OkHttp when a connection is established.
     * <p>
     * Sets {@code connected}, clears any pending reconnect, logs success,
     * and sends a short-lived JWT auth message to the server.
     */
    @Override
    public void onOpen(WebSocket webSocket, @NotNull Response response) {
        this.webSocket = webSocket;
        this.connected = true;
        this.authenticated = false;
        cancelReconnect();

        PluginLogger.info("WebSocket connection established.");

        final String token = JWT.create()
                .withClaim("server_id", WSConfig.getServerId())
                .withExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .sign(Algorithm.HMAC256(WSConfig.getJwtSecret()));

        final String authMessage = String.format(
                "{\"type\": \"AUTH\", \"token\": \"%s\", \"server_id\": \"%s\"}",
                token, WSConfig.getServerId()
        );

        webSocket.send(authMessage);
    }

    /**
     * Called when a text message is received from the server.
     * <p>
     * Routes messages by {@code type} to feature handlers (auth, role sync, placeholder).
     * If an {@code id} is present and corresponds to a pending request, the associated future is completed.
     *
     * @param webSocket the socket
     * @param text      raw JSON text
     */
    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        final JsonObject json = gson.fromJson(text, JsonObject.class);
        final String type = json.get("type").getAsString();
        final String id = json.has("id") ? json.get("id").getAsString() : null;
        boolean handled = false;

        switch (type) {
            case "AUTH_SUCCESS":
            case "AUTH_FAIL":
                handleAuthResponse(json, type);
                handled = true;
                break;
            case "SYNC_ROLE":
                RoleSyncUtil.handleSyncRole(plugin, json);
                handled = true;
                break;
            case "PLACEHOLDER":
                PlaceholderUtil.handlePlaceholderRequest(plugin, json);
                handled = true;
                break;
            default:
                break;
        }

        if (id != null) {
            final CompletableFuture<JsonObject> future = pendingRequests.remove(id);
            if (future != null) {
                future.complete(json);
                handled = true;
            }
        }

        if (!handled) {
            PluginLogger.warn("Received unhandled message: " + text);
        }
    }

    /**
     * Handles authentication responses ({@code AUTH_SUCCESS}/{@code AUTH_FAIL}).
     * <p>
     * On success, marks the client authenticated; on failure, logs reason and closes the socket.
     *
     * @param json auth payload
     * @param type response type
     */
    private void handleAuthResponse(JsonObject json, String type) {
        if (type.equals("AUTH_SUCCESS")) {
            this.authenticated = true;
            PluginLogger.info("Authentication successful. WebSocket is ready for use.");
        } else if (type.equals("AUTH_FAIL")) {
            this.authenticated = false;
            String errorMessage = json.has("error") ? json.get("error").getAsString() : "Unknown error";
            PluginLogger.error("Authentication failed: " + errorMessage);
            webSocket.close(1000, "Authentication failed");
        } else {
            PluginLogger.warn("Received unexpected authentication response type: " + type);
        }
    }

    /**
     * Sends a JSON response (server-initiated request handling) optionally binding the given {@code id}.
     *
     * @param response JSON payload to send
     * @param id       correlation id to attach (added if missing and non-null)
     */
    public void sendResponse(JsonObject response, String id) {
        if (response == null) return;
        if (id != null && !response.has("id")) {
            response.addProperty("id", id);
        }
        if (webSocket == null) {
            PluginLogger.error("WebSocket is not connected; cannot send response: " + response);
            return;
        }
        try {
            webSocket.send(gson.toJson(response));
        } catch (Exception ex) {
            PluginLogger.error("Failed to send WebSocket response: " + ex.getMessage());
        }
    }

    /**
     * Called when a failure occurs (I/O, TLS, protocol).
     * <p>
     * Resets state and schedules reconnect attempts if enabled.
     */
    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        this.connected = false;
        this.authenticated = false;
        scheduleReconnect();
    }

    /**
     * Called when the server initiates closing; acknowledges with normal close code.
     */
    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(1000, null);
    }

    /**
     * Called after the socket is closed.
     * <p>
     * Logs closure reason/code, resets state, and schedules reconnect attempts if enabled.
     */
    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        this.connected = false;
        this.authenticated = false;

        String str = "WebSocket connection closed";
        if (!reason.isEmpty()) {
            str += "with reason: " + reason;
        }

        if (code != 1000 && code != 1005) {
            str += " (" + code + ")";
        }

        if (!reason.isEmpty()) {
            str += " - " + reason;
        }

        PluginLogger.warn(str);
        scheduleReconnect();
    }

    /**
     * Sends a raw JSON string to the server (fire-and-forget).
     *
     * @param json raw JSON text
     */
    public void sendMessage(String json) {
        if (webSocket != null) {
            webSocket.send(json);
        }
    }

    /**
     * Builds an {@link OkHttpClient} that trusts all certificates and applies a relaxed hostname verifier.
     * <p>
     * <strong>Warning:</strong> This reduces TLS security and is intended for development
     * or controlled environments where a self-signed certificate is required.
     *
     * @return configured OkHttp client
     * @throws IllegalStateException if SSL context initialization fails
     */
    private OkHttpClient createClientAllowingSelfSigned() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            // No-op, trust all client certificates
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            // No-op, trust all server certificates
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            final HostnameVerifier verifier = (hostname, session) -> {
                String expectedHost = WSConfig.getHost();
                boolean match = hostname.equalsIgnoreCase(expectedHost) || hostname.equals("localhost");
                if (!match) {
                    PluginLogger.warn("Hostname mismatch: expected " + expectedHost + ", got " + hostname);
                }
                return match;
            };

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(verifier)
                    .build();

        } catch (Exception e) {
            PluginLogger.error("Failed to create WebSocket client: " + e.getMessage());
            throw new IllegalStateException("WebSocket client configuration failed", e);
        }
    }

    /**
     * Sends a request expecting a response correlated by {@code id}.
     * <p>
     * The {@code id} is added to the outgoing message, a future is registered in {@link #pendingRequests},
     * and a timeout (5s) is scheduled. When a matching response arrives in {@link #onMessage(WebSocket, String)},
     * the future completes successfully; otherwise it completes exceptionally on timeout.
     *
     * @param message request payload (will be mutated to include {@code id})
     * @param id      correlation id (must be unique per request)
     * @return a future completing with the response JSON
     */
    public CompletableFuture<JsonObject> sendRequest(JsonObject message, String id) {
        message.addProperty("id", id);

        final CompletableFuture<JsonObject> future = new CompletableFuture<>();
        pendingRequests.put(id, future);

        scheduler.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Timeout while waiting for response: " + id));
                pendingRequests.remove(id);
            }
        }, 5, TimeUnit.SECONDS);

        sendMessage(message.toString());
        future.whenComplete((res, ex) -> pendingRequests.remove(id));

        return future;
    }

    /**
     * Schedules periodic reconnection attempts (every 30 seconds) if not already scheduled
     * and reconnection is enabled.
     */
    private synchronized void scheduleReconnect() {
        if (!shouldReconnect) {
            return;
        }
        if (reconnectTask != null && !reconnectTask.isDone()) {
            return;
        }
        PluginLogger.warn("Failed to connect to WebSocket server. Reconnecting in 30 seconds...");
        reconnectTask = scheduler.scheduleAtFixedRate(this::connect, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Cancels any scheduled reconnection task.
     */
    private synchronized void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }
}
