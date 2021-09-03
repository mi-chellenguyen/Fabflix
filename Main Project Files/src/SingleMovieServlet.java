
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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();
			
			////////////////// Queries ///////////////////
			String movieDataQuery = "SELECT R.movieId, M.title, M.year, M.director, R.rating\r\n" +
									"FROM movies M, ratings R\r\n" +
									"WHERE M.id = ? AND R.movieId = M.id;";
			
			// Selects all stars in specified movie
            String allStarsQuery = "SELECT SIM.movieId, SIM.starId, S.name\r\n" + 
				            	   "FROM stars S, stars_in_movies SIM\r\n" + 
				            	   "WHERE ? = SIM.MovieId AND S.id = SIM.starId;";
            
            // Selects all genres in a specified movie
            String allGenresQuery = "SELECT G.name, G.id\r\n" + 
				            		"FROM genres G, genres_in_movies GIM\r\n" + 
				            		"WHERE ? = GIM.movieId AND G.id = GIM.genreId;";
            
            
			// Declare our statement
			PreparedStatement movieDataStatement = dbcon.prepareStatement(movieDataQuery);

			// Set the parameter represented by "?" in the query to the id we get from url
			movieDataStatement.setString(1, id);
			
			// Perform the query
			ResultSet movieDataResults = movieDataStatement.executeQuery();
			
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("prevBrowsingUrl", ((User)request.getSession().getAttribute("user")).getBrowsingURL());
			
			if(movieDataResults.next()) {
				String movie_id = movieDataResults.getString("movieId");
	            String movie_title = movieDataResults.getString("title");
	            String movie_year = movieDataResults.getString("year");
	            String movie_director = movieDataResults.getString("director");
	            Float movie_rating = movieDataResults.getFloat("rating");
	            
	            jsonObject.addProperty("movie_id", movie_id);
	            jsonObject.addProperty("movie_title", movie_title);
	            jsonObject.addProperty("movie_year", movie_year);
	            jsonObject.addProperty("movie_director", movie_director);
	            jsonObject.addProperty("movie_rating", movie_rating);
	            
	            /////// ADD STARS /////////
	            PreparedStatement allStarsStatement = dbcon.prepareStatement(allStarsQuery);
	            allStarsStatement.setString(1, movie_id);
	            
	            ResultSet allStarsResults = allStarsStatement.executeQuery();
	            
	            JsonArray stars = new JsonArray();
	            
	            while(allStarsResults.next()) {
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
	            
	            /////// ADD GENRES /////////
	            PreparedStatement allGenresStatement = dbcon.prepareStatement(allGenresQuery);
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
	            allGenresResults.close();
	            allGenresStatement.close();
	            jsonObject.add("genres", genres);        
			}
            // write JSON string to output
            out.write(jsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			movieDataResults.close();
			movieDataStatement.close();
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
