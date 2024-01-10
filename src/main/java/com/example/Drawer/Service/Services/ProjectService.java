package com.example.Drawer.Service.Services;
import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import graphql.servlet.GraphQLSingleInvocationInput;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.dataloader.impl.Assertions;
import org.hibernate.mapping.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Service
public class ProjectService {
    Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final WebClient webClient;
    private final WebClient graphQLWebClient;
    GraphQlClient graphQlClient;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public ProjectService(WebClient.Builder webClientBuilder, WebClient.Builder graphQLWebClient) {
        this.webClient = webClientBuilder.baseUrl("http://20.84.17.217/").build();
        this.graphQLWebClient = graphQLWebClient.baseUrl("http://20.84.17.217/api/users/graphql").build();
        //this.graphQLWebClient = graphQLWebClient.baseUrl("http://localhost:8082/api/users/graphql").build();
        WebClient client = WebClient.builder()
                .baseUrl("https://countries.trevorblades.com")
                .build();
        graphQlClient = HttpGraphQlClient.builder(client).build();
    }




    @Retry(name = "retryUpload", fallbackMethod = "fallbackAfterRetry")
    public String getIDFromUserMicroservice(String username) {
        logger.error("Entered service (getIDFromUserMicroservice)");
        RestTemplate restTemplate = new RestTemplate();
        //String fooResourceUrl = "http://20.84.17.217/api/users/get-id?username=";
        String fooResourceUrl = "http://localhost:8082/api/users/get-id?username=";
        ResponseEntity<String> response = restTemplate.getForEntity(fooResourceUrl+username, String.class);

        if(response.getStatusCode()==HttpStatus.OK)
            logger.info("HTTP request to User API is good :)");

        logger.error("Exited service getIDFromUserMicroservice");
        return response.getBody();
    }

    public record User(
            @Id
            Integer id,
            String username,
            String password,
            String profileDescription,
            String profile_path
    ) {
    }


    @CircuitBreaker(name = "GraphBreaker", fallbackMethod = "fallbackGraphql")
    public String getIDFromUserWithGrapQL(String username) {

        String graphqlQuery = "{"+
                  "getUserByUsername(username: \""+username+"\") {"+
                    "id "+
                    "username "+
                    "password "+
                    "profileDescription "+
                    "}"+
                "}";


        HttpGraphQlClient graphQlClient = HttpGraphQlClient.builder(this.graphQLWebClient).build();

        GraphQlClient.RetrieveSpec projectMono = graphQlClient.document(graphqlQuery).retrieve("getUserByUsername");

        User userInfo = projectMono.toEntity(User.class).block();

        assert userInfo != null;
        return userInfo.id.toString();
    }

    public String fallbackGraphql(Exception ex) {
        logger.error("Entered in to fallback method after multiple retry. Will return ID=0");
        logger.error(ex.toString());
        return "0";
    }

    public String fallbackAfterRetry(Exception ex) {
        logger.error("Entered in to fallback method after multiple retry. Will return ID=0");
        logger.error(ex.toString());
        return "0";
    }
}
