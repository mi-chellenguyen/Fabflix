import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Movie {
	private String id;
	private String title;
	private int year;
	private String director;
	private int rating;
	private Set<Genre> genresList;
	private ArrayList<Star> starsList;
	
	public Movie(){
		this.id = "";
		this.title = "";
		this.year = 0;
		this.director = "";
		genresList = new HashSet<Genre>();
		starsList = new ArrayList<Star>();
	}
	
	public Movie(String id,String title, int year,String director) {
		this.id = id;
		this.title = title;
		this.year  = year;
		this.director = director;	
		genresList = new HashSet<Genre>();
		starsList = new ArrayList<Star>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getYear() {
		return this.year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getRating() {
		return this.rating;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String getDirector() {
		return this.director;
	}
	
	public void setDirector(String director) {
		this.director = director;
	}
	
	public Set<Genre> getGenresList() {
		return genresList;
	}
	
	public void setGenresList(Set<Genre> tempGenresList) {
		this.genresList = new HashSet<Genre>(tempGenresList);
	}
	
	public ArrayList<Star> getStarsList() {
		return starsList;
	}
	
	public void setStarsList(ArrayList<Star> starsList) {
		this.starsList = new ArrayList<Star>(starsList);
	}
	
	public void addStar(Star star) {
		this.starsList.add(star);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Movie Details - ");
		sb.append("Id:" + getId());
		sb.append(", ");
		sb.append("Title:" + getTitle());
		sb.append(", ");
		sb.append("Year:" + getYear());
		sb.append(", ");
		sb.append("Director:" + getDirector());
		sb.append(", ");
		sb.append("\nGenres: " + getGenresList());//String.join(", ", getGenresList()));
		sb.append("\nStars: " + getStarsList());
		sb.append("\n");	
		return sb.toString();
	}
}
