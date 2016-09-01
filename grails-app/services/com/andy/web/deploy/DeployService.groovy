package com.andy.web.deploy


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.andyqu.docker.deploy.Tool
import com.andyqu.docker.deploy.DeployEngine
import com.andyqu.docker.deploy.ProjectMetaManager;
import com.andyqu.docker.deploy.event.DeployEvent
import com.andyqu.docker.deploy.event.DeployStage
import com.andyqu.docker.deploy.history.HistoryManager;
import com.andyqu.docker.deploy.model.ProjectMeta
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe

import grails.transaction.Transactional
import groovy.json.JsonSlurper;
import javax.annotation.PostConstruct

@Transactional
class DeployService {
	def static final Logger LOGGER = LoggerFactory.getLogger("DeployService")
	def static  int MAX_CONCURRENT_DEPLOYING=2

	def ProjectMetaManager projectMetaManager
	def HistoryManager historyManager

	def EventBus geventBus
	
	@PostConstruct
	def postInit(){
		geventBus.register(this)
	}

	//所有部署历史
	Map<String,DeployEvent> statusM=[:]

	//正在被部署的container
	Map<String,DeployEvent> deploying=[:]

	def DeployStage fetchDeployStatus(String containerName) {
		DeployEvent event=statusM.get(containerName)
		LOGGER.info "event_name=fetchDeployStatus key={} value={}", containerName, event
		LOGGER.debug "event_name=show_all_status value={}",statusM
		return event?.stage
	}

	def Boolean isDeployEnded(String containerName){
		return fetchDeployStatus(containerName)==DeployStage.END
	}
	
	@Subscribe
	def void receive(DeployEvent event) {
		statusM[event.containerName]=event

		LOGGER.info "event_name=received_DeployEvent event={}", event
		switch(event.stage){
			case DeployStage.END:
				deploying.remove(event.containerName)
				break;
			default:
				deploying[event.containerName]=event
				break;
		}
	}

	def int getMaxConcurrent(){
		return MAX_CONCURRENT_DEPLOYING
	}
	
	def Boolean canDeploy(){
		LOGGER.info "event_name=canDeploy deploying={}",deploying.keySet()
		return deploying.size()<MAX_CONCURRENT_DEPLOYING
	}

	def deploy(deployContext, String dockerName){
		def contextFolderPath = "${deployContext.config.workFolder}/${dockerName}/"
		Logger newLogger=Tool.configureLogger(contextFolderPath, dockerName)
		
		new Thread(){
					@Override
					public void run(){
						DeployEngine engine=null
						if(deployContext.config.useDockerSock==1){
							engine = new DeployEngine()
						}else{
							engine = new DeployEngine(deployContext.config.dockerDaemon.host, deployContext.config.dockerDaemon.port)
						}
						engine.eventBus=geventBus
						engine.logger=newLogger
						engine.projectMetaManager=projectMetaManager
						engine.historyManager=historyManager
						engine.deploy(
								dockerName,
								deployContext.config.ownerName,
								deployContext.config.project as ProjectMeta,
								deployContext.config.imgName,
								deployContext.config
								)}
				}.start()
	}
}
