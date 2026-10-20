package com.homeapp.auth.user;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeapp.auth.event.UserEventPublisher;
import com.homeapp.auth.household.Household;
import com.homeapp.auth.household.HouseholdNotFoundException;
import com.homeapp.auth.household.HouseholdRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    public UserService(UserRepository userRepository,
                       HouseholdRepository householdRepository,
                       PasswordEncoder passwordEncoder,
                       UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventPublisher = userEventPublisher;
    }

    /** Rejestracja zakłada nową rodzinę, a rejestrujący zostaje jej adminem. */
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailIsFree(email);

        Household household = householdRepository.save(new Household(request.householdName().trim()));

        User admin = new User(
            household,
            email,
            passwordEncoder.encode(request.password()),
            request.firstName().trim(),
            request.birthDate(),
            request.phoneNumber(),
            Role.ADMIN);

        return saveAndPublish(admin);
    }

    /** Admin dodaje domownika do swojej rodziny (z kontem albo bez). */
    @Transactional
    public UserResponse addMember(UUID householdId, AddMemberRequest request) {
        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new HouseholdNotFoundException(householdId));

        String email = normalizeEmail(request.email());
        if (email != null) {
            ensureEmailIsFree(email);
        }
        String passwordHash = request.password() == null ? null : passwordEncoder.encode(request.password());

        User member = new User(
            household,
            email,
            passwordHash,
            request.firstName().trim(),
            request.birthDate(),
            request.phoneNumber(),
            Role.USER);

        return saveAndPublish(member);
    }

    /** Admin zmienia imię, datę urodzenia i telefon domownika ze swojej rodziny. */
    @Transactional
    public UserResponse updateMember(UUID householdId, UUID userId, UpdateMemberRequest request) {
        User member = userRepository.findByIdAndHouseholdId(userId, householdId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        member.updateProfile(request.firstName().trim(), request.birthDate(), request.phoneNumber());
        userEventPublisher.publishUserUpdated(member);
        return UserResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listMembers(UUID householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new HouseholdNotFoundException(householdId);
        }
        return userRepository.findAllByHouseholdIdOrderByBirthDateAsc(householdId).stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userRepository.findById(userId)
            .map(UserResponse::from)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserResponse saveAndPublish(User user) {
        User saved = userRepository.save(user);
        userEventPublisher.publishUserRegistered(saved);
        return UserResponse.from(saved);
    }

    private void ensureEmailIsFree(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
