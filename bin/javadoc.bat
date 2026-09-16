@echo off
chcp 65001 >nul
echo ========================================
echo 生成Java文档
echo ========================================

set SRC_DIR=..\src
set DOC_DIR=..\doc\api

if not exist %DOC_DIR% mkdir %DOC_DIR%

echo 正在生成Java文档...
javadoc -encoding UTF-8 -docencoding UTF-8 -d %DOC_DIR% -author -version -windowtitle "推箱子游戏 API文档" %SRC_DIR%\logic\*.java %SRC_DIR%\ui\*.java

if %errorlevel% equ 0 (
    echo 文档生成成功！
    echo 文档目录: %DOC_DIR%
    echo 请打开 %DOC_DIR%\index.html 查看文档
) else (
    echo 文档生成失败！
    pause
    exit /b 1
)

pause
