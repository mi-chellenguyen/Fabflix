function handleCartResult(resultData) {
	let cartTableBodyElement = jQuery("#cart_table_body");
	
	 for (let i = 0; i < resultData.length; i++) {
		 console.log(resultData[i]["movieId"] + " " + resultData[i]["quantity"]);
		 let itemHTML = '';
		 itemHTML = '<div class="item">' +
         				'<div class="title-box">' +
         					'<h5>'+ resultData[i]["title"] +'</h5>' +
         				'</div>' +
         				'<div class="quantity">' +
							'<form id= quantity-form action="cart.html" method="GET">' +
								'<input id="task" type="hidden" name="task" value="update">' +
								'<input id="movieId" type="hidden" name="movie_id" value="' + resultData[i]["movieId"] + '">' +
								'<input class="quantity" type="number" name="quantity" min="0" value="' + resultData[i]["quantity"] + '">' +
								'<input id="submitBtn" class="updateBtn" type="submit" value="Update">' +
							'</form>' +
						'</div>' +
						'<div class="delete-container">' +
							'<form id="delete-form" action="cart.html" method="GET">' +
								'<input id="task" type="hidden" name="task" value="remove">' +
								'<input id="movieId" type="hidden" name="movie_id" value="' + resultData[i]["movieId"] + '">' +
								'<input class="quantity" type="hidden" name="quantity" min="0" value="' + resultData[i]["quantity"] + '">' +
				    			'<button class="transparent-btn fas fa-trash trashBtn" type="submit"></button>' +
							'</form>' +
						'</div>' +
					'</div>';
		 
		 cartTableBodyElement.append(itemHTML);
	 }
	
	let checkoutBtnHTML = '';
	checkoutBtnHTML = '<div class="checkout-btn">' +
	           		  		'<a href="checkout.html" class="button" name="checkout-btn">Checkout</a>' +
	           		  '</div>';
	cartTableBodyElement.append(checkoutBtnHTML);	
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    data: location.search.substring(1),
    success: (resultData) => handleCartResult(resultData)
});