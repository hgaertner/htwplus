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

function changeText(id, parent) {
	var heightBefore = parseInt(getCurrentStyle(document.getElementById(id), "height").replace(/[a-z]/g, ""));
	if (heightBefore > 0)
		parent.innerHTML = "ältere Kommentare anzeigen...";
	else
		parent.innerHTML = "ältere Kommentare ausblenden...";
	window.setTimeout("resizeRings()", 400);
}

function resizeRings() {
	var offset = $("#content").height() % 12;
	if (offset != 0)
		$("#content").css('padding-bottom', (12 - offset) + "px");
	else
		$("#content").css('padding-bottom', '0');
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