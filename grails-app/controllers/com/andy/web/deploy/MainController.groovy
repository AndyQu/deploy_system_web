package com.andy.web.deploy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File

import com.andyqu.OSType

import com.andyqu.docker.deploy.DeployContext
import com.andyqu.docker.deploy.DeployEngine
import com.andyqu.docker.deploy.GlobalContext
import com.andyqu.docker.deploy.ProjectMetaManager
import com.andyqu.docker.deploy.history.DeployHistory
import com.andyqu.docker.deploy.history.HistoryManager;;
import com.andyqu.docker.deploy.model.PortMeta
import com.andyqu.docker.deploy.model.ProjectMeta
import com.mongodb.DBObject

import com.andyqu.docker.deploy.Tool

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper

class MainController {
	def static final Logger LOGGER = LoggerFactory.getLogger("DeployEngine")
	def static final String PortConfigPrefix="portConfig"
	
	def GlobalContext globalContext
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
		render view:"project.gsp",model:[project:pMetas[0],histories:histories, portConfigPrefix:PortConfigPrefix]
	}
	
	def deploy(){
		LOGGER.info "event_name=deploy key={}", params
		assert params.projectName == params.pname
		
		def ownerName=params.ownerName
		def branchName=params.branchName
		def projectName=params.projectName
		try{
			ProjectMeta pmeta = projectMetaManager.getProjectMetas([projectName]).getAt(0)
			pmeta.gitbranchName=branchName
			pmeta.projectName=projectName
			
			/*
			 * 处理端口配置
			 */
			LOGGER.info "key={} event_name=show_ports value={}",params, pmeta.getPortList()
			pmeta.getPortList().each { 
				portMeta->
					def portParams = params.findAll { 
						it.key.startsWith("${PortConfigPrefix}_${portMeta.port}") 
					}
					LOGGER.info "event_name=process_portConfig key={} values={}",portMeta.port, portParams
	
					String type=portParams["${PortConfigPrefix}_${portMeta.port}_type"]
					if(type=="default"){
						LOGGER.info "event_name=use_default_config key={}", portMeta.port
					}else if(type=="random"){
						portMeta.setHostPort(-1)
						LOGGER.info "event_name=random_apply_host_port key={}", portMeta.port
					}else if(type=="apply"){
						int appliedPort=Integer.parseInt(portParams["${PortConfigPrefix}_${portMeta.port}_appliedHostPort"])
						portMeta.setHostPort(appliedPort)
						LOGGER.info "event_name=apply_fixed_host_port key={} host_port={}", portMeta.port, appliedPort
					}
			}
			
			
			//合并：环境配置
			DeployContext deployContext=new DeployContext()
			String dockerName = Tool.generateContainerName(ownerName, [pmeta])
			def projectsMeta=[
				"ownerName": ownerName,
				"projects" : [pmeta]
			]
			LOGGER.debug "key={} event_name=generated_projectsMeta value={}", params, projectsMeta
			deployContext.config = Tool.objsToJson(projectsMeta	, globalContext.hostConfig	)
			LOGGER.info "key={} event_name=generated_final_deployContext value={}", params, deployContext
			def key=params
			
			/*
			 * 开始部署
			 */
			def contextFolderPath = "${deployContext.config.workFolder}/${dockerName}/"
			Logger newLogger=Tool.configureLogger(contextFolderPath, dockerName)
			
			new Thread(){
						@Override
						public void run(){
							LOGGER.info "key={} event_name=useDockerSock:${deployContext.config.useDockerSock}", key
							DeployEngine engine=null
							if(deployContext.config.useDockerSock==1){
								engine = new DeployEngine()
							}else{
								engine = new DeployEngine(deployContext.config.dockerDaemon.host, deployContext.config.dockerDaemon.port)
							}
							engine.logger=newLogger
							engine.projectMetaManager=projectMetaManager
							engine.historyManager=historyManager
							engine.deploy(
									dockerName,
									deployContext.config.ownerName,
									deployContext.config.projects as List<ProjectMeta>,
									deployContext.config.imgName,
									deployContext.config
									)}
					}.start()
			LOGGER.info "event_name=chain_deploy_history() containerName={}", dockerName
//			chain action:"deploy_history", params: [pname:projectName, containerName: dockerName]
//			chain action:"deploy_history", model: [history:
//				new DeployHistory(
//						contextConfig:deployContext.config
//					),
//				containerName:dockerName
//				]
			deploy_history new DeployHistory(
						contextConfig:deployContext.config,
						projectNames:[projectName]
					),
				dockerName
		}catch(Exception e){
			LOGGER.error "event_name=deploy_exception key={} e={}",params, e
			redirect uri:"/error"
		}
	}
	
	def show_deploy_history(){
		LOGGER.info "event_name=show_deploy_history key={}", params
		def projectName=params.pname
		def containerName=params.containerName
		List<DBObject> histories = historyManager.fetchHistories projectName, [containerName:containerName]
		if(histories.isEmpty()){
			render view:"/error.gsp", [message:"历史记录不存在"]
		}else{ 
			if(histories.size()>=2){
				LOGGER.error "event_name=multiple_histories_found value={}", histories
			}
//			chain action:"deploy_history",model:[history:histories.get(0), containerName:containerName]
			deploy_history histories.get(0),containerName
		}
	}
	
	def deploy_history(history, containerName){
//			def history=chainModel.history
//			def containerName=chainModel.containerName
			assert history.contextConfig.workFolder!=null
			File historyFolder=new File(history.contextConfig.workFolder+File.separator+containerName)
			def files=[]
			if(historyFolder.exists()){
				historyFolder.eachFileRecurse {
					File it->
						if(it.isFile()){
							files.add(it)
						}
				}
			}
			render view:"history.gsp",model:[history:history, fileList:files]
		}
}
