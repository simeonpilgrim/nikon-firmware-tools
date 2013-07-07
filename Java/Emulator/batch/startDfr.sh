#!/bin/bash 
echo "Starting Dfr"
java -Xmx512m -cp "./NikonEmulator.jar":"./lib/commons-io-2.1.jar":"./lib/commons-lang3-3.1.jar":"./lib/xstream-1.4.2.jar":"./lib/jacksum.jar" com.nikonhacker.disassembly.fr.Dfr "$@"

