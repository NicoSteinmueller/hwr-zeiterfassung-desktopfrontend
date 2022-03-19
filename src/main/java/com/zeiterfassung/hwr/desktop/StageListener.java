package com.zeiterfassung.hwr.desktop;


import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {

    private final String TITLE;

    public StageListener(@Value("${spring.application.window.title}") String applicationTitle){
        this.TITLE = applicationTitle;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        Scene scene = new Scene( new BorderPane(), 600, 600);
        stage.setScene(scene);
        stage.setTitle(this.TITLE);
        stage.show();
    }
}
