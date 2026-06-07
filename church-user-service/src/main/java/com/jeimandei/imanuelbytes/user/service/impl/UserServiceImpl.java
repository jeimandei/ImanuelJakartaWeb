package com.jeimandei.imanuelbytes.user.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
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
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Soft-deleting user id={}", id);
        User user = findUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        user.touchUpdatedAt();
        userRepository.save(user);
        log.info("Soft-deleted (set INACTIVE) user id={}", id);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
