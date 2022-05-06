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

/**
 * The Web client controller.
 */
@Controller
public class WebClientController
{
    private final String BASEURL;
    private Login model;

    /**
     * Instantiates a new Web client controller.
     *
     * @param baseUrl the base url
     * @param login   the login
     */
    public WebClientController(@Value("${spring.application.api.baseUrl}") String baseUrl, Login login)
    {
        this.BASEURL = baseUrl;
        this.model = login;
    }

    @NotNull
    private final Predicate<HttpStatus> isAccepted = httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED);

    /**
     * Verify login.
     *
     * @param acceptedResponse the accepted response
     * @param errorResponse    the error response
     */
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


    /**
     * Post time.
     *
     * @param isStart          if is start
     * @param isBreak          if is break
     * @param projectID        the project id
     * @param acceptedResponse the accepted response
     * @param errorResponse    the error response
     */
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

    /**
     * Fetch todays last booked time optional.
     *
     * @return optional of time Action
     */
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

    /**
     * Fetch projects list.
     *
     * @return list of Project
     */
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

    /**
     * Fetch the user name map.
     *
     * @return the map of first and last name of the user
     */
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

    /**
     * build the Uri
     * @param path the path
     * @return the function of UriBuilder and Uri
     */
    @NotNull
    private Function<UriBuilder, URI> buildUri(String path)
    {
        return uriBuilder -> uriBuilder.path(path)
                .queryParam("email", model.getEmail())
                .queryParam("password", model.getPassword())
                .build();
    }

    /**
     * build the complex Uri
     * @param isStart   if is Start
     * @param isBreak   if is End
     * @param projectID the project ID
     * @return Function of UriBuilder and Uri
     */
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
