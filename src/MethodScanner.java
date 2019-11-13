import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import jdk.nashorn.internal.codegen.CompilerConstants;

public class MethodScanner extends TreeScanner<ClassBean, ClassBean> {

    @Override
    public ClassBean visitClass(ClassTree classTree, ClassBean classBean) {
        classBean.setClassTree(classTree);
        super.visitClass(classTree, classBean);
        classBean.setClassTree(classTree);
        return classBean;
    }

    @Override
    public ClassBean visitMethod(MethodTree methodTree, ClassBean classBean) {
        classBean.setMethodTree(methodTree);
        super.visitMethod(methodTree, classBean);
        classBean.setMethodTree(null);

        for(int i=classBean.getAssignmentTrees().size()-1;i>-1;i--){
            //去除未执行sql的对象
            boolean notDoSql = true;
            for(int j=0;j<classBean.getDoSqls().size();j++){
                if(classBean.getDoSqls().get(j).getMethodSelect() instanceof JCTree.JCFieldAccess){
                    if(classBean.getAssignmentTrees().get(i).getVariable().toString().equals(((JCTree.JCFieldAccess)classBean.getDoSqls().get(j).getMethodSelect()).getExpression().toString())){
                        notDoSql = false;
                    }
                }
            }
            if(notDoSql){
                classBean.getAssignmentTrees().remove(i);
            }
        }
        classBean.getNoCloseConns().addAll(classBean.getAssignmentTrees());
        classBean.getAssignmentTrees().clear();
        return classBean;
    }

    @Override
    public ClassBean visitVariable(VariableTree variableTree, ClassBean classBean) {
        super.visitVariable(variableTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitAssignment(AssignmentTree assignmentTree, ClassBean classBean) {
        if(assignmentTree.getExpression() instanceof NewClassTree){
            if(ClassBean.CONNSTATEMENT_TYPE.equals(((NewClassTree) assignmentTree.getExpression()).getIdentifier().toString())){
                classBean.setAssignmentTrees(assignmentTree);
            }
        }
        super.visitAssignment(assignmentTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitMethodInvocation(MethodInvocationTree methodInvocationTree, ClassBean classBean) {
        if(methodInvocationTree.getMethodSelect() instanceof JCTree.JCFieldAccess){
            JCTree.JCFieldAccess meth = (JCTree.JCFieldAccess)methodInvocationTree.getMethodSelect();
            //执行方法的对象是否是之前定义的connStatement对象
            boolean isAssign = false;
            int indexInAssign = 0;
            for(int i=0;i<classBean.getAssignmentTrees().size();i++){
                if(classBean.getAssignmentTrees().get(i).getVariable().toString().equals(meth.getExpression().toString())){
                    isAssign = true;
                    indexInAssign = i;
                    break;
                }
            }
            //是ConnStatement和执行了sql方法的对象存入dosqls
            if(isAssign&&ClassBean.STATEMENTSQL_METHOD.equals(meth.getIdentifier().toString())){
                classBean.setDoSqls(methodInvocationTree);
            }
            //是ConnStatement和执行close方法的对象
            else if(isAssign&&ClassBean.CLOSE_METHOD.equals(meth.getIdentifier().toString())){
                //对象已执行过sql
                boolean isDosql = false;
                for (int i=0;i<classBean.getDoSqls().size();i++){
                    if(classBean.getDoSqls().get(i).getMethodSelect() instanceof JCTree.JCFieldAccess){
                        if(((JCTree.JCFieldAccess)classBean.getDoSqls().get(i).getMethodSelect()).getExpression().toString().equals(meth.getExpression().toString())){
                            isDosql = true;
                            break;
                        }
                    }
                }
                if(isDosql){
                    classBean.getAssignmentTrees().remove(indexInAssign);
                }
            }
        }
        super.visitMethodInvocation(methodInvocationTree, classBean);
        return classBean;
    }
}
