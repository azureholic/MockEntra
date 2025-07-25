package com.example.demoapi.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final OAuth2Properties oauth2Properties;

    public JwtRoleConverter(OAuth2Properties oauth2Properties) {
        this.oauth2Properties = oauth2Properties;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract standard OAuth2 scopes - sufficient for basic token validation
        Object scopeClaim = jwt.getClaim(oauth2Properties.getScopeClaim());
        if (scopeClaim instanceof String) {
            String scope = (String) scopeClaim;
            authorities.addAll(Arrays.stream(scope.split(" "))
                .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                .collect(Collectors.toSet()));
        } else if (scopeClaim instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> scopes = (Collection<String>) scopeClaim;
            authorities.addAll(scopes.stream()
                .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                .collect(Collectors.toSet()));
        }
        
        return authorities;
    }
}
