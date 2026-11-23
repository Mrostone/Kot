package kot.ast.function;

import kot.assembly.Assembly;
import kot.ast.*;
import kot.binder.Binder;
import kot.utils.ErrorHandler;
import kot.utils.Position;
import kot.utils.Regs;

import java.util.*;

public class CallExp extends Exp {
    public String name;
    public List<Exp> args;
    public FunctionDec functionDec;

    public CallExp(){
    }

    public CallExp(String name, List<Exp> args, FunctionDec functionDec, Position pos){
        this.name = name;
        this.args = args;
        this.functionDec = functionDec;
        this.pos = pos;
    }

    @Override
    public Binder bind(Binder bind) {
        try {
            functionDec = bind.getFunc(name);
        } catch (Exception e) {
            ErrorHandler.error(this, 3, e.getMessage());
        }

        for (Exp e : args) {
            bind = e.bind(bind);
        }

        functionDec.isUsed = true;

        return bind;
    }

    @Override
    public void typeCheck() {
        if (args.size() != functionDec.list.size()) {
            ErrorHandler.error(this, 4, "number argument invalid got : " +args.size() + "excpected : " + functionDec.list.size());
        }
        for (int i = 0; i < args.size(); i++) {
            args.get(i).typeCheck();
            if (args.get(i).getType() != functionDec.list.get(i).getType()) {
                ErrorHandler.error(this, 4, "argument " + i + "invalid type : " + args.get(i).getType() + " excpected : " + functionDec.list.get(i).getType());
            }
        }
        this.type = functionDec.getType();
    }

    @Override 
    public Assembly assemble(Assembly asm) {
        if (name.equals("print")) {
            String out = "";
            Exp e = args.getFirst();
            e.assemble(asm);
            out = out.concat(String.format("\tmov $%s, %%rdi\n", e.nameAssembly));
            out = out.concat("\tcall print_string\n");
            asm.lastExp = out;
            return asm;
        }
        else if (name.equals("exit")) {
            return asm;
        }
        else if (name.equals("strCmp")) {
            return asm;
        }
        else if (name.equals("toString")) {
            return asm;
        }
        else {
            int arg = 0;
            String out = "";
            for (Exp e : args)
            {
                e.assemble(asm);
                out = out.concat(String.format("\tmov %s, %s\n", asm.lastExp, Regs.getReg(arg)));
                arg++;
            }
            out = out.concat("\tcall " + name + "\n");
            asm.lastExp = out;
            return asm;
        }
    }
}