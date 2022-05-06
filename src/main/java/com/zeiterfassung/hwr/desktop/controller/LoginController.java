package com.zeiterfassung.hwr.desktop.controller;

import com.google.common.hash.Hashing;
import com.zeiterfassung.hwr.desktop.component.views.ButtonPane;
import com.zeiterfassung.hwr.desktop.component.views.LoginPane;
import com.zeiterfassung.hwr.desktop.entities.Login;
import com.zeiterfassung.hwr.desktop.entities.StageEntity;
import com.zeiterfassung.hwr.desktop.interfaces.CustomFunctionalInterface;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * The Login controller.
 */
@Controller
public class LoginController
{
    private Login model;
    private LoginPane loginPane;
    private ButtonPane nextPane;
    private ButtonPaneController nextPaneController;
    private WebClientController webClientController;
    private StageEntity stageEntity;

    /**
     * Instantiates a new Login controller.
     *
     * @param login                the login
     * @param loginPane            the login pane
     * @param buttonPane           the button pane
     * @param buttonPaneController the button pane controller
     * @param webClientController  the web client controller
     * @param stageEntity          the stage entity
     */
    public LoginController(Login login,
                           LoginPane loginPane,
                           ButtonPane buttonPane,
                           ButtonPaneController buttonPaneController,
                           WebClientController webClientController,
                           StageEntity stageEntity)
    {
        this.model = login;
        this.loginPane = loginPane;
        this.nextPane = buttonPane;
        this.nextPaneController = buttonPaneController;
        this.webClientController = webClientController;
        this.stageEntity = stageEntity;
    }

    /**
     * Sets controller.
     */
    public void setController()
    {
        loginPane.getBtnSubmit().setOnAction(submitEventHandler);
    }

    /**
     * change the Scene
     */
    @NotNull
    private final Runnable changeScene = () ->
    {
        Scene nextScene = new Scene(nextPane.asParent());
        nextPaneController.setController();
        nextScene.getStylesheets().add("static/styling.css");
        stageEntity.getPrimaryStage().setScene(nextScene);
    };

    /**
     * accept Response Function
     */
    @NotNull
    private final Function<ClientResponse, Mono<? extends Throwable>> acceptedResponse = clientResponse ->
    {
        Platform.runLater(changeScene);
        return Mono.empty();
    };

    /**
     * display Error
     */
    @NotNull
    private final Runnable displayError = () -> loginPane.getErrorLabel().setVisible(true);

    /**
     * error Response
     */
    @NotNull
    private final Function<ClientResponse, Mono<? extends Throwable>> errorResponse = response ->
    {
        Platform.runLater(displayError);
        return Mono.empty();
    };

    /**
     * Hash SHA 256 (no Salt or Pepper)
     */
    @NotNull
    private final Function<String, String> hash = text ->
            Hashing.sha256()
                    .hashString(text, StandardCharsets.UTF_8)
                    .toString();

    /**
     * set email, (hashed)password Login-model
     */
    @NotNull
    private final CustomFunctionalInterface setLogin = () ->
    {
        model.setEmail(loginPane.getTextFieldEmail().getText());
        String password = loginPane.getPasswordField().getText();
        String hashedPassword = hash.apply(password);
        model.setPassword(hashedPassword);
    };

    /**
     * Submitbtn Event-handler
     */
    @NotNull
    private final EventHandler<ActionEvent> submitEventHandler = btnClick ->
    {
        setLogin.execute();
        webClientController.verifyLogin(acceptedResponse, errorResponse);
    };
}
