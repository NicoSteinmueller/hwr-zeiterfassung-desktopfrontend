package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;

@Data
public class TimeAction
{
    private boolean isStart;
    private boolean isBreak;
    private int projectId;
}
