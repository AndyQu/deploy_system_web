<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统-${history.projectNames[0]}</title>
        <!-- <script src="jquery.logviewer.js"></script> -->
        <script type="text/javascript">
            function clickLogFile(fileURL){
                setURL(fileURL)
                reStart()
            }
        </script>
	</head>
	<body>
		<div>
			<a href="/projects">主页</a>
            <span>></span>
            <a href="/projects">项目列表</a>
            <span>></span>
            <a href="/projects/${history.projectNames[0]}">${history.projectNames[0]}</a>
		</div>
		<div>
			<h1>日志文件列表</h1>
			<ol>
				<g:each status="i" in="${fileList}" var="logFile">
					<li>
                        <button class="btn btn-default" type="submit" onclick="clickLogFile('/resources/${logFile.getPath()}')">
                            ${logFile.getPath()}
                        </button>
					</li>
				</g:each>
			</ol>
		</div>
        <div id="header">
            <a id="pauseButton" href='#'>Pause</a>.
        </div>
        <pre id="logFileContent">Loading...</pre>
        <div style="margin-right:100px;">
            <table class="history_table" class="deploy_info_table" style="margin-left: 50px;">
                    <caption>
                        <strong>${history.containerName}</strong>
                    </caption>
                    <col span="1" style="background-color:LightSeaGreen ;" />
                    <tbody>
                            <tr>
                                <td><p>容器名称</p></td>
                                <td>${history.containerName}</td>
                            </tr>
                            <tr>
                                <td>容器ID</td>
                                <td>${history.containerId}</td>
                            </tr>
                            <tr>
                                <td>主机名称</td>
                                <td>${history.hostName}</td>
                            </tr>
                            <tr>
                                <td>主机IP</td>
                                <td>${history.hostIp}</td>
                            </tr>
                            <tr>
                                <td>开始时间</td>
                                <td>${history.startTime}</td>
                            </tr>
                            
                            <tr>
                                <td>结束时间</td>
                                <td>${history.endTime}</td>
                            </tr>
                            <tr>
                                <td>开始时间戳</td>
                                <td>${history.startTimeStamp}</td>
                            </tr>
                            <tr>
                                <td>结束时间戳</td>
                                <td>${history.endTimeStamp}</td>
                            </tr>
                            <tr>
                                <td>状态</td>
                                <td>${history.status}</td>
                            </tr>
                            <tr>
                                <td>环境</td>
                                <td>
                                    <pre>
                                    ${new groovy.json.JsonBuilder(history.contextConfig).toPrettyString()}
                                    </pre>
                                </td>
                            </tr>
                            <tr>
                                <td>容器配置</td>
                                <td>
                                    <pre>
                                    ${new groovy.json.JsonBuilder(history.containerConfig).toPrettyString()}
                                    </pre>
                                </td>
                            </tr>
                        </tbody>
                </table>
            </div>
    </body>
</html>