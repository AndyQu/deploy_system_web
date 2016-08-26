import org.apache.catalina.loader.WebappLoader

eventConfigureTomcat = {tomcat ->

	def context = tomcat.addWebapp('/statics' , '/')
	def loader = new WebappLoader(tomcat.class.classLoader)
//	loader.addRepository(new File('/home/mohadib/workspace/acrm/lib').toURI().toURL().toString())
	loader.container = context
	context.loader = loader
}