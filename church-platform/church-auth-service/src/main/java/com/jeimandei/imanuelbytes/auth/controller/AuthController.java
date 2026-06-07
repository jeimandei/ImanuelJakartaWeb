package com.jeimandei.imanuelbytes.auth.controller;

import com.jeimandei.imanuelbytes.auth.dto.AuthResponse;
import com.jeimandei.imanuelbytes.auth.dto.LoginRequest;
import com.jeimandei.imanuelbytes.auth.dto.RegisterRequest;
import com.jeimandei.imanuelbytes.auth.dto.UserInfoResponse;
import com.jeimandei.imanuelbytes.auth.service.AuthService;
import com.jeimandei.imanuelbytes.common.constant.AppConstants;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.exception.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints.
 *
 * <ul>
 *   <li>{@code POST /api/auth/register} – create a new account and receive a JWT.</li>
 *   <li>{@code POST /api/auth/login}    – authenticate and receive a JWT.</li>
 *   <li>{@code POST /api/auth/logout}   – signal intent to log out (client drops token).</li>
 *   <li>{@code GET  /api/auth/me}       – retrieve the current user's profile.</li>
 * </ul>
 *
 * <p>All responses are wrapped in the platform-standard {@link ApiResponse} envelope.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -------------------------------------------------------------------------
    // Public endpoints
    // -------------------------------------------------------------------------

    /**
     * Register a new user account.
     *
     * @param request validated registration payload
     * @return 201 Created with the issued JWT and user summary
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", authResponse));
    }

    /**
     * Authenticate an existing user.
     *
     * @param request validated login payload (username or email + password)
     * @return 200 OK with the issued JWT and user summary
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    // -------------------------------------------------------------------------
    // Authenticated endpoints
    // -------------------------------------------------------------------------

    /**
     * Log the current user out.
     *
     * <p>With stateless JWTs the server cannot actively invalidate a token, so
     * this endpoint instructs the service layer to perform any optional server-side
     * bookkeeping (e.g. deny-list) and returns a success message. The client is
     * expected to discard the token locally.</p>
     *
     * @param authorizationHeader the {@code Authorization: Bearer <token>} header
     * @return 200 OK with a logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = AppConstants.JWT_HEADER, required = false)
            String authorizationHeader) {

        String token = null;
        if (authorizationHeader != null
                && authorizationHeader.startsWith(AppConstants.JWT_PREFIX)) {
            token = authorizationHeader.substring(AppConstants.JWT_PREFIX.length());
        }
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Retrieve the authenticated user's profile.
     *
     * @return 200 OK with the current user's info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("You must be logged in to access this resource");
        }

        // The principal is set to the username string by JwtAuthenticationFilter
        String username = authentication.getName();
        UserInfoResponse userInfo = authService.getUserInfo(username);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
