import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;

import java.util.ArrayList;
import java.util.List;

public class ClassBean {
    static final String CONNSTATEMENT_TYPE = "ConnStatement";

    private ClassTree classTree;
    private MethodTree methodTree;
    private List<NewClassTree> newClassTrees = new ArrayList<>();

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

    public List<NewClassTree> getNewClassTrees() {
        return newClassTrees;
    }

    public void setNewClassTrees(NewClassTree newClassTrees) {
        this.newClassTrees.add(newClassTrees);
    }
}
