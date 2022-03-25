package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Human implements IHuman
{
    private String firstName;
    private String lastName;
    private String email;
}
