package com.zeiterfassung.hwr.desktop.javafxEntrypoint;

import com.zeiterfassung.hwr.desktop.DesktopApplication;
import com.zeiterfassung.hwr.desktop.entities.StageEntity;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


/**
 * The Java fx application.
 */
public class JavaFxApplication extends Application
{

    private ConfigurableApplicationContext context;
    @Autowired
    private StageEntity stageEntity;

    /**
     * override the init methode
     */
    @Override
    public void init()
    {

        ApplicationContextInitializer<GenericApplicationContext> initializer = applicationContext ->
        {
            applicationContext.registerBean(Application.class, () -> JavaFxApplication.this);
            applicationContext.registerBean(Parameters.class, this::getParameters);
            applicationContext.registerBean(HostServices.class, this::getHostServices);
        };

        this.context = new SpringApplicationBuilder()
                .sources(DesktopApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    /**
     * start the application with the primary Stage
     *
     * @param primaryStage  the primary Stage
     */
    @Override
    public void start(Stage primaryStage)
    {
        stageEntity.setPrimaryStage(primaryStage);
        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    /**
     * methode for stop the application
     */
    @Override
    public void stop()
    {
        this.context.close();
        Platform.exit();
    }

}
