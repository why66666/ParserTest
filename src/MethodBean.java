

import java.util.List;

public class MethodBean {
	
	private String returnType;
	
	private String name;
	
	private String modifiers;
	
	private int paramsize;
	
	private List<String> params;
	
	private boolean isDefault;
	
	private boolean isAbstract;
	
	private boolean isConstruction;
	
	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModifiers() {
		return modifiers;
	}

	public void setModifiers(String modifiers) {
		this.modifiers = modifiers;
	}

	public int getParamsize() {
		return paramsize;
	}

	public void setParamsize(int paramsize) {
		this.paramsize = paramsize;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public boolean isConstruction() {
		return isConstruction;
	}

	public void setConstruction(boolean isConstruction) {
		this.isConstruction = isConstruction;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	@Override
	public String toString() {
		return "MethodBean [returnType=" + returnType + ", name=" + name + ", modifiers=" + modifiers + ", paramsize="
				+ paramsize + ", params=" + params + "]";
	}
	
	

}
