This software is a collection of tools aimed at better understand how camera firmware works.

IMPORTANT : modifying your firmware can result in potentially DESTROYING ("bricking") your camera.
By using these tools, you agree that it is your entire fault and that we cannot be held responsible for anything.
That is what is repeated in legal terms at the end of this document.

Basically, all cameras using a Fujitsu FR or Toshiba TX family CPU could benefit from these tools,
although some parts such as firmware decrypting are specific to certain Nikon cameras.
Multiple tools can be accessed from the command-line (assuming that your Java environment -
JAVA_HOME and PATH variables - is set up correctly).

They are pre-wrapped as batch files for convenience. You should be able to simply drop a file on a batch to
process it (take care, for Dfr, a "Dfr.txt" file is expected to be next to the binary file you drop unless you
specify options in the batch file. a sample dfr.txt file is provided for your convenience)

They are all of the form :
java -cp NikonEmulator.jar;<additional_libs> <command> <params>

Where <command> can be :
    com.nikonhacker.gui.EmulatorUI :    Starts the main emulator user interface. From there, many other tools can be
                                        accessed via the menus.
                                        <params> is an optional (decoded) firmware binary file to load on startup. If
                                        you don't have one, either start it with no param and decode one using the
                                        Tools menu or use the FirmwareDecoder command line tool (see below)
    com.nikonhacker.disassembly.fr.Dfr: Starts the Dfr tool, a Fujitsu FR disassembler ported from Kevin Schoedel's
                                        original Dfr.
                                        <params> are exactly the same as the original Dfr except for filemap and
                                        memorymap output options. Start it with no <params> for help
    com.nikonhacker.disassembly.fr.Dfr: Starts the Dtx tool, a Toshiba TMP19A disassembler.
                                        <params> are similar to Dfr above. Start it with no <params> for help
    com.nikonhacker.encoding.FirmwareDecoder :  Decodes a encrypted firmware to its different files. By default, the
                                                intermediary 'res' file staged is skipped so you immediately get
                                                files that can be loaded in other tools

Other command line tools such as firmware encoder and console-based emulator are available for debugging purpose.

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
