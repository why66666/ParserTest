import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MethodScanner extends TreeScanner<ClassBean, ClassBean> {

    @Override
    public ClassBean visitClass(ClassTree classTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(classTree);
        //节点进栈
        classBean.pushTreeStack(classTree);
        super.visitClass(classTree, classBean);

        for(Tree isDosql : classBean.getDoSqlsNoAssign().keySet()){
            for(Tree noAssign : classBean.getNoAssign()){
                if(((VariableTree)noAssign).getName().toString().equals(((JCTree.JCFieldAccess)((MethodInvocationTree)isDosql).getMethodSelect()).getExpression().toString())){
                    classBean.setNoCloseConns(noAssign);
                }
            }
        }

        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }

    @Override
    public ClassBean visitMethod(MethodTree methodTree, ClassBean classBean) {
        classBean.setMethodTree(methodTree);
        super.visitMethod(methodTree, classBean);
        classBean.setMethodTree(null);
        Map<Tree, Stack<Tree>> map = classBean.getAssign();

        Map<Tree, Stack<Tree>> keepMap = new HashMap<>();
        //去除未执行sql的对象
        for (Map.Entry<Tree, Stack<Tree>> entry : map.entrySet()) {
            boolean notDoSql = true;
            for (MethodInvocationTree methodInvocationTree : classBean.getDoSqls()) {
                if (methodInvocationTree.getMethodSelect() instanceof JCTree.JCFieldAccess) {
                    if (entry.getKey() instanceof AssignmentTree && ((AssignmentTree) entry.getKey()).getVariable().toString().equals(((JCTree.JCFieldAccess) methodInvocationTree.getMethodSelect()).getExpression().toString())) {
                        notDoSql = false;
                    }
                }
            }
            if (notDoSql) {
                //父节点为Class的全局变量保留
                if (!(entry.getValue().peek() instanceof ClassTree)) {
                    classBean.getAssign().remove(entry.getKey());
                } else {
                    if (!keepMap.containsKey(entry.getKey())) keepMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        classBean.getNoCloseConns().addAll(classBean.getAssign().keySet());
        classBean.getAssign().clear();
        classBean.getAssign().putAll(keepMap);
        return classBean;
    }

    @Override
    public ClassBean visitVariable(VariableTree variableTree, ClassBean classBean) {
        if (variableTree.getInitializer() instanceof NewClassTree) {
            if (ClassBean.CONNSTATEMENT_TYPE.equals(((NewClassTree) variableTree.getInitializer()).getIdentifier().toString())) {
                Stack<Tree> stack = new Stack<>();
                stack.addAll(classBean.getTreeStack());
                classBean.setAssign(variableTree, stack);
                if(classBean.getTreeStack().peek() instanceof ClassTree){
                    classBean.setNoAssign(variableTree);
                }
            }
        }
        super.visitVariable(variableTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitAssignment(AssignmentTree assignmentTree, ClassBean classBean) {
        if (assignmentTree.getExpression() instanceof NewClassTree) {
            if (ClassBean.CONNSTATEMENT_TYPE.equals(((NewClassTree) assignmentTree.getExpression()).getIdentifier().toString())) {
                Stack<Tree> stack = new Stack<>();
                stack.addAll(classBean.getTreeStack());
                classBean.setAssign(assignmentTree, stack);
            }
        }
        super.visitAssignment(assignmentTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitMethodInvocation(MethodInvocationTree methodInvocationTree, ClassBean classBean) {
        if (methodInvocationTree.getMethodSelect() instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess meth = (JCTree.JCFieldAccess) methodInvocationTree.getMethodSelect();
            //执行方法的对象是否是之前定义的connStatement对象
            boolean isAssign = false;
            Map.Entry<Tree, Stack<Tree>> inAssignEntry = null;
            Map<Tree, Stack<Tree>> map = classBean.getAssign();
            for (Map.Entry<Tree, Stack<Tree>> entry : map.entrySet()) {
                if (entry.getKey() instanceof AssignmentTree && ((AssignmentTree) entry.getKey()).getVariable().toString().equals(meth.getExpression().toString()) || (entry.getKey() instanceof VariableTree && ((VariableTree) entry.getKey()).getName().toString().equals(meth.getExpression().toString()))) {
                    isAssign = true;
                    inAssignEntry = entry;
                    break;
                }
            }
            //是ConnStatement和执行了sql方法的对象存入dosqls
            if(isAssign){
                if (ClassBean.STATEMENTSQL_METHOD.equals(meth.getIdentifier().toString())) {
                    classBean.setDoSqls(methodInvocationTree);
                }
                //是ConnStatement和执行close方法的对象
                else if (ClassBean.CLOSE_METHOD.equals(meth.getIdentifier().toString())) {
                    //对象已执行过sql
                    boolean isDosqlAddIsSame = false;
                    for (MethodInvocationTree mTree : classBean.getDoSqls()) {
                        if (methodInvocationTree.getMethodSelect() instanceof JCTree.JCFieldAccess) {
                            if (((JCTree.JCFieldAccess) mTree.getMethodSelect()).getExpression().toString().equals(meth.getExpression().toString()) && Utils.hasTree(classBean.getTreeStack(), inAssignEntry.getValue().peek())) {
                                isDosqlAddIsSame = true;
                                break;
                            }
                        }
                    }
                    if (isDosqlAddIsSame) {
                        classBean.getAssign().remove(inAssignEntry.getKey());
                        classBean.getNoCloseConns().remove(inAssignEntry.getKey());
                    }
                }
            }else{
                if (ClassBean.STATEMENTSQL_METHOD.equals(meth.getIdentifier().toString())) {
                    Stack<Tree> stack = new Stack<>();
                    stack.addAll(classBean.getTreeStack());
                    classBean.setDoSqlsNoAssign(methodInvocationTree,stack);
                }
                //是ConnStatement和执行close方法的对象
                else if (ClassBean.CLOSE_METHOD.equals(meth.getIdentifier().toString())) {
                    for (Map.Entry<Tree, Stack<Tree>> entry : classBean.getDoSqlsNoAssign().entrySet()) {
                        if (methodInvocationTree.getMethodSelect() instanceof JCTree.JCFieldAccess) {
                            MethodInvocationTree mt = (MethodInvocationTree)entry.getKey();
                            JCTree.JCFieldAccess ms = (JCTree.JCFieldAccess) (mt.getMethodSelect());
                            if (ms.getExpression().toString().equals(meth.getExpression().toString())&& Utils.hasTree(classBean.getTreeStack(), entry.getValue().peek())) {
                                classBean.getDoSqlsNoAssign().remove(entry.getKey());
                                break;
                            }
                        }
                    }
                }
            }
        }
        super.visitMethodInvocation(methodInvocationTree, classBean);
        return classBean;
    }

    @Override
    public ClassBean visitForLoop(ForLoopTree forLoopTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(forLoopTree);
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
        classBean.setTree(ifTree);
        //节点进栈
        classBean.pushTreeStack(ifTree);
        super.visitIf(ifTree, classBean);
        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }

    @Override
    public ClassBean visitWhileLoop(WhileLoopTree whileLoopTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(whileLoopTree);
        //节点进栈
        classBean.pushTreeStack(whileLoopTree);
        super.visitWhileLoop(whileLoopTree, classBean);
        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }

    @Override
    public ClassBean visitTry(TryTree tryTree, ClassBean classBean) {
        //存储父节点
        classBean.setTree(tryTree);
        //节点进栈
        classBean.pushTreeStack(tryTree);
        super.visitTry(tryTree, classBean);
        //节点出栈
        classBean.popTreeStack();
        //存储父节点
        classBean.setTree(classBean.peekTreeStack());
        return classBean;
    }
}
