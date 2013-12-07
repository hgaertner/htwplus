function getCurrentStyle (element, cssPropertyName) {
   if (window.getComputedStyle) {
     return window.getComputedStyle(element, '').getPropertyValue(cssPropertyName.replace(/([A-Z])/g, "-$1").toLowerCase());
   }
   else if (element.currentStyle) {
     return element.currentStyle[cssPropertyName];
   }
   else {
     return '';
   }
}

function resizeRings() {
	var offset = $("#hp-content").height() % 12;
	if (offset != 0)
		$("#hp-content").css('padding-bottom', (12 - offset) + "px");
	else
		$("#hp-content").css('padding-bottom', '0');
}

function toggleMediaSelection(parent) {
	var childs = document.getElementById("mediaList").getElementsByTagName("input");
	for (i = 0; i < childs.length; i++) {
		childs[i].checked = parent.checked;
	}
}

/* toggle enter */
$('.modal-body').keypress(function(e) {
	var code = null;
    code = (e.keyCode ? e.keyCode : e.which);
    if (code == 13) {
        e.preventDefault();
    }
});

$(window).resize(function() {
	resizeRings();
});

resizeRings();
$('[rel="tooltip"]').tooltip();
$('[rel="popover"]').popover();


/*
 *  Token
 */
$(document).ready(function () {

	var preSelection = $("input:radio[name=visibility]:checked").val();
	if(preSelection == 2) {
		$("#token-input").show();
	}
	
	$("input:radio[name=visibility]").click(function() {
		var selection = $(this).val();
		if(selection == 2) {
			$("#token-input").fadeIn();
		} else {
			$("#token-input").fadeOut();
		}
		
	});
	
});

