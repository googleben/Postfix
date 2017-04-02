import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.generic.ClassGen;

import java.util.HashMap;

import static com.sun.org.apache.bcel.internal.Constants.ACC_PUBLIC;

public class PostfixClass {
    
    private ClassGen classGen;
    private HashMap<String, PostfixMethod> methods;
    
    public PostfixClass(String name) {
        classGen = new ClassGen(name, "java/lang/Object", name+".class", ACC_PUBLIC, null);
        methods = new HashMap<>();
    }
    
    public void addDefaultConstructor() {
        classGen.addEmptyConstructor(ACC_PUBLIC);
    }
    
    public PostfixMethod getMethod(String name) {
        return methods.get(name);
    }
    
    public void addMethod(PostfixMethod method) {
        methods.put(method.getName(), method);
    }
    
    public ClassGen getClassGen() {
        return classGen;
    }
    
    public JavaClass compile() {
        for (PostfixMethod m : methods.values()) classGen.addMethod(m.compile());
        return classGen.getJavaClass();
    }
    
}
