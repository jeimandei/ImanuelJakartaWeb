package com.jeimandei.imanuelbytes.user.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.user.audit.AuditClientService;
import com.jeimandei.imanuelbytes.user.dto.AssignRolesRequest;
import com.jeimandei.imanuelbytes.user.dto.ChangePasswordRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserStatusRequest;
import com.jeimandei.imanuelbytes.user.dto.UserDto;
import com.jeimandei.imanuelbytes.user.entity.Role;
import com.jeimandei.imanuelbytes.user.entity.User;
import com.jeimandei.imanuelbytes.user.entity.UserStatus;
import com.jeimandei.imanuelbytes.user.mapper.UserMapper;
import com.jeimandei.imanuelbytes.user.repository.RoleRepository;
import com.jeimandei.imanuelbytes.user.repository.UserRepository;
import com.jeimandei.imanuelbytes.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>All mutating operations are wrapped in a transaction.  Read operations use
 * {@code readOnly = true} for a minor performance benefit.</p>
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditClientService auditClient;
    private final JavaMailSender mailSender;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper,
                           AuditClientService auditClient,
                           JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.auditClient = auditClient;
        this.mailSender = mailSender;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pageable: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::userToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = findUserById(id);
        return userMapper.userToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.userToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String query, Pageable pageable) {
        log.debug("Searching users with query='{}', pageable={}", query, pageable);
        return userRepository.searchUsers(query, pageable)
                .map(userMapper::userToDto);
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    @Override
    public UserDto createUser(CreateUserRequest request) {
        log.debug("Creating user with username: {}", request.getUsername());

        // Uniqueness checks
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException(
                    "Username already exists",
                    Map.of("username", "Username '" + request.getUsername() + "' is already taken"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException(
                    "Email already exists",
                    Map.of("email", "Email '" + request.getEmail() + "' is already registered"));
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Resolve and assign roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByRoleNameIn(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        User saved = userRepository.save(user);
        log.info("Created new user: id={}, username={}", saved.getId(), saved.getUsername());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_USER", "User",
                    String.valueOf(saved.getId()), saved.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_USER {}: {}", saved.getUsername(), e.getMessage());
        }
        return userMapper.userToDto(saved);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        log.debug("Updating user id={}", id);
        User user = findUserById(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        user.touchUpdatedAt();
        User saved = userRepository.save(user);
        log.info("Updated user id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_USER", "User",
                    String.valueOf(saved.getId()), saved.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_USER {}: {}", saved.getId(), e.getMessage());
        }
        return userMapper.userToDto(saved);
    }

    @Override
    public UserDto updateUserStatus(Long id, UpdateUserStatusRequest request) {
        log.debug("Updating status for user id={} to {}", id, request.getStatus());
        User user = findUserById(id);
        user.setStatus(request.getStatus());
        user.touchUpdatedAt();
        User saved = userRepository.save(user);
        log.info("Updated status of user id={} to {}", saved.getId(), saved.getStatus());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CHANGE_STATUS", "User",
                    String.valueOf(saved.getId()), saved.getUsername() + " → " + saved.getStatus());
        } catch (Exception e) {
            log.warn("Audit log failed for CHANGE_STATUS user {}: {}", saved.getId(), e.getMessage());
        }
        return userMapper.userToDto(saved);
    }

    @Override
    public UserDto assignRoles(Long id, AssignRolesRequest request) {
        log.debug("Assigning roles {} to user id={}", request.getRoles(), id);
        User user = findUserById(id);

        List<Role> roles = roleRepository.findByRoleNameIn(request.getRoles());
        if (roles.isEmpty()) {
            throw new ValidationException(
                    "No valid roles found",
                    Map.of("roles", "None of the provided role names exist in the system"));
        }

        user.setRoles(new HashSet<>(roles));
        user.touchUpdatedAt();
        User saved = userRepository.save(user);
        log.info("Assigned {} roles to user id={}", roles.size(), saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "ASSIGN_ROLES", "User",
                    String.valueOf(saved.getId()), saved.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for ASSIGN_ROLES user {}: {}", saved.getId(), e.getMessage());
        }
        return userMapper.userToDto(saved);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request, String currentUsername) {
        log.debug("Processing password change for user id={}", id);
        User user = findUserById(id);

        // Confirm passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException(
                    "Passwords do not match",
                    Map.of("confirmPassword", "New password and confirmation password do not match"));
        }

        // Verify current password (always required — admins must know the user's current pw,
        // or use the updateUserStatus / separate admin-reset flow instead)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException(
                    "Current password is incorrect",
                    Map.of("currentPassword", "The current password you provided is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.touchUpdatedAt();
        userRepository.save(user);
        log.info("Password changed successfully for user id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CHANGE_PASSWORD", "User",
                    String.valueOf(user.getId()), user.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for CHANGE_PASSWORD user {}: {}", id, e.getMessage());
        }
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Soft-deleting user id={}", id);
        User user = findUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        user.touchUpdatedAt();
        userRepository.save(user);
        log.info("Soft-deleted (set INACTIVE) user id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_USER", "User",
                    String.valueOf(user.getId()), user.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_USER {}: {}", id, e.getMessage());
        }
    }

    @Override
    public void resetPassword(Long id) {
        log.debug("Admin password reset for user id={}", id);
        User user = findUserById(id);

        String tempPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.touchUpdatedAt();
        userRepository.save(user);
        log.info("Temporary password set for user id={}", id);

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject("Your password has been reset - GMIM Imanuel Jakarta");
            mail.setText(
                "Dear " + (user.getFullName() != null ? user.getFullName() : user.getUsername()) + ",\n\n" +
                "An administrator has reset your password.\n\n" +
                "Your temporary password is: " + tempPassword + "\n\n" +
                "Please log in and change your password immediately.\n\n" +
                "GMIM Imanuel Jakarta"
            );
            mailSender.send(mail);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }

        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "RESET_PASSWORD", "User",
                    String.valueOf(user.getId()), user.getUsername());
        } catch (Exception e) {
            log.warn("Audit log failed for RESET_PASSWORD user {}: {}", id, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}
