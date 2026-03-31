import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class test_bitwise {
    public static void main(String[] args) {
        Globals globals = JsePlatform.standardGlobals();
        
        // Test shift left (<<)
        LuaValue result1 = globals.load("return 1 << 2").call();
        System.out.println("1 << 2 = " + result1.tojstring());
        
        // Test shift right (>>)
        LuaValue result2 = globals.load("return 8 >> 1").call();
        System.out.println("8 >> 1 = " + result2.tojstring());
        
        // Test bitwise AND (&)
        LuaValue result3 = globals.load("return 5 & 3").call();
        System.out.println("5 & 3 = " + result3.tojstring());
        
        // Test bitwise OR (|)
        LuaValue result4 = globals.load("return 5 | 3").call();
        System.out.println("5 | 3 = " + result4.tojstring());
        
        // Test bitwise XOR (~)
        LuaValue result5 = globals.load("return 5 ~ 3").call();
        System.out.println("5 ~ 3 = " + result5.tojstring());
        
        // Test bitwise NOT (~)
        LuaValue result6 = globals.load("return ~5").call();
        System.out.println("~5 = " + result6.tojstring());
        
        // Test more complex expressions
        LuaValue result7 = globals.load("return (1 << 4) + (15 & 7)").call();
        System.out.println("(1 << 4) + (15 & 7) = " + result7.tojstring());
        
        // Test large numbers that exceed int range
        LuaValue result8 = globals.load("return 4294967296").call();
        System.out.println("4294967296 = " + result8.tojstring() + " islong=" + result8.islong());
        
        LuaValue result9 = globals.load("return 4294967296 + 1").call();
        System.out.println("4294967296 + 1 = " + result9.tojstring());
        
        LuaValue result10 = globals.load("return 1 << 40").call();
        System.out.println("1 << 40 = " + result10.tojstring() + " islong=" + result10.islong());
        
        LuaValue result11 = globals.load("return 0xFFFFFFFF & 0xFFFF").call();
        System.out.println("0xFFFFFFFF & 0xFFFF = " + result11.tojstring());
    }
}