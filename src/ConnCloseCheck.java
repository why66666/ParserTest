import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnCloseCheck {
    private ParserFactory factory;

    private ConnCloseCheck() {
        factory = getParserFactory();
    }

    static ConnCloseCheck getInstance() {
        return new ConnCloseCheck();
    }


    private ParserFactory getParserFactory() {
        Context context = new Context();
        JavacFileManager.preRegister(context);
        return ParserFactory.instance(context);
    }

    private CharSequence readFile(String file) throws IOException {
        FileInputStream fin = null;
        FileChannel ch = null;
        ByteBuffer buffer = null;
        try {
            fin = new FileInputStream(file);
            ch = fin.getChannel();
            buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fin != null) {
            fin.close();
        }
        if (ch != null) {
            ch.close();
        }
        assert buffer != null;
        return Charset.defaultCharset().decode(buffer);
    }


    public void doCheck(String javaPath) throws Exception {
        ClassBean classBean = parseMethodDefs(javaPath);
        System.out.println(classBean==null||classBean.getNoClose()==null?"无泄漏":"泄露对象:"+classBean.getNoClose().toString());
    }

    private JCTree.JCCompilationUnit parse(String file) throws IOException {
        Parser parser = factory.newParser(readFile(file), true, false, true);
        return parser.parseCompilationUnit();
    }

    private ClassBean parseMethodDefs(String javaPath) throws IOException {
        JCTree.JCCompilationUnit unit = parse(javaPath);
        MethodScanner scanner = new MethodScanner();
        return scanner.visitCompilationUnit(unit, new ClassBean());
    }


    public static void main(String[] args) {
        String filePath = "D:\\Work\\WorkFile\\_tom_0magazine__jsp.java";
        try {
            getInstance().doCheck(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MethodScanner extends TreeScanner<ClassBean, ClassBean> {
        private static Stack connStatement;

        MethodScanner() {
            connStatement = new Stack<String>();
        }

        @Override
        public ClassBean visitExpressionStatement(ExpressionStatementTree expressionStatementTree, ClassBean classBean) {

            String expressionStatementString = expressionStatementTree.getExpression().toString().trim();
            String statement = expressionStatementString.replaceAll("\\s*=\\s*", "=");
            if (statement.indexOf("new ConnStatement()") > 0) {
                System.out.println("进栈语句:"+expressionStatementString);
                connStatement.push(statement.substring(0, statement.indexOf("=")));
                System.out.println("进栈:"+statement.substring(0, statement.indexOf("=")));
            } else if (expressionStatementString.indexOf(".close()") > 0) {
                System.out.println("出栈判断:"+expressionStatementString);
                String closePOJO = expressionStatementString.substring(0,expressionStatementString.indexOf(".close()"));
                if(connStatement.search(closePOJO)>0){
                    System.out.println("出栈语句:"+expressionStatementString);
                    if (!connStatement.peek().equals(closePOJO)) {
                        List<String> cals = classBean.getNoClose();
                        cals.add((String) connStatement.peek());
                        classBean.setNoClose(cals);
                    }
                    System.out.println("出栈:"+connStatement.peek());
                    connStatement.pop();
                }
            }
            return super.visitExpressionStatement(expressionStatementTree, classBean);
        }
    }
}
