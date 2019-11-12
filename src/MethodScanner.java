import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

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
        classBean.setMethodTree(methodTree);
        return classBean;
    }

    @Override
    public ClassBean visitVariable(VariableTree variableTree, ClassBean classBean) {
        super.visitVariable(variableTree, classBean);
        return classBean;
    }



    @Override
    public ClassBean visitNewClass(NewClassTree newClassTree, ClassBean classBean) {
        if(ClassBean.CONNSTATEMENT_TYPE.equals(newClassTree.getIdentifier().toString())){
            classBean.getNewClassTrees().add(newClassTree);
        }
        super.visitNewClass(newClassTree, classBean);
        return classBean;
    }
}
