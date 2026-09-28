package com.debrajkundu.grabpic.config;

import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.debrajkundu.grabpic.security.JwtService;
import com.debrajkundu.grabpic.user.entity.User;
import com.debrajkundu.grabpic.user.service.UserService;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserService userService;

    public StompAuthChannelInterceptor(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new MessagingException("Missing bearer token for WebSocket connection");
            }

            String token = authorization.substring(7);
            String email = jwtService.extractUsername(token);
            User user = userService.getUserByEmail(email)
                    .filter(candidate -> jwtService.isTokenValid(token, candidate))
                    .orElseThrow(() -> new MessagingException("Invalid bearer token for WebSocket connection"));

            accessor.setUser(new UsernamePasswordAuthenticationToken(user, token, Collections.emptyList()));
        } else if (accessor.getUser() == null) {
            throw new MessagingException("Unauthenticated WebSocket message");
        }

        return message;
    }
}