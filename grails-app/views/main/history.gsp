<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统-${history.projectNames[0]}</title>
        <!--<script src="/assets/main/tmp.js"></script>-->
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
            </table>
        </div>
        <div style="margin-top:20px;margin-bottom:20px;margin-left:50px;margin-right:100px;">
            <pre id="logFileContent">Loading...</pre>
        </div>
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