package com.zeiterfassung.hwr.desktop;


import com.zeiterfassung.hwr.desktop.component.LoginPane;
import com.zeiterfassung.hwr.desktop.controller.LoginController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


@Component
public class StageListener implements ApplicationListener<StageReadyEvent>
{

    private final String TITLE;
    private final ClassPathResource CSS;
    private LoginPane pane;
    private LoginController controller;


    public StageListener(@Value("${spring.application.window.title}") String applicationTitle,
                         @Value("static/styling.css") ClassPathResource classPathResource,
                         LoginPane loginPane, LoginController loginController)
    {
        this.TITLE = applicationTitle;
        this.CSS = classPathResource;
        this.pane = loginPane;
        this.controller = loginController;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event)
    {
        Stage stage = event.getStage();
        Scene scene = new Scene(pane.getParent());
        controller.setController();
        scene.getStylesheets().add(this.CSS.getPath());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(this.TITLE);
        stage.show();
    }
}
