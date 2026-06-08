package com.jeimandei.imanuelbytes.user.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.user.dto.AssignRolesRequest;
import com.jeimandei.imanuelbytes.user.dto.ChangePasswordRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateUserStatusRequest;
import com.jeimandei.imanuelbytes.user.dto.UserDto;
import com.jeimandei.imanuelbytes.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for user management endpoints.
 *
 * <p>Base path: {@code /api/users}.  All endpoints require at minimum a valid JWT
 * (i.e. an authenticated request).  Mutation endpoints that affect other users
 * are further restricted to {@code ROLE_ADMIN} via {@code @PreAuthorize}.</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // GET /api/users  —  list or search users (authenticated)
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of users.  When the optional {@code query} parameter
     * is present and non-blank, a full-text search across username, email, and full
     * name is performed instead.
     *
     * @param query optional search term
     * @param page  zero-based page index (default 0)
     * @param size  page size (default 10)
     * @param sort  sort field and direction, e.g. {@code username,asc} (default {@code id,asc})
     * @return paginated list of {@link UserDto}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<UserDto> result;

        if (query != null && !query.isBlank()) {
            log.debug("Searching users with query='{}', page={}, size={}", query, page, size);
            result = userService.searchUsers(query.trim(), pageable);
        } else {
            log.debug("Listing all users, page={}, size={}", page, size);
            result = userService.getAllUsers(pageable);
        }

        log.info("Retrieved {} users (total={})", result.getNumberOfElements(), result.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", PageResponse.from(result)));
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id}  —  get user by ID (authenticated)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        log.debug("Fetching user by id={}", id);
        UserDto user = userService.getUserById(id);
        log.info("Retrieved user id={}, username={}", user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    // -------------------------------------------------------------------------
    // GET /api/users/username/{username}  —  get user by username (authenticated)
    // -------------------------------------------------------------------------

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        log.debug("Fetching user by username='{}'", username);
        UserDto user = userService.getUserByUsername(username);
        log.info("Retrieved user id={} for username='{}'", user.getId(), username);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    // -------------------------------------------------------------------------
    // POST /api/users  —  create user (ROLE_ADMIN)
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        log.debug("Creating user with username='{}'", request.getUsername());
        UserDto created = userService.createUser(request);
        log.info("Created user id={}, username='{}'", created.getId(), created.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}  —  update user profile (authenticated, own profile or admin)
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.debug("Updating user id={}", id);
        UserDto updated = userService.updateUser(id, request);
        log.info("Updated user id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}/status  —  update status (ROLE_ADMIN)
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.debug("Updating status for user id={}, newStatus={}", id, request.getStatus());
        UserDto updated = userService.updateUserStatus(id, request);
        log.info("Updated status for user id={} to {}", updated.getId(), updated.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updated));
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}/roles  —  assign roles (ROLE_ADMIN)
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request) {
        log.debug("Assigning roles to user id={}, roles={}", id, request.getRoles());
        UserDto updated = userService.assignRoles(id, request);
        log.info("Assigned roles to user id={}: {}", updated.getId(), request.getRoles());
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", updated));
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}/password  —  change password (authenticated, own account)
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        log.debug("Changing password for user id={}, requestedBy='{}'", id, currentUsername);
        userService.changePassword(id, request, currentUsername);
        log.info("Password changed for user id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/users/{id}  —  soft delete (ROLE_ADMIN)
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.debug("Deleting (deactivating) user id={}", id);
        userService.deleteUser(id);
        log.info("Deactivated user id={}", id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a Spring Data {@link Pageable} from the raw query parameters.
     *
     * <p>The {@code sort} parameter is expected in the form {@code field,direction}
     * (e.g. {@code username,desc}).  If the format is invalid, the default sort
     * {@code id ASC} is used instead.</p>
     */
    private Pageable buildPageable(int page, int size, String sort) {
        Sort springSort;
        try {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1
                    ? Sort.Direction.fromString(parts[1].trim())
                    : Sort.Direction.ASC;
            springSort = Sort.by(direction, field);
        } catch (Exception e) {
            springSort = Sort.by(Sort.Direction.ASC, "id");
        }
        return PageRequest.of(page, size, springSort);
    }
}
