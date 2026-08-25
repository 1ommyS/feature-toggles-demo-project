@echo off
setlocal enabledelayedexpansion
if "%GRADLE_VERSION%"=="" set GRADLE_VERSION=8.14.3
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set BOOTSTRAP_DIR=%GRADLE_USER_HOME%\demo-bootstrap
set DIST_DIR=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%
set ZIP=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip
set URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%DIST_DIR%\bin\gradle.bat" (
  if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$u='%URL%'; $z='%ZIP%'; Invoke-WebRequest -UseBasicParsing $u -OutFile $z; $expected=(Invoke-WebRequest -UseBasicParsing ($u+'.sha256')).Content.Trim(); $actual=(Get-FileHash -Algorithm SHA256 $z).Hash.ToLower(); if ($actual -ne $expected.ToLower()) { throw 'Gradle distribution checksum mismatch' }; Expand-Archive -Force $z '%BOOTSTRAP_DIR%'"
)

call "%DIST_DIR%\bin\gradle.bat" %*
