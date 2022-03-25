package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Login implements ILogin
{

    private String email;
    private String password;

}
