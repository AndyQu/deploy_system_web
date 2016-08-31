/* Copyright (c) 2012: Daniel Richman. License: GNU GPL 3 */
/* Additional features: Priyesh Patel                     */

var logContentArea = "#logFileContent";
var pauseButton = "#pauseButton";

var idFileTitle="#logFileTitle"
var idLoadState="#loadState"
var idRequestRange="#requestRange"
var idResponseRange="#responseRange"
var idNotice="#notice"

function setState(stateMap){
	if(stateMap.idFileTitle!=null){
		$(idFileTitle).html(stateMap.idFileTitle)
	}
	if(stateMap.idLoadState!=null){
		$(idLoadState).html(stateMap.idLoadState)
	}
	if(stateMap.idRequestRange!=null){
		$(idRequestRange).html(stateMap.idRequestRange)
	}
	if(stateMap.idResponseRange!=null){
		$(idResponseRange).html(stateMap.idResponseRange)
	}
	if(stateMap.idNotice!=null){
		$(idNotice).html(stateMap.idNotice)
	}
}
/**
 * Context variables
 */
var FIX_RETURN = true
var MAX_LOAD_BYTES = 30 * 1024; /* 30KB */
var TAIL_INTERVAL = 3000; /* 1s */

var logFileSize = 0
var logData = "";
var requestRange= null
var contentSize=0

var MODE_FETCH_FILE_CUR_TAIL=0
var MODE_FETCH_FILE_NEW_TAIL=1
var mode = MODE_FETCH_FILE_CUR_TAIL

var isLoading = false;
var tailTimer = null
// var targetFileUrl = "/resources/tmp/docker-deploy/web.log";
var targetFileUrl=null

var kill = false;
var pause = false;
var reverse = false;
var scrollelems = [ "html", "body" ];

function clickLogFile(fileEle){
	var fileURL=fileEle.getAttribute("id")
	tailYourLog(fileURL)
}

function tailYourLog(fileURL){
	if(getURL()!=fileURL){
		targetFileUrl=fileURL
		setURL(fileURL)
		reStart()
	}else{
	}
}

function getURL(){
	return targetFileUrl
}
function setURL(fileUrl) {
	targetFileUrl = fileUrl
}
function reStart() {
	clearTimeout(tailTimer)
	pause = false
	logFileSize=0
	logData=""
	showLog()
	tailLog()
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

function refreshRequestParams(){
	if (logFileSize === 0) {
		requestRange = "-" + MAX_LOAD_BYTES.toString();
		mode=MODE_FETCH_FILE_CUR_TAIL
	} else {
		requestRange = (logFileSize - 1).toString() + "-";
		mode=MODE_FETCH_FILE_NEW_TAIL
	}
}
function process200(data, s, xhr){
	contentSize = logFileSize = parseInt2(xhr.getResponseHeader("Content-Length"));

	logData=data
	showLog()

	setState({
		idFileTitle:targetFileUrl,
		idLoadState:"loaded",
		idResponseRange:"",
		idNotice:"Server不支持Partial Get"
	})
	//TODO
	//显示Notice：Server不支持Partial Get
}
function process206(data,s,xhr){
	var c_r = xhr.getResponseHeader("Content-Range");
	contentRange=c_r
	if (!c_r)
		throw "Server did not respond with a Content-Range";

	logFileSize = parseInt2(c_r.split("/")[1]);
	contentSize = parseInt2(xhr.getResponseHeader("Content-Length"));
	
	var added = false;
	if (mode==MODE_FETCH_FILE_CUR_TAIL) {
		logData = data;
		added = true;
	} else if(mode==MODE_FETCH_FILE_NEW_TAIL){
		/* Drop the first byte (see above) */
		if (data.length > 1){
			added = true;
			logData += data.substring(1);
			if (logData.length > MAX_LOAD_BYTES) {
				var start = logData.indexOf("\n", logData.length - MAX_LOAD_BYTES);
				logData = logData.substring(start + 1);
			}
		}
		else{
			// throw "received data length<=1 data="+data
		}
	}else{
		throw "invalid mode:"+mode;
	}

	if (added)
		showLog(added);
	setState({
		idFileTitle:targetFileUrl,
		idLoadState:"loaded",
		idResponseRange:contentRange,
		idNotice:""
	})
	tailTimer = setTimeout(tailLog, TAIL_INTERVAL);
}

function processError(xhr, s, t) {
	isLoading = false;
	if (xhr.status === 416 || xhr.status == 404) {
		/* 416: Requested range not satisfiable: log was truncated. */
		/* 404: Retry soon, I guess */
		tailTimer = setTimeout(tailLog, TAIL_INTERVAL);
	}
	setState({
		idFileTitle:targetFileUrl,
		idLoadState:"load Error",
		idResponseRange:"",
		idNotice:"http status code:"+xhr.status+",response:"+xhr.responseText
	})
}


function tailLog() {
	if (kill | isLoading) return;
	isLoading = true;

	refreshRequestParams()

	setState({
		idFileTitle:targetFileUrl,
		idLoadState:"loading",
		idRequestRange:requestRange,
		idResponseRange:"",
		idNotice:""
	})
	/* The "log_file_size - 1" deliberately reloads the last byte, which we already
	 * have. This is to prevent a 416 "Range unsatisfiable" error: a response
	 * of length 1 tells us that the file hasn't changed yet. A 416 shows that
	 * the file has been trucnated */
	$.ajax(targetFileUrl, {
		dataType : "text",
		cache : false,
		headers : {
			Range : "bytes=" + requestRange
		},
		success : function(data, s, xhr) {
			isLoading = false;
			if (xhr.status === 206) {
				return process206(data,s,xhr)
			} else if (xhr.status === 200) {
				return process200(data, s, xhr)
			} else {
				throw "Unexpected status " + xhr.status;
			}
		},
		error : processError
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

function showLog() {
	if (pause) return;

	var t = logData;

	if (reverse) {
		var t_a = t.split(/\n/g);
		t_a.reverse();
		if (t_a[0] == "")
			t_a.shift();
		t = t_a.join("\n");
	}

	if (FIX_RETURN)
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
		showLog();
		e.preventDefault();
	});
});