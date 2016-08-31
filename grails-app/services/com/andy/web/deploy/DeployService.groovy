package com.andy.web.deploy


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.andyqu.docker.deploy.event.DeployEvent
import com.andyqu.docker.deploy.event.DeployStage
import com.google.common.eventbus.Subscribe;

import grails.transaction.Transactional

@Transactional
class DeployService {
	def static final Logger LOGGER = LoggerFactory.getLogger("DeployService")
	
	Map<String,DeployEvent> statusM=[:]
    def fetchDeployStatus(String containerName) {
		DeployEvent event=statusM.get(containerName)
		LOGGER.info "event_name=fetchDeployStatus key={} value={}", containerName, event
		return event?.stage
    }
	def Boolean isDeployEnded(String containerName){
		return fetchDeployStatus(containerName)==DeployStage.END
	}
	@Subscribe 
	def void receive(DeployEvent event) {
		statusM[event.containerName]=event
		LOGGER.info "event_name=received_DeployEvent event={}", event
	}
}
