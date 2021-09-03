/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    let returnHomeContainerElement = jQuery("#title-container");
    
    let redirectURL = "";
    let redirectLocation = "";
    if (resultData["prevBrowsingUrl"].localeCompare("main.html") == 0)
	{
    	redirectURL = "main.html";
    	redirectLocation = "Main"
	}
    else 
	{
    	redirectURL = "movie-list.html?" + resultData["prevBrowsingUrl"];
    	redirectLocation = "Search Results";
	}
    
    returnHomeContainerElement.append('<a id="returnToBrowse" class="returnToBrowse" href="' + redirectURL + '">Back to ' + redirectLocation + '</a>');

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<h5 align=center><i>Movie Name: " + resultData["movie_title"] + "</i></h5>");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    
    let cardHTML = "";
    cardHTML += '<div class="card text-left text-dark border-danger mb-3" style="width: 18rem; display: inline-block; margin:15px;">' +
                    '<div class="card-header" style="display: flex;">' +
                        '<a href="single-movie.html?id=' + resultData['movie_id'] + '" style="width: 18rem">' +
                            '<b>' + resultData["movie_title"] + '</b>' +    // display movie_title for the link text
                        '</a>' +
                        '<form id="add-to-cart-form" action="cart.html" method="GET">' +
                        	'<input id="task" type="hidden" name="task" value="add">' +
                            '<input id="movie_id" type="hidden" name="movie_id" value="' + resultData["movie_id"] + '"/>'+
			    			'<input id="quantity" type="hidden" name="quantity" value="1"/>'+
			    			'<div style="text-align: right">'+
			    				'<button id="addToCartBtn" class="fas fa-cart-plus addToCartBtn" type="submit" value=""></button>'+
			    			'</div>'+
		    			'</form>' + 
                    '</div>' +
                    '<ul class="text-left list-group list-group-flush">' +
                		'<li class="list-group-item">' + '<b>ID: </b>' + resultData["movie_id"] + '</li>' +
                    	'<li class="list-group-item">' + '<b>Rating: </b>' + resultData["movie_rating"] + '</li>' +
                        '<li class="list-group-item">' + '<b>Director: </b>' + resultData["movie_director"] + '</li>' +
                        '<li class="list-group-item">' + '<b>Release Year: </b>' + resultData["movie_year"] + '</li>' +
                        '<li class="list-group-item">' + '<b>Genres: </b>' + '<a href="movie-list.html?genreName=' + resultData.genres[0].genre_name + 
                        '&sort-by=title_asc' + 
                        '&page=1'+
                        '&movies-per-page=10' + '">' 
		             	+ resultData.genres[0].genre_name + '</a>';
    
					        for (let genre = 1, l = resultData.genres.length; genre < l; genre++) {
					            cardHTML +=  '<a href="movie-list.html?genreName=' + resultData.genres[genre].genre_name + 
					            '&sort-by=title_asc' + 
	                            '&page=1'+
	                            '&movies-per-page=10' + '">' +
					            ', ' +resultData.genres[genre].genre_name + '</a>';
					         }
					       
        cardHTML += 	'</li>' +
	                    '<li class="list-group-item">'+ '<b>Stars: </b>';
	
	                         for (let star = 0, l = resultData.stars.length; star < l; star++) {
	                            cardHTML +=  '<br><a href="single-star.html?id=' + resultData.stars[star].star_id + '">' 
	                             	+ resultData.stars[star].star_name + '</a>';
	                         }
	                         
	    cardHTML +=     '</li>' +
	                '</ul>' +
	            '</div>';
    movieTableBodyElement.append(cardHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});