$(document).ready(function () {
	/*
	 * Bind the action.
	 * Using 'on' is critical, otherwise the binding would be lost after the request
	 */
	$('#newModal').on("click", "#submitGroup", myAjax);
	
	function myAjax() {
		jsRoutes.controllers.GroupController.create().ajax({
			type: "POST",
			data: $('#newGroupForm').serialize(),
			success: mySuccess
		});
		return false;
	}
		
	function mySuccess(data) {
		$('#newModal').html(data);
	}
	
});