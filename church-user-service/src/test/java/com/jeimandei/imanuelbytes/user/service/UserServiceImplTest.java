package com.jeimandei.imanuelbytes.user.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.user.dto.ChangePasswordRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserStatusRequest;
import com.jeimandei.imanuelbytes.user.dto.UserDto;
import com.jeimandei.imanuelbytes.user.entity.Role;
import com.jeimandei.imanuelbytes.user.entity.User;
import com.jeimandei.imanuelbytes.user.entity.UserStatus;
import com.jeimandei.imanuelbytes.user.mapper.UserMapper;
import com.jeimandei.imanuelbytes.user.repository.RoleRepository;
import com.jeimandei.imanuelbytes.user.repository.UserRepository;
import com.jeimandei.imanuelbytes.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * Uses {@link MockitoExtension} only — no Spring context is loaded.
 * All collaborators (repositories, encoder, mapper) are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl unit tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // -------------------------------------------------------------------------
    // Shared test data helpers
    // -------------------------------------------------------------------------

    private User buildActiveUser(Long id, String username, String email) {
        User user = new User(username, email, "hashed-password");
        user.setId(id);
        user.setFullName("Test User");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private UserDto buildUserDto(Long id, String username, String email, UserStatus status) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setStatus(status);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setRoles(Collections.emptyList());
        return dto;
    }

    // =========================================================================
    // getUserById
    // =========================================================================

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("found user is mapped to UserDto and returned")
        void found_returnsUserDto() {
            User user = buildActiveUser(1L, "jdoe", "jdoe@example.com");
            UserDto expected = buildUserDto(1L, "jdoe", "jdoe@example.com", UserStatus.ACTIVE);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.userToDto(user)).thenReturn(expected);

            UserDto result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("jdoe", result.getUsername());
            assertEquals("jdoe@example.com", result.getEmail());
            assertEquals(UserStatus.ACTIVE, result.getStatus());

            verify(userRepository).findById(1L);
            verify(userMapper).userToDto(user);
        }

        @Test
        @DisplayName("not found throws ResourceNotFoundException")
        void notFound_throwsResourceNotFoundException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.getUserById(99L)
            );

            assertTrue(ex.getMessage().contains("99"),
                    "Exception message should contain the queried id");
            verify(userRepository).findById(99L);
            verifyNoInteractions(userMapper);
        }
    }

    // =========================================================================
    // createUser
    // =========================================================================

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        private CreateUserRequest validRequest() {
            return new CreateUserRequest(
                    "newuser",
                    "newuser@example.com",
                    "P@ssword1",
                    "New User",
                    "+1234567890",
                    null
            );
        }

        @Test
        @DisplayName("success case saves user and returns UserDto")
        void success_returnsUserDto() {
            CreateUserRequest request = validRequest();
            User saved = buildActiveUser(10L, "newuser", "newuser@example.com");
            UserDto expectedDto = buildUserDto(10L, "newuser", "newuser@example.com", UserStatus.ACTIVE);

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(passwordEncoder.encode("P@ssword1")).thenReturn("hashed-P@ssword1");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            when(userMapper.userToDto(saved)).thenReturn(expectedDto);

            UserDto result = userService.createUser(request);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("newuser", result.getUsername());

            verify(userRepository).existsByUsername("newuser");
            verify(userRepository).existsByEmail("newuser@example.com");
            verify(passwordEncoder).encode("P@ssword1");
            verify(userRepository).save(any(User.class));
            verify(userMapper).userToDto(saved);
        }

        @Test
        @DisplayName("duplicate username throws ValidationException before any save")
        void duplicateUsername_throwsValidationException() {
            CreateUserRequest request = validRequest();

            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> userService.createUser(request)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("username"),
                    "Exception message should mention 'username'");
            assertTrue(ex.getErrors().containsKey("username"),
                    "Errors map should contain a 'username' entry");

            verify(userRepository).existsByUsername("newuser");
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate email throws ValidationException before any save")
        void duplicateEmail_throwsValidationException() {
            CreateUserRequest request = validRequest();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> userService.createUser(request)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("email"),
                    "Exception message should mention 'email'");
            assertTrue(ex.getErrors().containsKey("email"),
                    "Errors map should contain an 'email' entry");

            verify(userRepository).existsByUsername("newuser");
            verify(userRepository).existsByEmail("newuser@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("roles in request are resolved via RoleRepository and assigned to user")
        void withRoles_resolvesAndAssignsRoles() {
            CreateUserRequest request = new CreateUserRequest(
                    "admin", "admin@example.com", "P@ssword1",
                    "Admin User", null, List.of("ROLE_ADMIN")
            );
            Role adminRole = new Role("ROLE_ADMIN", "Administrator");
            User saved = buildActiveUser(20L, "admin", "admin@example.com");
            UserDto expectedDto = buildUserDto(20L, "admin", "admin@example.com", UserStatus.ACTIVE);

            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("P@ssword1")).thenReturn("hashed");
            when(roleRepository.findByRoleNameIn(List.of("ROLE_ADMIN"))).thenReturn(List.of(adminRole));
            when(userRepository.save(any(User.class))).thenReturn(saved);
            when(userMapper.userToDto(saved)).thenReturn(expectedDto);

            UserDto result = userService.createUser(request);

            assertNotNull(result);
            verify(roleRepository).findByRoleNameIn(List.of("ROLE_ADMIN"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().getRoles().contains(adminRole),
                    "Saved user should have the ROLE_ADMIN role assigned");
        }
    }

    // =========================================================================
    // updateUserStatus
    // =========================================================================

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatus {

        @Test
        @DisplayName("ACTIVE user is set to INACTIVE (soft deactivation)")
        void activeToInactive_setsStatusInactive() {
            User activeUser = buildActiveUser(5L, "member", "member@church.org");
            assertEquals(UserStatus.ACTIVE, activeUser.getStatus());

            UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.INACTIVE);

            User savedUser = buildActiveUser(5L, "member", "member@church.org");
            savedUser.setStatus(UserStatus.INACTIVE);
            UserDto expectedDto = buildUserDto(5L, "member", "member@church.org", UserStatus.INACTIVE);

            when(userRepository.findById(5L)).thenReturn(Optional.of(activeUser));
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.userToDto(savedUser)).thenReturn(expectedDto);

            UserDto result = userService.updateUserStatus(5L, request);

            assertEquals(UserStatus.INACTIVE, result.getStatus());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals(UserStatus.INACTIVE, captor.getValue().getStatus(),
                    "Persisted user entity must have INACTIVE status");
        }

        @Test
        @DisplayName("user not found throws ResourceNotFoundException")
        void notFound_throwsResourceNotFoundException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.INACTIVE);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.updateUserStatus(999L, request)
            );

            verify(userRepository, never()).save(any());
        }
    }

    // =========================================================================
    // deleteUser
    // =========================================================================

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("sets user status to INACTIVE (soft delete) and saves")
        void softDelete_setsInactiveAndSaves() {
            User user = buildActiveUser(7L, "todelete", "todelete@church.org");

            when(userRepository.findById(7L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.deleteUser(7L);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals(UserStatus.INACTIVE, captor.getValue().getStatus(),
                    "Deleted user must be persisted with INACTIVE status (soft-delete)");
            verify(userRepository, never()).delete(any(User.class));
            verify(userRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("not found throws ResourceNotFoundException and no save occurs")
        void notFound_throwsResourceNotFoundExceptionWithNoSave() {
            when(userRepository.findById(404L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.deleteUser(404L)
            );

            verify(userRepository, never()).save(any());
        }
    }

    // =========================================================================
    // changePassword
    // =========================================================================

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("correct current password and matching new passwords updates hash")
        void success_updatesPasswordHash() {
            User user = buildActiveUser(3L, "pwuser", "pw@church.org");
            user.setPasswordHash("old-hash");

            when(userRepository.findById(3L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("OldPass1!", "old-hash")).thenReturn(true);
            when(passwordEncoder.encode("NewPass1!")).thenReturn("new-hash");
            when(userRepository.save(any(User.class))).thenReturn(user);

            ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!", "NewPass1!", "NewPass1!");

            assertDoesNotThrow(() -> userService.changePassword(3L, request, "pwuser"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("new-hash", captor.getValue().getPasswordHash(),
                    "Password hash must be updated to the encoded new password");
        }

        @Test
        @DisplayName("mismatched new password and confirm password throws ValidationException")
        void passwordMismatch_throwsValidationException() {
            User user = buildActiveUser(3L, "pwuser", "pw@church.org");
            user.setPasswordHash("old-hash");

            when(userRepository.findById(3L)).thenReturn(Optional.of(user));

            ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!", "NewPass1!", "DifferentPass1!");

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> userService.changePassword(3L, request, "pwuser")
            );

            assertTrue(ex.getMessage().toLowerCase().contains("match") ||
                            ex.getErrors().containsKey("confirmPassword"),
                    "Exception should indicate password mismatch");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("wrong current password throws ValidationException")
        void wrongCurrentPassword_throwsValidationException() {
            User user = buildActiveUser(3L, "pwuser", "pw@church.org");
            user.setPasswordHash("old-hash");

            when(userRepository.findById(3L)).thenReturn(Optional.of(user));
            // New password and confirm match so we reach the current password check
            when(passwordEncoder.matches("WrongPass!", "old-hash")).thenReturn(false);

            ChangePasswordRequest request = new ChangePasswordRequest("WrongPass!", "NewPass1!", "NewPass1!");

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> userService.changePassword(3L, request, "pwuser")
            );

            assertTrue(ex.getMessage().toLowerCase().contains("password") ||
                            ex.getErrors().containsKey("currentPassword"),
                    "Exception should indicate the current password is wrong");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("user not found throws ResourceNotFoundException")
        void notFound_throwsResourceNotFoundException() {
            when(userRepository.findById(888L)).thenReturn(Optional.empty());

            ChangePasswordRequest request = new ChangePasswordRequest("any", "newpass1!", "newpass1!");

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.changePassword(888L, request, "ghost")
            );

            verify(userRepository, never()).save(any());
        }
    }
}
