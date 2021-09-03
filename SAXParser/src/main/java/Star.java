public class Star {
	private String id;
	private String name;
	private int birthYear;
	
	public Star() {
		this.id = "";
		this.name = "";
		this.birthYear = 0;
	}
	
	public Star(String id, String name, int birthYear) {
		this.id = id;
		this.name = name;
		this.birthYear = birthYear;
	}
	
	public Star(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getBirthYear() {
		return birthYear;
	}
	
	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}
	
	public String toString() {
		return name;
	}
}
