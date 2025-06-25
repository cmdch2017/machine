package com.example.machine.service;

import com.example.machine.model.Robot;
import com.example.machine.model.MapPoint;
import com.example.machine.model.Point;
import com.example.machine.model.PathConnection;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@Service
@EnableScheduling
public class RobotService {
    private Robot robot;
    private List<MapPoint> mapPoints;
    private final Random random = new Random();
    private final SimpMessagingTemplate messagingTemplate;
    private Queue<Point> pathPoints = new LinkedList<>();
    private Point currentTarget = null;
    private static final double MOVEMENT_SPEED = 20.0;
    private static final double ARRIVAL_THRESHOLD = 5.0;
    private List<Point> currentPath = new ArrayList<>();

    public RobotService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.robot = new Robot();
        this.mapPoints = initializeMapPoints();
        
        // 设置机器人初始位置（在点位1）
        this.robot.setX(100);
        this.robot.setY(50);
        this.robot.setAngle(0);
        
        // 初始化默认路径
        initializeDefaultPath();
    }

    private void initializeDefaultPath() {
        // 创建默认路径点（完整的1->2->4->5路径）
        List<Point> defaultPath = new ArrayList<>();
        
        // 从点位1开始
        defaultPath.add(new Point(100, 50));  // 点位1
        defaultPath.add(new Point(100, 75));  // 垂直向下的转折点
        defaultPath.add(new Point(100, 100)); // 点位2
        defaultPath.add(new Point(200, 100)); // 水平向右的转折点
        defaultPath.add(new Point(300, 100)); // 点位4
        defaultPath.add(new Point(350, 100)); // 水平向右的转折点
        defaultPath.add(new Point(400, 100)); // 点位5
        
        // 设置当前路径
        this.currentPath = defaultPath;
        
        // 发送初始路径
        messagingTemplate.convertAndSend("/topic/path", currentPath);
    }

    public Robot getRobotStatus() {
        return robot;
    }

    public void moveToPoint(MapPoint target) {
        // 清空当前路径点队列，但保留currentPath用于显示
        pathPoints.clear();
        
        // 获取当前位置最近的点位
        Point currentPos = new Point(robot.getX(), robot.getY());
        MapPoint startPoint = findNearestPoint(currentPos);
        
        if (startPoint != null) {
            List<Point> path = new ArrayList<>();
            
            // 如果当前位置不在起点，先添加到当前位置的路径
            if (distance(currentPos, new Point(startPoint.getX(), startPoint.getY())) > ARRIVAL_THRESHOLD) {
                path.add(currentPos);
                path.add(new Point(startPoint.getX(), startPoint.getY()));
            }
            
            // 根据目标点找到完整路径
            List<String> waypoints = new ArrayList<>();
            
            // 根据目标点确定需要经过的路径点
            if ("5".equals(target.getId())) {
                waypoints.addAll(Arrays.asList("2", "4", "5"));
            } else if ("4".equals(target.getId())) {
                waypoints.addAll(Arrays.asList("2", "4"));
            } else if ("2".equals(target.getId())) {
                waypoints.add("2");
            }
            
            if (!waypoints.isEmpty()) {
                // 使用路径点规划路径
                List<Point> plannedPath = findPathThroughWaypoints(startPoint, waypoints);
                path.addAll(plannedPath);
            } else {
                // 如果没有预设路径点，直接寻找到目标的路径
                List<Point> directPath = findPathToTarget(startPoint.getId(), target.getId());
                path.addAll(directPath);
            }
            
            if (!path.isEmpty()) {
                // 更新当前路径显示
                currentPath.clear();
                currentPath.addAll(path);
                
                // 更新路径点队列
                pathPoints.addAll(path);
                
                // 设置第一个目标点
                currentTarget = pathPoints.poll();
                if (currentTarget != null) {
                    robot.setCurrentTask("Moving to " + target.getName());
                    robot.setStatus("MOVING");
                    moveRobotTowards(currentTarget.getX(), currentTarget.getY());
                }
            }
            
            // 发送路径更新
            messagingTemplate.convertAndSend("/topic/path", currentPath);
        }
    }

    private List<Point> findPathToTarget(String startId, String targetId) {
        List<Point> path = new ArrayList<>();
        MapPoint current = findPointById(startId);
        
        while (current != null && !targetId.equals(current.getId())) {
            // 找到通向目标的连接
            Optional<PathConnection> connection = current.getConnections().stream()
                .filter(c -> {
                    MapPoint next = findPointById(c.getTargetId());
                    return next != null && (
                        c.getTargetId().equals(targetId) || // 直接连接到目标
                        "4".equals(c.getTargetId()) // 或者是通过点位4的路径
                    );
                })
                .findFirst();
            
            if (connection.isPresent()) {
                // 添加当前点位
                path.add(new Point(current.getX(), current.getY()));
                // 添加路径中的所有转折点
                path.addAll(connection.get().getWaypoints());
                // 移动到下一个点位
                current = findPointById(connection.get().getTargetId());
            } else {
                break;
            }
        }
        
        // 添加最后一个点位
        if (current != null && targetId.equals(current.getId())) {
            path.add(new Point(current.getX(), current.getY()));
        }
        
        return path;
    }

    private List<Point> findPathThroughWaypoints(MapPoint start, List<String> waypointIds) {
        List<Point> completePath = new ArrayList<>();
        MapPoint current = start;
        
        for (String waypointId : waypointIds) {
            List<Point> pathSegment = findPathToTarget(current.getId(), waypointId);
            if (!pathSegment.isEmpty()) {
                // 如果不是第一段路径，移除重复的起点
                if (!completePath.isEmpty()) {
                    pathSegment.remove(0);
                }
                completePath.addAll(pathSegment);
                current = findPointById(waypointId);
                if (current == null) break;
            }
        }
        
        return completePath;
    }

    private MapPoint findNearestPoint(Point position) {
        return mapPoints.stream()
            .min((p1, p2) -> {
                double d1 = distance(position, new Point(p1.getX(), p1.getY()));
                double d2 = distance(position, new Point(p2.getX(), p2.getY()));
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    private double distance(Point p1, Point p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public List<MapPoint> getMapPoints() {
        return mapPoints;
    }

    public Robot updateRobotPosition(Robot updatedRobot) {
        this.robot.setX(updatedRobot.getX());
        this.robot.setY(updatedRobot.getY());
        this.robot.setAngle(updatedRobot.getAngle());
        return this.robot;
    }

    @Scheduled(fixedRate = 50)
    public void updateRobotMovement() {
        if (currentTarget != null && robot.getStatus().equals("MOVING")) {
            double dx = currentTarget.getX() - robot.getX();
            double dy = currentTarget.getY() - robot.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < ARRIVAL_THRESHOLD) {
                robot.setX(currentTarget.getX());
                robot.setY(currentTarget.getY());
                
                currentTarget = pathPoints.poll();
                if (currentTarget != null) {
                    robot.setCurrentTask("Moving to next waypoint");
                    moveRobotTowards(currentTarget.getX(), currentTarget.getY());
                } else {
                    robot.setStatus("IDLE");
                    robot.setCurrentTask("Arrived at destination");
                    // 不清除路径显示
                    // currentPath.clear();
                    // messagingTemplate.convertAndSend("/topic/path", currentPath);
                }
            } else {
                double moveDistance = MOVEMENT_SPEED * 0.05;
                if (moveDistance > distance) {
                    moveDistance = distance;
                }
                
                double dirX = dx / distance;
                double dirY = dy / distance;
                
                robot.setX(robot.getX() + dirX * moveDistance);
                robot.setY(robot.getY() + dirY * moveDistance);
                
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                robot.setAngle(angle);
            }
            
            messagingTemplate.convertAndSend("/topic/position", robot);
        }
    }

    private void moveRobotTowards(double targetX, double targetY) {
        double dx = targetX - robot.getX();
        double dy = targetY - robot.getY();
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        robot.setAngle(angle);
        
        messagingTemplate.convertAndSend("/topic/position", robot);
    }

    private MapPoint findPointById(String id) {
        return mapPoints.stream()
                .filter(point -> id.equals(point.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<MapPoint> initializeMapPoints() {
        List<MapPoint> points = new ArrayList<>();
        
        // 创建点位（根据图片布局）
        MapPoint point1 = new MapPoint("1", "点位1", 100, 50, "WORK");  // 左上角起点
        MapPoint point2 = new MapPoint("2", "点位2", 100, 100, "WORK"); // 左下角点位
        MapPoint point4 = new MapPoint("4", "点位4", 300, 100, "WORK"); // 中间点位
        MapPoint point5 = new MapPoint("5", "点位5", 400, 100, "WORK"); // 右侧点位
        
        // 添加路径连接
        // 点位1到点位2的连接
        PathConnection p1ToP2 = new PathConnection("2");
        p1ToP2.addWaypoint(new Point(100, 75)); // 垂直向下
        point1.addConnection(p1ToP2);
        
        // 点位2到点位4的连接
        PathConnection p2ToP4 = new PathConnection("4");
        p2ToP4.addWaypoint(new Point(200, 100)); // 水平向右
        point2.addConnection(p2ToP4);
        
        // 点位4到点位5的连接
        PathConnection p4ToP5 = new PathConnection("5");
        p4ToP5.addWaypoint(new Point(350, 100)); // 水平向右
        point4.addConnection(p4ToP5);
        
        // 添加点位到列表
        points.add(point1);
        points.add(point2);
        points.add(point4);
        points.add(point5);
        
        return points;
    }
} 