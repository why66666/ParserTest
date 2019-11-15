import com.sun.source.tree.*;

import java.util.*;

public class ClassBean {
    static final String CONNSTATEMENT_TYPE = "ConnStatement";
    static final String STATEMENTSQL_METHOD = "executeUpdate";
    static final String CLOSE_METHOD = "close";

    //当前类
    private ClassTree classTree;
    //当前方法
    private MethodTree methodTree;
    //当前当前所在父节点
    private Tree tree;

    //节点栈
    private Stack<Tree> treeStack = new Stack<>();

    //所有定义了ConnStatement的对象，节点栈
    private Map<Tree,Stack<Tree>> assign = new HashMap<>();
    //未释放的对象
    private List<Tree> noCloseConns = new ArrayList<>();
    //执行了sql的对象
    private List<MethodInvocationTree> doSqls = new ArrayList<>();

    public ClassTree getClassTree() {
        return classTree;
    }

    public void setClassTree(ClassTree classTree) {
        this.classTree = classTree;
    }

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

    public void pushTreeStack(Tree tree){
        this.treeStack.push(tree);
    }

    public Tree popTreeStack(){
        return this.treeStack.empty()?null:this.treeStack.pop();
    }

    public Tree peekTreeStack(){
        return this.treeStack.empty()?null:this.treeStack.peek();
    }

    public Map<Tree, Stack<Tree>> getAssign() {
        return assign;
    }

    public void setAssign(Tree tree, Stack<Tree> stack) {
        this.assign.put(tree,stack);
    }

    public List<Tree> getNoCloseConns() {
        return noCloseConns;
    }

    public void setNoCloseConns(Tree noCloseConn) {
        this.noCloseConns.add(noCloseConn);
    }

    public List<MethodInvocationTree> getDoSqls() {
        return doSqls;
    }

    public void setDoSqls(MethodInvocationTree doSql) {
        this.doSqls.add(doSql);
    }
}
