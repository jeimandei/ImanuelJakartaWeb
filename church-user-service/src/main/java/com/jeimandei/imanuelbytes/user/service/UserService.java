package com.jeimandei.imanuelbytes.user.service;

import com.jeimandei.imanuelbytes.user.dto.AssignRolesRequest;
import com.jeimandei.imanuelbytes.user.dto.ChangePasswordRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserStatusRequest;
import com.jeimandei.imanuelbytes.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for user lifecycle management.
 *
 * <p>All methods that look up a user by identifier will throw
 * {@link com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException}
 * when no matching record is found.</p>
 */
public interface UserService {

    /**
     * Returns a page of all users in the system.
     *
     * @param pageable pagination and sort parameters
     * @return a page of {@link UserDto}s
     */
    Page<UserDto> getAllUsers(Pageable pageable);

    /**
     * Retrieves a single user by their primary-key identifier.
     *
     * @param id the user's database ID
     * @return the corresponding {@link UserDto}
     */
    UserDto getUserById(Long id);

    /**
     * Retrieves a single user by their username.
     *
     * @param username the unique username
     * @return the corresponding {@link UserDto}
     */
    UserDto getUserByUsername(String username);

    /**
     * Creates a new user account (admin operation).
     *
     * <p>Validates uniqueness of username and email, encodes the plain-text
     * password, and assigns the requested roles before persisting.</p>
     *
     * @param request the creation request payload
     * @return the newly created user as a {@link UserDto}
     */
    UserDto createUser(CreateUserRequest request);

    /**
     * Updates the editable profile fields of an existing user.
     *
     * <p>Only non-null fields in the request are applied; {@code null} means
     * "leave unchanged".</p>
     *
     * @param id      the ID of the user to update
     * @param request the update payload
     * @return the updated user as a {@link UserDto}
     */
    UserDto updateUser(Long id, UpdateUserRequest request);

    /**
     * Changes the lifecycle status of a user account (admin operation).
     *
     * @param id      the ID of the user whose status to change
     * @param request the new status
     * @return the updated user as a {@link UserDto}
     */
    UserDto updateUserStatus(Long id, UpdateUserStatusRequest request);

    /**
     * Replaces the complete set of roles assigned to a user (admin operation).
     *
     * @param id      the ID of the target user
     * @param request the new role assignment
     * @return the updated user as a {@link UserDto}
     */
    UserDto assignRoles(Long id, AssignRolesRequest request);

    /**
     * Changes the password for a user account.
     *
     * <p>For non-admin callers, {@code currentUsername} is compared against the
     * target user to ensure users can only change their own passwords.  The
     * current password is verified before applying the new one, and
     * {@code newPassword} must equal {@code confirmPassword}.</p>
     *
     * @param id              the ID of the user whose password to change
     * @param request         the password change payload
     * @param currentUsername the username extracted from the JWT of the caller
     */
    void changePassword(Long id, ChangePasswordRequest request, String currentUsername);

    /**
     * Soft-deletes a user by setting their status to {@link com.jeimandei.imanuelbytes.user.entity.UserStatus#INACTIVE}.
     *
     * <p>No records are physically removed from the database.</p>
     *
     * @param id the ID of the user to deactivate
     */
    void deleteUser(Long id);

    /**
     * Full-text search across username, email, and full name fields.
     *
     * @param query    the search term
     * @param pageable pagination and sort parameters
     * @return a page of {@link UserDto}s matching the query
     */
    Page<UserDto> searchUsers(String query, Pageable pageable);
}
