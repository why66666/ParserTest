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
import java.util.concurrent.LinkedBlockingQueue;

public class ConnCloseCheck {
    private ParserFactory factory;
    private ConnCloseCheck() {
        factory = getParserFactory();
    }

    static ConnCloseCheck getInstance(){
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
        try{
            fin = new FileInputStream(file);
            ch = fin.getChannel();
            buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(fin!=null){
            fin.close();
        }
        if(ch!=null){
            ch.close();
        }
        assert buffer != null;
        return Charset.defaultCharset().decode(buffer);
    }



    public void doCheck(String javaPath) throws Exception {
        ClassBean classBean = parseMethodDefs(javaPath);
        System.out.println(classBean.getNoClose().toString());
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
        String filePath = "D:\\test\\javaFile\\SysUpdateWF.java";
        try {
            getInstance().doCheck(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MethodScanner extends TreeScanner<ClassBean, ClassBean> {
        private static LinkedBlockingQueue connStatement;
        MethodScanner() {
            connStatement = new LinkedBlockingQueue();
        }

        @Override
        public ClassBean visitExpressionStatement(ExpressionStatementTree expressionStatementTree, ClassBean classBean) {

            String expressionStatementString = expressionStatementTree.getExpression().toString().trim();
            expressionStatementString = expressionStatementString.replaceAll("\\s*=\\s*","=");
            System.out.println("==>"+expressionStatementString);
            if(expressionStatementString.indexOf("new ConnStatement()")>0){
                System.out.println(expressionStatementString.substring(0,expressionStatementString.indexOf("=")));
                connStatement.add(expressionStatementString.substring(0,expressionStatementString.indexOf("=")));
            }else if(expressionStatementString.indexOf(connStatement.element()+".close()")>0                                                                                    ){
                connStatement.remove();
            }
            return super.visitExpressionStatement(expressionStatementTree, classBean);
        }
    }
}
