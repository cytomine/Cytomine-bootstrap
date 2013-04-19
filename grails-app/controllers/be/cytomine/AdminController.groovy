package be.cytomine

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.project.Project
import geb.Browser
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService

    def index() {
        //testCyto()
        testImage()
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
