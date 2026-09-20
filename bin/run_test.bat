@echo off
chcp 65001 >nul
echo ========================================
echo 运行推箱子游戏单元测试
echo ========================================

cd /d %~dp0..

set JAR=lib\junit-platform-console-standalone-1.10.0.jar
set CLASSES=classes
set TEST_CLASSES=test-classes

if not exist %JAR% (
    echo 错误: 未找到 JUnit jar 文件: %JAR%
    exit /b 1
)

if not exist %TEST_CLASSES% (
    echo 错误: 未找到编译好的测试类文件
    echo 请先运行 compile_test.bat 编译测试代码
    exit /b 1
)

echo.
echo 开始运行测试...
echo.

java -jar %JAR% --class-path %CLASSES%;%TEST_CLASSES% --scan-class-path %TEST_CLASSES% --details=verbose

echo.
echo 测试执行完毕
