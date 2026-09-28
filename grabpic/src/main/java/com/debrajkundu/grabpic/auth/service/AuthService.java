package com.debrajkundu.grabpic.auth.service;

import com.debrajkundu.grabpic.auth.dto.AuthResponse;
import com.debrajkundu.grabpic.auth.dto.LoginRequest;
import com.debrajkundu.grabpic.auth.dto.RegisterRequest;
import com.debrajkundu.grabpic.common.exception.InvalidCredentialsException;
import com.debrajkundu.grabpic.security.JwtService;
import com.debrajkundu.grabpic.user.entity.User;
import com.debrajkundu.grabpic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        User user = userService.registerUser(request.email(), request.password());
        return new AuthResponse(
                jwtService.generateToken(user)
        );
    }

    public AuthResponse login(LoginRequest request){
        User user = userService.getUserByEmail(request.email()).orElseThrow(
                InvalidCredentialsException::new
        );

        boolean passwordCheck = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if(!passwordCheck){
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(
                jwtService.generateToken(user)
        );
    }
}
