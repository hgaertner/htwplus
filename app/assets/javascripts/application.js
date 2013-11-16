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
	var heightContent = parseInt(getCurrentStyle(document.getElementById("content"), "height").replace(/[a-z]/g, ""));
	if (document.getElementById("smallHead") == null)
		heightContent += 52;
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

function toggleMediaSelection(parent) {
	var childs = document.getElementById("mediaList").getElementsByTagName("input");
	for (i = 0; i < childs.length; i++) {
		childs[i].checked = parent.checked;
	}
}

function setFileNames() {
	var names = "";
	var fileList = document.getElementById("fileupload-input").files;
	for (var i = 0; i < fileList.length; i++) {
        if ('name' in fileList[i]) {
            names += fileList[i].name;
        } else {
            names += fileList[i].fileName;
        }
        if (i != fileList.length - 1) {
        	names += ", ";
        }
	}
	document.getElementById("files").value = names;
}

function mediaUpload() {
	document.getElementById("loadingMedia").style.display = "inline-block";
	document.getElementById("fileuploadForm").submit();
	document.getElementById("fileuploadForm").submit();
}

/* toggle enter */
$('.modal-body').keypress(function(e) {
	var code = null;
    code = (e.keyCode ? e.keyCode : e.which);
    if (code == 13) {
        e.preventDefault();
    }
});

$('[rel="tooltip"]').tooltip('toggle');
$('[rel="tooltip"]').tooltip('hide');
$('[rel="popover"]').popover('toggle');
$('[rel="popover"]').popover('hide');