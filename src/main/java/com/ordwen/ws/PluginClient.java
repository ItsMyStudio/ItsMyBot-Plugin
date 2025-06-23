package com.ordwen.ws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ordwen.configuration.essentials.WebSocketClient;
import com.ordwen.utils.PluginLogger;
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

public class PluginClient extends WebSocketListener {

    private WebSocket webSocket;
    private OkHttpClient client;

    public void connect() {
        if (webSocket != null) {
            webSocket.close(1000, "Reconnecting");
        }

        this.client = createClientAllowingSelfSigned();

        final Request request = new Request.Builder()
                .url("wss://" + WebSocketClient.getHost() + ":" + WebSocketClient.getPort())
                .build();

        client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, @NotNull Response response) {
        this.webSocket = webSocket;

        final String token = JWT.create()
                .withClaim("server_id", WebSocketClient.getServerId())
                .withExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .sign(Algorithm.HMAC256(WebSocketClient.getJwtSecret()));

        final String authMessage = String.format(
                "{\"type\": \"AUTH\", \"token\": \"%s\", \"server_id\": \"%s\"}",
                token, WebSocketClient.getServerId()
        );

        webSocket.send(authMessage);

        PluginLogger.info("[WebSocket] Connected and sent AUTH");
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        PluginLogger.info("[WebSocket] " + text);
        // traite les messages ici
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        PluginLogger.error("Failed to connect to WebSocket server.");
        PluginLogger.error("Please verify that the bot is running and the host/port are correct.");
        PluginLogger.error("Details: " + t.getMessage());

        if (response != null) {
            PluginLogger.error("Response: " + response.message());
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        PluginLogger.info("[WebSocket] Closing: " + reason);
        webSocket.close(1000, null);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        PluginLogger.info("[WebSocket] Closed: " + reason);
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
                String expectedHost = WebSocketClient.getHost();
                boolean match = hostname.equalsIgnoreCase(expectedHost) || hostname.equals("localhost");
                if (!match) {
                    PluginLogger.warn("[WebSocket] Hostname mismatch: expected " + expectedHost + ", got " + hostname);
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
}
