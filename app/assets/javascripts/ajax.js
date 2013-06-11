$(document).ready(function () {
	
	/*
	 * Bind the action.
	 * Using 'on' is critical, otherwise the binding would be lost after the request
	 */
	$('#newModal').on("click", "#submitGroup", createGroupRequest);
	$('#registerModal').on("click", "#submitSignup", sumitSignupRequest);
	
	
	function createGroupRequest() {
		jsRoutes.controllers.GroupController.create().ajax({
			type: "POST",
			data: $('#newGroupForm').serialize(),
			success: createGroup
		});
		return false;
	}
	
	function sumitSignupRequest() {
		jsRoutes.controllers.AccountController.submit().ajax({
			type: "POST",
			data: $('#newSignupForm').serialize(),
			success: submitSignup
		});
		return false;
	}
	
	function createGroup(data) {
		$('#newModal').html(data);
	}
	
	function submitSignup(data) {
		$('#registerModal').html(data);
	}
	
});