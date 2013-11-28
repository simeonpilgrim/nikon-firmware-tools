@echo off
echo Emulator batch building script starting...
cd %~dp0

if NOT "%1"=="help" goto start
echo ################## HELP #######################
echo Batch building Emulator. Possible usage:
echo.
echo %~n0             Clean, compile all sources and build .jar
echo %~n0 templates   Clean, compile only template sources
echo %~n0 jar         Only create jar
echo %~n0 release     Create release archive
echo %~n0 env         Set environment (variables) and exit
echo %~n0 help        Show this help
echo.
echo If environment is set, it is possible to recompile any source file using %%CC%%
echo For example:
echo.
echo    %%CC%% src\main\com\nikonhacker\Format.java
echo.
echo.
echo NEED: javac/jar 1.7 be installed in PATH
goto :EOF

:start

if "%1"=="env" goto global
setlocal
:global

rem #################### Environment #############
echo Setting up environment...

set lib-dir=lib
set source-dir=src\main
set template-dir=src\template

set target-dir=batch_build
set gen-dir=%target-dir%\gen
set class-dir=%target-dir%\classes
set jar-dir=%target-dir%

set app-name=NikonEmulator
for /F "tokens=5 delims==<>/ " %%i in ('findstr "\"app-version\"" build.xml') do set app-version=%%~i

set CC=javac -deprecation -g -g:lines,vars,source -source 1.7 -target 1.7 -d %class-dir% -sourcepath %source-dir%;%gen-dir% -classpath "%lib-dir%/commons-io-2.1.jar;%lib-dir%/commons-lang3-3.1.jar;%lib-dir%/jacksum.jar;%lib-dir%/xstream-1.4.2.jar;%lib-dir%/jgraphx.jar;%lib-dir%/rsyntaxtextarea.jar;%lib-dir%/miglayout-4.0.jar;%lib-dir%/glazedlists-1.8.0_java15.jar"

if "%1"=="env" (
echo *** Set
goto :EOF
)

del %jar-dir%\%app-name%-%app-version%.zip 2> nul
if "%1"=="release" goto makerelease

del %jar-dir%\%app-name%.jar 2> nul
if "%1"=="jar" goto makejar

rem ### Clean
rmdir /S /Q %class-dir% 2> nul
if ERRORLEVEL 1 goto error
mkdir %class-dir%
if ERRORLEVEL 1 goto error

rmdir /S /Q %gen-dir% 2> nul
if ERRORLEVEL 1 goto error
mkdir %gen-dir%
if ERRORLEVEL 1 goto error

rem #################### Templates #############

echo Generating some source files from templates to %gen-dir%...
time /T > build.tmp & set /p _TIME=< build.tmp
date /T > build.tmp & set /p _DATE=< build.tmp
del build.tmp
set build-time=%_DATE%%_TIME%:00

setlocal enabledelayedexpansion

for /R %template-dir% %%i in (*.java) do (
  set _DIR=%%~dpi
  set _DIR=!_DIR:%~dp0%template-dir%=%gen-dir%!
  set _NAME=!_DIR!%%~nxi
  mkdir !_DIR!
  IF EXIST !_NAME! del !_NAME!
  for /F "usebackq tokens=* delims=" %%j in ("%%i") do (
    set _line=%%j
    set _line=!_line:@APPNAME@=%app-name%!
    set _line=!_line:@APPVERSION@=%app-version%!
    set _line=!_line:@BUILDTIME@=%build-time%!
    echo !_line! >> !_NAME!
  )
  %CC% "!_NAME!"
  if ERRORLEVEL 1 goto error
)
endlocal

if "%1"=="templates" (
echo *** Done.
goto :EOF
)
rem ##################### Sources ###############

echo Compiling source files to %class-dir%...

rem ### for optimization
setlocal enabledelayedexpansion

for /R %source-dir% %%i in (*.java) do (
rem ### for optimisation
  set _FILE=%%~dpni
  set _FILE=!_FILE:%~dp0%source-dir%=%class-dir%!.class
  if not exist !_FILE! (
    %CC% "%%i"
    if ERRORLEVEL 1 goto :error
  )
)
rem ### for optimisation
endlocal

rem ##################### Jar ###############

echo Building jar file %jar-dir%\%app-name%.jar...

:makejar
xcopy /S /Q /Y %source-dir:/=\%\*.png %class-dir%\
if ERRORLEVEL 1 goto error

xcopy /S /Q /Y %source-dir:/=\%\*.jpg %class-dir%\
if ERRORLEVEL 1 goto error

xcopy /S /Q /Y %source-dir:/=\%\*.properties %class-dir%\
if ERRORLEVEL 1 goto error

if not exist %jar-dir%/nul mkdir %jar-dir%
jar -cf %jar-dir%\%app-name%.jar -C %class-dir% .
if ERRORLEVEL 1 goto error

echo *** Done.
goto :EOF

rem ##################### Optional ###############
:makerelease
setlocal

jar -cfM %jar-dir%\%app-name%-%app-version%.zip %lib-dir%/*.jar
if ERRORLEVEL 1 goto error

jar -ufM %jar-dir%\%app-name%-%app-version%.zip -C %jar-dir% %app-name%.jar
if ERRORLEVEL 1 goto error

cd batch
jar -ufM ..\%jar-dir%\%app-name%-%app-version%.zip *.bat *.sh
if ERRORLEVEL 1 goto error
cd ..

jar -ufM %jar-dir%\%app-name%-%app-version%.zip *.txt
if ERRORLEVEL 1 goto error

jar -ufM %jar-dir%\%app-name%-%app-version%.zip -C conf .
if ERRORLEVEL 1 goto error

jar -ufM %jar-dir%\%app-name%-%app-version%.zip -C data .
if ERRORLEVEL 1 goto error

endlocal
echo *** Done.
goto :EOF

:error
echo !!! Build failed.
pause

