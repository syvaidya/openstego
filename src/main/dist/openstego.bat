@echo off

rem @app.name@ - Steganography utility to hide messages into cover files
rem Copyright 2007-@time.year@ (c) @author.name@ (mailto:@author.mail@)

if "%OS%"=="Windows_NT" setlocal

rem Set options to be passed to java command line
set JAVA_OPTS=-Xmx1024m

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java
if "%1"=="" set JAVA_EXE=javaw

java -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute
set ERROR_MESSAGE=ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." ^& Chr(13) ^& Chr(10) ^& Chr(13) ^& Chr(10) ^& "Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\%JAVA_EXE%.exe

if exist "%JAVA_EXE%" goto execute
set ERROR_MESSAGE=ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%" ^& Chr(13) ^& Chr(10) ^& Chr(13) ^& Chr(10) ^& "Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
goto fail

:execute
if "%1"=="" goto execWindowLess
"%JAVA_EXE%" %JAVA_OPTS% -jar "%~dp0\lib\openstego.jar" %*
goto finish

:execWindowLess
start "@app.name@" "%JAVA_EXE%" %JAVA_OPTS% -jar "%~dp0\lib\openstego.jar"

:finish
if "%OS%"=="Windows_NT" endlocal
exit /b 0

:fail
set TMPFILE="%TEMP%\@app.name@-%RANDOM%.vbs"
echo MsgBox "%ERROR_MESSAGE%", 0, "@app.name@ v@app.version@" > %TMPFILE%
"cscript.exe" //Nologo %TMPFILE%
del /f %TMPFILE%
exit /b 1
