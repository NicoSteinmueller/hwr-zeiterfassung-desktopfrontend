package com.zeiterfassung.hwr.desktop.component;

import com.zeiterfassung.hwr.desktop.entities.Human;
import com.zeiterfassung.hwr.desktop.entities.Login;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("Login")
public class LoginPane implements IUILayout
{

    @Autowired
    private ButtonPane nextPane;
    @Autowired
    private Login Login;
    @Autowired
    private Human user;
    private BorderPane borderPane;
    private VBox vBox;
    private TextField textFieldEmail;
    private PasswordField passwordField;
    private Button btnSubmit;

    @Override
    public Parent getParent()
    {
        Image image = new Image("static/logo.png", 150, 150, true, true);
        textFieldEmail = new TextField();
        textFieldEmail.setPromptText("Benutzername");
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        btnSubmit = new Button("login");
        btnSubmit.getStyleClass().add("redButton");
        vBox = new VBox();
        vBox.getChildren().addAll(textFieldEmail, passwordField, btnSubmit);
        borderPane = new BorderPane();
        borderPane.setLeft(new ImageView(image));
        borderPane.setCenter(vBox);

        btnSubmit.setOnAction(click ->
        {
            if (isUserValid())
            {
                Button btn = (Button) click.getSource();
                Scene scene = btn.getScene();
                Stage stage = (Stage) scene.getWindow();
                Scene nextScene = new Scene(nextPane.getParent());
                nextScene.getStylesheets().add("static/styling.css");
                stage.setScene(nextScene);
            }
        });


        return borderPane;
    }

    private boolean isUserValid()
    {
        //TODO Uservalidation
        return true;
    }

}
