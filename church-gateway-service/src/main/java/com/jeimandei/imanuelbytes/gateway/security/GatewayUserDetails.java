package com.jeimandei.imanuelbytes.gateway.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class GatewayUserDetails implements UserDetails {

    private final String username;
    private final String jwtToken;
    private final String email;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;
    private String profileImageUrl;
    private String phoneNumber;

    public GatewayUserDetails(String username, String jwtToken, String email,
                               String fullName, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.email = email;
        this.fullName = fullName;
        this.authorities = authorities;
    }

    public String getJwtToken() { return jwtToken; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<String> getRoles() {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
