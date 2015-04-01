package be.cytomine.processing

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ForbiddenException
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.UserAnnotationService
import be.cytomine.project.Project
import be.cytomine.security.AuthWithToken
import be.cytomine.security.LoginController
import be.cytomine.test.HttpClient
import be.cytomine.utils.ValueComparator
import grails.converters.JSON
import groovy.sql.Sql
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.log4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static grails.async.Promises.*

import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * Retrieval is a server that can provide similar pictures of a request picture
 * It can suggest term for an annotation thx to similar picture
 */
class ImageRetrievalService {

    static transactional = false

    def currentRoleServiceProxy
    def cytomineService
    def dataSource
    def abstractImageService


    public void indexImageSync(BufferedImage image,String id, String storage, Map<String,String> properties) {
        log.info "Index image sync:$id $storage"
        indexImage(image,id,storage,properties)
    }

    public void indexImageAsync(BufferedImage image,String id, String storage, Map<String,String> properties) {
        log.info "Index image async:$id $storage"
        def process = task { indexImage(image,id,storage,properties) }
    }

    /**
     * Search similar annotation and best term for an annotation
     * @param project project which will provide annotation learning set
     * @param annotation annotation to search
     * @return [annotation: #list of similar annotation#, term: #map with best term#]
     */
    def listSimilarAnnotationAndBestTerm(Project project, AnnotationDomain annotation) throws Exception {
        log.info "Search similarities for annotation ${annotation.id}"
        def data = [:]

        if(annotation.location.numPoints<3) {
            data.term = []
            return data
        }

        //find project used for retrieval
        List<Long> projectSearch = []
        if(project.retrievalDisable) {
            //retrieval not available for this project, just return empty result
            return data
        } else if(project.retrievalAllOntology) {
            //retrieval available, look in index for all user annotation for the project with same ontologies
            projectSearch=getAllProjectId(annotation.project.ontology)
        } else {
            //retrieval avaliable, but only looks on a restricted project list
            projectSearch=project.retrievalProjects.collect {it.id}
        }

        //Only keep projects available for the current user
        boolean isAdmin = currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser)
        projectSearch = projectSearch.findAll{ Project.read(it).checkPermission(READ,isAdmin)}

        log.info "search ${annotation.id} on projects ${projectSearch}"
        log.info "log.addannotation2"
        //Get similar annotation
        def similarAnnotations = loadAnnotationSimilarities(annotation,projectSearch)
        data.annotation = similarAnnotations

        log.info "log.addannotation3"
        //Get all term from project
        def projectTerms = project.ontology.terms()
        def bestTermNotOrdered = getTermMap(projectTerms)
        ValueComparator bvc = new ValueComparator(bestTermNotOrdered);

        log.info "log.addannotation4"
        //browse annotation
        similarAnnotations.each { similarAnnotation ->
            //for each annotation, browse annotation terms
            def terms = similarAnnotation.terms()
            terms.each { term ->
                if (projectTerms.contains(term)) {
                    Double oldValue = bestTermNotOrdered.get(term)
                    //for each term, add similarity value
                    bestTermNotOrdered.put(term, oldValue + similarAnnotation.similarity)
                }
            }
        }
        log.info "log.addannotation5"

