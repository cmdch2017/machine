package com.example.machine.model;

import java.util.ArrayList;
import java.util.List;

public class PathConnection {
    private String targetId;
    private List<Point> waypoints;

    public PathConnection() {
        this.waypoints = new ArrayList<>();
    }

    public PathConnection(String targetId) {
        this.targetId = targetId;
        this.waypoints = new ArrayList<>();
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public List<Point> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Point> waypoints) {
        this.waypoints = waypoints;
    }

    public void addWaypoint(Point waypoint) {
        this.waypoints.add(waypoint);
    }
} 