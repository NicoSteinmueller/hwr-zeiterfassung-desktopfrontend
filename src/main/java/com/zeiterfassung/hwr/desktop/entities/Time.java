package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;

/**
 * The Time data.
 */
@Data
public class Time
{
    private boolean isStart;
    private boolean isBreak;
    private int projectID;
}
