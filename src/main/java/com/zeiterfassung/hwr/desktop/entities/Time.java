package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;

@Data
public class Time
{
        private boolean isStart;
        private boolean isBreak;
        private int projectID;
}
