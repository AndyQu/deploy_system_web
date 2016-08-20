package deploy_system_web

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		"/projects/${id}"{
			controller="Main"
			action="project"
		}
		"/projects"{
			controller="Main"
			action="index"
		}

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
