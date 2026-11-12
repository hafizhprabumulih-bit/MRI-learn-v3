@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem # Java startup script for Windows
@rem ##########################################################################

set DIRNAME=%~dp5
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Determine the Java command to use to start the JVM.
if "%JAVA_HOME%" == "" goto gnuhaus
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute

:gnuhaus
set JAVA_EXE=java.exe
%JAVA_HOME_MSG% %JAVA_HOME%
"%JAVA_EXE%" -version >NUL 2>&1
if %ERRORLEVEL% == 0 goto execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please install Java or set the JAVA_HOME variable.
exit /b 1

:execute
@rem Setup the JVM arguments
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

set JAVA_OPTS=%JAVA_OPTS% %DEFAULT_JVM_OPTS%

"%JAVA_EXE%" %JAVA_OPTS% -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*