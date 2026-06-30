import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public class BenchJavaCall {

    public static int square(int i) {
        return i * i;
    }

    public static int runLoops(int count, int innerN) {
        int total = 0;
        for (int c = 0; c < count; c++) {
            for (int i = 0; i < innerN; i++) {
                total += i * i;
            }
        }
        return total;
    }

    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();

        globals.set("BenchJavaCall", CoerceJavaToLua.coerce(BenchJavaCall.class));

        LuaValue chunk = globals.loadfile("bench/bench.lua");
        chunk.call();
    }
}
