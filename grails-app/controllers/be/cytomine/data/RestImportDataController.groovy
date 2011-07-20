package be.cytomine.data

import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.security.User
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.AnnotationTerm

class RestImportDataController {


    def annotations = {
        log.info("get annotation")

        String serverUrl = "http://139.165.108.140:48/cytomine-web"
        String idProject = params.idProject //from server

        def jsonProject = getProjectFromServer(idProject,serverUrl)
        def jsonAnnotations = getAnnotationsFromServer(idProject,serverUrl)
        def jsonUsers = getUsersFromServer(serverUrl)



        String projectName = jsonProject.name
        log.info("project.name="+ projectName)
        Project project = null
        Project.list().each {
           log.info("it="+ it)
           if(it.name.toUpperCase().equals(projectName.toUpperCase())) {
                 project = it
           }
        }
        log.info("project="+ project)
        if(!project) {
            log.error("Project is null");
            return;
        }

        def users = [:]
        for(int i=0;i<jsonUsers.length();i++) {
            def jsonUser = jsonUsers.get(i)
            log.info jsonUser.id + "="+ jsonUser.username
            users[(jsonUser.id)]= jsonUser.username
        }
        log.info "users="+ users




        for(int i=0;i<jsonAnnotations.length();i++) {
            def elem = jsonAnnotations.get(i);
            log.info("****************** ANNOTATION "+ (i+1) + "/"+ jsonAnnotations.length() +" ******************************")
            log.info("elem="+ elem)

            //get image
            AbstractImage baseimage = AbstractImage.findByFilename(elem.imageFilename)
            log.info("baseimage="+baseimage)
            ImageInstance image = ImageInstance.findByBaseImageAndProject(baseimage,project)
            log.info("image="+image)
            //change id from other sever by id from current server
            elem.image = image.id

            User user = User.findByUsername(users[(elem.user)])
            //change id from other sever by id from current server
            if(!user) user = User.findByUsername("rmaree")
            elem.user = user.id

            //create annotation
            Annotation annotation = Annotation.createFromData(elem)
            annotation.name = ""
            log.info("annotation="+annotation + " image="+elem.image + " user="+elem.user);
            if(!annotation.validate()) log.info("ERROR ANNOTATION:"+annotation.errors);
            annotation.save(flush:true)
            def jsonTerms = elem.term
            for(int j=0;j<jsonTerms.length();j++) {
                def jsonTerm =  jsonTerms.get(j)

                Term term = Term.findByNameAndOntology(jsonTerm.name,project.ontology)
                log.info("name="+jsonTerm.name +" and " + project.ontology.id)
                log.info("term="+term)
                AnnotationTerm.link(annotation,term)
            }
        }


    }

    def getAnnotationsFromServer(String idProjectFromServer,String url) {

        String URL = url+"/api/project/"+idProjectFromServer+"/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
        client.get()
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response "+ code)
        def json = JSON.parse(response)
        json
    }

    def getUsersFromServer(String url) {

        String URL = url+"/api/user.json"
        HttpClient client = new HttpClient();
        client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
        client.get()
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response "+ code)
        def json = JSON.parse(response)
        json
    }

    def getProjectFromServer(String idProjectFromServer,String url) {

        String URL = url+"/api/project/" + idProjectFromServer +".json"
        HttpClient client = new HttpClient();
        client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
        client.get()
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response "+ code)
        def json = JSON.parse(response)
        json
    }

}
