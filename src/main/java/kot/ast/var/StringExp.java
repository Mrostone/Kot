package kot.ast.var;

import kot.assembly.Assembly;
import kot.ast.Exp;
import kot.ast.type.StringType;
import kot.utils.Position;
import kot.assembly.*;

public class StringExp extends Exp{
    public String value;

    public StringExp(String value, Position pos)
    {
        this.value = value;
        this.pos = pos;
    }

    @Override
    public void typeCheck() {
        this.type = StringType.getInstance();
    }

    @Override
    public Assembly assemble(Assembly asm) {
        String name = "string_" + asm.nbString;
        this.nameAssembly = name;
        asm.nbString++;
        String var = String.format("%s: .ascii \"%s\"\n\t.equ len, . - %s\n", name, value, name);
        asm.stringMap.put(this, var);
        return asm;
    }
}
