package com.andy.web.deploy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.andyqu.docker.deploy.DeployContext
import com.andyqu.docker.deploy.DeployEngine
import com.andyqu.docker.deploy.ProjectMetaManager
import com.andyqu.docker.deploy.history.HistoryManager;;
import com.andyqu.docker.deploy.model.PortMeta
import com.andyqu.docker.deploy.model.ProjectMeta
import com.mongodb.DBObject

import com.andyqu.docker.deploy.Tool
import groovy.json.JsonSlurper

class MainController {
	def static final Logger LOGGER = LoggerFactory.getLogger("DeployEngine")
	def static final String portConfigPrefix="portConfig"
	def ProjectMetaManager projectMetaManager
	def HistoryManager historyManager
	
	def JsonSlurper jsonSlurper
	
    def index() {
			def projectNames = projectMetaManager.getAllProjectNames()
			projectNames.each {
				LOGGER.info "event_name=got_project name={}", it
			}
			render view:"index.gsp",model:[projectList:projectNames]
	}
	
	def project(){
		String projectName=params.id
		Collection<ProjectMeta> pMetas=projectMetaManager.getProjectMetas([projectName])
		List<DBObject> histories = historyManager.fetchHistories(projectName)
		render view:"project.gsp",model:[project:pMetas[0],histories:histories, portConfigPrefix:""]
	}
	
	def deploy(){
		LOGGER.info "event_name=deploy key={}", params
		
		def ownerName=params.owerName
		def branchName=params.branchName
		def projectName=params.projectName
		try{
			ProjectMeta pmeta = projectMetaManager.getProjectMetas([projectName]).getAt(0)
			pmeta.gitbranchName=branchName
			pmeta.projectName=projectName
			
			/*
			 * 处理端口配置
			 */
			
			pmeta.getPortList().each { 
				PortMeta portMeta->
					def portParams = params.findAll { it.key.startsWith("${portConfigPrefix}_${portMeta.port}") }
					LOGGER.info "event_name=process_portConfig key={} values={}",portMeta.getPort(), portParams
	
					String type=portParams["${portConfigPrefix}_${portMeta.port}_type"]
					if(type=="default"){
						LOGGER.info "event_name=use_default_config key={}", portMeta.getPort()
					}else if(type=="random"){
						portMeta.setHostPort(-1)
						LOGGER.info "event_name=random_apply_host_port key={}", portMeta.getPort()
					}else if(type=="apply"){
						int appliedPort=Integer.parseInt(portParams["${portConfigPrefix}_${portMeta.port}_appliedHostPort"])
						portMeta.setHostPort(appliedPort)
						LOGGER.info "event_name=apply_fixed_host_port key={} host_port={}", portMeta.getPort(), appliedPort
					}
			}
			
			
			//合并：环境配置
			DeployContext deployContext=new DeployContext()
			//TODO
			deployContext.setEnvConfigFile("/localhost_mac/envConf.json")
			def projectsMeta=[
				"ownerName": ownerName,
				"projects" : [pmeta]
			]
			LOGGER.debug "key={} event_name=generated_projectsMeta value={}", params, projectsMeta
			deployContext.config = Tool.objsToJson(projectsMeta	, deployContext.hostConfig	)
			LOGGER.info "key={} event_name=generated_final_deployContext value={}", params, deployContext
			
			/*
			 * 开始部署
			 */
			
			new Thread(){
						@Override
						public void run(){
							LOGGER.info "key={} event_name=useDockerSock:${context.config.useDockerSock}", params
							DeployEngine engine=null
							if(deployContext.config.useDockerSock==1){
								engine = new DeployEngine()
							}else{
								engine = new DeployEngine(deployContext.config.dockerDaemon.host, deployContext.config.dockerDaemon.port)
							}
							engine.deploy(
									deployContext.config.ownerName,
									deployContext.config.projects as List<ProjectMeta>,
									deployContext.config.imgName,
									deployContext.config
									)}
					}.start()
			
			render view:"deploy_info.gsp"
		}catch(Exception e){
			LOGGER.error "event_name=deploy_exception key={} e={}",params, e
			redirect uri:"/error"
		}
	}
}
