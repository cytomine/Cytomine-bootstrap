package be.cytomine.image

import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.Description
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import groovy.sql.Sql
import org.hibernate.FetchMode

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * TODO:: refactor + doc!!!!!!!
 */
class ImageInstanceService extends ModelService {

    static transactional = false

    def cytomineService
    def transactionService
    def userAnnotationService
    def algoAnnotationService
    def dataSource
    def reviewedAnnotationService
    def imageSequenceService
    def propertyService
    def annotationIndexService

    def currentDomain() {
        return ImageInstance
    }

    def read(def id) {
        def image = ImageInstance.read(id)
        if(image) {
            SecurityACL.check(image.container(),READ)
            checkDeleted(image)
            //println image.id+"=>"+image.deleted + " version"+image.version
        }
        image
    }


    def list(Project project) {
        SecurityACL.check(project,READ)

        def images = ImageInstance.createCriteria().list {
            createAlias("baseImage", "i")
            eq("project", project)
            isNull("parent")
            order("i.created", "desc")
            fetchMode 'baseImage', FetchMode.JOIN
            isNull("deleted")
        }
        return images
    }

    /**
     * Get all image id from project
     */
    public List<Long> getAllImageId(Project project) {
        SecurityACL.check(project,READ)

        //better perf with sql request
        String request = "SELECT a.id FROM image_instance a WHERE project_id="+project.id  + " AND parent_id IS NULL AND deleted IS NULL"
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    def list(User user) {
        SecurityACL.checkIsSameUser(user,cytomineService.currentUser)
        def data = []

        //user_image already filter nested image
        new Sql(dataSource).eachRow("select * from user_image where user_image_id = ? order by original_filename",[user.id]) {
            data << [id:it.id, filename:it.filename, originalFilename: it.original_filename, projectName:it.project_name,  project:it.project_id]
        }
        return data
    }

    def listLastOpened(User user, Long offset = null, Long max = null) {
        //get id of last open image
        SecurityACL.checkIsSameUser(user,cytomineService.currentUser)
        def data = []

        String offsetString = ""
        if(offset!=null) {
            offsetString = offsetString + " OFFSET " + offset
        }
        if(max!=null) {
            offsetString = offsetString + " LIMIT " + max
        }

        new Sql(dataSource).eachRow("SELECT image_id,extract(epoch from max(user_position.created))*1000 as maxDate\n" +
                "FROM user_position\n" +
                "WHERE user_position.user_id = ?\n" + //no join with image instance / abstract img / project...too heavy
                "GROUP BY image_id \n" +
                "ORDER BY maxDate desc " + offsetString,[user.id]) {
            try {
                ImageInstance image = read(it.image_id)
                 data << [id:it.image_id,date:it.maxDate, thumb: UrlApi.getAbstractImageThumbURL(image.baseImage.id),originalFilename:image.baseImage.originalFilename,project:image.project.id]
            } catch(CytomineException e) {
               //if user has data in user_position but has no access to picture,  ImageInstance.read will throw a forbiddenException
            }
         }
        return data
    }





    def listTree(Project project) {
        SecurityACL.check(project,READ)

        def children = []
        list(project).each { image->
            children << [ id : image.id, key : image.id, title : image.baseImage.originalFilename, isFolder : false, children : []]
        }
        def tree = [:]
        tree.isFolder = true
        tree.hideCheckbox = true
        tree.name = project.getName()
        tree.title = project.getName();
        tree.key = project.getId()
        tree.id = project.getId()
        tree.children = children
        return tree
    }

    def list(Project project, String sortColumn, String sortDirection, String search) {
        SecurityACL.check(project,READ)

        String abstractImageAlias = "ai"
        String _sortColumn = ImageInstance.hasProperty(sortColumn) ? sortColumn : "created"
        _sortColumn = AbstractImage.hasProperty(sortColumn) ? abstractImageAlias + "." + sortColumn : "created"
        String _search = (search != null && search != "") ? "%"+search+"%" : "%"

        return ImageInstance.createCriteria().list() {
            createAlias("baseImage", abstractImageAlias)
            eq("project", project)
            isNull("parent")
            isNull("deleted")
            fetchMode 'baseImage', FetchMode.JOIN
            ilike(abstractImageAlias + ".originalFilename", _search)
            order(_sortColumn, sortDirection)
        }


    }

    private long copyAnnotationLayer(ImageInstance image, User user, ImageInstance based, def usersProject,Task task, double total, double alreadyDone,SecUser currentUser, Boolean giveMe ) {
        log.info "copyAnnotationLayer=$image | $user "
         def alreadyDoneLocal = alreadyDone
         UserAnnotation.findAllByImageAndUser(image,user).each {
             copyAnnotation(it,based,usersProject,currentUser,giveMe)
             log.info "alreadyDone=$alreadyDone total=$total"
             taskService.updateTask(task,Math.min(100,((alreadyDoneLocal/total)*100d).intValue()),"Start to copy ${total.intValue()} annotations...")
             alreadyDoneLocal = alreadyDoneLocal +1
         }
        alreadyDoneLocal
    }


    private def copyAnnotation(UserAnnotation based, ImageInstance dest,def usersProject,SecUser currentUser,Boolean giveMe) {
        log.info "copyAnnotationLayer=${based.id}"

        //copy annotation
        UserAnnotation annotation = new UserAnnotation()
        annotation.created = based.created
        annotation.geometryCompression = based.geometryCompression
        annotation.image = dest
        annotation.location = based.location
        annotation.project = dest.project
        annotation.updated =  based.updated
        annotation.user = (giveMe? currentUser : based.user)
        annotation.wktLocation = based.wktLocation
        userAnnotationService.saveDomain(annotation)

        //copy term

        AnnotationTerm.findAllByUserAnnotation(based).each { basedAT ->
            if(usersProject.contains(basedAT.user.id) && basedAT.term.ontology==dest.project.ontology) {
                AnnotationTerm at = new AnnotationTerm()
                at.user = basedAT.user
                at.term = basedAT.term
                at.userAnnotation = annotation
                userAnnotationService.saveDomain(at)
            }
        }

        //copy description
        Description.findAllByDomainIdent(based.id).each {
            Description description = new Description()
            description.data = it.data
            description.domainClassName = it.domainClassName
            description.domainIdent = annotation.id
            userAnnotationService.saveDomain(description)
        }

        //copy properties
        Property.findAllByDomainIdent(based.id).each {
            Property property = new Property()
            property.key = it.key
            property.value = it.value
            property.domainClassName = it.domainClassName
            property.domainIdent = annotation.id
            userAnnotationService.saveDomain(property)
        }

    }

    public def copyLayers(ImageInstance image,def layers,def usersProject,Task task, SecUser currentUser,Boolean giveMe) {
        taskService.updateTask(task, 0, "Start to copy...")
        double total = 0
        if (task) {
            layers.each { couple ->
                def idImage = Long.parseLong(couple.split("_")[0])
                def idUser = Long.parseLong(couple.split("_")[1])
                def number = annotationIndexService.count(ImageInstance.read(idImage), SecUser.read(idUser))
                total = total + number
            }
        }
        taskService.updateTask(task, 0, "Start to copy $total annotations...")
        double alreadyDone = 0
        layers.each { couple ->
            def idImage = Long.parseLong(couple.split("_")[0])
            def idUser = Long.parseLong(couple.split("_")[1])
            alreadyDone = copyAnnotationLayer(ImageInstance.read(idImage), SecUser.read(idUser), image, usersProject,task, total, alreadyDone,currentUser,giveMe)
        }
        return []
    }


    private def getLayersFromAbstractImage(AbstractImage image, ImageInstance exclude, def currentUsersProject,def layerFromNewImage, Project project = null) {
           //get id of last open image

           def layers = []
           def adminsMap = [:]

           def req1 = getLayersFromAbtrsactImageSQLRequestStr(true,project)
           new Sql(dataSource).eachRow(req1,[image.id,exclude.id]) {
               if(currentUsersProject.contains(it.project) && layerFromNewImage.contains(it.user)) {
                   layers << [image:it.image,user:it.user,projectName:it.projectName,project:it.project,lastname:it.lastname,firstname:it.firstname,username:it.username,admin:it.admin]
                   adminsMap.put(it.image+"_"+it.user,true)
               }

           }

        def req2 = getLayersFromAbtrsactImageSQLRequestStr(false,project)

            new Sql(dataSource).eachRow(req2,[image.id,exclude.id]) {
                if(!adminsMap.get(it.image+"_"+it.user) && currentUsersProject.contains(it.project) && layerFromNewImage.contains(it.user)) {
                    layers << [image:it.image,user:it.user,projectName:it.projectName,project:it.project,lastname:it.lastname,firstname:it.firstname,username:it.username,admin:it.admin]
                }

            }
            return layers

    }

    private String getLayersFromAbtrsactImageSQLRequestStr(boolean admin,Project project = null) {
        return """
            SELECT ii.id as image,su.id as user,p.name as projectName, p.id as project, su.lastname as lastname, su.firstname as firstname, su.username as username, '${admin}' as admin, count_annotation as annotations
            FROM image_instance ii, project p, ${admin? "admin_project" : "user_project" } up, sec_user su, annotation_index ai
            WHERE base_image_id = ?
            AND ii.id <> ?
            AND ii.deleted IS NULL
            AND ii.parent_id IS NULL
            AND ii.project_id = p.id
            AND up.id = p.id
            AND up.user_id = su.id
            AND ai.user_id = su.id
            AND ai.image_id = ii.id
            ${project? "AND p.id = " + project.id  : ""}
            ORDER BY p.name, su.lastname,su.firstname,su.username;
        """

    }



    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.project,Project,READ)
        SecurityACL.checkReadOnly(json.project,Project)
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        log.info "json=$json"
        def project = Project.read(json.project)
        def baseImage = AbstractImage.read(json.baseImage)
        log.info "project=$project baseImage=$baseImage"
        def alreadyExist = ImageInstance.findByProjectAndBaseImage(project,baseImage)

