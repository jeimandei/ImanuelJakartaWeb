package com.jeimandei.imanuelbytes.user.mapper;

import com.jeimandei.imanuelbytes.user.dto.UserDto;
import com.jeimandei.imanuelbytes.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:11:12+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto userToDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setRoles( rolesToRoleNames( user.getRoles() ) );
        userDto.setId( user.getId() );
        userDto.setUsername( user.getUsername() );
        userDto.setEmail( user.getEmail() );
        userDto.setFullName( user.getFullName() );
        userDto.setPhoneNumber( user.getPhoneNumber() );
        userDto.setProfileImageUrl( user.getProfileImageUrl() );
        userDto.setStatus( user.getStatus() );
        userDto.setCreatedAt( user.getCreatedAt() );
        userDto.setUpdatedAt( user.getUpdatedAt() );
        userDto.setLastLoginAt( user.getLastLoginAt() );

        return userDto;
    }

    @Override
    public List<UserDto> usersToDtos(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( users.size() );
        for ( User user : users ) {
            list.add( userToDto( user ) );
        }

        return list;
    }
}
