package com.jeimandei.imanuelbytes.auth.service;

import com.jeimandei.imanuelbytes.auth.entity.Role;
import com.jeimandei.imanuelbytes.auth.entity.User;
import com.jeimandei.imanuelbytes.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security {@link UserDetailsService} implementation that resolves a
 * principal by either username or email address.
 *
 * <p>Marked {@link Transactional} (read-only) because the {@code roles} collection
 * is fetched eagerly inside the same Hibernate session.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by username or email.
     *
     * <p>Spring Security calls this method during filter-chain authentication.
     * The supplied {@code username} parameter may actually contain an email
     * address — this service resolves both.</p>
     *
     * @param username the username or email address from the authentication token
     * @return a fully populated {@link UserDetails} instance
     * @throws UsernameNotFoundException if no matching user is found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with username or email: " + username));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getRoleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() ==
                        com.jeimandei.imanuelbytes.auth.entity.UserStatus.LOCKED)
                .credentialsExpired(false)
                .disabled(user.getStatus() ==
                        com.jeimandei.imanuelbytes.auth.entity.UserStatus.INACTIVE)
                .build();
    }
}
