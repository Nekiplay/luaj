import java.io.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.compiler.*;

public class TestRoundTrip {
    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();

        // Compile Lua source, dump to bytecode, reload, and execute
        String[] scripts = {
            "return 1 + 2",
            "return 'hello'",
            "return 10 / 3",
            "return 10 % 3",
            "return 2 ^ 10",
            "return 1 << 2",
            "return 8 >> 1",
            "return 5 & 3",
            "return 5 | 3",
            "return 5 ~ 3",
            "return ~0",
            "return 7 // 2",
            "function f(x) return x * 2 end return f(21)",
            "local a = 1; local b = 2; return a + b",
            "return not true and false or true",
        };

        int passed = 0;
        for (String script : scripts) {
            try {
                Prototype p = globals.compilePrototype(new StringReader(script), "test.lua");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DumpState.dump(p, baos, false);
                
                Prototype loaded = LoadState.instance.undump(new ByteArrayInputStream(baos.toByteArray()), "test.lua");
                LuaClosure closure = new LuaClosure(loaded, globals);
                LuaValue result = closure.call();
                
                // Also run the original script directly to compare
                LuaValue expected = globals.load(script).call();
                
                boolean match;
                if (result.islong() && expected.islong()) {
                    match = result.tolong() == expected.tolong();
                } else if (result.isnumber() && expected.isnumber()) {
                    match = result.todouble() == expected.todouble();
                } else {
                    match = result.tojstring().equals(expected.tojstring());
                }
                
                if (match) {
                    System.out.println("PASS: " + script + " => " + result.tojstring());
                    passed++;
                } else {
                    System.out.println("FAIL: " + script + " => got " + result.tojstring() + ", expected " + expected.tojstring());
                }
            } catch (Exception e) {
                System.out.println("FAIL: " + script + " => " + e.getMessage());
            }
        }
        
        System.out.println("\n" + passed + "/" + scripts.length + " tests passed");
    }
}
