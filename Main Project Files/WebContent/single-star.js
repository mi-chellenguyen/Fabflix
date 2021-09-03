
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

    console.log("handleResult: populating movie table from resultData");

    let returnHomeContainerElement = jQuery("#title-container");
    
    let redirectURL = "";
    let redirectLocation = "";
    if (resultData[0].localeCompare("main.html") == 0)
	{
    	redirectURL = "main.html";
    	redirectLocation = "Main"
	}
    else 
	{
    	redirectURL = "movie-list.html?" + resultData[0];
    	redirectLocation = "Search Results";
	}
    
    returnHomeContainerElement.append('<a id="returnToBrowse" class="returnToBrowse" href="' + redirectURL + '">Back to ' + redirectLocation + '</a>');
    
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    let cardHTML = "";
	cardHTML += '<div class="card text-center text-dark border-danger mb-3" style="width: 24rem; display: inline-block; margin: 15px">' +
    				'<div class="card-header"><b>' + resultData[1]["star_name"] +'</b></div>' +
    				'<ul class="text-left list-group list-group-flush">' +
    					'<li class="list-group-item">' + '<b>Date of Birth: </b>' + resultData[1]["star_dob"] + '</li>';
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 1; i < Math.min(11, resultData.length); i++) {
		cardHTML += '<li class="list-group-item">' + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
						 + resultData[i]["movie_title"] + '</li>';    // display movie_title for the link text
    }
    cardHTML += '</ul></div>';
    movieTableBodyElement.append(cardHTML); 
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});