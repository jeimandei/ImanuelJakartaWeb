package com.jeimandei.imanuelbytes.user.config;

import com.jeimandei.imanuelbytes.user.entity.User;
import com.jeimandei.imanuelbytes.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;

/**
 * Provides the {@link UserDetailsService} and {@link PasswordEncoder} beans.
 *
 * <p>The {@link UserDetailsService} loads a {@link User} from the shared
 * {@code users} table and adapts it to Spring Security's {@link UserDetails}
 * contract, mapping each {@code Role} to a {@link SimpleGrantedAuthority}.</p>
 *
 * <p>Separating this from {@link SecurityConfig} avoids circular bean
 * dependency issues that arise when {@code SecurityConfig} itself depends on
 * beans defined within it.</p>
 */
@Configuration
public class UserDetailsServiceConfig {

    private final UserRepository userRepository;

    public UserDetailsServiceConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username and wraps it in Spring Security's
     * {@link org.springframework.security.core.userdetails.User} record.
     *
     * @return the {@link UserDetailsService} bean
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found: " + username));

            var authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    authorities
            );
        };
    }

    /**
     * BCrypt password encoder used for password hashing and verification.
     *
     * @return the {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
