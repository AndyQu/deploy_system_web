package com.andy.web.deploy

import org.slf4j.LoggerFactory

class MainController {
	def static final LOGGER = LoggerFactory.getLogger("DeployEngine")
	def projectMetaManager
	def historyManager
	
    def index() {
			def projectNames = projectMetaManager.getAllProjectNames()
			projectNames.each {
				LOGGER.info "event_name=got_project name={}", it
			}
			render view:"index.gsp",model:[projectList:projectNames]
	}
	
	def project(){
		def projectName=params.id
		def pMetas=projectMetaManager.getProjectMetas([projectName])
		def histories = historyManager.fetchHistories(projectName)
		render view:"project.gsp",model:[project:pMetas[0],histories:histories]
	}
}
