package com.jeimandei.imanuelbytes.user.repository;

import com.jeimandei.imanuelbytes.user.entity.User;
import com.jeimandei.imanuelbytes.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Operates on the {@code users} table that is shared with church-auth-service.
 * Schema changes must go through the shared Flyway migrations.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to look up
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns a page of users whose account status matches the given value.
     *
     * @param status   the target {@link UserStatus}
     * @param pageable pagination and sort instructions
     * @return a page of matching users
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to test
     * @return {@code true} if a user with that username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to test
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);

    /**
     * Full-text search across {@code username}, {@code email}, and {@code full_name}.
     *
     * <p>The query is case-insensitive and uses a {@code LIKE} pattern match.
     * Any non-null result from this method is always paginated.</p>
     *
     * @param query    the search term (matched with {@code %query%} on each field)
     * @param pageable pagination and sort instructions
     * @return a page of users matching the search term in any of the three fields
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username)  LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.fullName)  LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    /**
     * Returns all users whose {@code birthday} falls in the given calendar month,
     * ordered by day of month ascending.  Users with a {@code null} birthday are
     * excluded.  Uses Hibernate's {@code month(...)} / {@code day(...)} HQL
     * extraction functions so the comparison is year-agnostic.
     *
     * @param month the calendar month (1–12)
     * @return matching users ordered by day of month
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.birthday IS NOT NULL
              AND MONTH(u.birthday) = :month
            ORDER BY DAY(u.birthday) ASC
            """)
    List<User> findByBirthdayMonth(@Param("month") int month);
}