        //Sort [term:rate] by rate (desc)
        TreeMap<Term, Double> bestTerm = new TreeMap(bvc);
        bestTerm.putAll(bestTermNotOrdered)
        def bestTermList = []
        log.info "log.addannotation6"
        //Put them in a list
        for (Map.Entry<Term, Double> entry: bestTerm.entrySet()) {
            Term term = entry.getKey()
            term.rate = entry.getValue()
            bestTermList << term
        }
        data.term = bestTermList
        return data
    }

    private def getTermMap(List<Term> termList) {
        def map = [:]
        termList.each {
            map.put(it, 0d)
        }
        map
    }
    /**
     * Get all project id for all project with this ontology
     * @param ontology Ontology filter
     * @return Project id list
     */
    public List<Long> getAllProjectId(Ontology ontology) {
        //better for perf than Project.findByOntology(ontology).collect {it.id}
        String request = "SELECT p.id FROM project p WHERE ontology_id="+ontology.id
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            data << it[0]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        return data
    }


    private def loadAnnotationSimilarities(AnnotationDomain searchAnnotation,List<Long> projectSearch) {
        log.info "get similarities for userAnnotation " + searchAnnotation.id + " on " + projectSearch
//        RetrievalServer server = RetrievalServer.findByDeletedIsNull()
//        def response = RetrievalHttpUtils.getPostSearchResponse(server.getFullURL(),'/retrieval-web/api/retrieval/search.json', searchAnnotation, searchAnnotation.getCropUrl(),projectSearch)
//        try {
//            def json = JSON.parse(response)
//            def result =  readRetrievalResponse(searchAnnotation,json)
//            return result
//        } catch (org.codehaus.groovy.grails.web.json.JSONException exception) { //server did not respond => 404
//            throw new ObjectNotFoundException("Retrieval : object not found")
//        }
        if(!RetrievalServer.list().isEmpty()) {
            RetrievalServer server = RetrievalServer.list().get(0)
            //def cropUrl = searchAnnotation.urlImageServerCrop(abstractImageService)
            //def responseJSON = doRetrievalSearch(server.url+"/api/search","admin","admin",ImageIO.read(new URL(cropUrl)),projectSearch.collect{it+""})
            def responseJSON = doRetrievalSearch(server.url+"/api/searchUrl","admin","admin",searchAnnotation.id,projectSearch.collect{it+""})
            def result =  readRetrievalResponse(searchAnnotation,responseJSON.data)
            log.info "result=$result"
            return result
        } else {
            log.info "No retrieval server found"
        }



    }



    public def doRetrievalSearch(String url, String username, String password, BufferedImage image,List<String> storages) {

        url = url+"?max=30&storages=${storages.join(";")}"

        HttpClient client = new HttpClient()

        log.info "url=$url"
        log.info "username=$username password=$password"

        client.connect(url,username,password)

        MultipartEntity entity = createEntityFromImage(image)

        client.post(entity)

        String response = client.getResponseData()
        int code = client.getResponseCode()
        log.info "code=$code response=$response"
        def json = JSON.parse(response)

        return json
    }

    public def doRetrievalSearch(String url, String username, String password, Long id,List<String> storages) {

        url = url+"?max=30&id=$id&storages=${storages.join(";")}"

        HttpClient client = new HttpClient()

        log.info "url=$url"
        log.info "username=$username password=$password"

        client.connect(url,username,password)

//        MultipartEntity entity = createEntityFromImage(image)

        client.post("")

        String response = client.getResponseData()
        int code = client.getResponseCode()
        log.info "code=$code response=$response"
        def json = JSON.parse(response)

        return json
    }


    private def readRetrievalResponse(AnnotationDomain searchAnnotation,def responseJSON) {
        def data = []
        for (int i = 0; i < responseJSON.length(); i++) {
            def annotationjson = responseJSON.get(i)
            try {
                UserAnnotation annotation = UserAnnotation.read(annotationjson.id)
                if (annotation && annotation.id != searchAnnotation.id) {
                    annotation.similarity = new Double(annotationjson.similarities)
                    data << annotation
                }
            }
            catch (AccessDeniedException ex) {log.info "User cannot have access to this userAnnotation"}
            catch (NotFoundException ex) {log.info "User cannot have access to this userAnnotation"}
        }
        return data
    }



    /**
     * Get missing annotation
     */
    public void indexMissingAnnotation() {
        //Get indexed resources
        List<Long> resources = getIndexedResource()
        Set<Long> ressourcesSet = new HashSet<Long>(resources)
        //Check if each annotation is well indexed
        def annotations = UserAnnotationService.extractAnnotationForRetrieval(dataSource)
        int i = 1
        def data = []

//        if(currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser)) {
//                String tokenKey = UUID.randomUUID().toString()
//                AuthWithToken token = new AuthWithToken(
//                        user : cytomineService.currentUser.username,
//                        expiryDate: new Date((long)new Date().getTime() + (48 * 60 * LoginController.ONE_MINUTE_IN_MILLIS)),
//                        tokenKey: tokenKey
//                ).save(flush : true)


    //        annotations = annotations.subList(0,100)
            annotations.each { annotation ->
                log.info "Annotation $i/" + annotations.size()
                if (!ressourcesSet.contains(annotation.id)) {
                    log.debug "Annotation $annotation.id IS NOT INDEXED"
                    try {

                        def cropUrl = AnnotationDomain.getAnnotationDomain(annotation.id).urlImageServerCrop(abstractImageService)
                        data << [id:annotation.id,storage:annotation.container,url:cropUrl]

                    } catch (Exception e) {log.error e}
                } else {
                    log.debug "Annotation $annotation.id IS INDEXED"
                }
                i++
            }

            RetrievalServer server = RetrievalServer.findByDeletedIsNull()
            String jsonData = (data as JSON).toString(true)
            log.info jsonData.substring(0,Math.min(100,jsonData.length()-1))
            if(server!=null) {
                log.info "Server $server!"
                //log.info "jsonData $jsonData!"
                String url = server.url + "/api/index/full"
                HttpClient client = new HttpClient()
                client.connect(url,"admin","admin")
                client.post(jsonData)
                String response = client.getResponseData()
                int code = client.getResponseCode()
                log.info "code $code!"
                log.info "response $response!"
            } else {
                log.warn "No retrieval found!"
            }
//        } else {
//            throw new ForbiddenException("YOU MUST BE ADMIN!!!!")
//        }

    }


