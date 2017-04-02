import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sun.org.apache.bcel.internal.Constants.ACC_PUBLIC;
import static com.sun.xml.internal.ws.org.objectweb.asm.Opcodes.ACC_STATIC;

public class PostfixMethod {
    
    private PostfixClass parentClass;
    private HashMap<String, LocalVariableGen> variables;
    private MethodGen methodGen;
    private InstructionList instructionList;
    private String name;
    private List<Token> statements;
    
    public PostfixMethod(int accessFlags, Type returnType, Type[] argumentTypes, String[] argumentNames, String name, PostfixClass parentClass) {
        this.name = name;
        this.parentClass = parentClass;
        this.instructionList = new InstructionList();
        methodGen = new MethodGen(accessFlags, returnType, argumentTypes, argumentNames, name, parentClass.getClassGen().getClassName(), instructionList, parentClass.getClassGen().getConstantPool());
        this.variables = new HashMap<>();
        this.statements = new ArrayList<>();
        parentClass.addMethod(this);
    }
    
    public PostfixClass getParentClass() {
        return parentClass;
    }
    public LocalVariableGen getVariable(String name) {
        return variables.get(name);
    }
    public LocalVariableGen addVariable(String name, Type type) {
        LocalVariableGen ans = methodGen.addLocalVariable(name, type, null, null);
        variables.put(name, ans);
        return ans;
    }
    public InstructionList getInstructionList() {
        return instructionList;
    }
    public String getName() {
        return name;
    }
    
    public void addStatement(Token t) {
        statements.add(t);
    }
    
    public Method compile() {
        for (Token t : statements) t.compile(this);
        instructionList.append(new RETURN());
        methodGen.setMaxStack();
        return methodGen.getMethod();
    }
    
}

class PostfixMainMethod extends PostfixMethod {
    
    public PostfixMainMethod(PostfixClass parentClass) {
        super(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] {new ArrayType(Type.STRING, 1)}, new String[] {"args"}, "main", parentClass);
    }
    
}
