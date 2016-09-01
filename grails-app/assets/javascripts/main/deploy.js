var QUERY_INTERVAL=5000 //5 seconds
function whetherDeployEnded(projectName, containerName, callback){
    var request=new XMLHttpRequest()
    request.onreadystatechange = function () {
        if (request.readyState == 4){
            if(request.status == 200) {
                var result=JSON.parse(request.responseText)
                if(result.data.isDeployEnded){
                    callback(projectName, containerName)
                }else{
                    setTimeout(
                        function(){
                            whetherDeployEnded(projectName, containerName, callback)
                        }, QUERY_INTERVAL
                    )
                }
                document.getElementById("deployStatus").innerHTML=result.data.isDeployEnded
            }else{

            }
        }
    }
    request.open("GET","/main/isDeployEnded/"+containerName, true)
    request.send()
}
function onDeployEnded(projectName,containerName){
    //http://localhost:8080/projects/CRM/histories/longlong-CRM_dev-20160831_13_54_03_912
    alert("部署完成："+containerName+". 转入部署历史页面......")
    location.assign("/projects/"+projectName+"/histories/"+containerName)
}