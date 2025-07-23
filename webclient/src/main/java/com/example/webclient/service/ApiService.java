package com.example.webclient.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class ApiService {

    @Value("${demo.api.base-url:http://localhost:8081}")
    private String apiBaseUrl;
    
    @Value("${demo.api.validate-endpoint:/api/validate}")
    private String validateEndpoint;

    private final WebClient webClient;

    public ApiService() {
        this.webClient = WebClient.builder().build();
    }

    public Mono<String> callApiWithoutToken() {
        return webClient.get()
                .uri(apiBaseUrl + validateEndpoint)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.just("401 Unauthorized - No token provided");
                    }
                    return Mono.just("Error: " + ex.getStatusCode() + " - " + ex.getMessage());
                });
    }

    public Mono<String> callApiWithToken(String token) {
        return webClient.get()
                .uri(apiBaseUrl + validateEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.just("401 Unauthorized - Invalid or expired token");
                    }
                    return Mono.just("Error: " + ex.getStatusCode() + " - " + ex.getMessage());
                });
    }
}
