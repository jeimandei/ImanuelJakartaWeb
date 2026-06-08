package com.jeimandei.imanuelbytes.user.mapper;

import com.jeimandei.imanuelbytes.user.dto.UserDto;
import com.jeimandei.imanuelbytes.user.entity.Role;
import com.jeimandei.imanuelbytes.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper that converts {@link User} entities to {@link UserDto} response objects.
 *
 * <p>Spring injects the generated implementation via constructor injection wherever
 * {@code UserMapper} is declared as a dependency.  The {@code componentModel = "spring"}
 * attribute causes MapStruct to generate a {@code @Component}-annotated implementation.</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a {@link User} entity to a {@link UserDto}.
     *
     * <p>The {@code roles} field is mapped from the set of {@link Role} entities to a
     * flat list of role-name strings using the {@link #rolesToRoleNames(Set)} helper.</p>
     *
     * @param user the entity to convert
     * @return the populated response DTO
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserDto userToDto(User user);

    /**
     * Converts a {@link Page} of {@link User} entities to a list of {@link UserDto}s.
     * Spring Data pages are handled by calling this method on each element individually.
     *
     * @param users the list of entities to convert
     * @return the corresponding list of DTOs
     */
    List<UserDto> usersToDtos(List<User> users);

    /**
     * Extracts the role name strings from a set of {@link Role} entities.
     *
     * @param roles the set of role entities
     * @return a list of role name strings (e.g. {@code ["ROLE_ADMIN", "ROLE_MEMBER"]})
     */
    @Named("rolesToRoleNames")
    default List<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(Role::getRoleName)
                .sorted()
                .collect(Collectors.toList());
    }
}
