package com.zeiterfassung.hwr.desktop.component;

import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.Project;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
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
@Getter
public class ButtonPane implements IUILayout
{
    private BorderPane borderpane;
    private Label greetingLabel;
    private HBox hBox;
    private ChoiceBox<String> btnProject;
    private Button btnStart;
    private Button btnEnd;
    private Button btnBreak;
    private Label errorLabel;

    @Override
    public Parent getParent()
    {
        greetingLabel = new Label();
        btnProject = new ChoiceBox<>();
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
        borderpane.setTop(greetingLabel);
        borderpane.setCenter(hBox);
        borderpane.setBottom(errorLabel);

        return borderpane;
    }




}
