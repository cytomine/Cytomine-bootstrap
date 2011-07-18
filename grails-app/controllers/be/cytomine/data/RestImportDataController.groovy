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


    def update = {

    }

    def terms = {

       //refresh ontology
    }

    def annotations = {
        log.info("get annotation")

        String serverUrl = "http://139.165.108.140:48/cytomine-web/"
        String idProject = 42 //from server
        String projectName = "CERVIX" //from client because name is different between deploy / dev

        def json = getAnnotationsFromServer(idProject,projectName,serverUrl)
        def jsonUsers = getUsersFromServer(serverUrl)

        def users = [:]
        for(int i=0;i<jsonUsers.length();i++) {
            def jsonUser = jsonUsers.get(i)
            log.info jsonUser.id + "="+ jsonUser.username
            users[(jsonUser.id)]= jsonUser.username
        }
        log.info "users="+ users


        Project project = Project.findByName(projectName);
        log.info("project="+ project.name)

        for(int i=0;i<json.length();i++) {
            def elem = json.get(i);
            log.info("elem="+ elem)

            //get image
            AbstractImage baseimage = AbstractImage.findByFilename(elem.imageFilename)
            ImageInstance image = ImageInstance.findByBaseImageAndProject(baseimage,project)
            //change id from other sever by id from current server
            elem.image = image.id

            User user = User.findByUsername(users[(elem.user)])
            //change id from other sever by id from current server
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

    def getAnnotationsFromServer(String idProjectFromServer, String projectNameFromClient, String url) {

        String URL = url+"/api/project/"+idProject+"/annotation.json"
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

}
