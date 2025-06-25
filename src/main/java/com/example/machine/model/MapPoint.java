package com.example.machine.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MapPoint {
    private String id;
    private String name;
    private double x;
    private double y;
    private String type;  // 点位类型：充电点、工作点等
    private List<PathConnection> connections;

    public MapPoint() {
        this.connections = new ArrayList<>();
    }

    public MapPoint(String id, String name, double x, double y, String type) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.type = type;
        this.connections = new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PathConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<PathConnection> connections) {
        this.connections = connections;
    }

    public void addConnection(PathConnection connection) {
        this.connections.add(connection);
    }
} 