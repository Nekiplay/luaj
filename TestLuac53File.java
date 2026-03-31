import java.io.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.compiler.*;

public class TestLuac53File {
    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();

        // Load the bytecode file compiled by luac53.exe
        String filename = "bitwise_ops.out";
        FileInputStream fis = new FileInputStream(filename);
        
        // Read first bytes to inspect header
        byte[] header = new byte[20];
        fis.read(header);
        fis.close();
        
        System.out.println("=== Bytecode header inspection ===");
        System.out.println("Signature: " + String.format("%02X %02X %02X %02X", header[0], header[1], header[2], header[3]));
        System.out.println("Version: 0x" + String.format("%02X", header[4]));
        System.out.println("Format: 0x" + String.format("%02X", header[5]));
        System.out.println("Little-endian: " + (header[6] != 0));
        System.out.println("sizeof(int): " + header[7]);
        System.out.println("sizeof(size_t): " + header[8]);
        System.out.println("sizeof(Instruction): " + header[9]);
        System.out.println("sizeof(lua_Integer): " + header[10]);
        System.out.println("sizeof(lua_Number): " + header[11]);
        System.out.println();
        
        // Now load and execute
        System.out.println("=== Loading and executing bytecode from luac53.exe ===");
        InputStream is = new FileInputStream(filename);
        LuaValue chunk = globals.load(is, "@" + filename, "b", globals);
        LuaValue result = chunk.call();
        System.out.println("\n=== Return value: " + result.tojstring() + " ===");
    }
}
