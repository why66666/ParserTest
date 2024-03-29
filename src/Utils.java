import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;

import java.util.Stack;

public class Utils {
    public Utils() {
    }

    public static boolean isSameTree(Tree tree1, Tree tree2) {
        return ((JCTree) tree1).pos == ((JCTree) tree2).pos;
    }

    public static boolean hasTree(Stack<Tree> stack, Tree tree) {
        Stack<Tree> stack1 = new Stack<>();
        stack1.addAll(stack);
        while (!stack1.empty()) {
            if (isSameTree(stack1.pop(), tree)) {
                return true;
            }
        }
        return false;
    }

}
