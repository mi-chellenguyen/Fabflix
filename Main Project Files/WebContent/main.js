/**
 * Handle the items in item list 
 * @param resultDataString jsonObject, needs to be parsed to html 
 */
function handleCartArray(resultDataString) {
    const resultArray = resultDataString.split(",");
    console.log(resultArray);
    
    // change it to html list
    let res = "<ul>";
    for(let i = 0; i < resultArray.length; i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";   
    }
    res += "</ul>";
    
    // clear the old array and show the new array in the frontend
    $("#item_list").html("");
    $("#item_list").append(res);
}


function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    
    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {

    	let cardHTML = "";
        cardHTML += '<div class="card text-left text-dark border-danger mb-3" style="width: 18rem; display: inline-block; margin:15px;">' +
                        '<div class="card-header" style="display: flex;">' +
                            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '" style="width: 18rem">' +
                                '<b>' + resultData[i]["movie_title"] + '</b>' +    // display movie_title for the link text
                            '</a>' +
                            '<form id="add-to-cart-form" action="cart.html" method="GET">' +
                            	'<input id="task" type="hidden" name="task" value="add">' +
	                            '<input id="movie_id" type="hidden" name="movie_id" value="' + resultData[i]["movie_id"] + '"/>'+
				    			'<input id="quantity" type="hidden" name="quantity" value="1"/>'+
				    			'<div style="text-align: right">'+
				    				'<button id="addToCartBtn" class="fas fa-cart-plus addToCartBtn" type="submit" value=""></button>'+
				    			'</div>'+
			    			'</form>' + 
                        '</div>' +
                        '<ul class="text-left list-group list-group-flush">' +
                    		'<li class="list-group-item">' + '<b>ID: </b>' + resultData[i]["movie_id"] + '</li>' +
                        	'<li class="list-group-item">' + '<b>Rating: </b>' + resultData[i]["movie_rating"] + '</li>' +
                            '<li class="list-group-item">' + '<b>Director: </b>' + resultData[i]["movie_director"] + '</li>' +
                            '<li class="list-group-item">' + '<b>Release Year: </b>' + resultData[i]["movie_year"] + '</li>' +
                            '<li class="list-group-item">' + '<b>Genres: </b>' + '<a href="movie-list.html?genreName=' + resultData[i].genres[0].genre_name + 
                            '&sort-by=title_asc' + 
                            '&page=1'+
                            '&movies-per-page=10' + '">' 
			             	+ resultData[i].genres[0].genre_name + '</a>';
        
						        for (let genre = 1, l = resultData[i].genres.length; genre < l; genre++) {
						            cardHTML +=  '<a href="movie-list.html?genreName=' + resultData[i].genres[genre].genre_name + 
						            '&sort-by=title_asc' + 
		                            '&page=1'+
		                            '&movies-per-page=10' + '">' +
						            ', ' +resultData[i].genres[genre].genre_name + '</a>';
						         }
						       
	        cardHTML += 	'</li>' +
		                    '<li class="list-group-item">'+ '<b>Stars: </b>';
		
		                         for (let star = 0, l = resultData[i].stars.length; star < l; star++) {
		                            cardHTML +=  '<br><a href="single-star.html?id=' + resultData[i].stars[star].star_id + '">' 
		                             	+ resultData[i].stars[star].star_name + '</a>';
		                         }
		                         
		    cardHTML +=     '</li>' +
		                '</ul>' +
		            '</div>';
        movieTableBodyElement.append(cardHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/main", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

/*
 * This function is called by the library when it needs to lookup a query.
 * 
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")	
	// TODO: if you want to check past query results first, you can do it here
	console.log("checking if query is in suggestions cache")
	
	if (query in suggestionsCache)
	{
		console.log("found query in suggestions cache")
		handleLookupAjaxSuccess(suggestionsCache[query], query, doneCallback) 

	}
	else
	{
		console.log("query not in suggestions cache. sending AJAX request to backend Java Servlet.")
		
		// sending the HTTP GET request to the Java Servlet endpoint movie-suggestion
		// with the query data
		jQuery.ajax({
			"method": "GET",
			// generate the request url from the query.
			// escape the query string to avoid errors caused by special characters 
			"url": "movie-suggestion?query=" + escape(query),
			"success": function(data) {
				// pass the data, query, and doneCallback function into the success handler
				handleLookupAjaxSuccess(data, query, doneCallback) 
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
}


var suggestionsCache = {};

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * 
 * data is the JSON data string you get from your Java Servlet
 * 
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
	console.log("lookup ajax successful")
	
	// parse the string into JSON
	var jsonData = JSON.parse(data);
	console.log(jsonData)
	console.log(query);
	
	// TODO: if you want to cache the result into a global variable you can do it here
	suggestionsCache[query] = data;
	
	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
	// TODO: jump to the specific result page based on the selected suggestion
	
	console.log("you selected " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"]);	
	
	// should go straight to movie-list page as suggestion["value"] as search query.
	console.log("redirecting to selected suggested movie");

	var url = "movie-list.html?title=" + suggestion["value"] + "&year=&director=&star-firstName=&star-lastName=&sort-by=title_asc&page=1&movies-per-page=10";
	window.location.replace(url);
		
}


/*
 * This statement binds the autocomplete library with the input box element and 
 *   sets necessary parameters of the library.
 * 
 * The library documentation can be find here: 
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 * 
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    
    // set minimum number of characters required to trigger autosuggest
    minChars: 3,
    
    // set number of maximum results to display for local lookup
    // do this on server side!!
});



/*
 * do normal full text search if no suggestion is selected 
 */
function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
	
	console.log("redirecting to normal search query");

	var url = "movie-list.html?title=" + escape(query) + "&year=&director=&star-firstName=&star-lastName=&sort-by=title_asc&page=1&movies-per-page=10";
	
	//set input box to query value
	document.getElementById('auto-complete').value(query);
	
	//click submit button programmatically 
	document.getElementById('search-submit').submit();
	
	window.location.replace(url);
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})