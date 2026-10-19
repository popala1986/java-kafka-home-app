package com.homeapp.auth.user;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.homeapp.auth.event.UserEventPublisher;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventPublisher = userEventPublisher;
    }

    public UserResponse register(RegisterUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }

        User user = new User(
            email,
            passwordEncoder.encode(request.password()),
            request.firstName().trim(),
            request.birthDate(),
            request.phoneNumber(),
            Role.USER);

        User saved = userRepository.save(user);
        userEventPublisher.publishUserRegistered(saved);
        return UserResponse.from(saved);
    }
}
