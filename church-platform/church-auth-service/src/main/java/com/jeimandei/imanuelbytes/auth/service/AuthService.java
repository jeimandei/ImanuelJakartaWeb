package com.jeimandei.imanuelbytes.auth.service;

import com.jeimandei.imanuelbytes.auth.dto.AuthResponse;
import com.jeimandei.imanuelbytes.auth.dto.LoginRequest;
import com.jeimandei.imanuelbytes.auth.dto.RegisterRequest;
import com.jeimandei.imanuelbytes.auth.dto.UserInfoResponse;

/**
 * Contract for the authentication domain: registration, login, user-info retrieval
 * and logout.
 *
 * <p>The single implementation is
 * {@link com.jeimandei.imanuelbytes.auth.service.impl.AuthServiceImpl}.</p>
 */
public interface AuthService {

    /**
     * Register a new user account.
     *
     * <p>Validates that the username and email are not already taken, encodes the
     * supplied plaintext password, assigns the default {@code ROLE_MEMBER} role,
     * persists the new user, and returns a fresh JWT.</p>
     *
     * @param request registration payload
     * @return authentication response containing the JWT and profile summary
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate an existing user.
     *
     * <p>Resolves the user by username or email, verifies the plaintext password
     * against the stored hash, checks that the account status is {@code ACTIVE},
     * records the login timestamp, and returns a fresh JWT.</p>
     *
     * @param request login payload
     * @return authentication response containing the JWT and profile summary
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieve public profile information for the given username.
     *
     * @param username the username whose profile to return
     * @return user-info response
     */
    UserInfoResponse getUserInfo(String username);

    /**
     * Invalidate a JWT token.
     *
     * <p>With purely stateless JWTs this is a no-op; it is provided as a hook for
     * future server-side token revocation (e.g. a deny-list stored in Redis).</p>
     *
     * @param token the raw JWT string (without the {@code Bearer } prefix)
     */
    void logout(String token);
}
