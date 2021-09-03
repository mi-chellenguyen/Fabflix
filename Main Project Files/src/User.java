import java.util.HashMap;

import javax.management.relation.Role;

/**
 * This User class only has the email field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String email;
    private HashMap<String, Integer> cart; 
    private String searchURL;
    private String role;

    public User(String email) {
        this.email = email;
        this.cart = new HashMap<String, Integer>();
    }
    
    public void setRole(String currRole)
    {
    	role = currRole;
    }
    
    public String getRole()
    {
    	return role;
    }
    
    public void addToCart(String movieId, int quantity) {
    	Integer value = cart.get(movieId);
    	if (value == null) {
	    	   value = 1;
	    	} 
    	else {
	    	   value += 1;
	    	}
    	cart.put(movieId, value);
    }
    
    // item is already in cart and update button is clicked
    public void updateCart(String movieId, int quantity) {
    	if (quantity == 0) {
    		cart.remove(movieId);
    	}
    	else {
    		cart.put(movieId, quantity);
    	}
    }
    
    public void removeFromCart(String movieId) {
    	cart.remove(movieId);
    }
    
    public void saveBrowsingURL(String URL)
    {
    	searchURL = URL;
    }
    
    public String getBrowsingURL()
    {
    	return searchURL;
    }
    
    public void emptyCart()
    {
    	cart.clear();
    }

    public String getEmail() { return this.email; }
    
    public HashMap<String, Integer> getCart() { return this.cart;};
}
