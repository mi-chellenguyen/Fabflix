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
// uses local storage to remember options selected 
localStorage.setItem("currPage", getParameterByName("page"));
localStorage.setItem("resultsPerPage", getParameterByName("movies-per-page"));
localStorage.setItem("sortBy", getParameterByName("sort-by"));

//assign onclick handler function to each sort-by radio button
var sortByOptions = document.forms["sort-by-form"].elements["sort-by"];
for (var index = 0, len=sortByOptions.length; index < len; index++) {
	sortByOptions[index].onclick = function() {
		//alert(this.value)
		if ('URLSearchParams' in window) {
		    var searchParams = new URLSearchParams(window.location.search);
		    searchParams.set("sort-by", this.value);
		    window.location.search = searchParams.toString();
		}
 	};
}

function onClickPrev() {
	if ('URLSearchParams' in window) {
	    var searchParams = new URLSearchParams(window.location.search);
	    searchParams.set("page", parseInt(localStorage.getItem("currPage")) - 1);
	    window.location.search = searchParams.toString();
	}
}

function onClickNext() {
	if ('URLSearchParams' in window) {
	    var searchParams = new URLSearchParams(window.location.search);
	    searchParams.set("page", parseInt(localStorage.getItem("currPage")) + 1);
	    window.location.search = searchParams.toString();
	}
}

function onChangeMoviesPerPage() {
	localStorage.setItem("numOfPages",  Math.ceil(localStorage.getItem("numOfResults")/localStorage.getItem("resultsPerPage")));
	
	if ('URLSearchParams' in window) {
	    var searchParams = new URLSearchParams(window.location.search);
	    searchParams.set("movies-per-page", document.getElementById( "movies-per-page" ).value);
	    searchParams.set("page", 1);
	    window.location.search = searchParams.toString();
	}
	//alert("Results per page is now: " + localStorage.getItem("resultsPerPage") + "\nNumber of pages should now be: " + localStorage.getItem("numOfPages"));
}

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    
    var numOfResults = resultData[resultData.length - 1]["numOfRows"];
    document.querySelector('.num-of-results').innerHTML = numOfResults + " result(s) found";

    document.getElementById(localStorage.getItem("sortBy")).checked = true;
      
    localStorage.setItem("numOfPages", Math.ceil(numOfResults / parseInt(localStorage.getItem("resultsPerPage"))));
    //alert("Current Page: " + localStorage.getItem("currPage") + ' / ' + localStorage.getItem("numOfPages"));
    document.querySelector('.page').innerHTML = localStorage.getItem("currPage") + ' / ' + localStorage.getItem("numOfPages");
       
	if (parseInt(localStorage.getItem("currPage")) < parseInt(localStorage.getItem("numOfPages"))) {
		document.querySelector( '.next' ).disabled = false;
	}
	else {
		document.querySelector('.next').disabled = true;
	}
	
	if (parseInt(localStorage.getItem("currPage")) <= 1) {
		document.querySelector( '.prev' ).disabled = true;
	}
	else {
		document.querySelector( '.prev' ).disabled = false;
	}
	
	
    document.getElementById("movies-per-page").value = localStorage.getItem("resultsPerPage");
   
    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length - 1; i++) {
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
				    				'<button id="addToCartBtn" class="button fas fa-cart-plus addToCartBtn" type="submit" value=""></button>'+
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
	 
//Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/adv-search-results", // Setting request url, which is mapped by MoviesServlet in Movies.java
    data: location.search.substring(1),
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});