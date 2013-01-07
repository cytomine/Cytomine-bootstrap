package be.cytomine

import be.cytomine.project.Project
import be.cytomine.test.HttpClient
import grails.converters.JSON
import be.cytomine.security.User
import groovyx.net.http.HTTPBuilder
import be.cytomine.api.UrlApi

import static groovyx.net.http.Method.POST
import groovyx.net.http.ContentType

import static groovyx.net.http.Method.GET
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.Ontology

class ElasticSearchController extends RestController {

    def index() {
        //checkState()
        //indexProject(Project.read(57))
        //searchProject(57)
//
//        Project.list().each {
//            indexProject(it);
//        }
//        searchProjectByUser(14)

//        Annotation.list().eachWithIndex{ it, index ->
//            println(index)
//            indexAnnotation(it)
//        }
        searchAnnotationByProject(57)
       // searchAnnotationByProjectAndTerm(57,4751)
         //searchAnnotationByProjectAndTermAndUsers(57,4751)

//        def geom = new WKTReader().read("POLYGON ((50 130, 150 130, 100 50, 50 130))")
//
//        int total = 500000
//        for(int i=Annotation.count();i<total;i++) {
//            println("====>"+((i/total)*100)+"%")
//            Annotation annotation = new Annotation()
//            annotation.name = i+""
//            annotation.location = geom
//            annotation.project = getRandomProject()
//            annotation.image = getRandomImageInstance(annotation.project)
//            annotation.zoomLevel = 1
//            annotation.user = getRandomUser()
//
//            println "annotation.validate=" + annotation.validate()
//            println "annotation.errors=" + annotation?.errors
//            println "annotation.save=" + annotation.save()
//
//            AnnotationTerm annotationTerm = new AnnotationTerm()
//            annotationTerm.annotation = annotation
//            annotationTerm.term = getRandomTerm()
//            annotationTerm.user = getRandomUser()
//
//            println "annotationTerm.validate="+annotationTerm.validate()
//            println "annotationTerm.save="+annotationTerm.save()
//
//            if(i%100==0) cleanUpGorm()
//
//        }
//
//        Annotation annotation = new Annotation()
//        annotation.name = "end"
//        annotation.location = new WKTReader().read("POLYGON ((50 130, 150 130, 100 50, 50 130))")
//        annotation.project = getRandomProject()
//        annotation.image = getRandomImageInstance(annotation.project)
//        annotation.zoomLevel = 1
//        annotation.user = getRandomUser()
//        println "annotation.validate=" + annotation.validate()
//        println "annotation.save=" + annotation.save(flush: true)
//
//        AnnotationTerm annotationTerm = new AnnotationTerm()
//        annotationTerm.annotation = annotation
//        annotationTerm.term = getRandomTerm()
//        annotationTerm.user = getRandomUser()
//
//        println "annotationTerm.validate="+annotationTerm.validate()
//        println "annotationTerm.save="+annotationTerm.save(flush: true)
    }
    private Project getRandomProject() {

        def randomInt = new Random().nextInt(projectChoice.size())
        return projectChoice.get(randomInt)
    }

    private ImageInstance getRandomImageInstance(Project project) {
        if(project.id==57) return images.get(0)
        if(project.id==58) return images.get(1)
        if(project.id==75609) return images.get(2)
        if(project.id==96497) return images.get(3)
        if(project.id==76103) return images.get(4)
    }

    private SecUser getRandomUser() {
        def randomInt = new Random().nextInt(userChoice.size())
        return userChoice.get(randomInt)
    }

    private Term getRandomTerm() {
        def randomInt = new Random().nextInt(terms.size())
        return terms.get(randomInt)
    }

    def projectChoice = [Project.read(57),Project.read(58),Project.read(75609),Project.read(96497),Project.read(76103)]
    def userChoice = [SecUser.read(18),SecUser.read(6980),SecUser.read(14),SecUser.read(6976),SecUser.read(6982)]
    def terms = [Term.read(4751),Term.read(5735),Term.read(4759),Term.read(4757),Term.read(4748)]
    def images = [
            ImageInstance.findByProject(Project.read(57)),
            ImageInstance.findByProject(Project.read(58)),
            ImageInstance.findByProject(Project.read(75609)),
            ImageInstance.findByProject(Project.read(96497)),
            ImageInstance.findByProject(Project.read(76103))
    ]


    private void indexProject(Project project) {
        HttpClient client = new HttpClient();
        client.connect("http://localhost:9200/cytomine/project/"+project.id, "xxx", "xxx");
        client.put(project.encodeAsJSON())
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println code + "=" + response

    }

