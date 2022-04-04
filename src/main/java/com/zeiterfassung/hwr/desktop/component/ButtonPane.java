package com.zeiterfassung.hwr.desktop.component;

import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.Project;
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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Qualifier("nextPane")
public class ButtonPane implements IUILayout
{

    @Autowired
    private Login model;
    private final String baseUrl;
    private BorderPane borderpane;
    private HBox hBox;
    private ChoiceBox<String> btnProject;
    private Button btnStart;
    private Button btnEnd;
    private Button btnBreak;

    public ButtonPane(@Value("${spring.application.api.human}") String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    @Override
    public Parent getParent()
    {
        Map<String, String> user = fetchUserName();
        String greeting = "Hi " + user.get("fistName") + " " + user.get("lastName");


        List<String> projectNames = fetchProjects().stream()
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

        btnProject.getStyleClass().add("redAlternativeButton");
        btnStart.getStyleClass().add("redButton");
        btnEnd.getStyleClass().add("redButton");
        btnBreak.getStyleClass().add("blueButton");

        borderpane = new BorderPane();
        borderpane.setTop(new Label(greeting));
        borderpane.setCenter(hBox);


        btnProject.setOnAction(select ->
        {
            btnStart.setText("Projekt wechseln");
            btnStart.setDisable(false);
        });

        btnStart.setOnAction(click ->
        {
            btnStart.setText("Arbeit fortsetzen");
            btnStart.setDisable(true);
            btnEnd.setDisable(false);
            btnBreak.setDisable(false);

            //TODO Day an Server schicken POST
        });

        btnEnd.setOnAction(click ->
        {
            btnStart.setDisable(false);
            btnEnd.setDisable(true);
            btnBreak.setDisable(true);

            //TODO Day an Server schicken PUT

        });

        btnBreak.setOnAction(click ->
        {
            btnStart.setDisable(false);
            btnEnd.setDisable(false);
            btnBreak.setDisable(true);

            //TODO Day an Server schicken PUT
        });

        return borderpane;
    }

    private List<Project> fetchProjects()
    {

        return WebClient.create(baseUrl)
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
        return WebClient.create(baseUrl)
                .get()
                .uri(uriBuilder -> uriBuilder.path("/name")
                        .queryParam("email", model.getEmail())
                        .queryParam("password", model.getPassword())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() { })
                .block();
    }
}
