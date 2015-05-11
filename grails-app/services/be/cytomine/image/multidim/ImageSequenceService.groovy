package be.cytomine.image.multidim

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
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
    def securityACLService

    def currentDomain() {
        return ImageSequence
    }

    def read(def id) {
        def image = ImageSequence.read(id)
        if(image) {
            securityACLService.check(image.container(),READ)
        }
        image
    }

    def get(def id) {
        def image = ImageSequence.get(id)
        if(image) {
            securityACLService.check(image.container(),READ)
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
            return [slice: null,zStack:null,time:null,channel:null, imageGroup:null]
        }

        def poss = ImageSequence.findAllByImageGroup(imageSeq.imageGroup)

        def z = []
        def t = []
        def c = []
        def s = []

        poss.each {
            z << it.zStack
            t << it.time
            c << it.channel
            s << it.slice
        }

        z = z.unique().sort()
        t = t.unique().sort()
        c = c.unique().sort()
        s = s.unique().sort()
        return [slice:s,zStack:z,time:t,channel:c, imageGroup:imageSeq.imageGroup.id,c:imageSeq.channel,z:imageSeq.zStack,s:imageSeq.slice,t:imageSeq.time]
    }

    //channel,zStack,slice,time
    def get(ImageGroup imageGroup,Integer channel,Integer zStack,Integer slice, Integer time) {
        def data = ImageSequence.findWhere([imageGroup:imageGroup,slice:slice,zStack:zStack,time:time,channel:channel])
        if (!data) {
             throw new ObjectNotFoundException("There is no sequence value for this image group [${imageGroup.id}] and theses values=[$channel,$zStack,$slice,$time,]")
        }
        data
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        securityACLService.check(json.imageGroup,ImageGroup,"container",READ)
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
        securityACLService.check(domain.container(),READ)

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
        securityACLService.check(domain.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id,  domain.image.id, domain.imageGroup.id]
    }
}
