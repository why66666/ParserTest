

public class CompMessage {
	
	private String name;
	
	private String type;
	
	private String message;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public CompMessage(String name, String type, String message) {
		super();
		this.name = name;
		this.type = type;
		this.message = message;
	}
	
	
	public CompMessage(){
		
	}

	@Override
	public String toString() {
		return "CompMessage [name=" + name + ", type=" + type + ", message=" + message + "]";
	}
	
	

}
