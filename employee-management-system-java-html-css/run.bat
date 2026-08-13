@echo off
if not exist out mkdir out
javac -d out src\com\example\employee\*.java
if errorlevel 1 pause & exit /b 1
java -cp out com.example.employee.Main
