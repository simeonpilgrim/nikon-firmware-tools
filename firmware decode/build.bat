@echo off
echo .NET batch building script starting...
cd %~dp0

if NOT "%1"=="help" goto start
echo ################## HELP #######################
echo Batch building Microsoft .NET projects without Visual Studio installed.
echo Possible usage:
echo.
echo %~n0             Clean, compile all sources and build .jar
echo %~n0 clean       Clean
echo %~n0 help        Show this help
echo.
echo NEED: Only Microsoft .NET framework >=v4 32-bit installed
goto :EOF

:start

setlocal enabledelayedexpansion

rem #################### Compiler #############
echo Searching for compiler...

csc.exe /? 2>nul 
if ERRORLEVEL 1 (
    for /D %%i in (%SystemRoot%\Microsoft.NET\Framework\v*) do (
        set _CSC=%%i
        set _VER=%%~nxi
        rem ### skip Framework 1.x because it doesn't have "partial"
        if NOT "!_VER:~0,3!"=="v1." (
        rem ### skip Framework 2.x because it doesn't have Xml.Linq
        if NOT "!_VER:~0,3!"=="v2." (
        rem ### skip Framework 3.x because it doesn't have Tuple
        if NOT "!_VER:~0,3!"=="v3." (
        if exist %%i\csc.exe goto cscfound
        )
        )
        )
    )
    echo CSC.EXE was not found. Either:
    echo - install Microsoft .NET Framework v4.0 or newer
    echo - if it is installed then add directory of CSC.EXE to the PATH environment variable
    goto :error
:cscfound
    set CSC=!_CSC!
)
set CSC=%CSC%\csc.exe
set target-dir=batch_bin

rem ##################### Clean #################

for /D %%i in (*) do (
echo Cleaning in "%%i"...

if exist "%%i"\%target-dir% (
    rmdir /S /Q "%%i"\%target-dir%
    if exist "%%i"\%target-dir%/nul goto :EOF
)
)

if "%1"=="clean" (
echo *** Done
goto :EOF
)

rem ##################### Sources ###############

for /D %%i in (*) do (
echo Compiling source files in "%%i"...

mkdir "%%i"\%target-dir%
if ERRORLEVEL 1 goto error

cd "%%i"
%CSC% /nologo /w:4 /o+ /t:exe /out:"%target-dir%\%%i.exe" /recurse:*.cs
if ERRORLEVEL 1 goto error
cd ..
)

endlocal
echo *** Done.
goto :EOF

:error
endlocal
echo !!! Build failed.
pause
