import com.sun.source.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ClassBean {
    static final String CONNSTATEMENT_TYPE = "ConnStatement";
    static final String STATEMENTSQL_METHOD = "executeUpdate";
    static final String CLOSE_METHOD = "close";

    private ClassTree classTree;
    private MethodTree methodTree;
    //所有定义了ConnStatement的对象
    private List<AssignmentTree> assignmentTrees = new ArrayList<>();
    //未释放的对象
    private List<ExpressionTree> noCloseConns = new ArrayList<>();
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

    public List<MethodInvocationTree> getDoSqls() {
        return doSqls;
    }

    public void setDoSqls(MethodInvocationTree doSql) {
        this.doSqls.add(doSql);
    }
}
