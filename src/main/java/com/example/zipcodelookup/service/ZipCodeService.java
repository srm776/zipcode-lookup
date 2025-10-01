package com.example.zipcodelookup.service;

import com.example.zipcodelookup.model.ZipCodeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ZipCodeService {
    
    private final WebClient webClient;
    
    public ZipCodeService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.zippopotam.us")
                .build();
    }
    
    public ZipCodeResponse getZipCodeInfo(String zipCode) {
        try {
            return webClient.get()
                    .uri("/us/{zipCode}", zipCode)
                    .retrieve()
                    .bodyToMono(ZipCodeResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching zip code information", e);
        }
    }
}