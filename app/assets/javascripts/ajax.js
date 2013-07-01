$(document).ready(function () {
	
	/*
	 * AJAX loading indicator
	 */
	$.ajaxSetup({
	    beforeSend:function(){
	        $("#loading").show();
	        // show gif here, eg:
	        $("#loading").css('display', 'inline-block');
	    },
	    complete:function(){
	        $("#loading").hide();
	    }
	});
	
	
	/*
	 * Bind the action.
	 * Using 'on' is critical, otherwise the binding would be lost after the request
	 */
	$('#createNewGroupModal').on("click", "#submitGroup", createGroupRequest);
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
		$('#createNewGroupModal').html(data);
	}
	
	function submitSignup(data) {
		$('#registerModal').html(data);
	}
	
	/*
	 * EDIT GROUP
	 */
	
	$('.editGroup').each(function(){
		$(this).click(function(){
			$('#editModal > .actual-modal').remove();
			$('#editModal > .loading-modal').show();
			$('#editModal').modal('show'); 
			$.ajax({
				url: $(this).attr('href'),
				type: "GET",
				success: editGroup
			});
			
			/* alert($(this).attr('href')); */
			return false;
		});
	});
	
	function editGroup(data) {
		$('#editModal > .loading-modal').hide();
		$(data).insertAfter('#editModal > .loading-modal');
	}
	
	$('#editModal').on("click", "#submitGroup", updateGroupRequest);
	
	function updateGroupRequest() {
		$.ajax({
			url: $('#editGroupForm').attr("action"),
			type: "POST",
			data: $('#editGroupForm').serialize(),
			success: updateGroup
		});
		return false;
	}
	
	function updateGroup(data) {
		$('.actual-modal').replaceWith(data);
	}
	
	/*
	 * END EDIT GROUP
	 */
	
	/*
	 * GROUP COMMENTS
	 */
	
	$('.addComment').each(function(){
		var context = $(this);
		$(".commentSubmit", this).click(function(){
			$.ajax({
				url: context.attr('action'),
				type: "POST",
				data: context.serialize(),
				success: function(data){
					context.before(data);
					context[0].reset();
				}
			});
			return false;
		});
	});
	
	$('.olderComments').each(function(){
		var id = $(this).attr('href').split('-')[1];
		var context = this;
		$(this).click(function(){
			$.ajax({
				url: "/post/"+id+"/olderComments",
				type: "GET",
				success: function(data){
					$("#collapse-"+id).html(data);
					$("#collapse-"+id).collapse();
				
				}
			});
		});
	});

		
	
	
	/*
	 * END GROUP COMMENTS
	 */
	
	
});
