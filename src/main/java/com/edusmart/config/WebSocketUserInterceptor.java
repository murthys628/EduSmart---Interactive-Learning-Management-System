package com.edusmart.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class WebSocketUserInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Principal userPrincipal = servletRequest.getServletRequest().getUserPrincipal();
            if (userPrincipal != null) {
                // 🛑 CRITICAL FIX: Use the key "principal"
                attributes.put("principal", userPrincipal); 
                System.out.println("✅ WebSocket handshake successful for user: " + userPrincipal.getName());
            } else {
                // If a user is not authenticated, they should not establish a STOMP connection.
                // Depending on your security configuration, you might want to return false here
                // to reject unauthenticated users. For now, we allow true but log the issue.
                System.out.println("⚠️ No authenticated Principal found during WebSocket handshake. Connection proceeding anonymously.");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}