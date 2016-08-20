<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>部署系统</title>
	</head>
	<body>
		<div>
			<a href="#">主页</a><span>></span><a href="#">项目列表</a>
		</div>
		<div>
			<ul>
				<g:each in="${projectList}" var="project">
        			<li>
            			<a href="/projects/${project}">${project}</a>
        			</li>
        		</g:each>       
			</ul>
		</div>
	</body>
</html>