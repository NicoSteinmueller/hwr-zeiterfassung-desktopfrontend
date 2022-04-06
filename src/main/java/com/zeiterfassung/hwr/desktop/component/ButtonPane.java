package com.zeiterfassung.hwr.desktop.component;

import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.Project;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@Qualifier("nextPane")
public class ButtonPane implements IUILayout
{

    @Autowired
    private Login model;
    private Boolean isWork;
    private final String baseUrl;
    private BorderPane borderpane;
    private HBox hBox;
    private ChoiceBox<String> btnProject;
    private Button btnStart;
    private Button btnEnd;
    private Button btnBreak;
    private Label errorLabel;

    public ButtonPane(@Value("${spring.application.api}") String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    @Override
    public Parent getParent()
    {
        isWork = true;
        Map<String, String> user = fetchUserName();
        String greeting = "Hi " + user.get("fistName") + " " + user.get("lastName");

        List<Project> projects = fetchProjects();
        List<String> projectNames = projects.stream()
                .map(Project::getName)
                .toList();
        btnProject = new ChoiceBox<>(FXCollections.observableArrayList(projectNames));
        btnProject.setValue(
                projectNames.stream()
                        .findFirst()
                        .orElse("Projekt")
        );

        btnStart = new Button("Arbeit beginnen");
        btnEnd = new Button("Arbeit beenden");
        btnEnd.setDisable(true);
        btnBreak = new Button("Pause");
        btnBreak.setDisable(true);
        hBox = new HBox();
        hBox.getChildren().addAll(btnProject, btnStart, btnEnd, btnBreak);

        errorLabel = new Label();

        btnProject.getStyleClass().add("redAlternativeButton");
        btnStart.getStyleClass().add("redButton");
        btnEnd.getStyleClass().add("redButton");
        btnBreak.getStyleClass().add("blueButton");

        borderpane = new BorderPane();
        borderpane.setTop(new Label(greeting));
        borderpane.setCenter(hBox);
        borderpane.setBottom(errorLabel);


        btnProject.setOnAction(select ->
        {
            btnStart.setText("Projekt wechseln");
            btnStart.setDisable(false);
        });

        btnStart.setOnAction(click ->
        {
            int projectID = getProjectID(projects);

            if (!isWork)
            {
                postTime(false, true, projectID, clientResponse -> Mono.empty());
            }

            postTime(true, false, projectID
                    , clientResponse ->
                    {
                        Platform.runLater(() ->
                        {
                            btnStart.setText("Arbeit fortsetzen");
                            btnStart.setDisable(true);
                            btnEnd.setDisable(false);
                            btnBreak.setDisable(false);
                        });

                        return Mono.empty();
                    });
        });

        btnEnd.setOnAction(click ->
        {

            int projectID = getProjectID(projects);

            postTime(false, false, projectID
                    , clientResponse ->
                    {
                        Platform.runLater(() ->
                        {
                            btnStart.setDisable(false);
                            btnEnd.setDisable(true);
                            btnBreak.setDisable(true);
                        });

                        return Mono.empty();
                    });

        });

        btnBreak.setOnAction(click ->
        {
            int projectID = getProjectID(projects);

            postTime(true, true, projectID
                    , clientResponse ->
                    {
                        Platform.runLater(() ->
                        {
                            isWork = false;
                            btnStart.setDisable(false);
                            btnEnd.setDisable(false);
                            btnBreak.setDisable(true);
                        });

                        return Mono.empty();
                    });
        });

        return borderpane;
    }

    private List<Project> fetchProjects()
    {

        return WebClient.create(baseUrl + "/human")
                .get()
                .uri(uriBuilder -> uriBuilder.path("/getAllProjects")
                        .queryParam("email", model.getEmail())
                        .queryParam("password", model.getPassword())
                        .build())
                .retrieve()
                .bodyToFlux(Project.class)
                .collectList()
                .block();
    }

    private Map<String, String> fetchUserName()
    {
        return WebClient.create(baseUrl + "/human")
                .get()
                .uri(uriBuilder -> uriBuilder.path("/name")
                        .queryParam("email", model.getEmail())
                        .queryParam("password", model.getPassword())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>()
                {
                })
                .block();
    }

    private int getProjectID(List<Project> projects)
    {
        String selectedProjectName = btnProject.getValue();
        return projects.stream()
                .filter(project -> project.getName().equals(selectedProjectName))
                .findFirst()
                .map(Project::getId)
                .orElseThrow();
    }

    private void postTime(Boolean isStart, Boolean isBreak, int projectID,
                          Function<ClientResponse, Mono<? extends Throwable>> acceptedResponse)
    {
        WebClient.create(baseUrl + "/book")
                .post()
                .uri(uriBuilder -> uriBuilder.path("/time")
                        .queryParam("email", model.getEmail())
                        .queryParam("password", model.getPassword())
                        .queryParam("isStart", isStart)
                        .queryParam("pause", isBreak)
                        .queryParam("note", "")
                        .queryParam("projectId", projectID)
                        .build())
                .retrieve()
                .onStatus(httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED), acceptedResponse)
                .onStatus(HttpStatus::isError, clientResponse ->
                {
                    Platform.runLater(() ->
                    {
                        String errorReason = clientResponse.statusCode().getReasonPhrase();
                        errorLabel.setText("Error: " + errorReason);
                    });
                    return Mono.empty();
                })
                .toBodilessEntity()
                .block();

    }
}
