import com.sun.source.tree.*;

import java.util.*;

public class ClassBean {
    static final String CONNSTATEMENT_TYPE = "ConnStatement";
    static final String STATEMENTSQL_METHOD = "executeUpdate";
    static final String CLOSE_METHOD = "close";

    //当前方法
    private MethodTree methodTree;
    //当前当前所在父节点
    private Tree tree;


    //节点栈
    private Stack<Tree> treeStack = new Stack<>();
    //所有定义了ConnStatement的对象，节点栈
    private Map<Tree, Stack<Tree>> assign = new HashMap<>();
    //未释放的对象
    private Set<Tree> noCloseConns = new HashSet<>();
    //执行了sql的对象
    private Set<MethodInvocationTree> doSqls = new HashSet<>();
    //执行了sql方法但未定义
    private Map<Tree, Stack<Tree>> doSqlsNoAssign = new HashMap<>();
    //未定义节点
    private Set<Tree> noAssign = new HashSet<>();



    public MethodTree getMethodTree() {
        return methodTree;
    }

    public void setMethodTree(MethodTree methodTree) {
        this.methodTree = methodTree;
    }

    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public Stack<Tree> getTreeStack() {
        return treeStack;
    }

    public void setTreeStack(Stack<Tree> treeStack) {
        this.treeStack = treeStack;
    }

    public void pushTreeStack(Tree tree) {
        this.treeStack.push(tree);
    }

    public Tree popTreeStack() {
        return this.treeStack.empty() ? null : this.treeStack.pop();
    }

    public Tree peekTreeStack() {
        return this.treeStack.empty() ? null : this.treeStack.peek();
    }

    public Map<Tree, Stack<Tree>> getAssign() {
        return assign;
    }

    public void setAssign(Tree tree, Stack<Tree> stack) {
        this.assign.put(tree, stack);
    }


    public Set<Tree> getNoCloseConns() {
        return noCloseConns;
    }

    public void setNoCloseConns(Tree noCloseConn) {
        this.noCloseConns.add(noCloseConn);
    }

    public Set<MethodInvocationTree> getDoSqls() {
        return doSqls;
    }

    public void setDoSqls(MethodInvocationTree doSql) {
        this.doSqls.add(doSql);
    }

    public Map<Tree, Stack<Tree>> getDoSqlsNoAssign() {
        return doSqlsNoAssign;
    }

    public void setDoSqlsNoAssign(Tree tree, Stack<Tree> stack) {
        this.doSqlsNoAssign.put(tree,stack);
    }

    public Set<Tree> getNoAssign() {
        return noAssign;
    }

    public void setNoAssign(Tree noAssign) {
        this.noAssign.add(noAssign);
    }
}
