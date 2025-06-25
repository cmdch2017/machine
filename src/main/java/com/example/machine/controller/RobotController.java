package com.example.machine.controller;

import com.example.machine.model.Robot;
import com.example.machine.model.MapPoint;
import com.example.machine.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/robot")
public class RobotController {

    @Autowired
    private RobotService robotService;

    @GetMapping("/status")
    public Robot getRobotStatus() {
        return robotService.getRobotStatus();
    }

    @PostMapping("/move")
    public void moveRobot(@RequestBody MapPoint target) {
        robotService.moveToPoint(target);
    }

    @GetMapping("/map-points")
    public List<MapPoint> getMapPoints() {
        return robotService.getMapPoints();
    }

    @MessageMapping("/update-position")
    @SendTo("/topic/position")
    public Robot updatePosition(Robot robot) {
        return robotService.updateRobotPosition(robot);
    }
} 