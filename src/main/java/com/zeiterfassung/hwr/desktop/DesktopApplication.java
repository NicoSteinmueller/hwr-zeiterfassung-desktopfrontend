package com.zeiterfassung.hwr.desktop;

import com.zeiterfassung.hwr.desktop.javafxEntrypoint.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The type Desktop application.
 */
@SpringBootApplication
public class DesktopApplication
{
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args)
    {
        Application.launch(JavaFxApplication.class, args);
    }
}
