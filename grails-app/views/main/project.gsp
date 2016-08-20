<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统-${project.projectName}</title>
        <style>
            table, th, td {
                border: 1px solid black;
            }
            td{
                width:250px;
            }
            .history_table{
                margin-left:50px;
            }
        </style>
	</head>
	<body>
		<div>
			<a href="/projects">主页</a>
            <span>></span>
            <a href="/projects">项目列表</a>
            <span>></span>
            <a href="/projects">${project.projectName}</a>
		</div>
		<div>
            <h1><strong>项目Meta</strong></h1>
            <table>
                <!-- <caption><b>项目Meta</b></caption> -->
                <col span="1" style="background-color:green;">
                <tbody>
                    <tr>
                        <td>项目名称</td>
                        <td>${project.projectName}</td>
                    </tr>
                    <tr>
                        <td>git repo url</td>
                        <td>${project.gitRepoUri}</td>
                    </tr>
                    <tr>
                        <td>分支名称</td>
                        <td>${project.gitbranchName}</td>
                    </tr>
                    <tr>
                        <td>是否需要Java Debug端口</td>
                        <td>${project.needJavaDebugPort}</td>
                    </tr>
                    <tr>
                        <td>容器中日志文件夹</td>
                        <td>${project.logFolder}</td>
                    </tr>
                    <tr>
                        <td>是否需要加载Node Lib</td>
                        <td>${project.needMountNodeLib}</td>
                    </tr>
                    <tr>
                        <td>是否需要加载Gradle Lib</td>
                        <td>${project.needMountGradleLib}</td>
                    </tr>
                    <g:each in="${project.portList}" var="portMeta">
                    <tr>
                        <td>Docker端口</td>
                        <td>${portMeta.port}</td> 
                    </tr>
                    <tr>
                        <td>主机端口</td>
                        <td>
                            <g:if test="${portMeta.hostPort}==null">
                                未设置(默认随机申请主机上可用的端口)
                            </g:if>
                            <g:else>
                                ${portMeta.hostPort}
                            </g:else>
                        </td> 
                    </tr>
                    <tr>
                        <td>说明</td>
                        <td>${portMeta.description}</td> 
                    </tr>
                    </g:each>  
                </tbody>
            </table>
            <div>
                <h1><strong>部署历史</strong></h1>
                <g:each status="i" in="${histories}" var="history">
                <table class="history_table">
                    <caption>
                        近<b>${i}</b>次部署:<strong>${history.containerName}</strong>
                    </caption>
                    <col span="1" style="background-color:green;">
                    <tbody>
                            <tr>
                                <td>容器名称</td>
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
                                <td>开始时间戳</td>
                                <td>${history.startTimeStamp}</td>
                            </tr>
                            <tr>
                                <td>结束时间</td>
                                <td>${history.endTime}</td>
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
                                <td>${history.contextConfig}</td>
                            </tr>
                            <tr>
                                <td>容器配置</td>
                                <td>${history.containerConfig}</td>
                            </tr>
                        </tbody>
                </table>
                </g:each>
            </div>
        </div>
	</body>
</html>