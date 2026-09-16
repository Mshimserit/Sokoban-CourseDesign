@echo off
chcp 65001 >nul
echo ========================================
echo 编译推箱子游戏
echo ========================================

set SRC_DIR=..\src
set CLASSES_DIR=..\classes

if not exist %CLASSES_DIR% mkdir %CLASSES_DIR%

echo 正在编译Java源代码...
javac -encoding UTF-8 -d %CLASSES_DIR% %SRC_DIR%\logic\*.java %SRC_DIR%\ui\*.java

if %errorlevel% equ 0 (
    echo 编译成功！
    echo class文件已输出到: %CLASSES_DIR%
) else (
    echo 编译失败！
    pause
    exit /b 1
)

pause
