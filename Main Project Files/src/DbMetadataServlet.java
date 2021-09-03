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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Servlet implementation class DbMetadataServlet
 */
@WebServlet(name = "DbMetadataServlet", urlPatterns = "/api/db-metadata")
public class DbMetadataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
        response.setContentType("application/json"); // Response mime type

		// Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try {
        	
        	// Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            DatabaseMetaData dbmd = dbcon.getMetaData();

            String[] tables = {"TABLE"};
            ResultSet tablesResults = dbmd.getTables(null, null, null, tables);
            
            JsonArray jsonArray = new JsonArray();
            while (tablesResults.next()) {
            	String table_name = tablesResults.getString("TABLE_NAME");
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("table_name", table_name.toUpperCase());
            	
            	ResultSet columnResults = dbmd.getColumns(null, null, table_name, null); 
            	
            	JsonArray columns = new JsonArray();
            	while (columnResults.next()) {
            		String col = columnResults.getString("COLUMN_NAME") + " "
                            	 + columnResults.getString("TYPE_NAME");
            		
            		columns.add(col);
                }
            	columnResults.close();

            	jsonObject.add("columns", columns);
            	jsonArray.add(jsonObject);
            }
           
            tablesResults.close();
            
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
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