        log.info "alreadyExist=${alreadyExist}"
        if(alreadyExist && alreadyExist.checkDeleted()) {
            //Image was previously deleted, restore it
            SecurityACL.check(alreadyExist.container(),ADMINISTRATION)
            SecurityACL.checkReadOnly(alreadyExist.container())
            def jsonNewData = JSON.parse(alreadyExist.encodeAsJSON())
            jsonNewData.deleted = null
            Command c = new EditCommand(user: currentUser)
            return executeCommand(c,alreadyExist,jsonNewData)
        } else {
            synchronized (this.getClass()) {
                Command c = new AddCommand(user: currentUser)
                return executeCommand(c,null,json)
            }
        }



    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(ImageInstance domain, def jsonNewData) {
        SecurityACL.check(domain.container(),READ)
        SecurityACL.check(jsonNewData.project,Project,READ)
        SecurityACL.checkReadOnly(domain.container())
        SecurityACL.checkReadOnly(jsonNewData.project,Project)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        executeCommand(c,domain,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(ImageInstance domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
//        SecurityACL.check(domain.container(),READ)
//        SecurityACL.checkReadOnly(domain.container())
//        SecUser currentUser = cytomineService.getCurrentUser()
//        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
//        return executeCommand(c,domain,null)

        //We don't delete domain, we juste change a flag
        SecurityACL.check(domain.container(),ADMINISTRATION)
        SecurityACL.checkReadOnly(domain.container())
        def jsonNewData = JSON.parse(domain.encodeAsJSON())
        jsonNewData.deleted = new Date().time
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        c.delete = true
        return executeCommand(c,domain,jsonNewData)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.baseImage?.filename, domain.project.name]
    }

//    def deleteDependentAlgoAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
//        AlgoAnnotation.findAllByImage(image).each {
//            algoAnnotationService.delete(it,transaction)
//        }
//    }
//
//    def deleteDependentReviewedAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
//        ReviewedAnnotation.findAllByImage(image).each {
//            reviewedAnnotationService.delete(it,transaction,null,false)
//        }
//    }
//
//    def deleteDependentUserAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
//        UserAnnotation.findAllByImage(image).each {
//            userAnnotationService.delete(it,transaction,null,false)
//        }
//    }
//
//    def deleteDependentUserPosition(ImageInstance image,Transaction transaction, Task task = null) {
//        UserPosition.findAllByImage(image).each {
//            it.delete()
//        }
//    }
//
//    def deleteDependentAnnotationIndex(ImageInstance image,Transaction transaction, Task task = null) {
//        AnnotationIndex.findAllByImage(image).each {
//            it.delete()
//         }
//    }
//
//    def deleteDependentImageSequence(ImageInstance image, Transaction transaction, Task task = null) {
//        ImageSequence.findAllByImage(image).each {
//            imageSequenceService.delete(it,transaction,null,false)
//        }
//    }
//
//    def deleteDependentProperty(ImageInstance image, Transaction transaction, Task task = null) {
//        Property.findAllByDomainIdent(image.id).each {
//            propertyService.delete(it,transaction,null,false)
//        }
//
//    }
//
//    def deleteDependentNestedImageInstance(ImageInstance image, Transaction transaction,Task task=null) {
//        NestedImageInstance.findAllByParent(image).each {
//            it.delete(flush: true)
//        }
//    }
}
