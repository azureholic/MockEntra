package com.example.demoapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtValidationProperties jwtValidationProperties;
    
    @Autowired
    private OAuth2Properties oauth2Properties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests(authz -> authz
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                .antMatchers("/api/validate").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtValidationProperties.getJwkSetUri()).build();
        
        // Standard JWT validations: timestamp, issuer, and custom audience
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(jwtValidationProperties.getValidTokenIssuer());
        
        // Custom audience validator
        OAuth2TokenValidator<Jwt> audienceValidator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                String expectedAudience = jwtValidationProperties.getValidAudience();
                
                // Check if audience is a list (standard JWT spec)
                List<String> audiences = jwt.getAudience();
                if (audiences != null && audiences.contains(expectedAudience)) {
                    return OAuth2TokenValidatorResult.success();
                }
                
                // Check if audience is a single string claim (Keycloak format)
                String audienceClaim = jwt.getClaimAsString("aud");
                if (audienceClaim != null && audienceClaim.equals(expectedAudience)) {
                    return OAuth2TokenValidatorResult.success();
                }
                
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience", 
                        "Invalid audience. Expected: " + expectedAudience + 
                        ", Found list: " + audiences + 
                        ", Found string: " + audienceClaim, null)
                );
            }
        };
        
        OAuth2TokenValidator<Jwt> withDefaults = new DelegatingOAuth2TokenValidator<>(
            timestampValidator, issuerValidator, audienceValidator
        );
        
        jwtDecoder.setJwtValidator(withDefaults);
        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter(oauth2Properties));
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        if (oauth2Properties.isCors()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowedMethods(Arrays.asList(
                oauth2Properties.getCorsAllowedMethods().split(",")
            ));
            configuration.setAllowedHeaders(Arrays.asList(
                oauth2Properties.getCorsAllowedHeaders().split(",")
            ));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge((long) oauth2Properties.getCorsMaxAge());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
