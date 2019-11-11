

import java.util.ArrayList;
import java.util.List;

public class ClassBean {
	
	
	private List<MethodBean> listMethod = new ArrayList<MethodBean>(); 
	
	private String classModifiers;
	
	private String name;
	
	private boolean isAbstract;
	
	private boolean isInterface;

	private List<String> noClose = new ArrayList<>();

	private List<String> sqlList = new ArrayList<String>();

	public String getClassModifiers() {
		return classModifiers;
	}

	public void setClassModifiers(String classModifiers) {
		this.classModifiers = classModifiers;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public List<MethodBean> getListMethod() {
		return listMethod;
	}

	public void setListMethod(MethodBean classMethod) {
		this.listMethod.add(classMethod);
	}

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	@Override
	public String toString() {
		return "ClassBean [listMethod=" + listMethod + ", classModifiers=" + classModifiers + ", isAbstract="
				+ isAbstract + "]";
	}

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(String sqlStr) {
		this.sqlList.add(sqlStr);
	}

	public List<String> getNoClose() {
		return noClose;
	}

	public void setNoClose(List<String> noClose) {
		this.noClose = noClose;
	}
}
