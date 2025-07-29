package com.ordwen.itsmybot.ws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.configuration.essential.WSConfig;
import com.ordwen.itsmybot.util.PluginLogger;
import com.ordwen.itsmybot.ws.handler.role.RoleSyncUtil;
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

public class WSClient extends WebSocketListener {

    private final ItsMyBotPlugin plugin;

    private final Map<String, CompletableFuture<JsonObject>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Gson gson = new Gson();

    private WebSocket webSocket;
    private final OkHttpClient client;
    private ScheduledFuture<?> reconnectTask;
    private volatile boolean shouldReconnect;

    public WSClient(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
        this.client = createClientAllowingSelfSigned();
        this.shouldReconnect = false;
    }

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

    public void disconnect() {
        shouldReconnect = false;
        cancelReconnect();

        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
    }

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

    @Override
    public void onOpen(WebSocket webSocket, @NotNull Response response) {
        this.webSocket = webSocket;
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

    private void handleAuthResponse(JsonObject json, String type) {
        if (type.equals("AUTH_SUCCESS")) {
            PluginLogger.info("Authentication successful. WebSocket is ready for use.");
        } else if (type.equals("AUTH_FAIL")) {
            String errorMessage = json.has("error") ? json.get("error").getAsString() : "Unknown error";
            PluginLogger.error("Authentication failed: " + errorMessage);
            webSocket.close(1000, "Authentication failed");
        } else {
            PluginLogger.warn("Received unexpected authentication response type: " + type);
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        //PluginLogger.warn("Failed to communicate with WS server. Details: " + t.getMessage());
        //PluginLogger.warn("If this is happening on reload, you can ignore this message.");
        //if (response != null) {
        //    PluginLogger.error("Response: " + response.message());
        //}

        scheduleReconnect();
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(1000, null);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
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

    public void sendMessage(String json) {
        if (webSocket != null) {
            webSocket.send(json);
        }
    }

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

    private synchronized void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }
}
