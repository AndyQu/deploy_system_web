var QUERY_INTERVAL=5000 //5 seconds
function whetherDeployEnded(containerName, callback){
    var request=new XMLHttpRequest()
    request.onreadystatechange = function () {
        if (request.readyState == 4 && request.status == 200) {
            var result=JSON.parse(request.responseText)
            if(result.data.isDeployEnded){
                callback(containerName)
            }else{
                setTimeout(
                    function(){
                        whetherDeployEnded(containerName, callback)
                    }, QUERY_INTERVAL
                )
            }
        }
    }
    request.open("GET","/main/isDeployEnded/"+containerName, true)
    request.send()
}
function onDeployEnded(containerName){
    //http://localhost:8080/projects/CRM/histories/longlong-CRM_dev-20160831_13_54_03_912
    alert("部署完成："+containerName+". 转入部署历史页面......")
    location.assign("/projects/CRM/histories/"+containerName)
}