<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统-${projectName}</title>
	</head>
	<body>
		<div>
			<a href="/projects">主页</a>
            <span>></span>
            <a href="/projects">项目列表</a>
            <span>></span>
            <a href="/projects/${projectName}">${projectName}</a>
		</div>
		<div>
			<h1>日志文件列表</h1>
			<ol>
                <g:if test="${fileList.isEmpty()}">
                    <h3 style="color:red;text-decoration:underline;font-style:oblique;">Notice:日志文件已经被删除</h3>
                </g:if>
                <g:else>
                    <g:each status="i" in="${fileList}" var="logFile">
                        <li>
                            <button 
                                id="resources${logFile.getPath().replace(File.separator,"").replace(".","")}"
                                data-url="/resources/${logFile.getPath()}"
                                class="btn btn-default" 
                                type="submit" 
                                onclick="clickLogFile(this)">
                                ${logFile.getPath()}
                            </button>
                        </li>
                    </g:each>
                </g:else>
			</ol>
		</div>
        <!--<div style="margin-top:20px;margin-bottom:20px;">
            <button id="pauseButton" class="btn btn-danger" type="submit">暂停tail日志</buttona>.
        </div>-->
        <div style="margin-top:20px;margin-bottom:20px;margin-left:50px;margin-right:100px;">
            <table class="table table-striped">
                <tr>
                    <td>文件URL</td>
                    <td id="logFileTitle"></td>
                </tr>
                <tr>
                    <td>加载状态</td>
                    <td id="loadState"></td>
                </tr>
                <tr>
                    <td>请求的字节范围</td>
                    <td id="requestRange"></td>
                </tr>
                <tr>
                    <td>接收到的字节范围</td>
                    <td id="responseRange"></td>
                </tr>
                <tr>
                    <td><strong>Notice</strong></td>
                    <td id="notice"></td>
                </tr>
                <tr>
                    <td><strong>部署状态</strong></td>
                    <td id="deployStatus"></td>
                </tr>
            </table>
        </div>
        <div style="margin-top:20px;margin-bottom:20px;margin-left:50px;margin-right:100px;">
            <pre id="logFileContent">Loading...</pre>
        </div>
        <script type="text/javascript" src="/assets/main/deploy.js"></script>
        <script type="text/javascript">
            window.onload = whetherDeployEnded("${projectName}", "${containerName}", onDeployEnded)
        </script>
    </body>
</html>