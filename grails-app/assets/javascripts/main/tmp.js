var selectedId = null
function clickLogFile(fileEle) {
	var id = fileEle.getAttribute("id")
	if (selectedId != null) {
		$("#" + selectedId).removeClass("btn-success")
	}
	selectedId = id
	console.log(selectedId)
	$("#" + selectedId).addClass("btn-success")

	tailYourLog(fileEle.getAttribute("data-url"))
}