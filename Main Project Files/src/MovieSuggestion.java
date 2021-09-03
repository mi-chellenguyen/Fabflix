import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    public MovieSuggestion() {
        super();
    }

    /*
     * 
     * Match the query against movie titles and return a JSON response.
     * 
     * The JSON response should look like this:
     * [
     * 	{ "value": "Movie Title", "data": { "movieId": tt1234 } },
     * 	{ "value": "Title Movie", "data": { "movieId": tt1235 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information (just stores movie id for now).
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			
			// search db using fulltext (not sure if properly implemented) search and add the results to JSON Array
			String prefixQuery = query + "*";
			
			String queryString = "SELECT id, title \n" + 
								 "FROM movies \n" + 
								 "WHERE MATCH (title) AGAINST ( ? IN BOOLEAN MODE) LIMIT 10;";
			
			 try {
	            Connection dbcon = dataSource.getConnection();
				PreparedStatement preparedQueryString = dbcon.prepareStatement(queryString);
				
				preparedQueryString.setString(1, prefixQuery);
				
				ResultSet results = preparedQueryString.executeQuery();
				
				
				while(results.next()) {
					String id = results.getString("id");
					String title = results.getString("title");
					
					jsonArray.add(generateJsonObject(id, title));
				}
			 }
			 catch (Exception e) {
		        	// write error message JSON object to output
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("errorMessage", e.getMessage());
					response.getWriter().write(jsonObject.toString());
					// set response status to 500 (Internal Server Error)
					response.setStatus(500);
				}
			
			response.getWriter().write(jsonArray.toString());
			return;
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object to be like this format:
	 * {
	 *   "value": "Movie Title",
	 *   "data": { "movieId": tt12345 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String id, String title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", title);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movieId", id);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}
}