package com.homeapp.auth.security;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeapp.auth.user.User;
import com.homeapp.auth.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        // Ten sam błąd dla "nie ma takiego emaila" i "złe hasło": nie zdradzamy, które konta istnieją.
        User user = userRepository.findByEmail(email)
            .filter(User::canLogIn)
            .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);

        return new LoginResponse(jwtService.issueToken(user), "Bearer", jwtService.expiresInSeconds());
    }
}
