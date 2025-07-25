package com.example.webclient.service;

import com.example.webclient.config.OAuth2ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class TokenService {

    @Autowired
    private OAuth2ClientProperties oauth2Properties;

    private final WebClient webClient;

    public TokenService() {
        this.webClient = WebClient.builder().build();
    }

    public Mono<String> acquireToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        
        // Always use Client Credentials Grant for app-to-app authentication
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", oauth2Properties.getClientId());
        formData.add("client_secret", oauth2Properties.getClientSecret());
        formData.add("scope", oauth2Properties.getScope());

        // Debug logging
        System.out.println("DEBUG: Token endpoint URL: " + oauth2Properties.getTokenEndpoint());
        System.out.println("DEBUG: Client ID: " + oauth2Properties.getClientId());
        System.out.println("DEBUG: Environment TOKEN_ENDPOINT: " + System.getenv("TOKEN_ENDPOINT"));

        return webClient.post()
                .uri(oauth2Properties.getTokenEndpoint())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }
}
