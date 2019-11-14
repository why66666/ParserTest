import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.corba.se.idl.Util;
import com.sun.tools.javac.tree.JCTree;
import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
            Map<MethodInvocationTree, Stack<Tree>> map = classBean.getDoSqls();
            for(Map.Entry<MethodInvocationTree, Stack<Tree>> entry : map.entrySet()){
                if(entry.getKey().getMethodSelect() instanceof JCTree.JCFieldAccess){
                    if(classBean.getAssignmentTrees().get(i).getVariable().toString().equals(((JCTree.JCFieldAccess)entry.getKey().getMethodSelect()).getExpression().toString())){
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
                classBean.setDoSqls(methodInvocationTree,classBean.getTreeStack());
            }
            //是ConnStatement和执行close方法的对象
            else if(isAssign&&ClassBean.CLOSE_METHOD.equals(meth.getIdentifier().toString())){
                //对象已执行过sql
                boolean isDosqlAndIsSub = false;
                Map<MethodInvocationTree, Stack<Tree>> map = classBean.getDoSqls();
                for(Map.Entry<MethodInvocationTree, Stack<Tree>> entry : map.entrySet()){
                    if(entry.getKey().getMethodSelect() instanceof JCTree.JCFieldAccess){
                        if(((JCTree.JCFieldAccess)entry.getKey().getMethodSelect()).getExpression().toString().equals(meth.getExpression().toString())&& Utils.hasTree(entry.getValue(),methodInvocationTree)){
                            isDosqlAndIsSub = true;
                            break;
                        }
                    }
                }
                if(isDosqlAndIsSub){
                    classBean.getAssignmentTrees().remove(indexInAssign);
                }
            }
        }
        super.visitMethodInvocation(methodInvocationTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitForLoop(ForLoopTree forLoopTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        //节点进栈
        classBean.pushTreeStack(forLoopTree);
        super.visitForLoop(forLoopTree, classBean);
        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }



    @Override
    public ClassBean visitIf(IfTree ifTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        //节点进栈
        classBean.pushTreeStack(ifTree);
        super.visitIf(ifTree, classBean);
        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }

}
