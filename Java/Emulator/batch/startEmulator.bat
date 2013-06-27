@echo off

echo Checking Java version...

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    rem echo Output: %%g
    set JAVAVER=%%g
)
set JAVAVER=%JAVAVER:"=%

for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
    rem @echo Major: %%v
    rem @echo Minor: %%w
    rem @echo Build: %%x
    set MAJMIN=%%v.%%w
)

if %MAJMIN% geq 1.7 goto version_ok

echo Your version of Java is %MAJMIN% (%JAVAVER%). Version 1.7 or more is required to run the Emulator
goto :end

:version_ok
echo Java version %JAVAVER% is OK.
echo Starting emulator UI...
call java -Xmx1024m -cp "%~dp0/NikonEmulator.jar";"%~dp0/lib/commons-io-2.1.jar";"%~dp0/lib/commons-lang3-3.1.jar";"%~dp0/lib/xstream-1.4.2.jar";"%~dp0/lib/jacksum.jar";"%~dp0/lib/jgraphx.jar";"%~dp0/lib/rsyntaxtextarea.jar";"%~dp0/lib/miglayout-4.0.jar";"%~dp0/lib/glazedlists-1.8.0_java15.jar" com.nikonhacker.gui.EmulatorUI %*
echo Done.
IF NOT ERRORLEVEL 1 goto :EOF
:end
pause
