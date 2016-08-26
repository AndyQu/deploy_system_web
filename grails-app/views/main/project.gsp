<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统-${project.projectName}</title>
        <style>
            
        </style>
	</head>
	<body>
		<div>
			<a href="/projects">主页</a>
            <span>></span>
            <a href="/projects">项目列表</a>
            <span>></span>
            <a href="/projects/${project.projectName}">${project.projectName}</a>
		</div>
		<div style="margin-right:200px;">
            <h1><strong>项目Meta</strong></h1>
            <table class="deploy_info_table">
                <col span="1" style="background-color:LightSeaGreen ;">
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
                </tbody>
            </table>
            

            <!-- <g:form controller="Main" action="deploy" name="deploy_form" id="${project.projectName}"></g:form> -->
            <form id="deploy_form" name="deploy_form" action="/projects/${project.projectName}/deploy" method="post" ></form>
            <h1><strong>参数配置</strong></h1>
            <table class="deploy_info_table">
                <col span="1" style="background-color:LightSeaGreen ;">
                <tbody>
                    <tr>
                        <td>部署人</td>
                        <td><input form="deploy_form" type="text" name="ownerName" value="annoy" placeholder="annoy"></td>
                    </tr>
                    <tr>
                        <td>分支名称</td>
                        <td><input form="deploy_form" type="text" name="branchName" value="${project.gitbranchName}" placeholder="${project.gitbranchName}"></td>
                    </tr>
                    
                </tbody>
            </table>
            <h1><strong>端口配置</strong></h1>
            <table class="deploy_info_table">
                <tr>
                    <td class="td_head">Docker端口</td>
                    <td class="td_head">主机端口</td>
                    <td class="td_head">端口说明</td>
                    <td class="td_head">自定义配置</td>
                    <td class="td_head">
                        固定端口</br>
                        （仅选择“申请固定端口”时起作用）
                    </td>
                </tr>
                    <g:each in="${project.portList}" var="portMeta">
                    <tr>
                        <td>${portMeta.port}</td> 
                        <td>
                            <g:if test="${portMeta.hostPort}==null">
                                未设置(默认随机申请主机上可用的端口)
                            </g:if>
                            <g:else>
                                ${portMeta.hostPort}
                            </g:else>
                        </td>
                        <td>${portMeta.description}</td> 
                        <td>
                            <dl>
                                <dt><input form="deploy_form" type="radio" name="${portConfigPrefix}_${portMeta.port}_type" value="default" checked>使用默认配置</input></dt>
                                <dt><input form="deploy_form" type="radio" name="${portConfigPrefix}_${portMeta.port}_type" value="random" >随机分配</input></dt>
                                <dt><input form="deploy_form" type="radio" name="${portConfigPrefix}_${portMeta.port}_type" value="apply" >申请固定端口</input></dt>
                            </dl>
                        </td>
                        <td>
                            <dl>
                                <dt>
                                    <input form="deploy_form" type="text" name="${portConfigPrefix}_${portMeta.port}_appliedHostPort" value="-1" placeholder="-1" />
                                </dt>
                                <dt>
                                    <input form="deploy_form" type="hidden" name="projectName" value="${project.projectName}" />
                                </dt>
                            </dl>
                        </td>
                    </tr>
                    </g:each>
            </table>
            <input form="deploy_form" style="height:60px;width:100px;margin-left:5px;background-color:DarkGoldenRod;float:right;" type="submit" value="开始部署">

            <div>
                <h1><strong>部署历史</strong></h1>
            </div>
                <g:each status="i" in="${histories}" var="history">
                <div>
                <table class="history_table" class="deploy_info_table" style="margin-left: 50px;">
                    <caption>
                        <strong>${history.containerName}</strong>(近${i+1}次)
                    </caption>
                    <col span="1" style="background-color:LightSeaGreen ;">
                    <tbody>
                            <tr>
                                <td><p>容器名称</p></td>
                                <td>${history.containerName}</td>
                            </tr>
                            <tr>
                                <td>容器ID</td>
                                <td>
                                    <a href="/projects/${project.projectName}/histories/${history.containerId}">
                                    ${history.containerId}
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>主机名称</td>
                                <td>${history.hostName}</td>
                            </tr>
                            <!--
                            <tr>
                                <td>主机IP</td>
                                <td>${history.hostIp}</td>
                            </tr>
                            -->
                            <tr>
                                <td>开始时间</td>
                                <td>${history.startTime}</td>
                            </tr>
                            
                            <tr>
                                <td>结束时间</td>
                                <td>${history.endTime}</td>
                            </tr>
                            <!--
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
                            -->
                        </tbody>
                </table>
                </div>
                </g:each>
            
        </div>
	</body>
</html>