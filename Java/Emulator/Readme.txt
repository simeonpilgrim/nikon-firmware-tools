This software is a collection of tools aimed at better understand how camera firmware works.

IMPORTANT : modifying your firmware can result in potentially DESTROYING ("bricking") your camera.
By using these tools, you agree that it is your entire fault and that we cannot be held responsible for anything.
That is what is repeated in legal terms at the end of this document.

Quick start:
1. Make sure Java 1.7 is installed (JAVA_HOME and PATH variables should be set up correctly)
2. Launch startEmulator.bat (Windows) or startEmulator.sh (Linux)
3. Go to http://nikonhacker.com/wiki/NikonEmulator and follow the instructions

Details:
Basically, all cameras using a Fujitsu FR or Toshiba TX family CPU could benefit from these tools,
although some parts such as firmware decrypting are specific to certain Nikon cameras.
Multiple tools can be accessed from the command-line. They are pre-wrapped as batch files for convenience.
You should be able to simply drop a file on a batch to process it
(take care, for Dfr/Dtx, a "Dfr.txt"/"Dtx.txt" file is expected to sit next to the binary file you drop unless you
specify options in the batch file. Sample ".txt" files for D5100 are provided for your convenience)

The batch files are :
    startEmulator : Starts the main emulator user interface. From there, many other tools can be
                    accessed via the menus.
                    <params> is an optional (decoded) firmware binary file to load on startup. If
                    you don't have one, either start with no param and decode one using the
                    Tools menu, or use first the FirmwareDecoder command line tool (see below)
    startDfr:   Starts the Dfr tool, a Fujitsu FR disassembler ported from Kevin Schoedel's original Dfr.
                Parameters are expected, and are the same as the ones of the original Dfr except for filemap and
                memorymap output options. Start it with no parameter for help
    startDtx:   Starts the Dtx tool, a Toshiba TMP19A disassembler.
                Parameters are mostly similar to Dfr above. Start it with no parameter for help
    startFirmwareDecoder :  Decodes an encrypted firmware into its different files.
                            By default, you immediately get fully decoded files that can be loaded in other tools

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
