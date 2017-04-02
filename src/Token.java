import com.sun.org.apache.bcel.internal.generic.*;

public abstract class Token {
    abstract void compile(PostfixMethod method);
}

class MethodCallToken extends ExpressionToken {
    
    private ExpressionToken valueGen;
    private String fieldClassName;
    private String fieldName;
    private String fieldSignature;
    private String methodClassName;
    private String methodName;
    private String methodSignature;
    
    public MethodCallToken(ExpressionToken valueGen, String fieldClassName, String fieldName, String fieldSignature, String methodClassName, String methodName, String methodSignature) {
        this.valueGen = valueGen;
        this.fieldClassName = fieldClassName;
        this.fieldName = fieldName;
        this.fieldSignature = fieldSignature;
        this.methodClassName = methodClassName;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }
    
    public void compile(PostfixMethod method) {
        //check if field and method are in constant pool and get their constant pool reference numbers
        ConstantPoolGen cp = method.getParentClass().getClassGen().getConstantPool();
        int fieldRef = cp.lookupFieldref(fieldClassName, fieldName, fieldSignature);
        if (fieldRef==-1) {
            //field not added to constant pool
            fieldRef = cp.addFieldref(fieldClassName, fieldName, fieldSignature);
        }
        int methodRef = cp.lookupMethodref(methodClassName, methodName, methodSignature);
        if (methodRef==-1) {
            //method not added to constant pool
            methodRef = cp.addMethodref(methodClassName, methodName, methodSignature);
        }
    
        InstructionList il = method.getInstructionList();
        il.append(new GETSTATIC(fieldRef));
        valueGen.compile(method);
        il.append(new INVOKEVIRTUAL(methodRef));
    }
    
}

class PrintlnMethodCallToken extends MethodCallToken {
    
    public PrintlnMethodCallToken(ExpressionToken valueGen) {
        super(valueGen, "java/lang/System", "out", "Ljava/io/PrintStream;", "java/io/PrintStream", "println", "(F)V");
    }
    
}

class VariableAssignmentToken extends Token {
    
    private String name;
    private ExpressionToken valueGen;
    
    public VariableAssignmentToken(String name, ExpressionToken valueGen) {
        this.name = name;
        this.valueGen = valueGen;
    }
    
    public void compile(PostfixMethod method) {
        LocalVariableGen var = method.getVariable(name);
        if (var==null) {
            //variable not added to method
            var = method.addVariable(name, Type.FLOAT);
        }
        valueGen.compile(method);
        method.getInstructionList().append(new FSTORE(var.getIndex()));
    }
    
}

class VariableAssignmentExpressionToken extends ExpressionToken {
    
    private String name;
    private ExpressionToken valueGen;
    
    public VariableAssignmentExpressionToken(String name, ExpressionToken valueGen) {
        this.name = name;
        this.valueGen = valueGen;
    }
    
    public void compile(PostfixMethod method) {
        LocalVariableGen var = method.getVariable(name);
        if (var==null) {
            //variable not added to method
            var = method.addVariable(name, Type.FLOAT);
        }
        valueGen.compile(method);
        method.getInstructionList().append(new DUP());
        method.getInstructionList().append(new FSTORE(var.getIndex()));
    }
    
}

abstract class ExpressionToken extends Token {

}

abstract class OperatorToken extends ExpressionToken {

}

abstract class BinaryMathToken extends OperatorToken {
    
    private ExpressionToken left;
    private ExpressionToken right;
    
    public BinaryMathToken(ExpressionToken left, ExpressionToken right) {
        this.left = left;
        this.right = right;
    }
    
    public void compile(PostfixMethod method) {
        left.compile(method);
        right.compile(method);
    }
    
}

class MultiplyToken extends BinaryMathToken {
    
    public MultiplyToken(ExpressionToken left, ExpressionToken right) {
        super(left, right);
    }
    
    public void compile(PostfixMethod method) {
        super.compile(method);
        method.getInstructionList().append(new FMUL());
    }
    
}

class DivideToken extends BinaryMathToken {
    
    public DivideToken(ExpressionToken left, ExpressionToken right) {
        super(left, right);
    }
    
    public void compile(PostfixMethod method) {
        super.compile(method);
        method.getInstructionList().append(new FDIV());
    }
    
}

class AddToken extends BinaryMathToken {
    
    public AddToken(ExpressionToken left, ExpressionToken right) {
        super(left, right);
    }
    
    public void compile(PostfixMethod method) {
        super.compile(method);
        method.getInstructionList().append(new FADD());
    }
    
}

class SubtractToken extends BinaryMathToken {
    
    public SubtractToken(ExpressionToken left, ExpressionToken right) {
        super(left, right);
    }
    
    public void compile(PostfixMethod method) {
        super.compile(method);
        method.getInstructionList().append(new FSUB());
    }
    
}

class ModulusToken extends BinaryMathToken {
    
    public ModulusToken(ExpressionToken left, ExpressionToken right) {
        super(left, right);
    }
    
    public void compile(PostfixMethod method) {
        super.compile(method);
        method.getInstructionList().append(new FREM());
    }
    
}

class LiteralToken extends ExpressionToken {
    
    private float value;
    
    public LiteralToken(float value) {
        this.value = value;
    }
    
    public void compile(PostfixMethod method) {
        //check if `value` is in the constant pool
        ConstantPoolGen cp = method.getParentClass().getClassGen().getConstantPool();
        int ref = cp.lookupFloat(value);
        if (ref==-1) {
            //float not found
            ref = cp.addFloat(value);
        }
        method.getInstructionList().append(new LDC(ref));
    }
    
}

class VariableLoadToken extends ExpressionToken {
    
    private String name;
    
    public VariableLoadToken(String name) {
        this.name = name;
    }
    
    public void compile(PostfixMethod method) {
        LocalVariableGen var = method.getVariable(name);
        if (var==null) {
            //variable not added to method
            var = method.addVariable(name, Type.FLOAT);
        }
        method.getInstructionList().append(new FLOAD(var.getIndex()));
    }
    
}
