package com.zeiterfassung.hwr.desktop;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
public class DesktopApplication
{
    public static void main(String[] args)
    {
        Application.launch(JavaFxApplication.class, args);
    }
}
