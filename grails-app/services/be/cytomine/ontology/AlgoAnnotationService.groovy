package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance

import be.cytomine.project.Project
import be.cytomine.security.SecUser

import com.vividsolutions.jts.geom.Geometry

import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode

import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.AnnotationDomain

class AlgoAnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def responseService
    def domainService
    def securityService

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        AlgoAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        AlgoAnnotation.findAllByProject(project)
    }
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        return AlgoAnnotation.findAllByImageAndUser(image, user)
    }

    AlgoAnnotation get(def id) {
        AlgoAnnotation.get(id)
    }

    AlgoAnnotation read(def id) {
        AlgoAnnotation.read(id)
    }

    /**
     * List all algoAnnotation
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        else if (multipleTerm) {
            log.info "multipleTerm"
            //get all algoannotationterm where annotation id is twice

            def data = []




            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob", userList)
                projections {
                    groupProperty("annotationIdent")
                    groupProperty("annotationClassName")
                    countDistinct("term")
                    countDistinct('created', 'createdSort')
                }
                order('createdSort','desc')
            }

            AlgoAnnotationTerm.list().each {
                println "### ALL:"+it
            }

            annotationsWithTerms.each {
                println "### RESULT:"+it
                String id = it[0]
                String className = it[1]
                Long termNumber = (Long)it[2]

                if(termNumber>1) {
                    AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(id,className)
                    if(imageInstanceList.contains(annotation.image)) {
                        data << annotation
                    }
                }
            }
            return data
        }
        else if (noTerm) {
            log.info "noTerm"

            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob", userList)
                projections {
                    groupProperty("annotationIdent")
                }
            }

            println "annotationsWithTerms="+annotationsWithTerms

            //annotationsWithTerms = annotationsWithTerms.collect{it[0]}

            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = []


            if (annotationsWithTerms.size() == 0) {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    order 'created', 'desc'
                })
            } else {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
            long start = new Date().time
            def annotations = AlgoAnnotation.createCriteria().list {
                eq("project", project)
                inList("user", userList)
                inList("image", imageInstanceList)
                fetchMode 'image', FetchMode.JOIN
                fetchMode 'image.baseImage', FetchMode.JOIN
                order 'created', 'desc'
            }
            long end = new Date().time
            log.info "time = " + (end - start) + "ms"
            return annotations
        }
    }


    def listForUserJob(Project project, Term term, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList) {
         if (userList.isEmpty()) return []
         if (imageInstanceList.isEmpty()) return []
         if (imageInstanceList.size() == project.countImages) {
             def data = []
             def criteria = AlgoAnnotationTerm.withCriteria() {
                 eq('project', project)
                 eq('term', term)
                 inList('userJob', userList)
                 projections {
                     groupProperty("annotationIdent")
                     groupProperty("annotationClassName")
                     groupProperty("rate")
                     groupProperty("term.id")
                     groupProperty("expectedTerm.id")
                 }
                 order 'rate', 'desc'
             }

             criteria.each {
                 data << [AlgoAnnotationTerm.retrieveAnnotationDomain(it[0],it[1]),it[2],it[3],it[4]]
             }
             return data
         } else {
             def data = []
             def criteria = AlgoAnnotationTerm.withCriteria() {
                 eq('project', project)
                 eq('term', term)
                 inList('userJob', userList)
                 projections {
                     groupProperty("annotationIdent")
                     groupProperty("annotationClassName")
                     groupProperty("rate")
                     groupProperty("term.id")
                     groupProperty("expectedTerm.id")
                 }
                 order 'rate', 'desc'
             }
             criteria.each {
                 AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(it[0],it[1])
                 if(imageInstanceList.contains(annotation.image))
                    data << [annotation,it[2],it[3],it[4]]
             }
             return data
         }
     }


    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //simplify annotation
        try {
            def data = simplifyPolygon(json.location)
            json.location = new WKTWriter().write(data.geometry)
            json.geometryCompression = data.rate
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        //Start transaction
        Transaction transaction = transactionService.start()

        //Synchronzed this part of code, prevent two annotation to be add at the same time
        synchronized (this.getClass()) {
            //Add annotation user
            json.user = currentUser.id
            //Add Annotation
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
            def annotationID = result?.data?.userannotation?.id
            log.info "annotation=" + annotationID + " json.term=" + json.term
            //Stop transaction
            transactionService.stop()

            return result
        }
    }

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def annotation = AlgoAnnotation.read(json.id)
            def data = simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)
        return result
    }

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def delete(def domain, def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(json.id, currentUser, transaction)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        log.info "Remove " + json.id + " from retrieval"
        return result
    }


    def deleteAnnotation(Long idAnnotation, SecUser currentUser, Transaction transaction) {
        return deleteAnnotation(idAnnotation, currentUser, true, transaction)
    }

    def deleteAnnotation(AnnotationDomain annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {

        log.info "*** deleteAnnotation1.vesion=" + annotation.version

        if (annotation) {
           //TODO:: delete all domain that reference the deleted domain
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    def deleteAnnotation(Long idAnnotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        log.info "Delete algoannotation: " + idAnnotation
        AlgoAnnotation annotation = AlgoAnnotation.read(idAnnotation)
        return deleteAnnotation(annotation, currentUser, printMessage, transaction)
    }

    private def simplifyPolygon(String form) {
         //TODO:: extract this method
        Geometry annotationFull = new WKTReader().read(form);

        Geometry lastAnnotationFull = annotationFull
        double ratioMax = 1.6d
        double ratioMin = 2d
        /* Number of point (ex: 500 points) */
        double numberOfPoint = annotationFull.getNumPoints()
        /* Maximum number of point that we would have (500/5 (max 150)=max 100 points)*/
        double rateLimitMax = Math.min(numberOfPoint / ratioMax, 150)
        /* Minimum number of point that we would have (500/10 (min 10 max 100)=min 50 points)*/
        double rateLimitMin = Math.min(Math.max(numberOfPoint / ratioMin, 10), 100)
        /* Increase value for the increment (allow to converge faster) */
        float incrThreshold = 0.25f
        double increaseIncrThreshold = numberOfPoint / 100d
        float i = 0;
        /* Max number of loop (prevent infinite loop) */
        int maxLoop = 500
        double rate = 0

        Boolean isPolygonAndNotValid =  (annotationFull instanceof com.vividsolutions.jts.geom.Polygon && !((Polygon)annotationFull).isValid())
        while (numberOfPoint > rateLimitMax && maxLoop > 0) {
            rate = i
            if (isPolygonAndNotValid) {
                lastAnnotationFull = TopologyPreservingSimplifier.simplify(annotationFull, rate)
            } else {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull, rate)
            }
            if (lastAnnotationFull.getNumPoints() < rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i = i + (incrThreshold * increaseIncrThreshold); maxLoop--;
        }
        return [geometry: lastAnnotationFull, rate: rate]
    }

    private def simplifyPolygon(String form, double rate) {
        //TODO:: extract this method
       Geometry annotation = new WKTReader().read(form);
        Boolean isPolygonAndNotValid =  (annotation instanceof com.vividsolutions.jts.geom.Polygon && !((Polygon)annotationFull).isValid())
        if (isPolygonAndNotValid) {
            //Preserving polygon shape but slower than DouglasPeuker
            annotation = TopologyPreservingSimplifier.simplify(annotation, rate)
        } else {
            annotation = DouglasPeuckerSimplifier.simplify(annotation, rate)
        }
        return [geometry: annotation, rate: rate]
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(AlgoAnnotation.createFromDataWithId(json), printMessage)
    }

    def create(AlgoAnnotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AlgoAnnotation.get(json.id), printMessage)
    }

    def destroy(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new AlgoAnnotation(), json), printMessage)
    }

    def edit(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AlgoAnnotation createFromJSON(def json) {
        return AlgoAnnotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        AlgoAnnotation annotation = AlgoAnnotation.get(json.id)
        if (!annotation) throw new ObjectNotFoundException("AlgoAnnotation " + json.id + " not found")
        return annotation
    }

}