//    /**
//     * Get missing annotation
//     */
//    public void indexMissingAnnotation() {
//        //Get indexed resources
//        List<Long> resources = getIndexedResource()
//        //Check if each annotation is well indexed
//        def annotations = userAnnotationService.listLightForRetrieval()
//        int i = 1
//        annotations.each { annotation ->
//            log.debug "Annotation $i/" + annotations.size()
//            if (!resources.contains(annotation.id)) {
//                log.debug "Annotation $annotation.id IS NOT INDEXED"
//                try {
//                    def cropUrl = annotation.urlImageServerCrop(abstractImageService)
//                    indexImageSync(
//                            ImageIO.read(new URL(cropUrl)),
//                            annotation.id+"",
//                            annotation.project+"",
//                            [:]
//                    )
//
//                } catch (Exception e) {log.error e}
//            } else {
//                log.debug "Annotation $annotation.id IS INDEXED"
//            }
//            i++
//        }
//    }

    /**
     * Get all annotation indexed from retrieval server
     */
    private List<Long> getIndexedResource() {
        RetrievalServer server = RetrievalServer.findByDeletedIsNull()
        String URL = server.url+"/api/images"
        List json = JSON.parse(getGetResponse(URL))
        List<Long> resources = new ArrayList<Long>()
        json.each { image ->
            log.debug "resource=" + Long.parseLong(image.id)
            resources.add(Long.parseLong(image.id))
        }
        resources
    }

    private String getGetResponse(String URL) {
        HttpClient client = new HttpClient();
        client.connect(URL, "admin", "admin");
        client.get()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

























    public void indexImage(BufferedImage image,String id, String storage, Map<String,String> properties) {
        if(!RetrievalServer.list().isEmpty()) {
            RetrievalServer server = RetrievalServer.list().get(0)
            doRetrievalIndex(server.url+"/api/images","admin","admin",image,id,storage,properties)
        } else {
            log.info "No retrieval server found"
        }
    }

    public def doRetrievalIndex(String url, String username, String password, BufferedImage image,String id, String storage, Map<String,String> properties) {
        List<String> keys = []
        List<String> values = []
        properties.each {
            keys << it.key
            values << it.value
        }

        url = url+"?id=$id&storage=$storage&keys=${keys.join(";")}&values=${values.join(";")}"

        HttpClient client = new HttpClient()

        log.info "url=$url"
        log.info "username=$username password=$password"

        client.connect(url,username,password)

        MultipartEntity entity = createEntityFromImage(image)

        client.post(entity)

         String response = client.getResponseData()
         int code = client.getResponseCode()
         log.info "code=$code response=$response"
        return [code:code,response:response]
    }

    public MultipartEntity createEntityFromImage(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        MultipartEntity myEntity = new MultipartEntity();
        myEntity.addPart("file", new ByteArrayBody(imageInByte, "file"));
        return myEntity
    }
}
