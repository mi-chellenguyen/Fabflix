import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieSAXParser extends DefaultHandler {
	
    HashMap<String, Movie> movieList; //acquired from main243.xml and casts124.xml
    HashMap<String, Star> starList; //acquired from actors63.xml
    HashMap<String, Genre> genreList; //acquired from main243.xml

    private String tempVal;
    private String directorName;
    private Set<Genre> tempGenresList;
    private String tempFilmId;
    
    private int maxStarIdNum; //stores the current max Star id number
    private int maxGenreIdNum;
    
    //to maintain context
    private Movie tempMovie;
    private Star tempStar;
    private Genre tempGenre;
    
    String loginUser = "mytestuser";
	String loginPasswd = "mypassword";
	String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
	
	Connection dbcon;

    public MovieSAXParser() throws Exception {
    	Class.forName("com.mysql.jdbc.Driver").newInstance();
    	dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    	
    	//Determine the max star id, query is made once, then this class will keep track
    	//and increment to avoid making multiple calls to DB
    	maxStarIdNum = findMaxStarIdNum();
    	maxGenreIdNum = findMaxGenreIdNum();
    
        movieList = new HashMap<String, Movie>();
        starList = new HashMap<String, Star>();
        
        genreList = new HashMap<String, Genre>();
        
        tempGenresList = new HashSet<Genre>();
    }

    public void run() throws SQLException {	
        parseDocument();
        //printData();
        writeToCSV();
        addToDatabase();
        dbcon.close();
    }

    private void parseDocument(){
    	
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {	
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", this);
            sp.parse("actors63.xml", this);
            sp.parse("casts124.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    
    private int findMaxStarIdNum() throws Exception{
    	String maxStarIdQuery = "SELECT MAX(id) as maxId FROM stars;";
    	PreparedStatement maxIdStatement = dbcon.prepareStatement(maxStarIdQuery);
    	ResultSet maxIdResult = maxIdStatement.executeQuery();
    	if (maxIdResult.next()) {
    		String maxId = maxIdResult.getString("maxId");
    		if(maxId != null) {
    			maxIdStatement.close();
    			maxIdResult.close();
    			return Integer.parseInt(maxId.substring(2));	
    		}
    		else {
    			System.out.println("max star id result returned null, max id set to 0");
    			maxIdStatement.close();
    			maxIdResult.close();
    			return 0;
    		}
    	}
    	maxIdStatement.close();
		maxIdResult.close();
    	return -1;
    }
    
    private int findMaxGenreIdNum() throws Exception{
    	String maxGenreIdQuery = "SELECT MAX(id) as maxId FROM genres;";
    	PreparedStatement maxIdStatement = dbcon.prepareStatement(maxGenreIdQuery);
    	ResultSet maxIdResult = maxIdStatement.executeQuery();
    	if (maxIdResult.next()) {
    		String maxId = maxIdResult.getString("maxId");
    		//System.out.println(maxId);
    		if(maxId != null) {
    			maxIdStatement.close();
    			maxIdResult.close();
    			return Integer.parseInt(maxId);	
    		}
    		else {
    			maxIdStatement.close();
    			maxIdResult.close();
    			System.out.println("max genre id result returned null, max id set to 0");
    			return 0;
    		}
    	}
    	maxIdStatement.close();
		maxIdResult.close();
    	return -1;
    }
    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("No of Movies '" + movieList.size() + "'.");

        Iterator<Entry<String, Movie>> it = movieList.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<String, Movie> pair = (Map.Entry<String, Movie>)it.next();
            System.out.println(pair.getValue().toString());
        }
    }
    
    private void writeToCSV() {
    	writeMovieListToCSV();
    	writeStarListToCSV();
    	writeGenreListToCSV();
    }
    
    private void writeMovieListToCSV() {
    	moviesTableCSV();
    	starsInMoviesTableCSV();
    	genresInMoviesTableCSV();
    	ratingsTableCSV();
    }
    
    private void moviesTableCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("movies_table.csv"))) {

  	      StringBuilder sb = new StringBuilder();
  	      sb.append("id,title,year,director\n");
  	      
  	      Iterator<Entry<String, Movie>> it = movieList.entrySet().iterator();
  	        while (it.hasNext()) {
  	        	Map.Entry<String, Movie> pair = (Map.Entry<String, Movie>)it.next();
  	            sb.append('"' + pair.getKey() + '"' + ',');
  	            sb.append('"' + pair.getValue().getTitle() + '"' + ',');
  	            sb.append('"' + Integer.toString(pair.getValue().getYear()) + '"' + ',');
  	            sb.append('"' + pair.getValue().getDirector() + '"');
  	            sb.append('\n');
  	        }

  	      writer.write(sb.toString());

  	      System.out.println("done writing to movies_table.csv!");
    	} 
	  	catch (FileNotFoundException e) {
	  		System.out.println(e.getMessage());
	  	}
    }
    
    private void starsInMoviesTableCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("stars_in_movies_table.csv"))) {

    	      StringBuilder sb = new StringBuilder();
    	      sb.append("starId,movieId\n");
    	      
    	      Iterator<Entry<String, Movie>> it = movieList.entrySet().iterator();
    	        while (it.hasNext()) {
    	        	Map.Entry<String, Movie> pair = (Map.Entry<String, Movie>)it.next();
    	        	if(pair.getValue().getStarsList().size() == 0) {
      	        		sb.append('"' + "nm0" + '"' + ',');
        	            sb.append('"' + pair.getKey() + '"' + ',');
        	            sb.append('\n');
      	        	}
    	        	else {
	    	        	for(Star star : pair.getValue().getStarsList()) {
		    	            sb.append('"' + star.getId() + '"' + ',');
		    	            sb.append('"' + pair.getKey() + '"' + ',');
		    	            sb.append('\n');
	    	        	}
    	        	}
    	        }

    	      writer.write(sb.toString());

    	      System.out.println("done writing to stars_in_movies_table.csv!");
      	} 
  	  	catch (FileNotFoundException e) {
  	  		System.out.println(e.getMessage());
  	  	}
    }
    
    private void genresInMoviesTableCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("genres_in_movies_table.csv"))) {
    	
  	      StringBuilder sb = new StringBuilder();
  	      sb.append("genreId,movieId\n");
  	      
  	    //Find id of null genre
  	    String nullId = "";
      	try {
               String query = "SELECT G.id " +
              		 		  "FROM genres G " + 
              		 		  "WHERE G.name = 'NULL';";
               
               PreparedStatement queryStatement = dbcon.prepareStatement(query);
               
               ResultSet queryResults = queryStatement.executeQuery();
               if(queryResults.next()) {
              	 nullId = queryResults.getString("id");
               }
               queryStatement.close();
   			   queryResults.close();
      	}
      	catch(Exception e) {
      		System.out.println("DB error: " + e.getMessage());
      	}
  	      
  	      Iterator<Entry<String, Movie>> it = movieList.entrySet().iterator();
  	      
  	        while (it.hasNext()) {
  	        	Map.Entry<String, Movie> pair = (Map.Entry<String, Movie>)it.next();
  	        	if(pair.getValue().getGenresList().size() == 0) {
  	        		sb.append('"' + nullId + '"' + ',');
    	            sb.append('"' + pair.getKey() + '"' + ',');
    	            sb.append('\n');
  	        	}
  	        	else {
	  	        	for(Genre genre : pair.getValue().getGenresList()) {
		    	            sb.append('"' + genre.getId() + '"' + ',');
		    	            sb.append('"' + pair.getKey() + '"' + ',');
		    	            sb.append('\n');
	  	        	}
  	        	}
  	        }

  	      writer.write(sb.toString());

  	      System.out.println("done writing to genres_in_movies_table.csv!");
    	} 
	  	catch (FileNotFoundException e) {
	  		System.out.println(e.getMessage());
	  	}
    }
    
    private void ratingsTableCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("ratings_table.csv"))) {

  	      StringBuilder sb = new StringBuilder();
  	      sb.append("movieId,rating,numVotes,\n");
  	      
  	      Iterator<Entry<String, Movie>> it = movieList.entrySet().iterator();
  	        while (it.hasNext()) {
  	        	Map.Entry<String, Movie> pair = (Map.Entry<String, Movie>)it.next();
  	            sb.append('"' + pair.getKey() + '"' + ',');
  	            sb.append("0" + ',');
  	            sb.append("0" + ',');
  	            sb.append('\n');
  	        }

  	      writer.write(sb.toString());

  	      System.out.println("done writing to ratings_table.csv!");
    	} 
	  	catch (FileNotFoundException e) {
	  		System.out.println(e.getMessage());
	  	}
    }
    
    private void writeStarListToCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("stars_table.csv"))) {

  	      StringBuilder sb = new StringBuilder();
  	      sb.append("id,name,birthYear\n");
  	      
  	      Iterator<Entry<String, Star>> it = starList.entrySet().iterator();
  	        while (it.hasNext()) {
  	        	Map.Entry<String, Star> pair = (Map.Entry<String, Star>)it.next();
  	        	if(pair.getKey().equals("")) {
  	        		System.out.println(pair.getValue().getId());
  	        		System.out.println(pair.getValue().getId());
  	        		System.out.println(pair.getValue().getId());
  	        		System.out.println(pair.getValue().getId());
  	        		
  	        	}
  	            sb.append('"' + pair.getValue().getId() + '"' + ',');
  	            sb.append('"' + pair.getKey() + '"' + ',');
  	            if (pair.getValue().getBirthYear() > 0) {
  	            	sb.append('"' + Integer.toString(pair.getValue().getBirthYear()) + '"' + ',');
  	            }
  	            else {
  	            	sb.append('"' + "NULL" + '"' + ',');
  	            }
  	            sb.append('\n');
  	        }

  	      writer.write(sb.toString());

  	      System.out.println("done writing to stars_table.csv!");
  	    } 
  	 catch (FileNotFoundException e) {
  		 System.out.println(e.getMessage());
  	 }
    }
    
    private void writeGenreListToCSV() {
    	try (PrintWriter writer = new PrintWriter(new File("genres_table.csv"))) {

    	      StringBuilder sb = new StringBuilder();
    	      sb.append("id,name\n");
    	      Iterator<Entry<String, Genre>> it = genreList.entrySet().iterator();
    	        while (it.hasNext()) {
    	        	Map.Entry<String, Genre> pair = (Map.Entry<String, Genre>)it.next();
    	            sb.append('"' + pair.getValue().getId()+ '"' + ',');
    	            sb.append('"' + pair.getKey() + '"' + ',');
    	            sb.append('\n');
    	        }

    	      writer.write(sb.toString());

    	      System.out.println("done writing to genres_table.csv!");
    	    } 
    	 catch (FileNotFoundException e) {
    		 System.out.println(e.getMessage());
    	 }
    }
    
    private void addToDatabase() {
    	System.out.println("\nstarting to load data into database...");
    	HashMap<String, String> csvToTable = new HashMap<String, String>();
    	csvToTable.put("movies_table.csv","movies");
    	csvToTable.put("stars_table.csv","stars");
    	csvToTable.put("genres_table.csv","genres");
    	try {
            String query = "LOAD DATA LOCAL INFILE ? INTO TABLE $tableName " +
                		   "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                		   "LINES TERMINATED BY '\\n' " +
                		   "IGNORE 1 LINES; ";
            
            String replacedQuery;
            PreparedStatement queryStatement = null;
            Iterator<Entry<String, String>> it = csvToTable.entrySet().iterator();
  	        while (it.hasNext()) {
  	        	Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next(); 	 
  	        	System.out.println("Loading " + pair.getKey() + " into " + pair.getValue() + " table.");
  	        	
  	        	replacedQuery = query.replace("$tableName", pair.getValue());
  	        	queryStatement = dbcon.prepareStatement(replacedQuery);
  	        	queryStatement.setString(1, pair.getKey());
  	        	queryStatement.executeQuery();
  	        }
  	        csvToTable.clear();
  	        csvToTable.put("genres_in_movies_table.csv","genres_in_movies");
  	        csvToTable.put("stars_in_movies_table.csv","stars_in_movies");
  	        csvToTable.put("ratings_table.csv","ratings");
  	        it = csvToTable.entrySet().iterator();
	  	    while (it.hasNext()) {
	  	    	Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next(); 	 
		        System.out.println("Loading " + pair.getKey() + " into " + pair.getValue() + " table.");
		        	
		        replacedQuery = query.replace("$tableName", pair.getValue());
		        queryStatement = dbcon.prepareStatement(replacedQuery);
		        queryStatement.setString(1, pair.getKey());
		        queryStatement.executeQuery();
		   }
  	       
  	      System.out.println("done loading data into database!");
  	      queryStatement.close();
    	}
        catch(Exception e) {
    		System.out.println("DB error: " + e.getMessage());
    	}
    
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of Movie
            tempMovie = new Movie();
            tempMovie.setDirector(directorName);
        }
        else if (qName.equalsIgnoreCase("actor")) {
        	//reset
        	tempStar = new Star();
        }
        else if (qName.equalsIgnoreCase("a")) {
        	//reset
        	tempStar = new Star();
        }
        else if (qName.equalsIgnoreCase("dirname")) {
        	//reset
        	directorName = "";
        }
        else if (qName.equalsIgnoreCase("cats")) {
        	//reset
        	tempGenresList.clear();
        }
        else if (qName.equalsIgnoreCase("cat")) {
        	//reset
        	tempGenre = new Genre();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
    	
        if (qName.equalsIgnoreCase("film")) {
        	if(tempMovie.getTitle().trim().isEmpty()) {
        		System.out.println("mains243.xml Inconsistency: "+ '"' + tempMovie.getTitle() + '"' + " is not a valid movie title. Will not add movie to movieList.");
        	}
        	if(!tempMovie.getId().trim().isEmpty() && !tempMovie.getTitle().trim().isEmpty()) {
	        	tempMovie.setRating(0);
	        	//Check if movie already exists in db
	        	try {
	                 String query = "SELECT count(*) as count " +
	                		 		"FROM movies M " + 
	                		 		"WHERE M.title = ? AND M.year = ? AND M.director = ?;";
	                 
	                 PreparedStatement queryStatement = dbcon.prepareStatement(query);
	                 queryStatement.setString(1, tempMovie.getTitle());
	                 queryStatement.setString(2, Integer.toString(tempMovie.getYear()));
	                 queryStatement.setString(3, tempMovie.getDirector());
	                 
	                 ResultSet queryResults = queryStatement.executeQuery();
	                 if(queryResults.next()) {
	                	 String count = queryResults.getString("count");
	                	 if (Integer.parseInt(count) == 0) {
	                		 movieList.put(tempMovie.getId(), tempMovie);
	                	 }
	                 }
	                 queryStatement.close();
	                 queryResults.close();
	        	}
	        	catch(Exception e) {
	        		System.out.println("DB error: " + e.getMessage());
	        	}
        	}

        } else if (qName.equalsIgnoreCase("dirname")) {
            directorName = tempVal;
            
        } else if (qName.equalsIgnoreCase("cat")) {
        	if(!tempVal.trim().isEmpty()) {
	        	tempGenre.setName(tempVal.trim());
	 
	        	//Check if genre already exists in db
	        	try {
	                 String query = "SELECT count(*) as count " +
	                		 		"FROM genres G " + 
	                		 		"WHERE G.name = ?" + ";";
	                 
	                 PreparedStatement queryStatement = dbcon.prepareStatement(query);
	                 queryStatement.setString(1, tempGenre.getName());
	                 ResultSet queryResults = queryStatement.executeQuery();
	                 if(queryResults.next()) {
	                	 String count = queryResults.getString("count");
	                	 if (Integer.parseInt(count) == 0) {
	                		 //System.out.println("COUNT " + count);
	                	 	 //Generate new genre id
	                		 if(!genreList.containsKey(tempGenre.getName())) { //if genre is not in genreList
		                		 maxGenreIdNum += 1;
		                		 String newGenreId = Integer.toString(maxGenreIdNum);
		                		 tempGenre.setId(newGenreId);
		                		 genreList.put(tempGenre.getName(), tempGenre);
		                		 tempGenresList.add(tempGenre);
	                		 }
	                		 else {
	                			 String genreId = genreList.get(tempGenre.getName()).getId();
	                			 tempGenre.setId(genreId);
	                			 tempGenresList.add(tempGenre);
	                		 }
	                	 }
	                	 else { //genre already exists in db
	                		 query = "SELECT id " +
		                		 	 "FROM genres G " + 
		                		 	 "WHERE G.name = ?" + ";";
	                		 queryStatement = dbcon.prepareStatement(query);
	    	                 queryStatement.setString(1, tempGenre.getName());
	    	                 queryResults = queryStatement.executeQuery();
	    	                 if(queryResults.next()) {
	    	                	 tempGenre.setId(queryResults.getString("id"));
	    	                	 tempGenresList.add(tempGenre);
	    	                 }
	                	 }
	                 }
	                 queryResults.close();
	                 queryStatement.close();
		         } catch (Exception e) {
		        	 System.out.println("DB error: " + e.getMessage());
		         }
        	}
        	
        } else if(qName.equalsIgnoreCase("cats")) {
        	tempMovie.setGenresList(tempGenresList);
        	
        } else if (qName.equalsIgnoreCase("fid")) {
        	tempMovie.setId(tempVal);
        	
        } else if (qName.equalsIgnoreCase("filmed")) {
        	tempMovie.setId(tempVal);
        		
        } else if (qName.equalsIgnoreCase("t")) {
        	tempMovie.setTitle(tempVal);
        	
        } else if (qName.equalsIgnoreCase("f")) {
        	tempFilmId = tempVal;
        	
        } else if (qName.equalsIgnoreCase("stagename")) {
        	tempStar.setName(tempVal);
        	
        } else if (qName.equalsIgnoreCase("dob")) {
        	try {
        	tempStar.setBirthYear(Integer.parseInt(tempVal));
        	}
        	catch(Exception e) {
        		//System.out.println("Error: " + e);
        		System.out.print("actors63.xml Inconsistency: "+ '"' +  tempVal + '"' + " is an invalid birth year for star Name: " + tempStar.getName()+ ". Will set to 0 instead\n");
        		tempStar.setBirthYear(0);
        	}
        	
        } else if (qName.equalsIgnoreCase("actor")) {
    	 	 //Generate new star id
    		 maxStarIdNum += 1;
    		 String newStarId = "nm" + Integer.toString(maxStarIdNum);
    		 tempStar.setId(newStarId);
    		 starList.put(tempStar.getName(), tempStar);

        } else if (qName.equalsIgnoreCase("a")) {
        	tempStar.setName(tempVal);
        
        	Star star = starList.get(tempStar.getName()); //get star obj to add to movieList
    		if(star == null) { //if star is not in starList from actors.xml, add star to starList anyway
    			//System.out.println("casts124.xml Inconsistency: Cannot find " + tempStar.getName() + " in actors63.xml. Will not link actor to movie." );
    			//Generate new star id
    			
    			if(!tempVal.trim().equals("")) {
    				maxStarIdNum += 1;
        			String newStarId = "nm" + Integer.toString(maxStarIdNum);
        			tempStar.setId(newStarId);
	   			    star = tempStar;
		   			if(!star.getName().trim().equals("s a") || !star.getName().trim().equals("")) {   
		   				starList.put(star.getName(), star);
		   			}
    			}
    		}
    		
    		if(star!= null && (!star.getName().trim().equals("s a") || !star.getName().trim().equals(""))) {
        		try {
            		movieList.get(tempFilmId).addStar(star);
            	}
            	catch(Exception e) {
            		//System.out.println("Error: " + e);
            		System.out.println("casts124.xml Inconsistency: Cannot find movie id: " + tempFilmId + " in mains243.xml. Will not connect actor named: " + tempStar.getName() + " to the movie.");
            	}
    		}
  
        } else if (qName.equalsIgnoreCase("year")) {
        	try {
        		tempMovie.setYear(Integer.parseInt(tempVal.trim()));
        	}
        	catch(NumberFormatException e) {
        		//System.out.println("Exception Message: " + e);
        		System.out.println("mains243.xml Inconsistency: Movie Year " + '"' +tempVal + '"' + " is not an integer. Will set to 0 instead.");
        		tempMovie.setYear(0);
        	}
        }
    }
    
    
	public static void main(String[] args) {
		MovieSAXParser msp;
		try {
			msp = new MovieSAXParser();
			msp.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
