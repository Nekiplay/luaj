import java.io.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.compiler.*;
import org.luaj.vm2.lib.*;

public class TestLua53Bytecode {
    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();

        // Test 1: Compile Lua source to bytecode, dump, and reload
        System.out.println("=== Test 1: Compile -> Dump -> Load -> Execute ===");
        String script = "return (1 << 4) + (15 & 7) + (255 >> 2)";
        Prototype p = globals.compilePrototype(new StringReader(script), "test.lua");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DumpState.dump(p, baos, false);
        byte[] bytes = baos.toByteArray();

        // Verify header
        System.out.println("Bytecode size: " + bytes.length + " bytes");
        System.out.println("Signature: " + String.format("%02X %02X %02X %02X", bytes[0], bytes[1], bytes[2], bytes[3]));
        System.out.println("Version: 0x" + String.format("%02X", bytes[4]));

        // Reload
        Prototype loaded = LoadState.instance.undump(new ByteArrayInputStream(bytes), "test.lua");
        LuaClosure closure = new LuaClosure(loaded, globals);
        LuaValue result = closure.call();
        System.out.println("Result: " + result.tojstring());
        System.out.println("Expected: 95 (16 + 7 + 64)");
        assert result.toint() == 95 : "Test 1 failed!";
        System.out.println("PASS\n");

        // Test 2: Bitwise NOT
        System.out.println("=== Test 2: Bitwise NOT ===");
        String script2 = "return ~0";
        Prototype p2 = globals.compilePrototype(new StringReader(script2), "test2.lua");
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DumpState.dump(p2, baos2, false);
        Prototype loaded2 = LoadState.instance.undump(new ByteArrayInputStream(baos2.toByteArray()), "test2.lua");
        LuaValue result2 = new LuaClosure(loaded2, globals).call();
        System.out.println("~0 = " + result2.tojstring());
        assert result2.toint() == -1 : "Test 2 failed!";
        System.out.println("PASS\n");

        // Test 3: Bitwise OR and XOR
        System.out.println("=== Test 3: Bitwise OR and XOR ===");
        String script3 = "return (5 | 3) + (5 ~ 3)";
        Prototype p3 = globals.compilePrototype(new StringReader(script3), "test3.lua");
        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        DumpState.dump(p3, baos3, false);
        Prototype loaded3 = LoadState.instance.undump(new ByteArrayInputStream(baos3.toByteArray()), "test3.lua");
        LuaValue result3 = new LuaClosure(loaded3, globals).call();
        System.out.println("(5 | 3) + (5 ~ 3) = " + result3.tojstring());
        assert result3.toint() == 13 : "Test 3 failed! (expected 7 + 6 = 13)";
        System.out.println("PASS\n");

        // Test 4: Integer division (//)
        System.out.println("=== Test 4: Integer division ===");
        String script4 = "return 7 // 2";
        Prototype p4 = globals.compilePrototype(new StringReader(script4), "test4.lua");
        ByteArrayOutputStream baos4 = new ByteArrayOutputStream();
        DumpState.dump(p4, baos4, false);
        Prototype loaded4 = LoadState.instance.undump(new ByteArrayInputStream(baos4.toByteArray()), "test4.lua");
        LuaValue result4 = new LuaClosure(loaded4, globals).call();
        System.out.println("7 // 2 = " + result4.tojstring());
        assert result4.toint() == 3 : "Test 4 failed!";
        System.out.println("PASS\n");

        // Test 5: Large shift (should produce long)
        System.out.println("=== Test 5: Large shift ===");
        String script5 = "return 1 << 40";
        Prototype p5 = globals.compilePrototype(new StringReader(script5), "test5.lua");
        ByteArrayOutputStream baos5 = new ByteArrayOutputStream();
        DumpState.dump(p5, baos5, false);
        Prototype loaded5 = LoadState.instance.undump(new ByteArrayInputStream(baos5.toByteArray()), "test5.lua");
        LuaValue result5 = new LuaClosure(loaded5, globals).call();
        System.out.println("1 << 40 = " + result5.tojstring() + " (islong=" + result5.islong() + ")");
        assert result5.islong() : "Test 5 failed - should be long!";
        assert result5.tolong() == 1099511627776L : "Test 5 failed!";
        System.out.println("PASS\n");

        // Test 6: Function with bitwise ops
        System.out.println("=== Test 6: Function with bitwise ops ===");
        String script6 = "function mask(n) return n & 0xFF end return mask(0x1234)";
        Prototype p6 = globals.compilePrototype(new StringReader(script6), "test6.lua");
        ByteArrayOutputStream baos6 = new ByteArrayOutputStream();
        DumpState.dump(p6, baos6, false);
        Prototype loaded6 = LoadState.instance.undump(new ByteArrayInputStream(baos6.toByteArray()), "test6.lua");
        LuaValue result6 = new LuaClosure(loaded6, globals).call();
        System.out.println("mask(0x1234) = " + result6.tojstring());
        assert result6.toint() == 0x34 : "Test 6 failed!";
        System.out.println("PASS\n");

        // Test 7: Verify bytecode version is 0x53
        System.out.println("=== Test 7: Verify bytecode version ===");
        String script7 = "return 1";
        Prototype p7 = globals.compilePrototype(new StringReader(script7), "test7.lua");
        ByteArrayOutputStream baos7 = new ByteArrayOutputStream();
        DumpState.dump(p7, baos7, false);
        byte[] b7 = baos7.toByteArray();
        int version = b7[4] & 0xFF;
        System.out.println("Bytecode version: 0x" + String.format("%02X", version));
        assert version == 0x53 : "Test 7 failed! Expected 0x53, got 0x" + String.format("%02X", version);
        System.out.println("PASS\n");

        System.out.println("=== ALL TESTS PASSED ===");
    }
}
