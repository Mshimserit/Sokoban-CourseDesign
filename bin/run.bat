@echo off
chcp 65001 >nul
echo ========================================
echo 运行推箱子游戏
echo ========================================

set CLASSES_DIR=..\classes

if not exist %CLASSES_DIR%\ui\MainFrame.class (
    echo 未找到编译后的class文件，请先运行compile.bat
    pause
    exit /b 1
)

echo 启动游戏...
java -cp %CLASSES_DIR% ui.MainFrame

if %errorlevel% neq 0 (
    echo 运行失败！
    pause
)
