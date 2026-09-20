@echo off
chcp 65001 >nul
echo ========================================
echo 编译推箱子游戏测试代码
echo ========================================

cd /d %~dp0..

set JAR=lib\junit-platform-console-standalone-1.10.0.jar
set CLASSES=classes
set TEST_CLASSES=test-classes
set SRC=src
set TEST_SRC=test

if not exist %JAR% (
    echo 错误: 未找到 JUnit jar 文件: %JAR%
    echo 请先下载 junit-platform-console-standalone-1.10.0.jar 到 lib 目录
    exit /b 1
)

if not exist %CLASSES% (
    echo 错误: 未找到编译好的主程序类文件
    echo 请先运行 compile.bat 编译主程序
    exit /b 1
)

if not exist %TEST_CLASSES% mkdir %TEST_CLASSES%

echo 编译测试代码...
javac -encoding UTF-8 -cp %CLASSES%;%JAR% -d %TEST_CLASSES% %TEST_SRC%\*.java

if %ERRORLEVEL% EQU 0 (
    echo 测试代码编译成功
) else (
    echo 测试代码编译失败
    exit /b 1
)
