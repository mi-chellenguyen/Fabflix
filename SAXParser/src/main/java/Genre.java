public class Genre {
	private String id;
	private String name;
	
	public Genre() {
		this.id = "";
		this.name = "";
	}
	
	public Genre(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == this) { 
            return true; 
        } 

        if (!(obj instanceof Genre)) { 
            return false; 
        }

        Genre g = (Genre) obj;

        return name.equals(g.name); 
    }
	
    @Override
    public int hashCode() {
        return name.hashCode();
    }
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}
