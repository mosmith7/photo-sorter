<html>

<body>

	<h1>Photo Sorter </h1>
	
	<h2>Photos loaded and ready to go. </h2>
	
	<h2>You are currently looking at the photo: ${location} </h2>
	
	<img alt="${location}" src="data:image/jpeg;base64,${base64}" width="400" height="400" />
	
	<form action = "previous" method="get">
		<input type="submit" value="Get previous photo" /> 
	</form>
	<form action = "next" method="get">
		<input type="submit" value="Get next photo" /> 
	</form>
	
	<form action = "/api/photo-sorter/tags/image/add" method="post" name="addImageToTagForm">
	Add new Tag to photo: <input type="text" name="photoLocation" value="blah">
		<input type="submit" value="Add Tag" onclick="addTagToImage()" /> 
	</form>
	
	<script type="text/javascript">
	function addTagToImage() {
		var formData = JSON.stringify($("#addImageToTagForm").serializeArray());
		$.ajax({
	        url: 'http://localhost:9000/api/tags/image/add',
	        type: 'POST',
	        data: formData,
	        success: function(){},
	        dataType: "json",
	        contentType: 'application/json',
	  });
		}
	</script>
	
</body>

</html>