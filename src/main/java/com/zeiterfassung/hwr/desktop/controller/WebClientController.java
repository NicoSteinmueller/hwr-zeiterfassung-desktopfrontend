package com.zeiterfassung.hwr.desktop.controller;

import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.Project;
import com.zeiterfassung.hwr.desktop.entities.TimeAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Controller
public class WebClientController
{
    private final String BASEURL;
    private Login model;

    public WebClientController(@Value("${spring.application.api.baseUrl}") String baseUrl, Login login)
    {
        this.BASEURL = baseUrl;
        this.model = login;
    }

    @NotNull
    private final Predicate<HttpStatus> isAccepted = httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED);

    protected void verifyLogin(Function<ClientResponse, Mono<? extends Throwable>> acceptedResponse,
                               Function<ClientResponse, Mono<? extends Throwable>> errorResponse)
    {
        WebClient.create(BASEURL + "/login")
                .post()
                .uri(buildUri("/basicLogin"))
                .bodyValue(model)
                .retrieve()
                .onStatus(isAccepted, acceptedResponse)
                .onStatus(HttpStatus::isError, errorResponse)
                .bodyToMono(HttpStatus.class)
                .block();
    }


    protected void postTime(Boolean isStart, Boolean isBreak, int projectID,
                  Function<ClientResponse, Mono<? extends Throwable>> acceptedResponse,
                  Function<ClientResponse, Mono<? extends Throwable>> errorResponse)
    {
        WebClient.create(BASEURL + "/book")
                .post()
                .uri(buildComplexUri(isStart, isBreak, projectID))
                .retrieve()
                .onStatus(httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED), acceptedResponse)
                .onStatus(HttpStatus::isError, errorResponse)
                .toBodilessEntity()
                .block();

    }

    protected Optional<TimeAction> fetchTodaysLastBookedTime()
    {
        return WebClient.create(BASEURL + "/time")
                .get()
                .uri(buildUri("/lastStatus"))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Optional<TimeAction>>()
                {
                })
                .block();
    }

    protected List<Project> fetchProjects()
    {
        return WebClient.create(BASEURL + "/human")
                .get()
                .uri(buildUri("/getAllProjects"))
                .retrieve()
                .bodyToFlux(Project.class)
                .collectList()
                .block();
    }

    protected Map<String, String> fetchUserName()
    {
        return WebClient.create(BASEURL + "/human")
                .get()
                .uri(buildUri("/name"))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>()
                {
                })
                .block();
    }

    @NotNull
    private Function<UriBuilder, URI> buildUri(String path)
    {
        return uriBuilder -> uriBuilder.path(path)
                .queryParam("email", model.getEmail())
                .queryParam("password", model.getPassword())
                .build();
    }

    @NotNull
    private Function<UriBuilder, URI> buildComplexUri(Boolean isStart, Boolean isBreak, int projectID)
    {
        return uriBuilder -> uriBuilder.path("/time")
                .queryParam("email", model.getEmail())
                .queryParam("password", model.getPassword())
                .queryParam("isStart", isStart)
                .queryParam("pause", isBreak)
                .queryParam("note", "")
                .queryParam("projectId", projectID)
                .build();
    }
}
