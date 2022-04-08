package com.zeiterfassung.hwr.desktop.component.views;

import com.zeiterfassung.hwr.desktop.interfaces.IUILayout;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("nextPane")
@Getter
public class ButtonPane implements IUILayout
{
    private BorderPane borderpane;
    private Label greetingLabel;
    private HBox hBoxBtn;
    private ChoiceBox<String> btnProject;
    private Button btnStart;
    private Button btnEnd;
    private Button btnBreak;
    private HBox hBoxLabel;
    private Label projectLabel;
    private Label errorLabel;

    @Override
    public Parent asParent()
    {
        greetingLabel = new Label();
        btnProject = new ChoiceBox<>();
        btnStart = new Button("Arbeit beginnen");
        btnEnd = new Button("Arbeit beenden");
        btnEnd.setDisable(true);
        btnBreak = new Button("Pause");
        btnBreak.setDisable(true);
        hBoxBtn = new HBox(btnProject, btnStart, btnEnd, btnBreak);

        projectLabel = new Label("Achtung: Projektauswahl mit 'Projekt wechseln' bestätigen!");
        projectLabel.setVisible(false);
        errorLabel = new Label();
        hBoxLabel = new HBox(projectLabel, errorLabel);

        btnProject.getStyleClass().add("redAlternativeButton");
        btnStart.getStyleClass().add("redButton");
        btnEnd.getStyleClass().add("redButton");
        btnBreak.getStyleClass().add("blueButton");

        borderpane = new BorderPane();
        borderpane.setTop(greetingLabel);
        borderpane.setCenter(hBoxBtn);
        borderpane.setBottom(hBoxLabel);

        return borderpane;
    }




}
