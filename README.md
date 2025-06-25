# Robot Control System

一个基于Spring Boot和Vue.js的机器人控制系统，实现了路径规划和实时位置更新功能。

## 功能特点

- 实时机器人位置显示
- 路径规划和可视化
- WebSocket实时通信
- 多点位导航
- 平滑的动画效果

## 技术栈

### 后端
- Spring Boot 2.7.5
- WebSocket
- Java 11

### 前端
- Vue 3
- TypeScript
- Element Plus
- Canvas绘图

## 快速开始

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 系统预览

系统启动后，访问 http://localhost:5174 即可看到机器人控制界面。

### 主要功能
1. 点位导航：系统支持多个预设点位（1、2、4、5）
2. 路径规划：自动规划最优路径
3. 实时更新：通过WebSocket实时显示机器人位置
4. 状态显示：实时显示机器人的位置、角度和任务状态

## 开发说明

### 后端开发
- 主要业务逻辑在 `RobotService.java`
- WebSocket配置在 `WebSocketConfig.java`
- 模型定义在 `model` 包下

### 前端开发
- 主要界面在 `RobotMap.vue`
- 状态管理使用 `robot.ts` store
- 类型定义在 `types` 目录下

## 注意事项

1. 确保后端8080端口未被占用
2. 前端开发时注意CORS配置
3. WebSocket连接默认使用8080端口 