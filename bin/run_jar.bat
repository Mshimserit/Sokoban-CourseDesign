@echo off
chcp 65001 >nul
echo ========================================
echo 运行推箱子游戏 (JAR版)
echo ========================================

cd /d %~dp0..

if not exist Sokoban.jar (
    echo 未找到 Sokoban.jar，请先运行 build_jar.bat 打包
    pause
    exit /b 1
)

echo 启动游戏...
start javaw -jar Sokoban.jar
