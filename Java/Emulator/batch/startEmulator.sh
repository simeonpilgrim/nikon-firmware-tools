#!/bin/bash 
echo "Starting emulator UI"
java -Xmx1024m -cp "NikonEmulator.jar":"lib/commons-io-2.1.jar":"lib/commons-lang3-3.1.jar":"lib/xstream-1.4.2.jar":"lib/jacksum.jar":"lib/jgraphx.jar":"lib/rsyntaxtextarea.jar":"lib/miglayout-4.0.jar":"lib/glazedlists-1.8.0_java15.jar" com.nikonhacker.gui.EmulatorUI %*

