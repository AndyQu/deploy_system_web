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
		HttpServletResponse httpResponse=(HttpServletResponse)response 
		def fileFullPath = params.file
		File f = new File(fileFullPath)
		httpResponse.setContentLength(f.length().intValue())
		
		def fileInputStream = new FileInputStream(f)
		def outputStream = httpResponse.getOutputStream()
		byte[] buffer = new byte[4096];
		int len;
		int contentLength=0
		while ((len = fileInputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
			contentLength+=len
		}
		
//		LOGGER.info "event_name=response_headers value={}", new JsonBuilder(response).toPrettyString()
//		LOGGER.info "event_name=response_headers value={}", ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE);
		outputStream.flush()
		outputStream.close()
		fileInputStream.close()
//		response.setHeader("Content-Length", "${contentLength}")
//		response.setContentLength(contentLength)
//		httpResponse.setIntHeader("Content-Length", contentLength)
		LOGGER.info "event_name=response_headers has_Content-Length={}", httpResponse.containsHeader("Content-Length")
	}
}
