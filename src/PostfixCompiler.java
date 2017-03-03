import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.generic.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class PostfixCompiler {
    
    public static void main(String[] args) throws FileNotFoundException {
        
        new PostfixCompiler(args.length!=0 ? args[0] : "Postfix.postfix").output();
        
    }
    
    String filename;
    
    ArrayList<Token> expressions;
    
    int stackMax;
    
    ClassGen cg;
    
    public PostfixCompiler(String filename) {
        this.filename = filename.replace(".postfix", "");
        cg = new ClassGen(filename, "java/lang/Object", filename+".class",
                Constants.ACC_PUBLIC | Constants.ACC_SUPER, new String[] {});
        cg.addEmptyConstructor(Constants.ACC_PUBLIC);
    }
    
    public void tokenize() {
        Scanner in = null;
        try {
            in = new Scanner(new File(filename+".postfix"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
    
        expressions = new ArrayList<>();
    
        stackMax = 0;
    
        while (in.hasNextLine()) {
            Token token = Token.BEGIN_EXPR;
            expressions.add(token);
            Scanner line = new Scanner(in.nextLine());
            int stackCurr = 0;
            while (line.hasNext()) {
                if (line.hasNextFloat()) {
                    Token c = new LiteralToken(line.nextFloat());
                    stackCurr++;
                    if (stackMax<stackCurr) stackMax = stackCurr;
                    token.next = c;
                    token = c;
                } else {
                    String operator = line.next();
                    if (stackCurr < 2) throw new RuntimeException("Too few arguments for operator "+operator);
                    Token c = new OperatorToken(operator);
                    stackCurr--;
                    token.next = c;
                    token = c;
                }
            }
        }
    }
    
    public void compile() {
    
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        
        cp.addFieldref("java/lang/System", "out", "Ljava/io/PrintStream;");
        cp.addMethodref("java/io/PrintStream", "println", "(F)V");
        
        for (Token head : expressions) {
            Token curr = head.next;
            il.append(new GETSTATIC(cp.lookupFieldref("java/lang/System", "out", "Ljava/io/PrintStream;")));
            while (curr!=Token.END_EXPR) {
                if (curr instanceof LiteralToken) {
                    LiteralToken c = (LiteralToken)curr;
                    int index = cp.lookupFloat(c.literal);
                    if (index==-1) {
                        index = cp.addFloat(c.literal);
                    }
                    il.append(new LDC(index));
                } else {
                    OperatorToken t = (OperatorToken)curr;
                    String op = t.operator;
                    if (op.equals("+")) {
                        il.append(new FADD());
                    }
                    if (op.equals("-")) {
                        il.append(new FSUB());
                    }
                    if (op.equals("*")) {
                        il.append(new FMUL());
                    }
                    if (op.equals("/")) {
                        il.append(new FDIV());
                    }
                    if (op.equals("%")) {
                        il.append(new FREM());
                    }
                }
                curr = curr.next;
            }
            il.append(new INVOKEVIRTUAL(cp.lookupMethodref("java/io/PrintStream", "println", "(F)V")));
        }
        
        il.append(new RETURN());
        
        MethodGen main = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC, Type.VOID, new Type[] {new ArrayType(Type.STRING, 1)},
                new String[] {"args"}, "main", filename.replace(".postfix", ""), il, cg.getConstantPool());
        
        //add 1 for reference to java.lang.System.out
        main.setMaxStack(stackMax+1);
        
        cg.addMethod(main.getMethod());
    }
    
    public void output() {
        tokenize();
        compile();
        try {
            cg.getJavaClass().dump(new File(filename+".class"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

class Token {
    
    public static final Token END_EXPR = new Token();
    
    public static final Token BEGIN_EXPR = new Token();
    
    public Token next = END_EXPR;
    
    Token() {}
    
}

class OperatorToken extends Token {
    
    public String operator;
    
    public OperatorToken(String operator) {
        this.operator = operator;
    }
    
}

class LiteralToken extends Token {
    
    public float literal;
    
    public LiteralToken(float literal) {
        this.literal = literal;
    }
    
}
