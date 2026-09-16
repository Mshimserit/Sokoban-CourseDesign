@echo off
chcp 65001 >nul
echo ========================================
echo 打包推箱子游戏为JAR文件
echo ========================================

set CLASSES_DIR=..\classes
set JAR_FILE=..\Sokoban.jar

if not exist %CLASSES_DIR%\ui\MainFrame.class (
    echo 未找到编译后的class文件，请先运行compile.bat
    pause
    exit /b 1
)

echo 创建MANIFEST.MF...
echo Main-Class: ui.MainFrame > MANIFEST.MF

echo 正在打包为JAR文件...
jar cfm %JAR_FILE% MANIFEST.MF -C %CLASSES_DIR% .

del MANIFEST.MF

if %errorlevel% equ 0 (
    echo 打包成功！
    echo JAR文件: %JAR_FILE%
    echo 运行命令: java -jar Sokoban.jar
) else (
    echo 打包失败！
    pause
    exit /b 1
)

pause
