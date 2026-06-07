package com.jeimandei.imanuelbytes.auth.service;

import com.jeimandei.imanuelbytes.auth.dto.AuthResponse;
import com.jeimandei.imanuelbytes.auth.dto.LoginRequest;
import com.jeimandei.imanuelbytes.auth.dto.RegisterRequest;
import com.jeimandei.imanuelbytes.auth.entity.Role;
import com.jeimandei.imanuelbytes.auth.entity.User;
import com.jeimandei.imanuelbytes.auth.entity.UserStatus;
import com.jeimandei.imanuelbytes.auth.repository.RoleRepository;
import com.jeimandei.imanuelbytes.auth.repository.UserRepository;
import com.jeimandei.imanuelbytes.auth.service.impl.AuthServiceImpl;
import com.jeimandei.imanuelbytes.common.exception.UnauthorizedException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>No Spring context, no database — all collaborators are Mockito mocks injected
 * via {@link MockitoExtension}.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    // -------------------------------------------------------------------------
    // Mocks
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private static final String USERNAME       = "john_doe";
    private static final String EMAIL          = "john@example.com";
    private static final String PASSWORD       = "secret123";
    private static final String ENCODED_PW     = "$2a$encoded";
    private static final String FULL_NAME      = "John Doe";
    private static final String PHONE          = "+628123456789";
    private static final String ROLE_MEMBER    = "ROLE_MEMBER";
    private static final String JWT_TOKEN      = "header.payload.signature";
    private static final long   EXPIRES_IN_MS  = 86_400_000L;

    private RegisterRequest validRegisterRequest;
    private Role memberRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FULL_NAME, PHONE);

        memberRole = new Role(ROLE_MEMBER, "Default member role");

        savedUser = new User(USERNAME, EMAIL, ENCODED_PW, FULL_NAME, PHONE);
        savedUser.setId(1L);
        savedUser.setRoles(Set.of(memberRole));
        // status defaults to ACTIVE in the User constructor
    }

    // =========================================================================
    // register()
    // =========================================================================

    @Test
    @DisplayName("register — success: persists user and returns AuthResponse with Bearer token")
    void register_validRequest_returnsAuthResponse() {
        // Arrange — no duplicates, role exists, encoder + jwt work
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName(ROLE_MEMBER)).thenReturn(Optional.of(memberRole));
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PW);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(eq(USERNAME), anyList())).thenReturn(JWT_TOKEN);
        when(jwtService.getExpirationMs()).thenReturn(EXPIRES_IN_MS);

        // Act
        AuthResponse response = authService.register(validRegisterRequest);

        // Assert — token and core fields
        assertNotNull(response);
        assertEquals(JWT_TOKEN,   response.getToken());
        assertEquals("Bearer",    response.getTokenType());
        assertEquals(EXPIRES_IN_MS, response.getExpiresIn());
        assertEquals(USERNAME,    response.getUsername());
        assertEquals(EMAIL,       response.getEmail());
        assertEquals(FULL_NAME,   response.getFullName());
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().contains(ROLE_MEMBER));

        // Assert — side effects
        verify(userRepository).existsByUsername(USERNAME);
        verify(userRepository).existsByEmail(EMAIL);
        verify(roleRepository).findByRoleName(ROLE_MEMBER);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(eq(USERNAME), anyList());
    }

    @Test
    @DisplayName("register — duplicate username: throws ValidationException with 'username' error key")
    void register_duplicateUsername_throwsValidationException() {
        // Arrange — username already taken, email is free
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        assertTrue(ex.getErrors().containsKey("username"),
                "errors map should contain the 'username' field");
        assertFalse(ex.getErrors().containsKey("email"),
                "errors map should NOT contain 'email' when only username is duplicated");

        // No save should happen
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — duplicate email: throws ValidationException with 'email' error key")
    void register_duplicateEmail_throwsValidationException() {
        // Arrange — username is free, email already registered
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        assertTrue(ex.getErrors().containsKey("email"),
                "errors map should contain the 'email' field");
        assertFalse(ex.getErrors().containsKey("username"),
                "errors map should NOT contain 'username' when only email is duplicated");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — both username and email duplicated: throws ValidationException with both error keys")
    void register_duplicateUsernameAndEmail_throwsValidationExceptionWithBothKeys() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        assertTrue(ex.getErrors().containsKey("username"));
        assertTrue(ex.getErrors().containsKey("email"));
        assertEquals(2, ex.getErrors().size());

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // login()
    // =========================================================================

    @Test
    @DisplayName("login — success with username: returns AuthResponse with Bearer token")
    void login_validUsername_returnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PW)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(eq(USERNAME), anyList())).thenReturn(JWT_TOKEN);
        when(jwtService.getExpirationMs()).thenReturn(EXPIRES_IN_MS);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals("Bearer",  response.getTokenType());
        assertEquals(USERNAME,  response.getUsername());
        assertEquals(EMAIL,     response.getEmail());

        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PW);
        verify(userRepository).save(savedUser);          // last-login timestamp update
    }

    @Test
    @DisplayName("login — user not found by username or email: throws UnauthorizedException")
    void login_userNotFound_throwsUnauthorizedException() {
        // Arrange — findByUsername returns empty, findByEmail also returns empty (via .or())
        LoginRequest request = new LoginRequest("ghost_user", PASSWORD);
        when(userRepository.findByUsername("ghost_user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost_user")).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(request));

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Invalid username/email or password"));

        // No token should be generated
        verify(jwtService, never()).generateToken(anyString(), anyList());
    }

    @Test
    @DisplayName("login — wrong password: throws UnauthorizedException")
    void login_wrongPassword_throwsUnauthorizedException() {
        LoginRequest request = new LoginRequest(USERNAME, "wrongpassword");
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PW)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(anyString(), anyList());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login — account is LOCKED: throws UnauthorizedException")
    void login_lockedAccount_throwsUnauthorizedException() {
        savedUser.setStatus(UserStatus.LOCKED);
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PW)).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("locked"),
                "error message should mention the account is locked");
        verify(jwtService, never()).generateToken(anyString(), anyList());
    }

    @Test
    @DisplayName("login — account is INACTIVE: throws UnauthorizedException")
    void login_inactiveAccount_throwsUnauthorizedException() {
        savedUser.setStatus(UserStatus.INACTIVE);
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PW)).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("inactive"),
                "error message should mention the account is inactive");
        verify(jwtService, never()).generateToken(anyString(), anyList());
    }

    @Test
    @DisplayName("login — success via email address (username field contains an email)")
    void login_validEmail_returnsAuthResponse() {
        // AuthServiceImpl first tries findByUsername, then falls back to findByEmail
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        when(userRepository.findByUsername(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PW)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(eq(USERNAME), anyList())).thenReturn(JWT_TOKEN);
        when(jwtService.getExpirationMs()).thenReturn(EXPIRES_IN_MS);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(USERNAME, response.getUsername());
    }
}
