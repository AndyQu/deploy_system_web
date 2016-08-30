package deploy_system_web

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		"/projects"{
			controller="Main"
			action="index"
		}
		"/projects/${id}"{
			controller="Main"
			action="project"
		}
		"/projects/${pname}/deploy"{
			controller="Main"
			action="deploy"
		}
		"/projects/${pname}/histories/${containerName}"{
			//id=container id
			controller="Main"
			action="show_deploy_history"
//			method: "POST"
		}
		
		"/exstatic"{
			controller="Static"
			action="index"
			method="GET"
		}
		

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
