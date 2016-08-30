/* Copyright (c) 2012: Daniel Richman. License: GNU GPL 3 */
/* Additional features: Priyesh Patel                     */

var logContentArea = "#logFileContent";
var pauseButton = "#pauseButton";

var fileTitle="#logFileTitle"
var loadState="#loadState"
var isFirstLoad="#isFirstLoad"
var requestRange="#requestRange"
var responseRange="#responseRange"

function setState(stateMap){
	if(stateMap.fileTitle!=null){
		$(fileTitle).html(stateMap.fileTitle)
	}
	if(stateMap.loadState!=null){
		$(loadState).html(stateMap.loadState)
	}
	if(stateMap.isFirstLoad!=null){
		$(isFirstLoad).html(stateMap.isFirstLoad)
	}
	if(stateMap.requestRange!=null){
		$(requestRange).html(stateMap.requestRange)
	}
	if(stateMap.responseRange!=null){
		$(responseRange).html(stateMap.responseRange)
	}
}


var scrollelems = [ "html", "body" ];

var url = "/resources/tmp/docker-deploy/web.log";
var fileEleId=null
var fix_rn = true;
var load = 30 * 1024; /* 30KB */
var poll = 3000; /* 1s */

var kill = false;
var loading = false;
var pause = false;
var reverse = false;
var log_data = "";
var log_file_size = 0;

var timeoutVar = null

function clickLogFile(fileEle){
	var fileURL=fileEle.getAttribute("id")
	fileEleId=fileURL
	if(getURL()!=fileURL){
		fileEleId=fileURL
		setURL(fileURL)
		reStart()
	}else{
	}
}

function getURL(){
	return url
}
function setURL(fileUrl) {
	url = fileUrl
}
function reStart() {
	clearTimeout(timeoutVar)
	pause = false
	log_file_size=0
	get_log()
}

/* :-( https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/parseInt */
function parseInt2(value) {
	// if (!(/^[0-9]+$/.test(value)))
	// 	throw "Invalid integer " + value;
	var v = Number(value);
	if (isNaN(v))
		throw "Invalid integer " + value;
	return v;
}

function get_log() {
	if (kill | loading) return;
	loading = true;

	var range;
	var first_load;
	var must_get_206;
	if (log_file_size === 0) {
		/* Get the last 'load' bytes */
		range = "-" + load.toString();
		//			range = load.toString() + "-";
		first_load = true;
		must_get_206 = false;
	} else {
		/* Get the (log_file_size - 1)th byte, onwards. */
		range = (log_file_size - 1).toString() + "-";
		first_load = false;
		must_get_206 = log_file_size > 1;
	}

	setState({
		fileTitle:fileEleId,
		loadState:"loading",
		isFirstLoad:first_load,
		requestRange:range,
		responseRange:""
	})
	/* The "log_file_size - 1" deliberately reloads the last byte, which we already
	 * have. This is to prevent a 416 "Range unsatisfiable" error: a response
	 * of length 1 tells us that the file hasn't changed yet. A 416 shows that
	 * the file has been trucnated */
	$.ajax(url, {
		dataType : "text",
		cache : false,
		headers : {
			Range : "bytes=" + range
		},
		success : function(data, s, xhr) {
			loading = false;

			var content_size;
			var contentRange=""
			if (xhr.status === 206) {
				var c_r = xhr.getResponseHeader("Content-Range");
				contentRange=c_r
				if (!c_r)
					throw "Server did not respond with a Content-Range";

				log_file_size = parseInt2(c_r.split("/")[1]);
				content_size = parseInt2(xhr.getResponseHeader("Content-Length"));
			} else if (xhr.status === 200) {
				if (must_get_206)
					throw "Expected 206 Partial Content";

				content_size = log_file_size = parseInt2(xhr.getResponseHeader("Content-Length"));
				contentRange=content_size
			} else {
				throw "Unexpected status " + xhr.status;
			}

			if (first_load && data.length > load)
				throw "Server's response was too long";

			var added = false;

			if (first_load) {
				/* Clip leading part-line if not the whole file */
				if (content_size < log_file_size) {
					var start = data.indexOf("\n");
					log_data = data.substring(start + 1);
				} else {
					log_data = data;
				}

				added = true;
			} else {
				/* Drop the first byte (see above) */
				log_data += data.substring(1);

				if (log_data.length > load) {
					var start = log_data.indexOf("\n", log_data.length - load);
					log_data = log_data.substring(start + 1);
				}

				if (data.length > 1)
					added = true;
			}

			if (added)
				show_log(added);
			setState({
				fileTitle:fileEleId,
				loadState:"loaded",
				isFirstLoad:first_load,
				// requestRange:range,
				responseRange:contentRange
			})
			timeoutVar = setTimeout(get_log, poll);
		},
		error : function(xhr, s, t) {
			loading = false;

			if (xhr.status === 416 || xhr.status == 404) {
				/* 416: Requested range not satisfiable: log was truncated. */
				/* 404: Retry soon, I guess */

				log_file_size = 0;
				log_data = "";
				show_log();

				
				timeoutVar = setTimeout(get_log, poll);
			} else {
				throw "Unknown AJAX Error (status " + xhr.status + ")";
			}
			setState({
				fileTitle:fileEleId,
				loadState:"load Error",
				isFirstLoad:first_load,
				// requestRange:range,
				responseRange:"http status code:"+xhr.status+",response:"+xhr.responseText
			})
		}
	});
}

function scroll(where) {
	for (var i = 0; i < scrollelems.length; i++) {
		var s = $(scrollelems[i]);
		if (where === -1)
			s.scrollTop(s.height());
		else
			s.scrollTop(where);
	}
}

function show_log() {
	if (pause) return;

	var t = log_data;

	if (reverse) {
		var t_a = t.split(/\n/g);
		t_a.reverse();
		if (t_a[0] == "")
			t_a.shift();
		t = t_a.join("\n");
	}

	if (fix_rn)
		t = t.replace(/\n/g, "\r\n");
	for (var i = 0; i < 5; i++) {
		t = t + "\r\n"
	}

	$(logContentArea).text(t);
	if (!reverse)
		scroll(-1);
}

function error(what) {
	kill = true;

	$(logContentArea).text("An error occured :-(.\r\n" +
		"Reloading may help; no promises.\r\n" +
		what);
	scroll(0);

	return false;
}

$(document).ready(function() {
	window.onerror = error;

	/* If URL is /logtail/?noreverse display in chronological order */
	//去掉开头的"?"问号
	var hash = location.search.replace(/^\?/, "");
	if (hash == "noreverse")
		reverse = false;

	/* Add pause toggle */
	$(pauseButton).click(function(e) {
		pause = !pause;
		$(pauseButton).text(pause ? "开始tail日志" : "暂停tail日志");
		show_log();
		e.preventDefault();
	});
//打开页面时不加载日志
//		get_log();
});