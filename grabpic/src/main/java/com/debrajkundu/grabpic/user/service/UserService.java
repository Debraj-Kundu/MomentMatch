package com.debrajkundu.grabpic.user.service;

import com.debrajkundu.grabpic.common.exception.UserAlreadyExistsException;
import com.debrajkundu.grabpic.user.entity.User;
import com.debrajkundu.grabpic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String email, String password){
        if(userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public boolean checkUserExists(String email){
        return userRepository.existsByEmail(email);
    }
}
