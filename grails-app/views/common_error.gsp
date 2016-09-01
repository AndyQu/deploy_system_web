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
            <pre>
            ${new groovy.json.JsonBuilder(error).toPrettyString()}
            </pre>
        </div>        
    </body>
</html>