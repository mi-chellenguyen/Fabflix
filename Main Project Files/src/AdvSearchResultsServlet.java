import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * This AdvSearchResultsServlet is declared in the web annotation below, 
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "AdvSearchResultsServlet", urlPatterns = "/api/adv-search-results")
public class AdvSearchResultsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    	String title = "";
    	String year = "";
    	String director = "";
    	String starFirstName = "";
    	String starLastName = "";
    	String genreName = "";
    	String startsWith = "";
    	String orderBy = request.getParameter("sort-by");        	
		String limit = request.getParameter("movies-per-page");
    	String offset = Integer.toString((Integer.parseInt(request.getParameter("page")) - 1) * Integer.parseInt(request.getParameter("movies-per-page")));

    	try {
        	title = request.getParameter("title");
        	year = request.getParameter("year");
        	director = request.getParameter("director");
        	starFirstName = request.getParameter("star-firstName");
        	starLastName = request.getParameter("star-lastName");
        }
        catch (Exception E){
        }
        
    	try {
    		 genreName = request.getParameter("genreName");
    	}
    	catch (Exception E){
    	}
    	
    	try {
    		startsWith = request.getParameter("startsWith");
    	}
    	catch (Exception E) {
    	}
    	
    	// If var == null, change it to ""
		title = ((title == null) ? "" : title); 
		year = ((year == null) ? "" : year); 
		director = ((director == null) ? "" : director); 
		starFirstName = ((starFirstName == null) ? "" : starFirstName); 
		starLastName = ((starLastName == null) ? "" : starLastName); 
		genreName =  ((genreName == null) ? "" : genreName); 
		startsWith = ((startsWith == null) ? "" : startsWith); 

		// Save current url params so that a user can come back to the same browse settings
    	String url = "";
		url += "title=" + title +
				"&year=" + year +
				"&director=" + director +
				"&star-firstName=" + starFirstName +
				"&star-lastName=" + starLastName +
				"&sort-by=" + orderBy +
				"&page=" + request.getParameter("page") +
				"&movies-per-page=" + limit;
		
		((User)request.getSession().getAttribute("user")).saveBrowsingURL(url);;
		
		// Keep values in an ArrayList for easy access later
    	ArrayList<String> searchInfo = new ArrayList<String>();
    	searchInfo.add(title);
    	searchInfo.add(year);
    	searchInfo.add(director);
    	searchInfo.add(starFirstName);
    	searchInfo.add(starLastName);
    	searchInfo.add(genreName);
    	searchInfo.add(startsWith);
    	searchInfo.add(limit);
    	searchInfo.add(offset); 
    	    	    	
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
       
        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            //////////////////Queries ///////////////////            
            String queryToGetMovieInfo = "SELECT M.id as movieId, M.title, M.year, M.director, R.rating\r\n" + 
            							 "FROM movies M, ratings R, stars_in_movies SIM, stars S, genres_in_movies GIM, genres G\r\n" + 
            							 "WHERE M.id = R.movieId AND M.id = SIM.movieId AND M.id = GIM.movieId\r\n" + 
    							 	     "AND GIM.genreId = G.id AND SIM.starId = S.id";
            
            // Selects all stars in specified movie
            String allStarsQuery = "SELECT SIM.movieId, SIM.starId, S.name\r\n" + 
				            	   "FROM stars S, stars_in_movies SIM\r\n" + 
				            	   "WHERE ? = SIM.MovieId AND S.id = SIM.starId;";
            
            // Selects all genres in a specified movie
            String allGenresQuery = "SELECT G.name, G.id\r\n" + 
				            		"FROM genres G, genres_in_movies GIM\r\n" + 
				            		"WHERE ? = GIM.movieId AND G.id = GIM.genreId;";
           
            if (title != "" || startsWith != "")
            {
            	queryToGetMovieInfo += " AND M.title LIKE ?";
            }
            if (year != "")
            {
            	queryToGetMovieInfo += " AND M.year LIKE ?";
            }
            if (director != "")
            {
            	queryToGetMovieInfo += " AND M.director LIKE ?";
            }
            if (starFirstName != "")
            {
            	queryToGetMovieInfo += " AND S.name LIKE ?";
            }
            if (starLastName != "")
        	{
            	queryToGetMovieInfo += " AND S.name LIKE ?";
        	}
            if (genreName != "")
            {
            	queryToGetMovieInfo += " AND ? = G.name";
            }
            queryToGetMovieInfo += "\r\nGROUP BY movieId, M.title, M.year, M.director, R.rating";
            
           ///// Count how many rows are returned from query without limit and offset ////
            PreparedStatement countMovieInfoStatement = dbcon.prepareStatement(queryToGetMovieInfo);  
          	int numStrAddedToQuery = 1;
          	
          	for (int count = 0; count < searchInfo.size() - 2; count++)	// -2 SO WE CAN COUNT W/O LIMIT + OFFSET
          	{
          		if (count == 5)		// GENRE
            	{
                    if (searchInfo.get(count) != "")
                    {
                    	countMovieInfoStatement.setString(numStrAddedToQuery, searchInfo.get(count));
                		numStrAddedToQuery++;
                    }
            	}
          		else if (count == 6)	// STARTSWITH
          		{
                    if (searchInfo.get(count) != "")
                    {
                    	countMovieInfoStatement.setString(numStrAddedToQuery, searchInfo.get(count).toString() + "%");
                		numStrAddedToQuery++;
                    }
          		}
            	else 
        		{
            		if (searchInfo.get(count) != "")	// ALL OTHERS FROM INDEX 0 - 4
                	{
            			countMovieInfoStatement.setString(numStrAddedToQuery, "%" + searchInfo.get(count).toString() + "%");
            			numStrAddedToQuery++;
                	}
               	}	
          	}
          	ResultSet countMovieInfoResults = countMovieInfoStatement.executeQuery();
          
          	int numOfRows = 0;
          	while (countMovieInfoResults.next()) {
          		numOfRows++;
          	}
          	
          	// Add order by, limit, and offset
          	queryToGetMovieInfo += "\r\nORDER BY ";
          	switch(orderBy) {
			case "title_asc":
				queryToGetMovieInfo += "title ASC";
				break;
			case "title_desc":
				queryToGetMovieInfo += "title DESC";
				break;
			case "rating_asc":
				queryToGetMovieInfo += "rating ASC";
				break;
			case "rating_desc":
				queryToGetMovieInfo += "rating DESC";
				break;
			default: 
                System.out.println("no match"); 	
          	}
            queryToGetMovieInfo += "\r\nLIMIT ?";
            queryToGetMovieInfo += "\r\nOFFSET ?;";
            PreparedStatement movieInfoStatement = dbcon.prepareStatement(queryToGetMovieInfo);
            
            numStrAddedToQuery = 1;
            for (int count = 0; count < searchInfo.size(); count++)
            {        	
        		if (count < 5) {	// SET TITLE, YR, DIRECTOR, STAR NAMES
        			if (searchInfo.get(count) != "")
                	{
        				movieInfoStatement.setString(numStrAddedToQuery, "%" + searchInfo.get(count).toString() + "%");
        				numStrAddedToQuery++;
                	}
        		}
        		else if (count == 5) // SET GENRE
            	{
        			if (searchInfo.get(count) != "")
        			{
            			movieInfoStatement.setString(numStrAddedToQuery, searchInfo.get(count));
                		numStrAddedToQuery++;
        			}
            	}
        		else if (count == 6)	// STARTSWITH
          		{
                    if (searchInfo.get(count) != "")
                    {
                    	movieInfoStatement.setString(numStrAddedToQuery, searchInfo.get(count).toString() + "%");
                		numStrAddedToQuery++;
                    }
          		}
        		else { 				// LIMIT AND OFFSET
        			if (searchInfo.get(count) != "")
        			{
            			movieInfoStatement.setLong(numStrAddedToQuery, Integer.parseInt(searchInfo.get(count)));
                		numStrAddedToQuery++;
        			}
        		}
            }
			ResultSet movieInfoResults = movieInfoStatement.executeQuery();
			
			JsonArray jsonArray = new JsonArray();
            while (movieInfoResults.next()) {
            	
            	////////// ADD MOVIE INFO ///////////
            	String movie_id = movieInfoResults.getString("movieId");
                String movie_title = movieInfoResults.getString("title");
                String movie_year = movieInfoResults.getString("year");
                String movie_director = movieInfoResults.getString("director");
                Float movie_rating = movieInfoResults.getFloat("rating");
                
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                
                //////// ADD STARS ///////////
                PreparedStatement allStarsStatement = dbcon.prepareStatement(allStarsQuery);

    			// Set the parameter represented by "?" in the query
    			allStarsStatement.setString(1, movie_id);

    			ResultSet allStarsResults = allStarsStatement.executeQuery();
    			
    			JsonArray stars = new JsonArray();
    			
    			while (allStarsResults.next()) {
	    			String star_id = allStarsResults.getString("starId");
	                String star_name = allStarsResults.getString("name");
	                
	                JsonObject star = new JsonObject();
	                star.addProperty("star_id", star_id);
	                star.addProperty("star_name", star_name);
	                stars.add(star);
    			}
    			jsonObject.add("stars", stars);
    			allStarsResults.close();
                allStarsStatement.close();
    			
    			//////// ADD GENRES ///////////
    			PreparedStatement allGenresStatement = dbcon.prepareStatement(allGenresQuery);

    			// Set the parameter represented by "?" in the query
    			allGenresStatement.setString(1, movie_id);

    			ResultSet allGenresResults = allGenresStatement.executeQuery();
    			
                JsonArray genres = new JsonArray();
                
                while(allGenresResults.next() ) {
                	String genre_id = allGenresResults.getString("id");
                	String genre_name = allGenresResults.getString("name");
                	
	                JsonObject genre = new JsonObject();
	                genre.addProperty("genre_id", genre_id);
	                genre.addProperty("genre_name", genre_name);
	                genres.add(genre);
                }
                jsonObject.add("genres", genres);  
                allGenresResults.close();
                allGenresStatement.close();
                
                jsonArray.add(jsonObject);
            }
            
            // add the number of rows  to jsonObject
            JsonObject numOfRowsJsonObj = new JsonObject();
            numOfRowsJsonObj.addProperty("numOfRows", numOfRows);
            jsonArray.add(numOfRowsJsonObj);
                        
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            movieInfoResults.close();
            movieInfoStatement.close();
            dbcon.close();                     
            
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);

        }
       
        out.close(); 
    }
}
