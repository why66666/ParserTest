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
    //当前for
    private Tree tree;
    //当前if
    private IfTree ifTree;
    //当前try
    private TryTree tryTree;
    //当前catch
    private CatchTree catchTree;
    //当前while
    private WhileLoopTree whileLoopTree;
    
    //for栈
    private Stack<Tree> treeStack = new Stack<>();
    //所有定义了ConnStatement的对象
    private List<AssignmentTree> assignmentTrees = new ArrayList<>();
    //未释放的对象
    private List<ExpressionTree> noCloseConns = new ArrayList<>();
    //执行了sql的对象
    private Map<MethodInvocationTree,Stack<Tree>> doSqls = new HashMap<>();

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

    public IfTree getIfTree() {
        return ifTree;
    }

    public void setIfTree(IfTree ifTree) {
        this.ifTree = ifTree;
    }

    public TryTree getTryTree() {
        return tryTree;
    }

    public void setTryTree(TryTree tryTree) {
        this.tryTree = tryTree;
    }

    public CatchTree getCatchTree() {
        return catchTree;
    }

    public void setCatchTree(CatchTree catchTree) {
        this.catchTree = catchTree;
    }

    public WhileLoopTree getWhileLoopTree() {
        return whileLoopTree;
    }

    public void setWhileLoopTree(WhileLoopTree whileLoopTree) {
        this.whileLoopTree = whileLoopTree;
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

    public List<AssignmentTree> getAssignmentTrees() {
        return assignmentTrees;
    }

    public void setAssignmentTrees(AssignmentTree assignmentTree) {
        this.assignmentTrees.add(assignmentTree);
    }

    public List<ExpressionTree> getNoCloseConns() {
        return noCloseConns;
    }

    public void setNoCloseConns(ExpressionTree noCloseConn) {
        this.noCloseConns.add(noCloseConn);
    }

    public Map<MethodInvocationTree, Stack<Tree>> getDoSqls() {
        return doSqls;
    }

    public void setDoSqls(MethodInvocationTree tree, Stack<Tree> stack) {
        this.doSqls.put(tree,stack);
    }
}
