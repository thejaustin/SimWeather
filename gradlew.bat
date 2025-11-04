@REM
@REM Copyright 2017 the original author or authors.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@if "%DEBUG%" == "" (set DEBUG=0)

@if "%DEBUG%" == "1" (echo on) else (echo off)

@REM Determine the Java command to use to start the JVM.
@if ""%JAVA_HOME%"" == """" (goto findJavaOnPath)
@if not exist "%JAVA_HOME%\bin\java.exe" (goto findJavaOnPath)
@set JAVACMD=""%JAVA_HOME%\bin\java.exe""
goto javaFound

:findJavaOnPath
@where java.exe >NUL 2>NUL
@if %ERRORLEVEL% == 0 (set JAVACMD=""java.exe"") else (goto noJavaFound)
goto javaFound

:noJavaFound
@echo ERROR: JAVA_HOME is not set and no 'java.exe' command can be found in your PATH.
@echo Please set the JAVA_HOME variable in your environment to match the
@echo location of your Java installation.
@goto fail

:javaFound
@REM Set Gradle properties
@set DEFAULT_JVM_OPTS=""-Xmx64m"" ""-Xms64m""
@set GRADLE_OPTS=""%GRADLE_OPTS% -Dorg.gradle.appname=gradle""

@REM Execute Gradle
@""%JAVACMD%"" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

:success
@exit /b 0

:fail
@exit /b 1
