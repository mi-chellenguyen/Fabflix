import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

/**
 * Servlet implementation class AddMovieServlet
 */
@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String title = request.getParameter("title");
		Integer year = Integer.valueOf(request.getParameter("year"));
		String director = request.getParameter("director");
		String single_star_name = request.getParameter("single-star-name");
		String single_genre = request.getParameter("single-genre");
		Float rating;
		Integer num_votes;
		
		if (request.getParameter("rating") == "")
		{
			rating = Float.parseFloat("0");
		}
		else {
			rating = Float.parseFloat(request.getParameter("rating"));
		}
		
		if (request.getParameter("num-votes") == "")
		{
			num_votes = 0;
		}
		else {
			num_votes = Integer.valueOf(request.getParameter("num-votes"));
		}
		
		 // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try {
        	// Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            CallableStatement addMovieStatement = dbcon.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?, ?, ?)}");

            addMovieStatement.setString(1, title);
            addMovieStatement.setInt(2, year);
            addMovieStatement.setString(3, director);
            addMovieStatement.setString(4, single_star_name);
            addMovieStatement.setString(5, single_genre);
            addMovieStatement.setFloat(6, rating);
            addMovieStatement.setInt(7, num_votes);
            addMovieStatement.registerOutParameter(8, Types.VARCHAR);
            addMovieStatement.execute();
            
            String add_movie_results = addMovieStatement.getString(8);

            if (add_movie_results.equals("Added movie successfully"))
            {
            	JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "success");
	            responseJsonObject.addProperty("message", "success");

	            response.getWriter().write(responseJsonObject.toString());
            }
            else // Results == 'Movie already exists'
            {
            	JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            responseJsonObject.addProperty("message", "Movie already exists in database!");
	            response.getWriter().write(responseJsonObject.toString());
            }
            
            response.setStatus(200);
            addMovieStatement.close();
            dbcon.close();
        }
        catch (Exception e) {
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
