import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public class BenchTable {

    public static int makeTablesInJava(int count, int size) {
        int sum = 0;
        LuaValue[] vals = new LuaValue[size];
        for (int c = 0; c < count; c++) {
            for (int i = 0; i < size; i++)
                vals[i] = LuaValue.valueOf(i);
            LuaTable t = LuaValue.listOf(vals);
            sum += t.length();
        }
        return sum;
    }

    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();
        globals.set("BenchTable", CoerceJavaToLua.coerce(BenchTable.class));
        LuaValue chunk = globals.loadfile("bench/bench_table.lua");
        chunk.call();
    }
}
