package com.jeimandei.imanuelbytes.auth.repository;

import com.jeimandei.imanuelbytes.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique username.
     *
     * @param username the username to look up
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their unique email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return {@code true} if a user with that username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check whether a user with the given email address already exists.
     *
     * @param email the email address to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);
}
