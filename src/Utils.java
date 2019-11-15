import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;

import java.util.Stack;

public class Utils {
    public Utils() {}

    public static boolean isSameTree(Tree tree1,Tree tree2){
        return ((JCTree)tree1).pos == ((JCTree)tree2).pos;
    }
    public static boolean hasTree(Stack<Tree> stack, Tree tree) {
        if(stack.empty()&&tree==null){
            return true;
        }
        while (!stack.empty()) {
            if (((JCTree) stack.pop()).pos == ((JCTree) tree).pos) {
                return true;
            }
        }
        return false;
    }
}
