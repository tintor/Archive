function highlight(checkbox) {
	var a = document.getElementById(checkbox.value);
	a.style.backgroundColor = checkbox.checked ? "#CCEEFF" : "";
	var b = document.getElementById(checkbox.value + "_c");
	b.style.backgroundColor = checkbox.checked ? "#CCEEFF" : "";
}

function showTooltip(text) {
	tooltip_text.innerHTML=text
	tooltip.style.visibility="visible"
	tooltip.style.position="absolute"
	tooltip.style.left=event.clientX+10
	tooltip.style.top=event.clientY
}

function hideTooltip() {
	tooltip.style.visibility="hidden"
}

function NewXmlHttp() {
	// Firefox, Opera 8.0+, Safari
	try {
		return new XMLHttpRequest();
	} catch (e) {}
	
	// Internet Explorer
	try {
		return new ActiveXObject("Msxml2.XMLHTTP");
	} catch (e) {}

	// Internet Explorer
	try {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} catch (e) {}

	alert("Your browser does not support AJAX!");
      return false;
}