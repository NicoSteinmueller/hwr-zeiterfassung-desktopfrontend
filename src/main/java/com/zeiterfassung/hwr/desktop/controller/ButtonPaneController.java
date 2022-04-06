package com.zeiterfassung.hwr.desktop.controller;

import com.zeiterfassung.hwr.desktop.component.ButtonPane;
import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.Project;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.function.Function;

@Controller
public class ButtonPaneController
{

    private ButtonPane view;
    private Login model;
    private final String BASEURL;
    private Boolean isWork;
    private Boolean isManual;
    private int currentProjectId;

    public ButtonPaneController(@Value("${spring.application.api.baseUrl}") String baseUrl,
                                Login login,
                                ButtonPane buttonPane)
    {
        this.view = buttonPane;
        this.model = login;
        this.BASEURL = baseUrl;
    }

    public void setController()
    {
        isWork = true;
        currentProjectId = -1;

        List<Project> projects = fetchProjects();
        List<String> projectNames = projects.stream()
                .map(Project::getName)
                .sorted()
                .toList();

        Map<String, String> user = fetchUserName();
        String greeting = "Hi " + user.get("firstName") + " " + user.get("lastName");
        view.getGreetingLabel().setText(greeting);

        view.getBtnProject().setItems(FXCollections.observableArrayList(projectNames));
        view.getBtnProject().setValue(projectNames.stream()
                .min(String::compareTo)
                .orElse("Projekt"));

        view.getBtnProject().setOnAction(projectEventHandler);
        view.getBtnStart().setOnAction(startEventHandler(projects));
        view.getBtnEnd().setOnAction(endEventHandler(projects));
        view.getBtnBreak().setOnAction(breakEventHandler(projects));
        view.getBtnProject().getSelectionModel()
                .selectedItemProperty()
                .addListener((change, oldValue, newValue) -> {

                    //Auswahl des Projekts im Dropdownmenü
                    int selectedProjectId = projects.stream()
                            .filter(project -> project.getName().equals(newValue))
                            .findFirst()
                            .map(Project::getId)
                            .orElseThrow();

                    //Darf nicht zum aktuellen Projekt wechseln
                    if(selectedProjectId == currentProjectId){
                       view.getBtnStart().setText("Arbeit fortsetzen");
                       if(!isManual && isWork){
                           view.getBtnStart().setDisable(true);
                       }
                    }


                });
    }


    @NotNull
    private EventHandler<ActionEvent> projectEventHandler = btnClick ->
    {
        view.getBtnStart().setText("Projekt wechseln");
        view.getBtnStart().setDisable(false);
    };

    @NotNull
    private EventHandler<ActionEvent> startEventHandler(List<Project> projects)
    {
        return btnClick ->
        {
            view.getErrorLabel().setVisible(false);
            int projectID = getProjectID(projects);

            if (!isWork)
            {
                postTime(false, true, projectID, clientResponse -> Mono.empty());
            }

            // Automatisch Arbeitszeitblock bei Projektwechsel beenden
            if (currentProjectId > 0 && currentProjectId != projectID && isWork && !isManual)
            {
                postTime(false, false, currentProjectId, clientResponse -> Mono.empty());
            }

            postTime(true, false, projectID, clientResponse ->
            {
                currentProjectId = projectID;

                Platform.runLater(() ->
                {
                    isWork = true;
                    isManual = false;
                    view.getBtnStart().setText("Arbeit fortsetzen");
                    setBtnStatus(true, false, false);
                });

                return Mono.empty();
            });
        };
    }


    @NotNull
    private EventHandler<ActionEvent> endEventHandler(List<Project> projects)
    {
        return click ->
        {
            view.getErrorLabel().setVisible(false);

            int projectID = getProjectID(projects);

            postTime(false, false, projectID, clientResponse ->
            {
                Platform.runLater(() ->
                {
                    isManual = true;
                    setBtnStatus(false, true, true);
                });
                return Mono.empty();
            });
        };
    }

    private EventHandler<ActionEvent> breakEventHandler(List<Project> projects)
    {
        return btnClick ->
        {
            view.getErrorLabel().setVisible(false);

            int projectID = getProjectID(projects);

            //Beendet Arbeitszeitblock
            postTime(false, false, projectID, clientResponse -> Mono.empty());

            //Started Pause
            postTime(true, true, projectID, clientResponse ->
            {
                Platform.runLater(() ->
                {
                    isWork = false;
                    setBtnStatus(false, true, true);
                });

                return Mono.empty();
            });
        };
    }

    private void setBtnStatus(Boolean disabledBtnStart, Boolean disabledBtnEnd, Boolean disabledBtnBreak)
    {
        view.getBtnStart().setDisable(disabledBtnStart);
        view.getBtnEnd().setDisable(disabledBtnEnd);
        view.getBtnBreak().setDisable(disabledBtnBreak);
    }

    private List<Project> fetchProjects()
    {
        return WebClient.create(BASEURL + "/human")
                .get()
                .uri(buildUri("/getAllProjects"))
                .retrieve()
                .bodyToFlux(Project.class)
                .collectList()
                .block();
    }


    private Map<String, String> fetchUserName()
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

    private int getProjectID(List<Project> projects)
    {
        String selectedProjectName = view.getBtnProject().getValue();
        return projects.stream()
                .filter(project -> project.getName().equals(selectedProjectName))
                .findFirst()
                .map(Project::getId)
                .orElseThrow();
    }

    private void postTime(Boolean isStart, Boolean isBreak, int projectID,
                          Function<ClientResponse, Mono<? extends Throwable>> acceptedResponse)
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

    @NotNull
    private Function<ClientResponse, Mono<? extends Throwable>> errorResponse = clientResponse ->
    {
        Platform.runLater(() ->
        {
            String errorReason = clientResponse.statusCode().getReasonPhrase();
            view.getErrorLabel().setText("Error: " + errorReason);
            view.getErrorLabel().setVisible(true);
        });
        return Mono.empty();
    };


}
