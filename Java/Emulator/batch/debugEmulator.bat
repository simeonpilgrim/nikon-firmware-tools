@echo off

rem default path value
set DBGSRCPATH=
if NOT "%1"=="" goto set DBGSRCPATH=%1
if NOT "%DBGSRCPATH%"=="" goto srcpath_ok
echo You must specify emulator sources path
goto :end

:srcpath_ok
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
call jdb -Xmx1024m -sourcepath %DBGSRCPATH% -classpath "%~dp0/NikonEmulator.jar";"%~dp0/lib/commons-io-2.1.jar";"%~dp0/lib/commons-lang3-3.1.jar";"%~dp0/lib/xstream-1.4.2.jar";"%~dp0/lib/jacksum.jar";"%~dp0/lib/jgraphx.jar";"%~dp0/lib/rsyntaxtextarea.jar";"%~dp0/lib/miglayout-4.0.jar";"%~dp0/lib/glazedlists-1.8.0_java15.jar" com.nikonhacker.gui.EmulatorUI %*
echo Done.

:end
pause
