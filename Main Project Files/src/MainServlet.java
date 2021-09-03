import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
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
import java.sql.Statement;

// Declaring a WebServlet called MainServlet, which maps to url "/api/main"
@WebServlet(name = "MainServlet", urlPatterns = "/api/main")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            Statement top20statement = dbcon.createStatement();
            
            // Queries 
            String top20query = "SELECT M.id as movieId, M.title, M.year, M.director, R.rating\r\n" + 
			            		"FROM movies M, ratings R\r\n" + 
			            		"WHERE M.id = R.movieId\r\n" + 
			            		"ORDER BY R.rating DESC\r\n" + 
			            		"LIMIT 20\r\n" + //number of results per page
			            		"OFFSET 0;"; //based on the page number user is currently on, offset = currPage * resultsPerPage
            
            // Selects all stars in specified movie
            String allStarsQuery = "SELECT SIM.movieId, SIM.starId, S.name\r\n" + 
				            	   "FROM stars S, stars_in_movies SIM\r\n" + 
				            	   "WHERE ? = SIM.MovieId AND S.id = SIM.starId;";
            
            // Selects all genres in a specified movie
            String allGenresQuery = "SELECT G.name, G.id\r\n" + 
            						"FROM genres G, genres_in_movies GIM\r\n" + 
            						"WHERE ? = GIM.movieId AND G.id = GIM.genreId;";
            
            ResultSet top20results = top20statement.executeQuery(top20query);
            
            ((User)request.getSession().getAttribute("user")).saveBrowsingURL("main.html");
            
            JsonArray jsonArray = new JsonArray();
           
            while (top20results.next()) {
            	////////// ADD MOVIE INFO ///////////
            	String movie_id = top20results.getString("movieId");
                String movie_title = top20results.getString("title");
                String movie_year = top20results.getString("year");
                String movie_director = top20results.getString("director");
                Float movie_rating = top20results.getFloat("rating");
                
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
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            top20results.close();
            top20statement.close();
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
