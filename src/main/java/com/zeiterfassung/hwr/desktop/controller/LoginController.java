package com.zeiterfassung.hwr.desktop.controller;

import com.google.common.hash.Hashing;
import com.zeiterfassung.hwr.desktop.component.ButtonPane;
import com.zeiterfassung.hwr.desktop.component.LoginPane;
import com.zeiterfassung.hwr.desktop.entities.Login;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Predicate;

@Controller
public class LoginController
{
    private LoginPane loginPane;
    private ButtonPane nextPane;
    private Login model;
    private final String BASEURL;

    public LoginController(@Value("${spring.application.api.baseUrl}") String baseUrl,
                           LoginPane loginPane, ButtonPane buttonPane, Login login){
        this.BASEURL = baseUrl;
        this.loginPane = loginPane;
        this.nextPane = buttonPane;
        this.model = login;
    }

    public void setController(){
        loginPane.getBtnSubmit().setOnAction(submitEventHandler);
    }

    private EventHandler<ActionEvent> submitEventHandler = btnClick ->
    {
        setLogin();
        validateLoginAndProceed(btnClick);
    };

    private void setLogin()
    {
        model.setEmail(loginPane.getTextFieldEmail().getText());
        model.setPassword(hash(loginPane.getPasswordField().getText()));
    }

    private void validateLoginAndProceed(ActionEvent btnClick)
    {
        WebClient.create(BASEURL+"/login")
                .post()
                .uri(buildUri)
                .bodyValue(model)
                .retrieve()
                .onStatus(isAccepted, response ->
                {
                    Platform.runLater(changeScene(btnClick));
                    return Mono.empty();
                })
                .onStatus(HttpStatus::isError, response ->
                {
                    Platform.runLater(displayError);
                    return Mono.empty();
                })
                .bodyToMono(HttpStatus.class)
                .block();
    }

    @NotNull
    private Function<UriBuilder, URI> buildUri = uriBuilder -> uriBuilder.path("/basicLogin")
            .queryParam("email", model.getEmail())
            .queryParam("password", model.getPassword())
            .build();


    private String hash(String text)
    {
        return Hashing.sha256()
                .hashString(text, StandardCharsets.UTF_8)
                .toString();
    }

    @NotNull
    private Runnable displayError = () -> loginPane.getErrorLabel().setVisible(true);

    @NotNull
    private Predicate<HttpStatus> isAccepted = httpStatus -> httpStatus.equals(HttpStatus.ACCEPTED);

    @Contract(pure = true)
    private @NotNull Runnable changeScene(ActionEvent event)
    {
        return () ->
        {
            Button btn = (Button) event.getSource();
            Scene scene = btn.getScene();
            Stage stage = (Stage) scene.getWindow();
            Scene nextScene = new Scene(nextPane.getParent());
            nextScene.getStylesheets().add("static/styling.css");
            stage.setScene(nextScene);

        };
    }
}
