package be.cytomine.image.multidim

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.READ

class ImageSequenceService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService
    def userAnnotationService
    def algoAnnotationService
    def dataSource
    def reviewedAnnotationService

    def currentDomain() {
        return ImageSequence
    }

    def read(def id) {
        def image = ImageSequence.read(id)
        if(image) {
            SecurityACL.check(image.container(),READ)
        }
        image
    }

    def get(def id) {
        def image = ImageSequence.get(id)
        if(image) {
            SecurityACL.check(image.container(),READ)
        }
        image
    }

    def get(ImageInstance image) {
        ImageSequence.findByImage(image)
    }

    def list(ImageGroup imageGroup) {
        ImageSequence.findAllByImageGroup(imageGroup)
    }

    def getPossibilities(ImageInstance image) {

        def imageSeq = ImageSequence.findByImage(image)
        if (!imageSeq)  {
            return [zStack:null,time:null,channel:null, imageGroup:null]
        }

        def poss = ImageSequence.findAllByImageGroup(imageSeq.imageGroup)
        println "poss=$poss"

        def z = []
        def t = []
        def c = []

        poss.each {
            println "it=$it"
            println "z=$z"
            z << it.zStack
            t << it.time
            c << it.channel
        }

        z = z.unique().sort()
        t = t.unique().sort()
        c = c.unique().sort()
        println "z final=$z"
        return [zStack:z,time:t,channel:c, imageGroup:imageSeq.imageGroup.id]
    }

    def get(ImageGroup imageGroup,Integer zStack,Integer time, Integer channel) {
        def data = ImageSequence.findWhere([imageGroup:imageGroup,zStack:zStack,time:time,channel:channel])
        if (!data) {
             throw new ObjectNotFoundException("There is no sequence value for this image group [${imageGroup.id}] and theses values=[$zStack,$time,$channel]")
        }
        data
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.imageGroup,ImageGroup,"container",READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        synchronized (this.getClass()) {
            Command c = new AddCommand(user: currentUser)
            executeCommand(c,null,json)
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(ImageSequence domain, def jsonNewData) {
        SecurityACL.check(domain.container(),READ)

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
    def delete(ImageSequence domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecurityACL.check(domain.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id,  domain.image.id, domain.imageGroup.id]
    }
}
