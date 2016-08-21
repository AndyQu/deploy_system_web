// Place your Spring DSL code here
beans = {
	jsonSlurper(groovy.json.JsonSlurper){}

	/*deployContext(com.andyqu.docker.deploy.DeployContext){ 
		envConfigFile='/localhost_mac/envConf.json' 
	}*/
	historyManager(com.andyqu.docker.deploy.history.HistoryManager){ 
		mongoConfig = '/mongodb.json' 
	}
	projectMetaManager(com.andyqu.docker.deploy.ProjectMetaManager){ 
		context=ref("deployContext") 
	}

	tool(com.andyqu.docker.deploy.Tool){

	}
	dockerTool(com.andyqu.docker.deploy.DockerTool){

	}
}
