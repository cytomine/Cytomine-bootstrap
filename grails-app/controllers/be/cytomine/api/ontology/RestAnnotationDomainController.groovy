package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import grails.converters.JSON

import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import javax.imageio.ImageIO
import be.cytomine.ontology.Ontology
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.ReviewedAnnotation
import org.omg.PortableServer.POAPackage.ObjectNotActive
import javassist.tools.rmi.ObjectNotFoundException
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.ontology.AlgoAnnotation
import groovy.sql.Sql
import com.vividsolutions.jts.geom.Geometry

class RestAnnotationDomainController extends RestController {

    def exportService
    def grailsApplication
    def userAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def mailService
    def dataSource
    def algoAnnotationService
    def reviewedAnnotationService

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())

        if (project) {
            Collection<SecUser> userList = []
            if (params.users != null && params.users != "null" && params.users != "") {
                userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
                if(!userList.isEmpty() && userList.get(0)?.algo()) {
                   forward(controller: "restAlgoAnnotation", action: "listByProject")
               } else {
                   forward(controller: "restUserAnnotation", action: "listByProject")
               }
            }
            else {
                forward(controller: "restUserAnnotation", action: "listByProject")
            }
        }
        else responseNotFound("Project", params.id)
    }


    def listByImageAndUser = {
        def user = SecUser.read(params.idUser)
        if (user) {
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "listByImageAndUser")
            } else {
                forward(controller: "restUserAnnotation", action: "listByImageAndUser")
            }
        }
        else if (!user) responseNotFound("User", params.idUser)
    }

    def listAnnotationByTerm = {
        Term term = termService.read(params.long('idterm'))
        if (term) {
            def allAnnotations = []
            List<Project> projects = term.ontology.projects
            projects.each {
                allAnnotations.addAll(userAnnotationService.list(it))
                allAnnotations.addAll(algoAnnotationService.list(it))
            }
            responseSuccess(allAnnotations)
        }
        else responseNotFound("Term", params.idterm)
    }


    def listAnnotationByProjectAndTerm = {
        log.info "listAnnotationByProjectAndTerm"
        Project project = projectService.read(params.long('idproject'), new Project())

        Collection<SecUser> userList = []
        if (params.users != null && params.users != "null" && project) {
            if (params.users != "") {
                userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
                if(!userList.isEmpty() && userList.get(0)?.algo()) {
                   forward(controller: "restAlgoAnnotation", action: "listAnnotationByProjectAndTerm")
               } else {
                   forward(controller: "restUserAnnotation", action: "listAnnotationByProjectAndTerm")
               }
            }
        }
        else {
            forward(controller: "restUserAnnotation", action: "listAnnotationByProjectAndTerm")
        }


    }


    def downloadDocumentByProject = {  //and filter by users and terms !
        // Export service provided by Export plugin

        def users = []
        if (params.users != null && params.users != "") {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }

        if(!users.isEmpty() && SecUser.read(users.first()).algo()) {
            forward(controller: "restAlgoAnnotation", action: "downloadDocumentByProject")
        } else   forward(controller: "restUserAnnotation", action: "downloadDocumentByProject")
    }

    def show = {
        AnnotationDomain annotation = userAnnotationService.read(params.long('id'))
        if(annotation) {
            log.info "Annotation is userAnnotation"
            forward(controller: "restUserAnnotation", action: "show")
        }
        else {
            annotation = algoAnnotationService.read(params.long('id'))
            if(annotation) {
                log.info "Annotation is algoAnnotation"
                forward(controller: "restAlgoAnnotation", action: "show")
            } else {
                forward(controller: "restReviewedAnnotation", action: "show")
            }
        }
    }


    def add = {
        try {
            SecUser user = cytomineService.currentUser
            def result
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "add")
            } else {
                forward(controller: "restUserAnnotation", action: "add")
            }
            //responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def update= {
        //def json = request.JSON
        log.info "update generic"
        if(params.getBoolean('fill'))
            forward(action: "fillAnnotation")
        else {

            try {
                log.info "update classic"
                SecUser user = cytomineService.currentUser
                def result
                if(user.algo()) {
                    forward(controller: "restAlgoAnnotation", action: "update")
                } else {
                    AnnotationDomain annotation = UserAnnotation.read(params.getLong("id"))

                    if(annotation)
                        forward(controller: "restUserAnnotation", action: "update")
                    else {
                        annotation = ReviewedAnnotation.read(params.getLong("id"))

                        if(annotation) {
                            if(annotation.user!=user) throw new ForbiddenException("You cannot update this annotation! Only ${annotation.user.username} can do that!");
                            forward(controller: "restReviewedAnnotation", action: "update")
                        }
                        else throw new ObjectNotFoundException("Annotation not found with id " + params.id)
                    }
                }
                //responseResult(result)
            } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
            }

        }

    }

    def delete = {
        try {
            SecUser user = cytomineService.currentUser
            def result
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "delete")
            } else {
                forward(controller: "restUserAnnotation", action: "delete")
            }
            //responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def fillAnnotation = {
        log.info "fillAnnotation"
        try {
            AnnotationDomain annotation = getAnnotationDomain(params.long('id'))
            if (!annotation) throw new ObjectNotFoundException("Review Annotation ${params.long('id')} not found!")

//            if(annotation.image.reviewUser && annotation.image.reviewUser.id!=cytomineService.currentUser.id)
//                throw new WrongArgumentException("You must be the image reviewer to modify annotation. Image reviewer is ${annotation.image.reviewUser?.username}.")

            def response = [:]

            //Is the first polygon always the big 'boundary' polygon?
            String newGeom = "POLYGON (" + getFirstLocation(annotation.location.toString()) +"))"
            println "new geometry = "+ newGeom
            println "old geometry = "+ annotation.location.toString().size()
            println "new geometry = "+ newGeom.size()
            def json = JSON.parse(annotation.encodeAsJSON())
            json.location = newGeom

            if(annotation.algoAnnotation) responseSuccess(algoAnnotationService.update(annotation,json))
            else if(annotation.reviewedAnnotation) responseSuccess(reviewedAnnotationService.update(annotation,json))
            else responseSuccess(userAnnotationService.update(annotation,json))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private String getFirstLocation(String form) {
        int i = 0
        int start, stop
        while(form.charAt(i)!='(') i++

        while(form.charAt(i+1)=='(') i++

        start = i
        println "mustbe(=" + form.charAt(i)
        println "mustNotbe(=" + form.charAt(i+1)

        while(form.charAt(i)!=')') i++

        stop = i
        println "mustbe)=" + form.charAt(i)
        println "mustNotbe)=" + form.charAt(i-1)

        print "final="+ form.substring(start,stop+1)

        form.substring(start,stop+1)

    }




    def addCorrection = {
        def json = request.JSON
        println "json="+json
        String location = json.location
        boolean review = json.review
        long idImage = json.image
        boolean remove = json.remove
        println "location="+location
        println "review="+review
        println "idImage="+idImage
        try {
            long idReviewedAnnotation = -1
            long idUserAnnotation = -1

            //if review mode, priority is done to reviewed annotation correction
            if(review) {
                idReviewedAnnotation = findReviewedAnnotationIdThatTouch(location,idImage,cytomineService.currentUser.id)
            }

            //there is no reviewed intersect annotation or user is not in review mode
            if(idReviewedAnnotation==-1) {
                idUserAnnotation = findUserAnnotationIdThatTouch(location,idImage,cytomineService.currentUser.id)
            }

            //there is no user/reviewed intersect
            if (idUserAnnotation == -1 && idReviewedAnnotation== -1) throw new WrongArgumentException("There is no intersect annotation!")

            def result
            if(idUserAnnotation!=-1) {
                def domain = userAnnotationService.read(idUserAnnotation)
                String fullLocation
                if(remove) fullLocation = doDiffAnnotation(domain.location.toString(),location)
                else fullLocation = doUnionAnnotation(domain.location.toString(),location)
                def jsonUpdate = JSON.parse(domain.encodeAsJSON())
                jsonUpdate.location = fullLocation
                result = userAnnotationService.update(domain,jsonUpdate)
            } else {
                def domain = reviewedAnnotationService.read(idReviewedAnnotation)
                println "SHOULD BE POLYGON:"+domain.location.toText()
                String fullLocation
                if(remove) fullLocation = doDiffAnnotation(domain.location.toString(),location)
                else fullLocation = doUnionAnnotation(domain.location.toString(),location)
                def jsonUpdate = JSON.parse(domain.encodeAsJSON())
                jsonUpdate.location = fullLocation
                result = reviewedAnnotationService.update(domain,jsonUpdate)
            }
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    Long findUserAnnotationIdThatTouch(String location, long idImage, long idUser) {

        String request = "SELECT annotation.id\n" +
                "FROM user_annotation annotation\n" +
                "WHERE annotation.image_id = $idImage\n" +
                "AND user_id = $idUser\n" +
                "AND ST_Intersects(annotation.location,GeometryFromText('"+location+"',0));"


        println "REQUEST=" + request
        def sql = new Sql(dataSource)

        def data = []
        long id = -1

        sql.eachRow(request) {
            if(id!=-1) {
                throw new WrongArgumentException("There is more than one intersect annotation!")
            }
            id = it[0]
        }
        println "findUserAnnotationIdThatTouch="+ id
        return id
    }

    Long findReviewedAnnotationIdThatTouch(String location, long idImage, long idUser) {
        String request = "SELECT annotation.id\n" +
                "FROM reviewed_annotation annotation\n" +
                "WHERE annotation.image_id = $idImage\n" +
                "AND user_id = $idUser\n" +
                "AND ST_Intersects(annotation.location,GeometryFromText('"+location+"',0));"

        println "REQUEST=" + request
        def sql = new Sql(dataSource)

        def data = []
        long id = -1

        sql.eachRow(request) {
            if(id!=-1) {
                throw new WrongArgumentException("There is more than one intersect annotation!")
            }
            id = it[0]
        }
        println "findReviewedAnnotationIdThatTouch="+ id
        return id


    }

    String doUnionAnnotation(String basedLocation, String locationToAdd) {
        println "basedLocation:"+basedLocation
        println "locationToAdd:"+locationToAdd

        Geometry geometry = new WKTReader().read(basedLocation).union(new WKTReader().read(locationToAdd))
        String fullLocation = geometry.toText()
        return fullLocation
    }
    String doDiffAnnotation(String basedLocation, String locationToAdd) {
        println "basedLocation:"+basedLocation
        println "locationToAdd:"+locationToAdd
        Geometry geometry = new WKTReader().read(basedLocation).difference(new WKTReader().read(locationToAdd))
        return geometry.toText()
    }
}
