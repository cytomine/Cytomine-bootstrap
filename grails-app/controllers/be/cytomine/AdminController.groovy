package be.cytomine

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.test.BasicInstanceBuilder
import com.vividsolutions.jts.io.WKTReader
import geb.Browser
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService

    def index() {
        //testCyto()
        //testImage()
        testRemoveAnnotation()
        testUnion()
    }


    def testRemoveAnnotation() {
        def aid = []
        def atid = []
        UserAnnotation.findAllByUserAndImage(SecUser.read(16),ImageInstance.read(8123101)).each { annotation ->
            aid << annotation.id
              AnnotationTerm.findAllByUserAnnotation(annotation).each { at ->
                  atid << at.id
              }
        }

        atid.each {
            AnnotationTerm.read(it).delete(flush: true)
        }

        aid.each {
            UserAnnotation.read(it).delete(flush: true)
        }

    }

    def testUnion() {
        ImageInstance image = ImageInstance.read(8123101)


        def a1 = BasicInstanceBuilder.getUserAnnotationNotExist()
        a1.location = new WKTReader().read("POLYGON ((83816 50999, 83822 50996, 83824 50997, 83828 50995, 83830 50993, 83830 50988, 83829 50986, 83827 50984, 83823 50982, 83821 50982, 83818 50980, 83816 50980, 83812 50982, 83807 50987, 83807 50991, 83812 50997, 83816 50999))")
        a1.image = image
        a1.project = image.project
        saveDomain(a1)

        def a2 = BasicInstanceBuilder.getUserAnnotationNotExist()
        a2.location = new WKTReader().read("POLYGON ((83893 51230, 83899 51227, 83902 51224, 83905 51223, 83912 51216, 83905 51209, 83902 51208, 83900 51206, 83896 51204, 83895 51205, 83894 51204, 83890 51206, 83883 51213, 83883 51215, 83882 51216, 83882 51221, 83885 51224, 83885 51225, 83887 51227, 83893 51230))")

        a2.image = image
        a2.project = image.project
        assert a2.save(flush: true)  != null


       def a3 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a3.location = new WKTReader().read("POLYGON ((83586 51264, 83588 51264, 83592 51262, 83599 51257, 83599 51252, 83600 51251, 83600 51242, 83596 51238, 83595 51235, 83592 51232, 83584 51228, 83576 51232, 83574 51234, 83572 51238, 83568 51242, 83568 51247, 83571 51250, 83573 51254, 83580 51261, 83586 51264))")

       a3.image = image
       a3.project = image.project
       assert a3.save(flush: true)  != null

       def a4 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a4.location = new WKTReader().read("POLYGON ((84292 50836, 84292 50815, 84266 50815, 84270 50823, 84270 50826, 84274 50828, 84277 50828, 84292 50836))")

       a4.image = image
       a4.project = image.project
       assert a4.save(flush: true)  != null

       def a5 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a5.location = new WKTReader().read("POLYGON ((84470 51065, 84471 51065, 84471 50926, 84453 50935, 84451 50935, 84385 50902, 84373 50890, 84369 50888, 84360 50888, 84353 50891, 84338 50883, 84338 50903, 84340 50906, 84340 50946, 84326 50960, 84326 50966, 84327 50967, 84329 50981, 84338 50991, 84338 50997, 84340 50999, 84351 51004, 84373 51026, 84375 51029, 84378 51030, 84380 51032, 84384 51034, 84389 51034, 84402 51041, 84403 51040, 84408 51043, 84410 51043, 84413 51041, 84417 51043, 84420 51043, 84423 51041, 84470 51065))")

       a5.image = image
       a5.project = image.project
       assert a5.save(flush: true)  != null


       def a6 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a6.location = new WKTReader().read("POLYGON ((84470 51182, 84471 51182, 84471 51153, 84464 51156, 84455 51165, 84455 51172, 84456 51173, 84456 51175, 84459 51178, 84460 51178, 84463 51180, 84467 51180, 84470 51182))")

       a6.image = image
       a6.project = image.project
       assert a6.save(flush: true)  != null

       def a7 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a7.location = new WKTReader().read("POLYGON ((84311 51189, 84315 51185, 84315 51177, 84314 51175, 84307 51168, 84307 51124, 84296 51112, 84296 51110, 84294 51108, 84290 51106, 84289 51107, 84286 51105, 84284 51105, 84273 51111, 84272 51110, 84263 51114, 84261 51116, 84260 51123, 84257 51128, 84257 51134, 84258 51135, 84258 51137, 84262 51141, 84262 51147, 84266 51149, 84292 51175, 84294 51178, 84295 51178, 84301 51184, 84311 51189))")

       a7.image = image
       a7.project = image.project
       assert a7.save(flush: true)  != null

       def a8 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a8.location = new WKTReader().read("POLYGON ((84043 51324, 84067 51324, 84067 51303, 84064 51300, 84060 51298, 84058 51298, 84057 51297, 84049 51301, 84046 51304, 84046 51310, 84043 51313, 84043 51324))")

       a8.image = image
       a8.project = image.project
       assert a8.save(flush: true)  != null


       def a9 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a9.location = new WKTReader().read("POLYGON ((84555 51324, 84983 51324, 84982 50890, 84958 50878, 84954 50879, 84949 50876, 84942 50880, 84917 50867, 84906 50871, 84902 50875, 84902 50882, 84883 50901, 84883 50921, 84890 50928, 84890 50968, 84869 50989, 84829 51009, 84798 50993, 84792 50993, 84783 50988, 84778 50988, 84746 51004, 84701 50982, 84679 50960, 84675 50958, 84659 50963, 84648 50973, 84607 50993, 84567 50973, 84557 50963, 84550 50960, 84529 50939, 84529 50898, 84550 50877, 84573 50866, 84581 50856, 84578 50842, 84572 50834, 84572 50815, 84474 50815, 84474 51254, 84534 51284, 84555 51305, 84555 51324))")

       a9.image = image
       a9.project = image.project
       assert a9.save(flush: true)  != null


       def a10 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a10.location = new WKTReader().read("POLYGON ((85488 51324, 85495 51324, 85495 51318, 85494 51318, 85493 51319, 85490 51320, 85488 51322, 85488 51324))")

       a10.image = image
       a10.project = image.project
       assert a10.save(flush: true)  != null


       def a11 = BasicInstanceBuilder.getUserAnnotationNotExist()
       a11.location = new WKTReader().read("POLYGON ((84986 51324, 85045 51324, 85045 51305, 85063 51286, 85063 51240, 85093 51210, 85093 51182, 85114 51161, 85154 51141, 85230 51181, 85251 51202, 85251 51242, 85241 51252, 85240 51263, 85246 51270, 85254 51275, 85263 51268, 85264 51263, 85264 51251, 85255 51242, 85255 51202, 85276 51181, 85316 51161, 85321 51162, 85346 51149, 85360 51156, 85367 51153, 85370 51150, 85372 51138, 85368 51133, 85347 51123, 85326 51102, 85325 51094, 85318 51089, 85304 51089, 85285 51099, 85269 51101, 85223 51078, 85201 51056, 85201 51029, 85198 51026, 85190 51022, 85170 51024, 85130 51004, 85104 50978, 85104 50958, 85102 50956, 85093 50952, 85076 50958, 85042 50941, 85034 50945, 85028 50951, 84986 50972, 84986 51324))")

       a11.image = image
       a11.project = image.project
       assert a11.save(flush: true)  != null

       def at1 = BasicInstanceBuilder.getAnnotationTermNotExist(a1,true)
       def at2 = BasicInstanceBuilder.getAnnotationTermNotExist(a2,true)
       def at3 = BasicInstanceBuilder.getAnnotationTermNotExist(a3,true)
       def at4 = BasicInstanceBuilder.getAnnotationTermNotExist(a4,true)
       def at5 = BasicInstanceBuilder.getAnnotationTermNotExist(a5,true)
       def at6 = BasicInstanceBuilder.getAnnotationTermNotExist(a6,true)
       def at7 = BasicInstanceBuilder.getAnnotationTermNotExist(a7,true)
       def at8 = BasicInstanceBuilder.getAnnotationTermNotExist(a8,true)
       def at9 = BasicInstanceBuilder.getAnnotationTermNotExist(a9,true)
       def at10 = BasicInstanceBuilder.getAnnotationTermNotExist(a10,true)
       def at11 = BasicInstanceBuilder.getAnnotationTermNotExist(a11,true)
       at1.term = Term.read(8844845)
       at2.term = at1.term
       at3.term = at1.term
       at4.term = at1.term
       at5.term = at1.term
       at6.term = at1.term
       at7.term = at1.term
       at8.term = at1.term
       at9.term = at1.term
       at10.term = at1.term
       at11.term = at1.term

        at2.save(flush:true)
       at3.save(flush:true)
       at4.save(flush:true)
       at5.save(flush:true)
       at6.save(flush:true)
       at7.save(flush:true)
       at8.save(flush:true)
       at9.save(flush:true)
       at10.save(flush:true)
       at11.save(flush:true)

        render "ok"
    }




































    def testImage() {
        int idProject = 57
        Project project = Project.read(idProject)
        def images = ImageInstance.findAllByProject(project)
        def data = []
        def filenames = images.collect{
            println it.baseImage.filename
            it.baseImage.filename.split("\\.")[0]
        }
        println filenames
        filenames = filenames.unique().sort()

        filenames.each { filename ->
            images = ImageInstance.findAllByProjectAndBaseImageInList(project,AbstractImage.findAllByFilenameLike("${filename}%"))
//            println "images=$images"
            println "groupame=$filename"

            ImageGroup imageGroup = new ImageGroup(project:project,name:filename)
            saveDomain(imageGroup)




            def slices = []
            //sort slice index (convert to long to avoid: string sort like 10, 1, 20...)
            images.each { img ->
                slices << Long.parseLong(img.baseImage.filename.split("\\.")[1].split("_")[0])
            }
            slices.sort()

            slices.eachWithIndex { slice, index ->
                def regex1 = "${filename}.${slice}\\_%"
                def regex2 = "${filename}.${slice}\\.%"
                //println regex1 + "==="+regex2
                def imagesFinal = ImageInstance.findAllByProjectAndBaseImageInList(project,AbstractImage.findAllByFilenameLikeOrFilenameLike(regex1,regex2))


                if(imagesFinal.size()==1) {
                     def image =imagesFinal.get(0)


                    ImageSequence imageSequence = new  ImageSequence(image:image,channel:0,zStack:0,slice:index,time:0,imageGroup:imageGroup)
                    saveDomain(imageSequence)
                } else {
                    println "error=${imagesFinal}"
                    throw new Exception();
                }
            }





        }


        def all = ImageGroup.findAllByProject(project)

                   all.each { imageG ->
                       def c = ImageSequence.createCriteria()
                       def imgSeq = c.list {
                            eq('imageGroup',imageG)
                            order('channel', 'asc')
                           order('zStack', 'asc')
                           order('slice', 'asc')
                           order('time', 'asc')
                       }
                       println "**************************************"+imageG.name
                       imgSeq.each {
                            println it.image.baseImage.filename

                       }

                   }




    }

    def saveDomain(def newObject) {
        newObject.checkAlreadyExist()
        if (!newObject.validate()) {
            log.error newObject.errors
            log.error newObject.retrieveErrors().toString()
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: true)) {
            throw new InvalidRequestException(newObject.retrieveErrors().toString())
        }
    }

    def testCyto() {
        Browser.drive {
            println "toto!!!"
            go "http://localhost:8080"
           // go "http://google.com/ncr"
//            println it
//            println $("body")
//            println $("body").text()
//            println $("body").children()
//
//            println $("img")
//            println $("input")

            printChildren($("body"),0)

            Thread.sleep(10000)

            waitFor {
                title.endsWith("Google Search")
            }

            printChildren($("body"),0)

              // make sure we actually got to the page
             assert title == "Google"

            $("input", name: "q").value("wikipedia")

            $("input", name: "btnG").click()

            waitFor { title.endsWith("Google Search") }

        }
    }

    def printChildren(def elem, int deep) {

        def prefix =""
        for(int i=0;i<deep;i++) {
            prefix = prefix + "\t"
        }

        println prefix + elem
//        println elem
        def childrens = elem.children()
        childrens.each {
            printChildren(it,deep+1)
        }
    }


    def testGeb() {
        Browser.drive {
            println "toto!!!"
            go "http://google.com/ncr"

            println it
            println $("body")
            println $("img")
            println $("input")

              // make sure we actually got to the page
             assert title == "Google"

            $("input", name: "q").value("wikipedia")

            $("input", name: "btnG").click()

            waitFor { title.endsWith("Google Search") }

        }
    }

}
