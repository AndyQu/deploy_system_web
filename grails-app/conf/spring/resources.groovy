import com.andyqu.docker.deploy.ConfigUtil;

// Place your Spring DSL code here
beans = {
	jsonSlurper(groovy.json.JsonSlurper){}

	globalContext(com.andyqu.docker.deploy.GlobalContext){
		def envFolder=ConfigUtil.getOSFolderName()
		envConfigFile="/${envFolder}/envConf.json" 
	}
	historyManager(com.andyqu.docker.deploy.history.HistoryManager){ 
		mongoConfig = '/mongodb.json' 
	}
	projectMetaManager(com.andyqu.docker.deploy.ProjectMetaManager){ 
		context=ref("globalContext") 
	}

	tool(com.andyqu.docker.deploy.Tool){

	}
	dockerTool(com.andyqu.docker.deploy.DockerTool){

	}
	resourceHttpRequestHandler(org.springframework.web.servlet.resource.ResourceHttpRequestHandler){
		locations=["file:/"]
	}
	simpleUrlHandlerMapping(org.springframework.web.servlet.handler.SimpleUrlHandlerMapping){
		urlMap=[
			"/resources/**":ref("resourceHttpRequestHandler")
			] 
	}
	geventBus(com.google.common.eventbus.EventBus){}
}
