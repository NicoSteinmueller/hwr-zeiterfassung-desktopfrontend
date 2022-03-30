package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class Time
{
        private DateTime start;
        private DateTime end;
        private boolean isBreak;
        private Project project;
}
