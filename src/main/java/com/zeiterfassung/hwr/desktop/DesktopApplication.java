package com.zeiterfassung.hwr.desktop;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DesktopApplication {

	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
	}

}
