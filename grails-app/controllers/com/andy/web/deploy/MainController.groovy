package com.andy.web.deploy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File

import com.andyqu.OSType

import com.andyqu.docker.deploy.DeployContext
import com.andyqu.docker.deploy.DeployEngine
import com.andyqu.docker.deploy.GlobalContext
import com.andyqu.docker.deploy.ProjectMetaManager
import com.andyqu.docker.deploy.event.DeployEvent
import com.andyqu.docker.deploy.event.DeployStage;
import com.andyqu.docker.deploy.history.DeployHistory
import com.andyqu.docker.deploy.history.HistoryManager;;
import com.andyqu.docker.deploy.model.PortMeta
import com.andyqu.docker.deploy.model.ProjectMeta
import com.mongodb.DBObject

import com.andyqu.docker.deploy.Tool
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper

class MainController {
	def static final Logger LOGGER = LoggerFactory.getLogger("MainController")
	def static final String PortConfigPrefix="portConfig"
	
	def GlobalContext globalContext
	def ProjectMetaManager projectMetaManager
	def HistoryManager historyManager
	
	def JsonSlurper jsonSlurper
	
	def DeployService deployService
	
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
		List<DeployHistory> histories = historyManager.fetchHistories(projectName)
		render view:"project.gsp",model:[project:pMetas[0],histories:histories, portConfigPrefix:PortConfigPrefix]
	}
	
	def deploy(){
		LOGGER.info "event_name=deploy key={}", params
		if(!deployService.canDeploy()){
			def builder=new JsonBuilder()
			def error = builder.error{
				code 404
				msg "并发部署到达上限。请稍后再试。"
				domain {
					max_concurrency deployService.getMaxConcurrent()
				}
			}
			LOGGER.warn "event_name=read_max_deploy_concurrency key={}", params
			render view:"/common_error.gsp",model:[error:error,projectName:params.projectName]
			return
		}
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
			LOGGER.info "key={} event_name=useDockerSock:${deployContext.config.useDockerSock}", key
			deployService.deploy(deployContext,dockerName)
			File historyFolder=new File(deployContext.config.workFolder+File.separator+dockerName)
			assert historyFolder.exists()
			def files=[]
			historyFolder.eachFileRecurse { File it->
				if(it.isFile()){
					files.add(it)
				}
			}
			render view:"deploy.gsp",model:[fileList:files, projectName:projectName, containerName:dockerName]
		}catch(Exception e){
			LOGGER.error "event_name=deploy_exception key={} e={}",params, e
			redirect uri:"/error"
		}
	}
	
	def show_deploy_history(){
		LOGGER.info "event_name=show_deploy_history key={}", params
		def projectName=params.pname
		def containerName=params.containerName
		List<DeployHistory> histories = historyManager.fetchHistories projectName, [containerName:containerName]
		if(histories.isEmpty()){
			render view:"/error.gsp", model: [message:"历史记录不存在"]
		}else{ 
			if(histories.size()>=2){
				LOGGER.error "event_name=multiple_histories_found value={}", histories
			}
			deploy_history histories.get(0),containerName
		}
	}
	
	def deploy_history(history, containerName){
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
	
	def DeployStage fetchDeployStage(){
		String containerName=params.id
		DeployStage stage = fetchDeployStatus(containerName)
		def builder= new JsonBuilder()
		builder.data{
			deployStage stage
		}
		render builder.toString()
	}
	
	def isDeployEnded(){
		String containerName=params.id
		def result=deployService.isDeployEnded(containerName)
		def builder= new JsonBuilder()
		builder.data{
			isDeployEnded result
		}
		render builder.toString()
	}
}
