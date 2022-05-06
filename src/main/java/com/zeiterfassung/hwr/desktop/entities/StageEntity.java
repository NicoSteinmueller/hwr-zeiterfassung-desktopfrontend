package com.zeiterfassung.hwr.desktop.entities;

import javafx.stage.Stage;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * The Stage entity.
 */
@Component
@Data
public class StageEntity
{
    /**
     * The Primary stage.
     */
    Stage primaryStage;
}
