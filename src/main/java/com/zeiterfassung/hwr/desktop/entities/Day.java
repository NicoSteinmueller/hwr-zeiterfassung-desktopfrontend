package com.zeiterfassung.hwr.desktop.entities;

import lombok.Data;

import java.util.Set;

@Data
public class Day
{
        private Human human;
        private Set<Time> time;
}
