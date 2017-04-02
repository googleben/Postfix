import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class PostfixCompiler {
    
    public static void main(String[] args) throws IOException {
        if (args.length==0) {
            System.out.println("No source file specified");
            return;
        }
        if (!args[0].endsWith(".postfix")) {
            System.out.println("Source files must end with .postfix");
            return;
        }
        new PostfixCompiler(args[0]).compile();
    }
    
    private String filename;
    
    private PostfixClass postfixClass;
    private PostfixMethod main;
    
    public PostfixCompiler(String filename) {
        this.filename = filename;
        postfixClass = new PostfixClass(filename.replace(".postfix", ""));
        postfixClass.addDefaultConstructor();
        main = new PostfixMainMethod(postfixClass);
    }
    
    private void parse() throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.endsWith("=")) {
                line = line.substring(0, line.length()-1);
                String name = line.substring(0, line.indexOf(' '));
                line = line.substring(line.indexOf(' '));
                main.addStatement(new VariableAssignmentToken(name, parseExpression(makeArrayList(line.split(" ")))));
            } else {
                //if it isn't assignment print it
                main.addStatement(new PrintlnMethodCallToken(parseExpression(makeArrayList(line.split(" ")))));
            }
        }
    }
    
    public void compile() throws IOException {
        parse();
        postfixClass.compile().dump(new File(postfixClass.getClassGen().getFileName()));
    }
    
    private ExpressionToken parseExpression(ArrayList<String> line) {
        if (line.size()==0) throw new RuntimeException("Tried to parse empty string");
        String s = line.remove(line.size()-1);
        if (s.matches("^\\d+$") || s.matches("^\\d*\\.\\d+$")) {
            //only a number
            return new LiteralToken(Float.parseFloat(s));
        }
        if (s.matches("^[A-z]+$")) {
            //only a variable name
            return new VariableLoadToken(s);
        }
        if (s.equals("*")) {
            return new MultiplyToken(parseExpression(line), parseExpression(line));
        }
        if (s.equals("/")) {
            return new DivideToken(parseExpression(line), parseExpression(line));
        }
        if (s.equals("+")) {
            return new AddToken(parseExpression(line), parseExpression(line));
        }
        if (s.equals("-")) {
            return new SubtractToken(parseExpression(line), parseExpression(line));
        }
        if (s.equals("%")) {
            return new ModulusToken(parseExpression(line), parseExpression(line));
        }
        if (s.equals("=")) {
            System.out.println();
            ExpressionToken to = parseExpression(line);
            return new VariableAssignmentExpressionToken(line.remove(line.size()-1), to);
        }
        
        throw new RuntimeException("Could not parse expression \""+line+"\"");
    }
    
    static ArrayList<String> makeArrayList(String[] line) {
        ArrayList<String> ans = new ArrayList<>();
        Collections.addAll(ans, line);
        return ans;
    }
    
}
