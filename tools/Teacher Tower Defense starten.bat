@echo off
rem Startet Teacher Tower Defense mit einem passenden Java (mindestens Java 21).
rem Ein Doppelklick auf die .jar wuerde oft ein altes Java 8 verwenden und scheitern.
setlocal
cd /d "%~dp0"

set "JAR=TeacherTowerDefense.jar"
if not exist "%JAR%" if exist "target\TeacherTowerDefense.jar" set "JAR=target\TeacherTowerDefense.jar"
if not exist "%JAR%" (
    echo TeacherTowerDefense.jar wurde nicht gefunden.
    pause
    exit /b 1
)

rem Neues Java suchen (der letzte Treffer gewinnt)
set "JAVAW="
for /d %%D in ("%USERPROFILE%\.jdks\*") do if exist "%%D\bin\javaw.exe" set "JAVAW=%%D\bin\javaw.exe"
for /d %%D in ("%ProgramFiles%\Java\jdk-2*") do if exist "%%D\bin\javaw.exe" set "JAVAW=%%D\bin\javaw.exe"
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jre-2*") do if exist "%%D\bin\javaw.exe" set "JAVAW=%%D\bin\javaw.exe"
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-2*") do if exist "%%D\bin\javaw.exe" set "JAVAW=%%D\bin\javaw.exe"

if not defined JAVAW (
    echo Kein Java 21 oder neuer gefunden.
    echo Bitte Java 21 installieren, z.B. von https://adoptium.net
    pause
    exit /b 1
)

start "" "%JAVAW%" -jar "%JAR%"
