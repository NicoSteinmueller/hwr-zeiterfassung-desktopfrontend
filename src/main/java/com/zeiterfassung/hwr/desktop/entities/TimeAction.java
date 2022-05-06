package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;

/**
 * The Time action data.
 */
@Data
public class TimeAction
{
    private boolean isStart;
    private boolean isBreak;
    private int projectId;
}
