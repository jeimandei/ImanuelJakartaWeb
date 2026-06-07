package com.jeimandei.imanuelbytes.auth.service.impl;

import com.jeimandei.imanuelbytes.auth.dto.AuthResponse;
import com.jeimandei.imanuelbytes.auth.dto.LoginRequest;
import com.jeimandei.imanuelbytes.auth.dto.RegisterRequest;
import com.jeimandei.imanuelbytes.auth.dto.UserInfoResponse;
import com.jeimandei.imanuelbytes.auth.entity.Role;
import com.jeimandei.imanuelbytes.auth.entity.User;
import com.jeimandei.imanuelbytes.auth.entity.UserStatus;
import com.jeimandei.imanuelbytes.auth.repository.RoleRepository;
import com.jeimandei.imanuelbytes.auth.repository.UserRepository;
import com.jeimandei.imanuelbytes.auth.service.AuthService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.UnauthorizedException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validates uniqueness of username/email before persisting a new user.</li>
 *   <li>Encodes plaintext passwords with BCrypt.</li>
 *   <li>Assigns {@code ROLE_MEMBER} to every newly registered user.</li>
 *   <li>Verifies credentials and account status on login.</li>
 *   <li>Delegates JWT generation to the shared {@link JwtService}.</li>
 * </ul>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String DEFAULT_ROLE = "ROLE_MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // -------------------------------------------------------------------------
    // AuthService implementation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering new user: {}", request.getUsername());

        // Validate uniqueness
        Map<String, String> errors = new HashMap<>();
        if (userRepository.existsByUsername(request.getUsername())) {
            errors.put("username", "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            errors.put("email", "Email '" + request.getEmail() + "' is already registered");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Registration failed: duplicate field(s)", errors);
        }

        // Resolve default role — fail fast if the roles table is not seeded
        Role defaultRole = roleRepository.findByRoleName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default role '" + DEFAULT_ROLE + "' not found. " +
                        "Ensure the roles table is seeded before registering users."));

        // Build and persist the user
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber()
        );
        user.setRoles(Set.of(defaultRole));
        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for: {}", request.getUsername());

        // Resolve user by username or email
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username/email or password");
        }

        // Check account status
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException(
                    "Account is locked. Please contact an administrator.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UnauthorizedException(
                    "Account is inactive. Please contact an administrator.");
        }

        // Record last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("User logged in successfully: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String username) {
        log.debug("Fetching user info for: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .sorted()
                .collect(Collectors.toList());

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roleNames,
                user.getStatus().name()
        );
    }

    @Override
    public void logout(String token) {
        // Stateless JWT: the token becomes invalid when it expires naturally.
        // For server-side revocation, add the token jti/sub to a deny-list here.
        log.debug("Logout requested — token will expire naturally (stateless JWT)");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Build an {@link AuthResponse} by generating a JWT for the given {@link User}.
     *
     * <p>The JWT subject is the username. Roles are embedded directly in the token
     * claims via the updated {@link JwtService#generateToken(String, List)} API.</p>
     */
    private AuthResponse buildAuthResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .sorted()
                .collect(Collectors.toList());

        String jwt = jwtService.generateToken(user.getUsername(), roleNames);

        return AuthResponse.of(
                jwt,
                jwtService.getExpirationMs(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roleNames
        );
    }
}
