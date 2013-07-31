$(document).ready(function () {
	
	/*
	 * AJAX loading indicator
	 */
	$.ajaxSetup({
	    beforeSend:function(){
	        $(".loading").show();
	        $(".loading").css('display', 'inline-block');
	    },
	    complete:function(){
	        $(".loading").hide();
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
			
			if($(context).hasClass('open')){
				$("#collapse-"+id).collapse('toggle');
				$(context).html("Ältere Kommentare anzeigen...");
				$(context).removeClass('open');
				$(context).addClass('closed');
			}
			
			else if($(context).hasClass('closed')){
				$("#collapse-"+id).collapse('toggle');
				$(context).html("Ältere Kommentare ausblenden...");
				$(context).removeClass('closed');
				$(context).addClass('open');
			}
			
			else if($(context).hasClass('unloaded')){
				var currentComments = $('#comments-' + id + ' > .media').length;
				$(context).html("Ältere Kommentare ausblenden...");
				$.ajax({
					url: "/post/olderComments?id=" + id + "&current=" + currentComments,
					type: "GET",
					success: function(data){
						$("#collapse-"+id).html(data);
						$("#collapse-"+id).collapse('toggle');
					
					}
				});
				$(context).addClass('open');
				$(context).removeClass('unloaded');
			}
			
			return false;
		});
	});

		
	/*
	 * END GROUP COMMENTS
	 */

	
	/*
	 * MEDIA UPLOAD
	 */
	
	
	/*
	 * END MEDIA UPLOAD
	 */
	
	
	/*
	 * GROUP SEARCH
	 */
	$('#groupSearchSubmit').click(function(){
		var keyword = $('#groupSearchQuery').val();
		$.ajax({
			url: "/gruppe/searchForGroup/" + keyword,
			type: "GET",
			success: function(data){
				$('#searchGroupResults').replaceWith(data)
			
			}
		});
		
	});
	
	
	
	/*
	 * END GROUP SEARCH
	 */
});
