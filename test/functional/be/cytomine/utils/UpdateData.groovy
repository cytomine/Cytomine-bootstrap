package be.cytomine.utils

import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Instrument

import be.cytomine.laboratory.Sample
import be.cytomine.security.User
import be.cytomine.image.Mime
import grails.converters.JSON

import org.apache.commons.logging.LogFactory
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.security.UserJob
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Discipline
import be.cytomine.security.Group
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobParameter
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import be.cytomine.ontology.AnnotationFilter

/**
 * User: lrollus
 * Date: 8/01/13
 * GIGA-ULg
 * 
 */
class UpdateData {

    private static final log = LogFactory.getLog(this)

    static def createUpdateSet(AbstractImage AbstractImage) {
        String oldFilename = "oldName"
        String newFilename = "newName"

        String oldGeom = "POINT (1111 1111)"
        String newGeom = "POINT (9999 9999)"

        Instrument oldScanner = BasicInstance.createOrGetBasicScanner()
        Instrument newScanner = BasicInstance.getNewScannerNotExist()
        newScanner.save(flush:true)

        Sample oldSlide = BasicInstance.createOrGetBasicSlide()
        Sample newSlide = BasicInstance.getBasicSlideNotExist()
        newSlide.save(flush:true)

        User oldUser = BasicInstance.createOrGetBasicUser()
        User newUser = BasicInstance.getBasicUserNotExist()
        newUser.save(flush:true)


        String oldPath = "oldPath"
        String newPath = "newPath"

        Mime oldMime = BasicInstance.createOrGetBasicMime() //TODO: replace by a mime different with image server
        Mime newMime = BasicInstance.createOrGetBasicMime()  //jp2

        Integer oldWidth = 1000
        Integer newWidth = 9000

        Integer oldHeight = 10000
        Integer newHeight = 900000


        def mapNew = ["filename":newFilename,"geom":newGeom,"scanner":newScanner,"sample":newSlide,"path":newPath,"mime":newMime,"width":newWidth,"height":newHeight,"user":newUser]
        def mapOld = ["filename":oldFilename,"geom":oldGeom,"scanner":oldScanner,"sample":oldSlide,"path":oldPath,"mime":oldMime,"width":oldWidth,"height":oldHeight,"user":oldUser]

        /* Create a old AbstractImage with point 1111 1111 */
        /* Create a old image */
        log.info("create image")
        AbstractImage imageToAdd = BasicInstance.createOrGetBasicAbstractImage()
        imageToAdd.filename = oldFilename
        imageToAdd.scanner = oldScanner
        imageToAdd.sample = oldSlide
        imageToAdd.path = oldPath
        imageToAdd.mime = oldMime
        imageToAdd.width = oldWidth
        imageToAdd.height = oldHeight
        imageToAdd.save(flush:true)

        /* Encode a new image to modify */
        AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.filename = newFilename
        jsonUpdate.scanner = newScanner.id
        jsonUpdate.slide = newSlide.id
        jsonUpdate.path = newPath
        jsonUpdate.mime = newMime.extension
        jsonUpdate.width = newWidth
        jsonUpdate.height = newHeight
        jsonImage = jsonUpdate.encodeAsJSON()

        return ['oldData':imageToEdit,'newData':jsonImage,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AlgoAnnotation annotation) {
        log.info "update algoAnnotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        UserJob oldUser = annotation.user
        UserJob newUser = annotation.user

        def mapNew = ["geom":newGeom,"user":newUser]
        def mapOld = ["geom":oldGeom,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create algoAnnotation")
        AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = oldUser
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = newUser.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AnnotationDomain annotation) {
        log.info "update AnnotationDomain:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        def oldUser = annotation.user
        def newUser = annotation.user

        def mapNew = ["geom":newGeom,"user":newUser]
        def mapOld = ["geom":oldGeom,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create AnnotationDomain")

        def jsonAnnotation

        if(annotation instanceof UserAnnotation) {
            log.info("create userAnnotation")
            UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = oldUser
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = newUser.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else if(annotation instanceof AlgoAnnotation) {
            AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = oldUser
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = newUser.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else {
            throw new Exception("Type is not supported!")
        }
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Discipline discipline) {
        log.info "update discipline"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 discipline */
         Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
         disciplineToAdd.name = oldName
         assert (disciplineToAdd.save(flush:true) != null)
         /* Encode a niew discipline Name2*/
         Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
         def jsonDiscipline = disciplineToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonDiscipline)
         jsonUpdate.name = newName
         jsonDiscipline = jsonUpdate.encodeAsJSON()
        return ['oldData':discipline,'newData':jsonDiscipline,'mapOld':mapOld,'mapNew':mapNew]
    }
    
    static def createUpdateSet(Sample sample) {
        log.info "update sample"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 sample */
         Sample sampleToAdd = BasicInstance.createOrGetBasicSample()
         sampleToAdd.name = oldName
         assert (sampleToAdd.save(flush:true) != null)
         /* Encode a niew sample Name2*/
         Sample sampleToEdit = Sample.get(sampleToAdd.id)
         def jsonSample = sampleToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSample)
         jsonUpdate.name = newName
         jsonSample = jsonUpdate.encodeAsJSON()
        return ['oldData':sample,'newData':jsonSample,'mapOld':mapOld,'mapNew':mapNew]
    }    


    static def createUpdateSet(Group group) {
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 group */
         Group groupToAdd = BasicInstance.createOrGetBasicGroup()
         groupToAdd.name = oldName
         assert (groupToAdd.save(flush:true) != null)
         /* Encode a niew group Name2*/
         Group groupToEdit = Group.get(groupToAdd.id)
         def jsonGroup = groupToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonGroup)
         jsonUpdate.name = newName
         jsonGroup = jsonUpdate.encodeAsJSON()
        return ['oldData':group,'newData':jsonGroup,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(ImageInstance image) {
        log.info "update ImageInstance"
        Project oldProject = BasicInstance.createOrGetBasicProject()
        Project newProject = BasicInstance.getBasicProjectNotExist()
        newProject.save(flush: true)

        AbstractImage oldImage = BasicInstance.createOrGetBasicAbstractImage()
        AbstractImage newImage = BasicInstance.getBasicAbstractImageNotExist()
        newImage.save(flush: true)

        User oldUser = BasicInstance.createOrGetBasicUser()
        User newUser = BasicInstance.getBasicUserNotExist()
        newUser.save(flush: true)

        def mapNew = ["project": newProject, "baseImage": newImage, "user": newUser]
        def mapOld = ["project": oldProject, "baseImage": oldImage, "user": oldUser]


        /* Create a old image */
        ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        imageToAdd.project = oldProject;
        imageToAdd.baseImage = oldImage;
        imageToAdd.user = oldUser;
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.project = newProject.id
        jsonUpdate.baseImage = newImage.id
        jsonUpdate.user = newUser.id

        jsonImage = jsonUpdate.encodeAsJSON()
        return ['oldData':image,'newData':jsonUpdate,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Job job) {
        log.info "update job"
        Integer oldProgress = 0
        Integer newProgress = 100

        def mapNew = ["progress": newProgress]
        def mapOld = ["progress": oldProgress]

        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        jobToAdd.progress = oldProgress
        assert (jobToAdd.save(flush: true) != null)

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.progress = newProgress
        jsonJob = jsonUpdate.encodeAsJSON()
        return ['oldData':job,'newData':jsonJob,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(JobData Jobdata) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Job oldJob = BasicInstance.createOrGetBasicJob()
        Job newJob = BasicInstance.getBasicJobNotExist()
        newJob.save(flush: true)

        def mapNew = ["key": newName, "job": newJob]
        def mapOld = ["key": oldName, "job": oldJob]

        def jsonJobData = Jobdata.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobData)
        jsonUpdate.key = newName
        jsonUpdate.job = newJob.id
        jsonJobData = jsonUpdate.encodeAsJSON()
        return ['oldData':Jobdata,'newData':jsonJobData,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(JobParameter jobparameter) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 jobparameter */
        log.info("create jobparameter")
        JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
        jobparameterToAdd.value = oldValue
        assert (jobparameterToAdd.save(flush: true) != null)

        /* Encode a niew jobparameter Name2*/
        JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
        def jsonJobparameter = jobparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobparameter)
        jsonUpdate.value = newValue
        jsonJobparameter = jsonUpdate.encodeAsJSON()
        return ['oldData':jobparameter,'newData':jsonJobparameter,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Ontology ontology) {
        log.info "update ontology"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 ontology */
         Ontology ontologyToAdd = BasicInstance.createOrGetBasicOntology()
         ontologyToAdd.name = oldName
         assert (ontologyToAdd.save(flush:true) != null)
         /* Encode a niew ontology Name2*/
         Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
         def jsonOntology = ontologyToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonOntology)
         jsonUpdate.name = newName
         jsonOntology = jsonUpdate.encodeAsJSON()
        return ['oldData':ontology,'newData':jsonOntology,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(Project project) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Ontology oldOtology = BasicInstance.createOrGetBasicOntology()
        Ontology newOtology = BasicInstance.getBasicOntologyNotExist()
        newOtology.save(flush: true)

        def mapNew = ["name": newName, "ontology": newOtology]
        def mapOld = ["name": oldName, "ontology": oldOtology]

        def jsonProject = project.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = newName
        jsonUpdate.ontology = newOtology.id
        jsonProject = jsonUpdate.encodeAsJSON()
        return ['oldData':project,'newData':jsonProject,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AnnotationFilter af) {
        String oldName = "Name1"
        String newName = Math.random()+""

        def mapNew = ["name": newName]
        def mapOld = ["name": oldName]

        def json = af.encodeAsJSON()
        def jsonUpdate = JSON.parse(json)
        jsonUpdate.name = newName
        json = jsonUpdate.encodeAsJSON()
        return ['oldData':af,'newData':json,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(ReviewedAnnotation annotation) {
         log.info "update reviewedannotation:" + annotation

         String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
         String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

         User oldUser = annotation.user
         User newUser = annotation.user

         Term oldTerm = BasicInstance.createOrGetBasicTerm()
         Term newTerm = BasicInstance.getBasicTermNotExist()
         newTerm.save(flush: true)

         def mapNew = ["geom":newGeom,"user":newUser,"term":newTerm]
         def mapOld = ["geom":oldGeom,"user":oldUser,"term":oldTerm]

         /* Create a old annotation with point 1111 1111 */
         log.info("create reviewedannotation")
         ReviewedAnnotation annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
         annotationToAdd.location =  new WKTReader().read(oldGeom)
         annotationToAdd.user = oldUser
         annotationToAdd.addToTerm(oldTerm)
         assert (annotationToAdd.save(flush:true) != null)

         /* Encode a niew annotation with point 9999 9999 */
         ReviewedAnnotation annotationToEdit = ReviewedAnnotation.get(annotationToAdd.id)
         def jsonEdit = annotationToEdit
         def jsonAnnotation = jsonEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonAnnotation)
         jsonUpdate.location = newGeom
         jsonUpdate.user = newUser.id
         jsonUpdate.term = [newTerm.id]
         jsonAnnotation = jsonUpdate.encodeAsJSON()
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
     }

    static def createUpdateSet(Software software) {
        log.info "update software"
        String oldName = "Name1"
        String newName = "Name2"
        String oldNameService = "projectService"
        String newNameService = "userAnnotationService"

        def mapNew = ["name": newName,"serviceName" : newNameService]
        def mapOld = ["name": oldName,"serviceName" : oldNameService]
         /* Create a Name1 software */
         Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()
         softwareToAdd.name = oldName
        softwareToAdd.serviceName = oldNameService
         assert (softwareToAdd.save(flush:true) != null)
         /* Encode a niew software Name2*/
         Software softwareToEdit = Software.get(softwareToAdd.id)
         def jsonSoftware = softwareToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftware)
         jsonUpdate.name = newName
        jsonUpdate.serviceName = newNameService
         jsonSoftware = jsonUpdate.encodeAsJSON()
        return ['oldData':software,'newData':jsonSoftware,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(SoftwareParameter softwareparameter) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 softwareparameter */
        log.info("create softwareparameter")
        SoftwareParameter softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()
        softwareparameterToAdd.name = oldValue
        assert (softwareparameterToAdd.save(flush: true) != null)

        /* Encode a niew softwareparameter Name2*/
        SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
        def jsonSoftwareparameter = softwareparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftwareparameter)
        jsonUpdate.value = newValue
        jsonSoftwareparameter = jsonUpdate.encodeAsJSON()
        return ['oldData':softwareparameter,'newData':jsonSoftwareparameter,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(Term term) {
        String oldName = "Name1"
        String newName = "Name2"

        String oldComment = "Comment1"
        String newComment = "Comment2"

        String oldColor = "000000"
        String newColor = "FFFFFF"

        Ontology oldOntology = BasicInstance.createOrGetBasicOntology()
        Ontology newOntology = BasicInstance.getBasicOntologyNotExist()
        newOntology.save(flush:true)

        def mapOld = ["name":oldName,"comment":oldComment,"color":oldColor,"ontology":oldOntology]
        def mapNew = ["name":newName,"comment":newComment,"color":newColor,"ontology":newOntology]


        /* Create a Name1 term */
        log.info("create term")
        Term termToAdd = BasicInstance.createOrGetBasicTerm()
        termToAdd.name = oldName
        termToAdd.comment = oldComment
        termToAdd.color = oldColor
        termToAdd.ontology = oldOntology
        assert (termToAdd.save(flush:true) != null)

        /* Encode a niew term Name2*/
        Term termToEdit = Term.get(termToAdd.id)
        def jsonTerm = termToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonTerm)
        jsonUpdate.name = newName
        jsonUpdate.comment = newComment
        jsonUpdate.color = newColor
        jsonUpdate.ontology = newOntology.id
        jsonTerm = jsonUpdate.encodeAsJSON()
        return ['oldData':term,'newData':jsonTerm,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(UserAnnotation annotation) {

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        User oldUser = annotation.user
        User newUser = annotation.user

        def mapNew = ["geom":newGeom,"user":newUser]
        def mapOld = ["geom":oldGeom,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create userAnnotation")
        UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = oldUser
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = newUser.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(User user) {
        log.info "update user"
        String oldFirstname = "Firstname1"
        String newFirstname = "Firstname2"

        String oldLastname = "Lastname1"
        String newLastname = "Lastname2"

        String oldEmail = "old@email.com"
        String newEmail = "new@email.com"

        String oldUsername = "Username1"
        String newUsername = "Username2"


        def mapOld = ["firstname":oldFirstname,"lastname":oldLastname,"email":oldEmail,"username":oldUsername]
        def mapNew = ["firstname":newFirstname,"lastname":newLastname,"email":newEmail,"username":newUsername]


        /* Create a Name1 user */
        log.info("create user")
        User userToAdd = BasicInstance.createOrGetBasicUser()
        userToAdd.firstname = oldFirstname
        userToAdd.lastname = oldLastname
        userToAdd.email = oldEmail
        userToAdd.username = oldUsername
        assert (userToAdd.save(flush:true) != null)

        /* Encode a niew user Name2*/
        User userToEdit = User.get(userToAdd.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.firstname = newFirstname
        jsonUpdate.lastname = newLastname
        jsonUpdate.email = newEmail
        jsonUpdate.username = newUsername
        jsonUser = jsonUpdate.encodeAsJSON()
        return ['oldData':user,'newData':jsonUser,'mapOld':mapOld,'mapNew':mapNew]
    }
}

