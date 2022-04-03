package com.zeiterfassung.hwr.desktop.component;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
@Qualifier("Login")
@Getter
public class LoginPane implements IUILayout
{
    private BorderPane borderPane;
    private final String imageUrl;
    private Image logo;
    private VBox vBox;
    private TextField textFieldEmail;
    private PasswordField passwordField;
    private Button btnSubmit;
    private Label errorLabel;

    public LoginPane(@Value("static/logo.png") String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    @Override
    public Parent getParent()
    {
        logo = new Image(imageUrl, 150, 150, true, false);
        textFieldEmail = new TextField();
        passwordField = new PasswordField();
        btnSubmit = new Button("Submit");
        errorLabel = new Label("Fehler");
        vBox = new VBox(5, textFieldEmail, passwordField, btnSubmit, errorLabel);
        borderPane = new BorderPane();
        textFieldEmail.setPromptText("Email");
        passwordField.setPromptText("Password");
        errorLabel.setVisible(false);
        borderPane.setCenter(vBox);
        borderPane.setLeft(new ImageView(logo));

        btnSubmit.getStyleClass().add("redButton");
        errorLabel.getStyleClass().add("errorLabel");
        return borderPane;
    }

}
