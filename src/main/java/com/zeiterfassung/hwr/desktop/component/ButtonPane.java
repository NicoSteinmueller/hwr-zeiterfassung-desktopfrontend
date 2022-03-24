package com.zeiterfassung.hwr.desktop.component;

import com.zeiterfassung.hwr.desktop.entities.Human;
import com.zeiterfassung.hwr.desktop.entities.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;


@Component
@Qualifier("nextPane")
public class ButtonPane implements IUILayout
{

    @Autowired
    private Human user;
    private BorderPane borderpane;
    private HBox hBox;
    private ChoiceBox<String> btnProject;
    private Button btnStart;
    private Button btnEnd;
    private Button btnBreak;

    @Override
    public Parent getParent()
    {
        String greeting = "Hi " + user.getFirstName();

        //TODO mock entfernen
        btnProject = new ChoiceBox<>(FXCollections.observableArrayList(
                "First", "Second", "Third"));
        btnProject.setValue("First");

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


        btnProject.setOnAction(select ->{
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
}
