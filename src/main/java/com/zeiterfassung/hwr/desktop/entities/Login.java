package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * The Login data
 */
@Component
@Data
public class Login
{
    private String email;
    private String password;
}
