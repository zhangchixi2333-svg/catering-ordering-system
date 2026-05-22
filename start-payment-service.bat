@echo off
chcp 65001 >nul
cd /d "%~dp0"
mvn clean compile spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
