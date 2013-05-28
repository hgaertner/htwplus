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
	var heightContent = getCurrentStyle(document.getElementById("content"), "height").replace(/[a-z]/g, "") - 15 + 115;
	var offset = heightContent % 12;
	if (offset != 0)
		document.getElementById("content").style.paddingBottom = 12 - offset + "px";
	else
		document.getElementById("content").style.paddingBottom = 0;
}

if (matchMedia) {
	var mq1 = window.matchMedia("(min-width: 1200px)");
	mq1.addListener(resizeRings);
	resizeRings(mq1);
	var mq2 = window.matchMedia("(max-width: 1199px)");
	mq2.addListener(resizeRings);
	resizeRings(mq2);
	var mq3 = window.matchMedia("(max-width: 979px)");
	mq3.addListener(resizeRings);
	resizeRings(mq3);
}

$('[rel="tooltip"]').tooltip('toggle');
$('[rel="tooltip"]').tooltip('hide');
$('[rel="popover"]').popover('toggle');
$('[rel="popover"]').popover('hide');