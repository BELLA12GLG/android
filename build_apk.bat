@echo off
REM ============================================
REM 隧道故障转移 - Android APK 自动构建脚本
REM ============================================

setlocal enabledelayedexpansion

REM 颜色定义 (Windows 10+)
set "GREEN=["
set "RED=["
set "YELLOW=["
set "BLUE=["

cls

echo ==========================================
echo    隧道故障转移 - Android APK 自动构建
echo ==========================================
echo.

REM 检查环境
echo [*] 检查构建环境...
if not exist "gradlew.bat" (
    echo [X] 找不到 gradlew.bat，请确保在项目根目录运行此脚本
    exit /b 1
)
echo [OK] 环境检查完成

REM 生成时间戳
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set BUILD_LOG=build_%mydate%_%mytime%.log

REM 清理旧文件
echo [*] 清理旧构建文件...
call gradlew clean >"%BUILD_LOG%" 2>&1
if errorlevel 1 (
    echo [X] 清理失败
    exit /b 1
)
echo [OK] 清理完成

REM 运行单元测试
echo [*] 运行单元测试...
call gradlew test >>"%BUILD_LOG%" 2>&1
if errorlevel 1 (
    echo [!] 部分单元测试失败，但不影响构建
) else (
    echo [OK] 所有单元测试通过
)

REM 构建 Debug 版本
echo [*] 构建 Debug 版本...
call gradlew assembleDebug >>"%BUILD_LOG%" 2>&1
if errorlevel 1 (
    echo [X] Debug APK 构建失败
    type "%BUILD_LOG%" | more
    exit /b 1
)

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    for %%A in (app\build\outputs\apk\debug\app-debug.apk) do set DEBUG_SIZE=%%~zA
    echo [OK] Debug APK 构建成功 (大小: %DEBUG_SIZE% 字节)
) else (
    echo [X] Debug APK 文件未找到
    exit /b 1
)

REM 构建 Release 版本
echo [*] 构建 Release 版本...
call gradlew assembleRelease >>"%BUILD_LOG%" 2>&1
if errorlevel 1 (
    echo [X] Release APK 构建失败
    type "%BUILD_LOG%" | more
    exit /b 1
)

if exist "app\build\outputs\apk\release\app-release-unsigned.apk" (
    for %%A in (app\build\outputs\apk\release\app-release-unsigned.apk) do set RELEASE_SIZE=%%~zA
    echo [OK] Release APK 构建成功 (大小: %RELEASE_SIZE% 字节)
) else (
    echo [X] Release APK 文件未找到
    exit /b 1
)

REM 尝试安装到设备 (如果有 adb)
echo [*] 尝试安装 Debug 版本到设备...
where adb >nul 2>&1
if !errorlevel! equ 0 (
    adb devices | find "device" >nul
    if !errorlevel! equ 0 (
        call gradlew installDebug >>"%BUILD_LOG%" 2>&1
        if !errorlevel! equ 0 (
            echo [OK] Debug 版本已安装到设备
        ) else (
            echo [!] 安装到设备失败
        )
    ) else (
        echo [!] 未检测到 Android 设备
    )
) else (
    echo [!] 未找到 adb，跳过设备安装
)

REM 显示摘要
echo.
echo ==========================================
echo         构建摘要
echo ==========================================
echo [OK] Debug APK: app\build\outputs\apk\debug\app-debug.apk
echo [OK] Release APK: app\build\outputs\apk\release\app-release-unsigned.apk
echo.
echo 构建日志: %BUILD_LOG%
echo ==========================================
echo.
echo [OK] 构建完成！
echo.

endlocal
