<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统</title>
	</head>
	<body>
		<div>
			<a href="/projects">主页</a><span>></span><a href="/projects">项目列表</a>
		</div>
		<div style="margin-left:10px;margin-right:100px;">
			<table class="history_table">
				<col span="1" style="background-color:LightSeaGreen ;">
				<tbody>
					<tr>
						<td>容器名称</td>
						<td>${dockerName}</td>
					</tr>
					<tr>
						<td>部署配置</td>
						<td>
							<pre>
							${new groovy.json.JsonBuilder(context).toPrettyString().encodeAsHTML()}
							</pre>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</body>
</html>