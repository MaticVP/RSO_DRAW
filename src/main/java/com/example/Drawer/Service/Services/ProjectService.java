package com.example.Drawer.Service.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectService {

    private final WebClient webClient;

    @Autowired
    public ProjectService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/").build();
    }

    public String getIDFromUserMicroservice(String username) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("username", username);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/get-id")
                        .queryParams(queryParams)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