    private void indexAnnotation(AnnotationDomain annotation) {
        HttpClient client = new HttpClient();
        client.connect("http://localhost:9200/cytomine/userannotation/"+annotation.id, "xxx", "xxx");
        client.put(annotation.encodeAsJSON())
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println code + "=" + response

    }

    private void searchProject(Long id) {
        HttpClient client = new HttpClient();
        client.connect("http://localhost:9200/cytomine/project/_search?q=id:"+id+"&pretty=true", "xxx", "xxx");
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println code + "=" + response
    }

    private void searchProjectByUser(Long id) {
        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'
        def params = ["from" : 0, "size" : 10000000,"query": ["term":["users":id]]]
        def paramsJSON = params as JSON

        http.request(POST) {

            uri.path = "cytomine/project/_search"
           send ContentType.JSON, paramsJSON.toString()
                   //'{"query":{"term":{"users":"6980"}}}'
           // send ContentType.JSON, '{"query":{"term":{"id":"58"}}}'
            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                responseSuccess(formatResult(json.toString()))
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }

    private void searchAnnotationByProject(Long id) {
        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'
        def params = ["from" : 0, "size" : 10000000,"query": ["term":["project":id]]]
        def paramsJSON = params as JSON

        http.request(POST) {

            uri.path = "cytomine/userannotation/_search"
           send ContentType.JSON, paramsJSON.toString()
                   //'{"query":{"term":{"users":"6980"}}}'
           // send ContentType.JSON, '{"query":{"term":{"id":"58"}}}'
            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                responseSuccess(formatResult(json.toString()))
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }

    private void searchAnnotationByProjectAndTerm(Long idProject, Long idTerm) {
        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'
        def params = ["from" : 0, "size" : 10000000,"query": ["bool":["must":[["term":["project":idProject]],["term":["term":idTerm]]]]]]
        def paramsJSON = params as JSON

        http.request(POST) {

            uri.path = "cytomine/userannotation/_search"
           send ContentType.JSON, paramsJSON.toString()
                   //'{"query":{"term":{"users":"6980"}}}'
           // send ContentType.JSON, '{"query":{"term":{"id":"58"}}}'
            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                responseSuccess(formatResult(json.toString()))
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }

    private void searchAnnotationByProjectAndTermAndUsers(Long idProject, Long idTerm) {
        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'
        def params = ["from" : 0,
                      "size" : 10000000,
                      "query": ["bool":["must":[["term":["project":idProject]],["term":["term":idTerm]]]]],
                      "filter": ["or" :
                              ["filters" : [["term":["userByTerm.user":14]],["term":["userByTerm.user":6980]]]],
                              "_cache" : true
                      ]
                     ]
        def paramsJSON = params as JSON

        http.request(POST) {

            uri.path = "cytomine/userannotation/_search"
           send ContentType.JSON, paramsJSON.toString()
                   //'{"query":{"term":{"users":"6980"}}}'
           // send ContentType.JSON, '{"query":{"term":{"id":"58"}}}'
            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                responseSuccess(formatResult(json.toString()))
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }


    def formatResult(def response) {
        def root = JSON.parse(response)
       // println "root="+root
//        println "root.hits="+root.hits
//        println "root.hits.hits="+root.hits.hits
//        println "root.hits.hits._source="+root.hits.hits._source

//        root.hits.hits._source.each {
//            println "**********************************************************************"
//            println it
//        }
        println root.hits.hits._source.size()
        return root.hits.hits._source
    }

/*    private void searchProject(String name) {

        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'
        def params = ["query": ["term":["name":name]]]
        def paramsJSON = params as JSON

        http.request(POST) {

            uri.path = "cytomine/project/_search"
           send ContentType.JSON, '{"query":{"term":{"name":"*NEO04*"}}}'
           // send ContentType.JSON, '{"query":{"term":{"id":"58"}}}'
            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                println json.toString()
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }*/


/*    private void checkState() {
        HttpClient client = new HttpClient();
        client.connect("http://localhost:9200/", "xxx", "xxx");
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println code + "=" + response
    }

        println "################################################################"
    */
    private void checkState() {

        String host = "http://localhost:9200/"

        def http = new HTTPBuilder(host)
        http.auth.basic 'xxx', 'xxx'

        http.request(GET) {

            response.success = { resp, json ->
                println "response succes: ${resp.statusLine}"
                println json.toString()
            }
            response.failure = { resp ->
                println "response error: ${resp.statusLine}"
                println resp
            }
        }
    }

}
