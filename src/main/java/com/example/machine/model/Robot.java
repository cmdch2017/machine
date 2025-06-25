package com.example.machine.model;

import lombok.Data;

@Data
public class Robot {
    private double x;
    private double y;
    private double angle;
    private String status;
    private String currentTask;
    
    public Robot() {
        this.x = 0.0;
        this.y = 0.0;
        this.angle = 0.0;
        this.status = "IDLE";
    }
} 