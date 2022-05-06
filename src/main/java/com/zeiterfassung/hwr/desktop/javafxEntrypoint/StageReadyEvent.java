package com.zeiterfassung.hwr.desktop.javafxEntrypoint;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * The type Stage ready event.
 */
public class StageReadyEvent extends ApplicationEvent
{
    /**
     * Gets stage.
     *
     * @return the stage
     */
    public Stage getStage()
    {
        return (Stage) getSource();
    }

    /**
     * Instantiates a new Stage ready event.
     *
     * @param source the source
     */
    public StageReadyEvent(Stage source)
    {
        super(source);
    }
}
