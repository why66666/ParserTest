import com.sun.source.tree.Tree;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import sun.security.jca.JCAUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

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
        for (Tree tree :
                classBean.getNoCloseConns()){
            System.out.println("tree:"+tree+",pos:"+((JCTree)tree).pos);
        }
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
}
