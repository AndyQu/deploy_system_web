package com.andy.web.deploy

import groovy.json.JsonBuilder
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class StaticController {
	def static final Logger LOGGER=LoggerFactory.getLogger(StaticController.class)

    def index() { 
		render view:"testPartialGet.gsp"
	}
}
