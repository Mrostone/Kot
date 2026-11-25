package kot.ast;

import kot.assembly.Assembly;
import kot.ast.type.IntType;
import kot.ast.type.VoidType;
import kot.ast.var.IntExp;
import kot.binder.Binder;
import kot.utils.Position;
import java.util.ArrayList;
import kot.utils.ErrorHandler;

public class IfExp extends Exp {
    Exp op;
    SeqExp body;
    ArrayList<IfExp> elifList;
    SeqExp elseClause;

    public IfExp(Exp op, SeqExp body, ArrayList<IfExp> elifList, SeqExp elseClause, Position pos) {
        this.op = op;
        this.body = body;
        this.elifList = elifList;
        this.elseClause = elseClause;
        this.pos = pos;
    }

    @Override
    public Binder bind(Binder bind) {
        if (op != null) {
            bind = op.bind(bind);
        }
        if (body != null) {
            bind = body.bind(bind);
        }
        for (IfExp el : elifList)
        {
            bind = el.bind(bind);
        }
        if(elseClause != null) {
            bind = elseClause.bind(bind);
        }
        return bind;
    }

    @Override
    public void typeCheck() {
        if (op != null) {
            op.typeCheck();
        }
        if(op.type != IntType.getInstance())
        {
            ErrorHandler.error(this, 4, "Op type is : " + op.type.getClass() + " should be : " + IntType.getInstance().getClass());
        }
        if (body != null) {
            body.typeCheck();
            this.type = body.getType();
        }
        else {
            this.type = VoidType.getInstance();
        }

        for (IfExp el : elifList)
        {
            el.typeCheck();
            if (this.type != el.getType())
            {
                ErrorHandler.error(this, 4, "Error if type : " + this.type.getClass() + " and elif type : " + el.getType().getClass());
            }
        }
        if(elseClause != null) {
            elseClause.typeCheck();
            if (this.type != elseClause.getType())
            {
                ErrorHandler.error(this, 4, "Error if type : " + this.type.getClass() + " and else type : " + elseClause.getType().getClass());
            }
        }
    }

    @Override
    public void clean() {
        if (op != null) {
            op.clean();
        }
        if (body != null) {
            body.clean();
        }
        for (IfExp el : elifList)
        {
            el.clean();
        }
        if(elseClause != null) {
            elseClause.clean();
        }

        int len = elifList.size();
        for (int i = 0; i < len; i++)
        {
            IfExp el = elifList.get(i);
            if (i == len - 1)
            {
                el.elseClause = this.elseClause;
                this.elseClause = null;
            }
            else
            {
                IfExp el2 = elifList.get(i + 1);
                el.elifList = new ArrayList<>();
                el.elifList.add(el2);
            }
        }
    }

    @Override
    public Assembly assemble(Assembly asm) {

        String name = "if_" + asm.nbIf;
        this.nameAssembly = name;
        asm.nbIf++;
        String out = name + ":\n";
        if (!elifList.isEmpty())
        {
            elifList.getFirst().assemble(asm);
        }
        if (elseClause != null)
        {
            elseClause.assemble(asm);
        }

        body.assemble(asm);

        if (op instanceof OpExp) {
            OpExp opExp = (OpExp) op;
            opExp.assemble(asm);
            out = out.concat(asm.lastExp);
            out = out.concat("\tcall my_if\n");
            out = out.concat("\tcmp $0, %rax\n");
            out = out.concat("\tjne " + body.nameAssembly + "\n");
        }
        else if (op instanceof IntExp) {
            IntExp intExp = (IntExp) op;
            intExp.assemble(asm);
            out = out.concat("\txor %r12, %r12\n");
            out = out.concat("\tcmp " + asm.lastExp + ", %r12\n");
            out = out.concat("\tjne " + body.nameAssembly + "\n");
        }
        if (elifList.isEmpty() && elseClause == null)
        {
            out = out.concat("\tret\n");
        }
        else if (elifList.isEmpty())
        {
            out = out.concat("\tcall " + elseClause.nameAssembly + "\n");
        }
        else
        {
            out = out.concat("\tcall " + elifList.getFirst().nameAssembly + "\n");
        }
        out = out.concat("\tret\n");

        // TODO other type of op

        asm.funcMap.put(this,out);

        asm.lastExp = "\tcall " + name + "\n";
        return asm;
    }
}