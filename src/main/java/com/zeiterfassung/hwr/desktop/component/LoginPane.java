package com.zeiterfassung.hwr.desktop.component;

import com.google.common.hash.Hashing;
import com.zeiterfassung.hwr.desktop.entities.Login;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@Component
@Qualifier("Login")
public class LoginPane implements IUILayout
{

    @Autowired
    private ButtonPane nextPane;
    @Autowired
    private Login login;
    private BorderPane borderPane;
    private VBox vBox;
    private TextField textFieldEmail;
    private PasswordField passwordField;
    private Button btnSubmit;
    private Label errorLabel;

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
        errorLabel = new Label("Fehler");
        errorLabel.setVisible(false);
        errorLabel.getStyleClass().add("errorLabel");
        vBox = new VBox();
        vBox.getChildren().addAll(textFieldEmail, passwordField, btnSubmit, errorLabel);
        borderPane = new BorderPane();
        borderPane.setLeft(new ImageView(image));
        borderPane.setCenter(vBox);

        btnSubmit.setOnAction(click ->
        {
            login.setEmail(textFieldEmail.getText());
            login.setPassword(hash(passwordField.getText()));

            WebClient.create("http://localhost:8080/login")
                    .post()
                    .uri(uriBuilder -> uriBuilder.path("/basicLogin")
                            .queryParam("email", login.getEmail())
                            .queryParam("password", login.getPassword())
                            .build())
                    .bodyValue(login)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED), response ->
                    {

                        Platform.runLater(() ->
                        {
                            Button btn = (Button) click.getSource();
                            Scene scene = btn.getScene();
                            Stage stage = (Stage) scene.getWindow();
                            Scene nextScene = new Scene(nextPane.getParent());
                            nextScene.getStylesheets().add("static/styling.css");
                            stage.setScene(nextScene);

                        });
                        return Mono.empty();
                    })
                    .onStatus(HttpStatus::isError, response ->
                    {
                        Platform.runLater(() ->
                        {
                            errorLabel.setVisible(true);
                        });
                        return Mono.empty();
                    })
                    .bodyToMono(HttpStatus.class)
                    .block();


        });


        return borderPane;
    }

    private String hash(String text)
    {
        return Hashing.sha256()
                .hashString(text, StandardCharsets.UTF_8)
                .toString();
    }


}
