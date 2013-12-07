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
	 * GENERIC AJAX FORM REQUEST
	 */
	
	$('.ajaxModal').on('hidden', function () {
		  $(this).removeData('modal');
		  $(this).find('.modal-body').html("Wird geladen ...");
	});
	
	$('.ajaxModalSave').click(function(){
		var form = $(this).parent().prev().find('.ajaxForm');
		var url = form.attr("action");
		$.ajax({
			url: url,
			dataType: "json",
			type: "POST",
			data: form.serialize(),
			success: function(data){
				var status = data.status;
				if(status == "redirect" && data.url != null) {
					window.location.replace(data.url);
				}
				if(status == "response" && data.payload != null) {
					form.replaceWith(data.payload);
				}
			}
		});
	});
	
	$('.modal-body').on("click", ".ajaxModalLink", function(){
		var url = $(this).attr("href");
		var modalBody = $(this).parentsUntil('.modal', '.modal-body');
		$.ajax({
			url: url,
			dataType: "json",
			type: "GET",
			success: function(data){
				var status = data.status;
				if(status == "redirect" && data.url != null) {
					window.location.replace(data.url);
				}
				if(status == "response" && data.payload != null) {
					modalBody.html(data.payload);
				}
			}
		});
		return false;
	});
	
	$('.modal-body').on("click", ".ajaxModalSubmit", function(){
		var form = $(this).closest('form');
		var url = form.attr('action');
		$.ajax({
			url: url,
			dataType: "json",
			type: "POST",
			data: form.serialize(),
			success: function(data){
				var status = data.status;
				if(status == "redirect" && data.url != null) {
					window.location.replace(data.url);
				}
				if(status == "response" && data.payload != null) {
					form.html(data.payload);
				}
			}
		});
		return false;
		
	});
	
	
	/*
	 * END GENERIC AJAX FORM REQUEST
	 */
	
	/*
	 * NEW GROUP MODAL
	 * 
	 * Bind the action.
	 * Using 'on' is critical, otherwise the binding would be lost after the request
	 */
	$('#createNewGroupModal').on("click", "#submitGroup", createGroupRequest);

	function createGroupRequest() {
		jsRoutes.controllers.GroupController.create().ajax({
			type: "POST",
			data: $('#newGroupForm').serialize(),
			success: createGroup
		});
		return false;
	}
	

	function createGroup(data) {
		$('#createNewGroupModal').html(data);
	}
	
	/*
	 * END NEW GROUP MODAL 
	 */
	
	
	/*
	 * REGISTER MODAL
	 */
	$('#registerModal').on("click", "#submitSignup", sumitSignupRequest);
	
	function sumitSignupRequest() {
		jsRoutes.controllers.AccountController.submit().ajax({
			type: "POST",
			data: $('#newSignupForm').serialize(),
			success: submitSignup
		});
		return false;
	}
	
	function submitSignup(data) {
		$('#registerModal').html(data);
	}
	/*
	 * END REGISTER MODAL
	 */
		
	/*
	 * GROUP COMMENTS
	 */
	
	$('.hp-comment-form').each(function(){
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
			
			window.setTimeout("resizeRings()", 400);
			
			return false;
		});
	});

		
	/*
	 * END GROUP COMMENTS
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
		
	/*
	 * ACCOUNT SEARCH
	 */
	$('#accountSearchSubmit').click(function(){
		var keyword = $('#accountSearchQuery').val();
		$.ajax({
			url: "/user/searchForAccount/" + keyword,
			type: "GET",
			succes: function(data){
				$('#searchAccountResults').replaceWith(data)
			}
		})
	})
	/*
	 * END ACCOUNT SEARCH
	 */
});
