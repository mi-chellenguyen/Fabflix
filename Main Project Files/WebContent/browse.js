function handleGenreResult(resultData) {
    console.log("handleGenreResult: populating genre-hyperlinks-container table from resultData");

    // Populate the genre table
    // Find the empty table body by id "browse-by-genre"
    let genreBodyElement = jQuery("#genre-hyperlinks-container");
    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
    	
        let cardHTML = "";
        cardHTML += '<div class="genre-block" style="display: inline-block;">' +
        				'<a href="movie-list.html?genreName=' + resultData[i] + 
        						'&sort-by=title_asc&page=1&movies-per-page=10' +'">' + resultData[i] + 
        				'</a>' +
        			'</div>';
        
        genreBodyElement.append(cardHTML);
    }
    
    // Add hyperlinked digits 
    let titleBodyElement = jQuery("#title-hyperlinks-container");
    for (let digit_count = 0; digit_count < 10; digit_count++)
	{
    	let cardHTML = "";
        cardHTML += '<div class="title-block" style="display: inline-block;">' +
        				'<a href="movie-list.html?startsWith=' + String.fromCharCode(48 + digit_count) + 
        						'&sort-by=title_asc&page=1&movies-per-page=10' +'">' + String.fromCharCode(48 + digit_count) + 
        				'</a>' +
        			'</div>';
        
        titleBodyElement.append(cardHTML);
	}
    titleBodyElement.append('</br>');
    
    // Add hyperlinked letters 
    for (let letter_count = 0; letter_count < 26; letter_count++)
	{    	
    	 let cardHTML = "";
         cardHTML += '<div class="title-block" style="display: inline-block;">' +
         				'<a href="movie-list.html?startsWith=' + String.fromCharCode(97 + letter_count) + 
         						'&sort-by=title_asc&page=1&movies-per-page=10' +'">' + String.fromCharCode(97 + letter_count) + 
         				'</a>' +
         			'</div>';
         
         titleBodyElement.append(cardHTML);
	}
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse", // Setting request url
    success: (resultData) => handleGenreResult(resultData)
});