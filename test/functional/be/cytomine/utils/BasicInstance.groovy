package be.cytomine.utils

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Instrument
import be.cytomine.project.Discipline
import be.cytomine.project.Project

import be.cytomine.social.SharedAnnotation
import com.vividsolutions.jts.io.WKTReader
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import be.cytomine.ontology.*
import be.cytomine.processing.*
import be.cytomine.security.*
import be.cytomine.laboratory.Sample
import be.cytomine.AnnotationDomain
import be.cytomine.test.Infos

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/02/11
 * Time: 13:37
 * Build sample domain data
 */
class BasicInstance {

    def springSecurityService

    private static Log log = LogFactory.getLog(BasicInstance.class)

    /**
     * Check if a domain is valide during test
     * @param domain Domain to check
     */
    static void checkDomain(def domain) {
        if(!domain.validate()) {
            log.warn domain.class.name+".errors=" + domain.errors
            assert false
        }
    }

    /**
     *  Check if a domain is well saved during test
     * @param domain Domain to check
     */
    static void saveDomain(def domain) {
        if(!domain.save(flush: true)) {
            log.warn domain.class+".errors=" + domain.errors
            assert false
        }
        assert domain != null
    }

    static UserAnnotation createUserAnnotation(Job job) {
        createUserAnnotation(job.project)
    }

    static UserAnnotation createUserAnnotation(Project project) {
        createUserAnnotation(project,null)
    }

    static UserAnnotation createUserAnnotation(Project project, ImageInstance image) {
        UserAnnotation a2 = BasicInstance.getBasicUserAnnotationNotExist()
        a2.project = project
        if(image) a2.image = image
        a2.user = BasicInstance.getNewUser()
        BasicInstance.checkDomain(a2)
        BasicInstance.saveDomain(a2)
        BasicInstance.createAnnotationTerm(a2)
        a2
    }

    static AnnotationTerm createAnnotationTerm(UserAnnotation annotation) {
        AnnotationTerm at = BasicInstance.getBasicAnnotationTermNotExist("")
        Term term = getBasicTermNotExist()
        term.ontology = annotation.project.ontology
        term.save(flush: true)
        at.userAnnotation = annotation
        at.term = term
        BasicInstance.checkDomain(at)
        BasicInstance.saveDomain(at)
        at
    }

    static AlgoAnnotation createAlgoAnnotation(Job job, UserJob userJob) {
        AlgoAnnotation annotation = BasicInstance.getBasicAlgoAnnotationNotExist()
        annotation.project = job.project
        annotation.user = userJob
        BasicInstance.checkDomain(annotation)
        BasicInstance.saveDomain(annotation)
        annotation
    }

    static AlgoAnnotationTerm createAlgoAnnotationTerm(Job job, AnnotationDomain annotation, UserJob userJob) {
        AlgoAnnotationTerm at = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        at.project = job.project
        at.annotationIdent = annotation.id
        at.annotationClassName = annotation.class.getName()
        at.userJob = userJob
        BasicInstance.checkDomain(at)
        BasicInstance.saveDomain(at)
        at
    }

    static ImageInstance createImageInstance(Project project) {
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        BasicInstance.checkDomain(image)
        BasicInstance.saveDomain(image)
        image
    }

    static ReviewedAnnotation createReviewAnnotation(ImageInstance image) {
        ReviewedAnnotation review = BasicInstance.getBasicReviewedAnnotationNotExist()
        review.project = image.project
        review.image = image
        BasicInstance.checkDomain(review)
        BasicInstance.saveDomain(review)
        review
    }

    static UserJob createUserJob() {
        createUserJob(null)
    }
    static UserJob createUserJob(Project project) {
        Job job = BasicInstance.getBasicJobNotExist()
        if(project) job.project = project
        BasicInstance.checkDomain(job)
        BasicInstance.saveDomain(job)
        BasicInstance.createSoftwareProject(job.software,job.project)

        UserJob userJob = BasicInstance.getBasicUserJobNotExist()
        userJob.job = job
        userJob.user = BasicInstance.getNewUser()
        BasicInstance.checkDomain(userJob)
        BasicInstance.saveDomain(userJob)
        userJob
    }

    static ImageFilter createOrGetBasicImageFilter() {
       def imagefilter = ImageFilter.findByName("imagetest")
       if(!imagefilter) {
           imagefilter = new ImageFilter()
           imagefilter.name = "imagetest"
           imagefilter.baseUrl = "baseurl"
           def processing = new ProcessingServer(url: "processingserverurl")
           processing.save(flush:true)
           imagefilter.processingServer = processing
           BasicInstance.checkDomain(imagefilter)
           BasicInstance.saveDomain(imagefilter)
       }
        imagefilter
    }

    static ImageFilterProject createOrGetBasicImageFilterProject() {
       def imagefilter = createOrGetBasicImageFilter()
       def project = createOrGetBasicProject()
       def result = ImageFilterProject.findByProjectAndImageFilter(project,imagefilter)
        if(!result) {
            result = ImageFilterProject.link(imagefilter,project)
        }
        return result
    }

    static ImageFilterProject getBasicImageFilterProjectNotExist() {
       def imagefilter = createOrGetBasicImageFilter()
       def project = getBasicProjectNotExist()
        project.save(flush: true)
       return new ImageFilterProject(imageFilter: imagefilter, project: project)
    }

    static UserAnnotation createOrGetBasicUserAnnotation() {
        log.debug "createOrGetBasicUserAnnotation()"
        def image = createOrGetBasicImageInstance()
        def annotation = UserAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: image,
                user: User.findByUsername(Infos.GOODLOGIN),
                project:image.project
        )
        checkDomain(annotation)
        saveDomain(annotation)
        annotation
    }

    static UserAnnotation getBasicUserAnnotationNotExist() {
        log.debug "getBasicUserAnnotationNotExist()"
        def image = createOrGetBasicImageInstance()
        UserAnnotation annotation = new UserAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: User.findByUsername(Infos.GOODLOGIN),
                project:image.project
        )
        checkDomain(annotation)
        annotation
    }

    static AlgoAnnotation createOrGetBasicAlgoAnnotation() {
        log.debug "createOrGetBasicAlgoAnnotation()"

        UserJob userJob = createOrGetBasicUserJob()

        def image = createOrGetBasicImageInstance()
        def annotation = AlgoAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: image,
                user: userJob,
                project:image.project
        )
        checkDomain(annotation)
        saveDomain(annotation)
        annotation
    }

    static AlgoAnnotation getBasicAlgoAnnotationNotExist() {
        log.debug "getBasicAlgoAnnotationNotExist()"

        UserJob userJob = createOrGetBasicUserJob()
        def image = createOrGetBasicImageInstance()
        AlgoAnnotation annotation = new AlgoAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: userJob,
                project:image.project
        )
        checkDomain(annotation)
        annotation
    }

    static ReviewedAnnotation createOrGetBasicReviewedAnnotation() {
        log.debug "createOrGetBasicReviewedAnnotation()"

        def basedAnnotation = getBasicUserAnnotationNotExist()
        basedAnnotation.save(flush: true)

        def image = createOrGetBasicImageInstance()
        def annotation = ReviewedAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: image,
                user: User.findByUsername(Infos.GOODLOGIN),
                project:image.project,
                status : 0,
                reviewUser: User.findByUsername(Infos.GOODLOGIN)
        )
        annotation.putParentAnnotation(basedAnnotation)
        checkDomain(annotation)
        saveDomain(annotation)

        def term = createOrGetBasicTerm()
        term.ontology = image.project.ontology
        checkDomain(term)
        saveDomain(term)

        annotation.addToTerm(term)
        checkDomain(annotation)
        saveDomain(annotation)
        annotation
    }

    static ReviewedAnnotation getBasicReviewedAnnotationNotExist() {
        log.debug "getBasicReviewedAnnotation()"

        def basedAnnotation = getBasicUserAnnotationNotExist()
        basedAnnotation.save(flush: true)

        def image = createOrGetBasicImageInstance()
        def annotation = ReviewedAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: image,
                user: User.findByUsername(Infos.GOODLOGIN),
                project:image.project,
                status : 0,
                reviewUser: User.findByUsername(Infos.GOODLOGIN)
        )
        annotation.putParentAnnotation(basedAnnotation)
        checkDomain(annotation)
        annotation
    }

    static SharedAnnotation createOrGetBasicSharedAnnotation() {
        log.debug "createOrGetBasicSharedAnnotation()"

        def sharedannotation = SharedAnnotation.findOrCreateWhere(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: createOrGetBasicUserAnnotation()
        )
        checkDomain(sharedannotation)
        saveDomain(sharedannotation)
        sharedannotation
    }

    static SharedAnnotation getBasicSharedAnnotationNotExist() {
        log.debug "getBasicSharedAnnotationNotExist()"
        def sharedannotation = new SharedAnnotation(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: createOrGetBasicUserAnnotation()
        )
        checkDomain(sharedannotation)
        sharedannotation
    }

    static UserJob createOrGetBasicUserJob() {
        log.debug "createOrGetBasicUserJob()"
        UserJob userJob = UserJob.findByUsername("BasicUserJob")
        if (!userJob) {
            userJob = new UserJob(
                    username: "BasicUserJob",
                    password: "PasswordUserJob",
                    enabled: true,
                    user : User.findByUsername(Infos.GOODLOGIN),
                    job: createOrGetBasicJob()
            )

            userJob.generateKeys()
            checkDomain(userJob)
            saveDomain(userJob)
            User.findByUsername(Infos.GOODLOGIN).getAuthorities().each { secRole ->
                SecUserSecRole.create(userJob, secRole)
            }
        }

        userJob
    }

    static UserJob getBasicUserJobNotExist() {
        log.debug "getBasicUserJobNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        UserJob userJob = UserJob.findByUsername(randomInt + "")
        while (userJob) {
            randomInt = random.nextInt()
            userJob = UserJob.findByUsername(randomInt + "")
        }
        
        def user = getBasicUserNotExist()
        user.save(flush: true)
        def job = getBasicJobNotExist()
        job.save(flush: true)

        userJob = new UserJob(username: randomInt+"BasicUserJob",password: "PasswordUserJob",enabled: true,user : user,job: job)

        user.getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }
        userJob.generateKeys()
        checkDomain(userJob)
        userJob
    }

    static AbstractImage createOrGetBasicAbstractImage() {
        log.debug "createOrGetBasicAbstractImage()"
        AbstractImage image = AbstractImage.findByFilename("filename")
        if (!image) {
            image = new AbstractImage(filename: "filename", scanner: createOrGetBasicScanner(), sample: null, mime: BasicInstance.createOrGetBasicMime(), path: "pathpathpath")
        }
        checkDomain(image)
        saveDomain(image)
        image
    }

    static AbstractImage getBasicAbstractImageNotExist() {
        log.debug "getBasicImageNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def image = AbstractImage.findByFilename(randomInt + "")
        while (image) {
            randomInt = random.nextInt()
            image = AbstractImage.findByFilename(randomInt + "")
        }
        image = new AbstractImage(filename: randomInt, scanner: createOrGetBasicScanner(), sample: null, mime: BasicInstance.createOrGetBasicMime(), path: "pathpathpath")
        checkDomain(image)
        image
    }
    
    static AnnotationTerm createOrGetBasicAnnotationTerm() {
        log.debug "createOrGetBasicAnnotationTerm()"
        def annotation = getBasicUserAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def user = User.findByUsername(Infos.GOODLOGIN)
        assert user != null

        def annotationTerm = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term, 'user': user)
        assert annotationTerm == null
        annotationTerm = AnnotationTerm.link(annotation, term,user)
        saveDomain(annotationTerm)
        annotationTerm
    }

    static AnnotationTerm getBasicAnnotationTermNotExist(String method) {
        log.debug "getBasicAnnotationTermNotExist()"
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def annotation = getBasicUserAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null

        def user = User.findByUsername(Infos.GOODLOGIN)
        assert user != null

        def annotationTerm = new AnnotationTerm(userAnnotation:annotation,term:term,user:user)
        annotationTerm
    }

    static AlgoAnnotationTerm createOrGetBasicAlgoAnnotationTerm() {
        log.debug "createOrGetBasicAlgoAnnotationTerm()"
        def annotation = getBasicUserAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def user = getBasicUserJobNotExist()
        user.save(flush: true)
        assert user != null

        def algoannotationTerm = AlgoAnnotationTerm.findWhere('annotationIdent': annotation.id, 'term': term, 'userJob': user)
        assert algoannotationTerm == null
        algoannotationTerm = new AlgoAnnotationTerm(term:term,expectedTerm:term,userJob:user,rate:0)
        algoannotationTerm.setAnnotation(annotation)
        saveDomain(algoannotationTerm)
        algoannotationTerm
    }

    static AlgoAnnotationTerm createOrGetBasicAlgoAnnotationTermForAlgoAnnotation() {
        log.debug "createOrGetBasicAlgoAnnotationTerm()"
        def annotation = getBasicAlgoAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def user = getBasicUserJobNotExist()
        user.save(flush: true)
        assert user != null

        def algoannotationTerm = AlgoAnnotationTerm.findWhere('annotationIdent': annotation.id, 'term': term, 'userJob': user)
        assert algoannotationTerm == null
        algoannotationTerm = new AlgoAnnotationTerm(term:term,expectedTerm:term,userJob:user,rate:0)
        algoannotationTerm.setAnnotation(annotation)
        saveDomain(algoannotationTerm)
        algoannotationTerm
    }

    static AlgoAnnotationTerm getBasicAlgoAnnotationTermNotExist() {
        log.debug "getBasicAnnotationTermNotExist()"
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def annotation = getBasicUserAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def user = getBasicUserJobNotExist()
        user.save(flush: true)
        assert user != null
        Infos.addUserRight(user.user,annotation.project)
        def algoannotationTerm = new AlgoAnnotationTerm(term:term,userJob:user, expectedTerm: term, rate:1d)
        algoannotationTerm.setAnnotation(annotation)
        algoannotationTerm
    }

    static AlgoAnnotationTerm getBasicAlgoAnnotationTermNotExistForAlgoAnnotation() {
        log.debug "getBasicAlgoAnnotationTermNotExistForAlgoAnnotation()"
        def term = getBasicTermNotExist()
        term.save(flush: true)
        assert term != null
        def annotation = getBasicAlgoAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def user = getBasicUserJobNotExist()
        user.save(flush: true)
        assert user != null
        Infos.addUserRight(user.user,annotation.project)
        def algoannotationTerm = new AlgoAnnotationTerm(term:term,userJob:user, expectedTerm: term, rate:1d)
        algoannotationTerm.setAnnotation(annotation)
        algoannotationTerm
    }

    static AbstractImageGroup createOrGetBasicAbstractImageGroup() {
        log.debug "createOrGetBasicAbstractImageGroup()"
        def abstractimage = getBasicAbstractImageNotExist()
        abstractimage.save(flush: true)
        assert abstractimage != null
        def group = getBasicGroupNotExist()
        group.save(flush: true)
        assert group != null
        def abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        assert abstractimageGroup == null

        if (!abstractimageGroup) {
            abstractimageGroup = AbstractImageGroup.link(abstractimage, group)
        }
        abstractimageGroup
    }

    static AbstractImageGroup getBasicAbstractImageGroupNotExist(String method) {
        log.debug "getBasicAbstractImageGroupNotExist()"
        def group = getBasicGroupNotExist()
        group.save(flush: true)
        assert group != null
        def abstractimage = getBasicAbstractImageNotExist()
        abstractimage.save(flush: true)
        assert abstractimage != null
        def abstractimageGroup = new AbstractImageGroup(abstractimage: abstractimage, group: group)
        abstractimageGroup
    }

    static ProcessingServer createOrGetBasicProcessingServer() {
        log.debug "createOrGetBasicProcessingServer()"
        def ps = ProcessingServer.findByUrl("processing_server_url")
        if (!ps) {
            ps = new ProcessingServer(url: "processing_server_url")
        }
        checkDomain(ps)
        saveDomain(ps)
        ps
    }
    
    static Discipline createOrGetBasicDiscipline() {
        log.debug "createOrGetBasicDiscipline()"
        def discipline = Discipline.findByName("BASICDISCIPLINE")
        if (!discipline) {
            discipline = new Discipline(name: "BASICDISCIPLINE")
        }
        checkDomain(discipline)
        saveDomain(discipline)
        discipline
    }

    static Discipline getBasicDisciplineNotExist() {
        log.debug "createOrGetBasicDisciplineNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def discipline = Discipline.findByName(randomInt + "")
        while (discipline) {
            randomInt = random.nextInt()
            discipline = Discipline.findByName(randomInt + "")
        }
        discipline = new Discipline(name: randomInt + "")
        checkDomain(discipline)
        discipline
    }

    static ImageInstance createOrGetBasicImageInstance() {
        log.info "createOrGetBasicImageInstance()"
        ImageInstance image = getBasicImageInstanceNotExist();
        checkDomain(image)
        saveDomain(image)
        return image
    }

    static ImageInstance getBasicImageInstanceNotExist() {
        log.info "getBasicImageInstanceNotExist()"
        AbstractImage img = BasicInstance.getBasicAbstractImageNotExist()
        img.save(flush: true)
        ImageInstance image = new ImageInstance(
                baseImage: img,
                project: BasicInstance.createOrGetBasicProject(),
                //slide: BasicInstance.createOrGetBasicSlide(),
                user: BasicInstance.createOrGetBasicUser())
        image.baseImage.save(flush: true)
        checkDomain(image)
        image
    }

    static Job createOrGetBasicJob() {
        log.debug  "createOrGetBasicJob()"
        def job
        def jobs = Job.findAllByProjectIsNotNull()
        if(jobs.isEmpty()) {
            Project project = createOrGetBasicProject()
            job = new Job(project:project,software:createOrGetBasicSoftware())
            checkDomain(job)
            saveDomain(job)
        } else {
            job = jobs.first()
        }
        assert job!=null
        job
    }

    static Job getBasicJobNotExist() {
        log.debug "getBasicJobNotExist()"
        Software software = getBasicSoftwareNotExist()
        Project project = getBasicProjectNotExist()
        software.save(flush:true)
        project.save(flush : true)
        Job job =  new Job(software:software, project : project)
        checkDomain(job)
        job
    }

    static SoftwareProject createSoftwareProject(Software software, Project project) {
        SoftwareProject softProj = SoftwareProject.findBySoftwareAndProject(software,project)
        if(softProj) return softProj
        else {
            softProj = new SoftwareProject(project: project, software: software)
            checkDomain(softProj)
            saveDomain(softProj)
        }
        softProj
    }






    static JobData getBasicJobDataNotExist() {
        log.debug "getBasicJobDataNotExist()"
        Job job = getBasicJobNotExist()
        job.save(flush:true)
        JobData jobData =  new JobData(job:job, key : "TESTKEY", filename: "filename.jpg")
        checkDomain(jobData)
        jobData
    }


    static JobData createOrGetBasicJobData() {
        log.debug  "createOrGetBasicJobData()"
        def jobData = getBasicJobDataNotExist()
        jobData.save(flush: true)
        jobData
    }


    static JobParameter createOrGetBasicJobParameter() {
        log.debug "createOrGetBasicJobparameter()"

        def job = createOrGetBasicJob()
        def softwareParam = createOrGetBasicSoftwareParameter()

        def jobparameter = JobParameter.findByJobAndSoftwareParameter(job,softwareParam)
        if (!jobparameter) {

            jobparameter = new JobParameter(value: "toto", job:job,softwareParameter:softwareParam)
            checkDomain(jobparameter)
            saveDomain(jobparameter)
        }
        assert jobparameter != null
        jobparameter
    }

    static JobParameter getBasicJobParameterNotExist() {
        log.debug "getBasicJobparameterNotExist()"
        def job = getBasicJobNotExist()
        def softwareParam = getBasicSoftwareParameterNotExist()
        job.save(flush:true)
        softwareParam.save(flush:true)

        def jobparameter = new JobParameter(value: "toto", job:job,softwareParameter:softwareParam)
        checkDomain(jobparameter)
        jobparameter
    }
    
    static Ontology createOrGetBasicOntology() {
        log.debug "createOrGetBasicOntology()"
        def ontology = Ontology.findByName("BasicOntology")
        if (!ontology) {
            ontology = new Ontology(name: "BasicOntology", user: createOrGetBasicUser())
            checkDomain(ontology)
            saveDomain(ontology)
        }
        assert ontology != null

        def term = getBasicTermNotExist()
        term.ontology = ontology
        checkDomain(term)
        saveDomain(term)

        ontology
    }

    static Ontology getBasicOntologyNotExist() {
        log.debug "getBasicOntologyNsotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def ontology = Ontology.findByName(randomInt + "")
        while (ontology) {
            randomInt = random.nextInt()
            ontology = Ontology.findByName(randomInt + "")
        }
        ontology = new Ontology(name: randomInt + "", user: createOrGetBasicUser())
        checkDomain(ontology)
        ontology
    }

    static Project createOrGetBasicProject() {
        log.debug "createOrGetBasicProject()"
        def name = "BasicProject".toUpperCase()
        def project = Project.findByName(name)
        if (!project) {
            project = new Project(name: name, ontology: createOrGetBasicOntology(), discipline: createOrGetBasicDiscipline())
            checkDomain(project)
            saveDomain(project)
        }
        assert project != null
        project
    }

    static Project getBasicProjectNotExist() {
        log.debug "getBasicProjectNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def project = Project.findByName(randomInt + "")
        def ontology = getBasicOntologyNotExist()
        ontology.save(flush: true)
        while (project) {
            randomInt = random.nextInt()
            project = Project.findByName(randomInt + "")
        }
        project = new Project(name: randomInt + "", ontology: ontology, discipline: createOrGetBasicDiscipline())
        project
    }

    static Relation createOrGetBasicRelation() {
        log.debug "createOrGetBasicRelation()"
        def relation = Relation.findByName("BasicRelation")
        if (!relation) {
            relation = new Relation(name: "BasicRelation")
            checkDomain(relation)
            saveDomain(relation)
        }
        assert relation != null
        relation
    }

    static Relation getBasicRelationNotExist() {
        log.debug "createOrGetBasicRelationNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def relation = Relation.findByName(randomInt + "")
        while (relation) {
            randomInt = random.nextInt()
            relation = Relation.findByName(randomInt + "")
        }
        relation = new Relation(name: randomInt + "")
        checkDomain(relation)
        assert relation != null
        relation
    }
    
    static RelationTerm createOrGetBasicRelationTerm() {
        log.debug "createOrGetBasicRelationTerm()"
        def relation = createOrGetBasicRelation()
        def term1 = createOrGetBasicTerm()
        def term2 = createOrGetAnotherBasicTerm()

        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) {
            relationTerm = RelationTerm.link(relation, term1, term2)
        }
        assert relationTerm != null
        relationTerm
    }

    static RelationTerm getBasicRelationTermNotExist() {
        log.debug "getBasicRelationTermNotExist()"
        def relation = getBasicRelationNotExist()
        def term1 = getBasicTermNotExist()
        def term2 = getBasicTermNotExist()
        relation.save(flush: true)
        term1.save(flush: true)
        term2.save(flush: true)
        def relationTerm = new RelationTerm(relation: relation, term1: term1, term2: term2)
        checkDomain(relationTerm)
        assert relationTerm != null
        relationTerm
    }

    static Mime createOrGetBasicMime() {

        log.debug "createOrGetBasicMime1()"
        def mime = Mime.findByExtension("tif")
        mime.refresh()
        mime.imageServers()
        mime
    }

    static Mime getBasicMimeNotExist() {
        def mime = Mime.findByMimeType("mimeT");
        log.debug "mime=" + mime
        if (mime == null) {
            log.debug "mimeList is empty"
            mime = new Mime(extension: "ext", mimeType: "mimeT")
            mime.validate()
            log.debug("mime.errors=" + mime.errors)
            mime.save(flush: true)
            log.debug("mime.errors=" + mime.errors)
        }
        assert mime != null
        mime
    }

    static Instrument createOrGetBasicScanner() {

        log.debug "createOrGetBasicScanner()"
        Instrument scanner = new Instrument(brand: "brand", model: "model")
        log.info(scanner)
        scanner.validate()
        log.info("validate")
        log.debug "scanner.errors=" + scanner.errors
        scanner.save(flush: true)
        log.debug "scanner.errors=" + scanner.errors
        assert scanner != null
        scanner
    }

    static Instrument getNewScannerNotExist() {

        log.debug "getNewScannerNotExist()"
        def scanner = new Instrument(brand: "newBrand", model: "newModel")
        scanner.validate()
        log.debug "scanner.errors=" + scanner.errors
        scanner.save(flush: true)
        log.debug "scanner.errors=" + scanner.errors
        assert scanner != null
        scanner
    }

    static Sample createOrGetBasicSlide() {
        log.debug "createOrGetBasicSlide()"
        def name = "BasicSlide".toUpperCase()
        def slide = Sample.findByName(name)
        if (!slide) {

            slide = new Sample(name: name)
            slide.validate()
            log.debug "sample.errors=" + slide.errors
            slide.save(flush: true)
            log.debug "sample.errors=" + slide.errors
        }
        assert slide != null
        slide
    }

    static Sample getBasicSlideNotExist() {

        log.debug "getBasicSlideNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def slide = Sample.findByName(randomInt + "")

        while (slide) {
            randomInt = random.nextInt()
            slide = Sample.findByName(randomInt + "")
        }

        slide = new Sample(name: randomInt + "")
        assert slide.validate()
        log.debug "sample.errors=" + slide.errors
        slide
    }

    static User getOldUser() {

        log.debug "createOrGetBasicUser()"
        User user = User.findByUsername("stevben")
        assert user != null
        user
    }

    static User getNewUser() {

        log.debug "createOrGetBasicUser()"
        User user = User.findByUsername("lrollus")
        assert user != null
        user
    }

    static User createOrGetBasicUser() {

        log.debug "createOrGetBasicUser()"
        def user = SecUser.findByUsername("BasicUser")
        if (!user) {
            user = new User(
                    username: "BasicUser",
                    firstname: "Basic",
                    lastname: "User",
                    email: "Basic@User.be",
                    password: "password",
                    enabled: true)
            user.generateKeys()
            user.validate()
            log.debug "user.errors=" + user.errors
            user.save(flush: true)
            log.debug "user.errors=" + user.errors
        }
        assert user != null
        user
    }

    static User createOrGetBasicUser(String username, String password) {
        def springSecurityService = ApplicationHolder.application.getMainContext().getBean("springSecurityService")
        log.debug "createOrGetBasicUser()"
        def user = SecUser.findByUsername(username)
        if (!user) {
            user = new User(
                    username: username,
                    firstname: "Basic",
                    lastname: "User",
                    email: "Basic@User.be",
                    password: password,
                    enabled: true)
            user.generateKeys()
            user.validate()
            log.debug "user.errors=" + user.errors
            user.save(flush: true)
            log.debug "user.errors=" + user.errors
            try {
               SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_USER"))
            } catch(Exception e) {
                log.warn(e)
            }
        }
        assert user != null
        user
    }

    static User createOrGetBasicAdmin(String username, String password) {
        User user = createOrGetBasicUser(username,password)
            try {
               SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_ADMIN"))
            } catch(Exception e) {
                log.warn(e)
            }
        assert user != null
        user
    }

    static User getBasicUserNotExist() {

        log.debug "getBasicUserNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def user = User.findByUsername(randomInt + "")

        while (user) {
            randomInt = random.nextInt()
            user = User.findByUsername(randomInt + "")
        }

        user = new User(
                username: randomInt + "",
                firstname: "BasicNotExist",
                lastname: "UserNotExist",
                email: "BasicNotExist@User.be",
                password: "password",
                enabled: true)
        user.generateKeys()
        assert user.validate()
        log.debug "user.errors=" + user.errors
        user
    }

    static Group createOrGetBasicGroup() {
        log.debug "createOrGetBasicGroup()"
        def name = "BasicGroup".toUpperCase()
        def group = Group.findByName(name)
        if (!group) {

            group = new Group(name: name)
            group.validate()
            log.debug "group.errors=" + group.errors
            group.save(flush: true)
            log.debug "group.errors=" + group.errors
        }
        assert group != null
        group
    }

    static Group getBasicGroupNotExist() {

        log.debug "getBasicGroupNotExist()"
        def random = new Random()
        def randomInt = random.nextInt()
        def group = Group.findByName(randomInt + "")

        while (group) {
            randomInt = random.nextInt()
            group = Group.findByName(randomInt + "")
        }

        group = new Group(name: randomInt + "")
        assert group.validate()
        log.debug "user.errors=" + group.errors
        group
    }

    static Project createOrGetBasicProjectWithRight() {
    	log.debug "createOrGetBasicProjectWithRight()"
        Project project = createOrGetBasicProject()
        Infos.addUserRight(Infos.GOODLOGIN,project)
        return project
    }

    static Term createOrGetBasicTerm() {
        log.debug "createOrGetBasicTerm()"
        def term = Term.findByName("BasicTerm")
        if (!term) {

            term = new Term(name: "BasicTerm", ontology: createOrGetBasicOntology(), color: "FF0000")
            term.validate()
            log.debug "term.errors=" + term.errors
            term.save(flush: true)
            log.debug "term.errors=" + term.errors
        }
        assert term != null
        term
    }

    static Term createOrGetAnotherBasicTerm() {
        log.debug "createOrGetBasicTerm()"
        def term = Term.findByName("AnotherBasicTerm")
        if (!term) {

            term = new Term(name: "AnotherBasicTerm", ontology: createOrGetBasicOntology(), color: "F0000F")
            term.validate()
            log.debug "term.errors=" + term.errors
            term.save(flush: true)
            log.debug "term.errors=" + term.errors
        }
        assert term != null
        term
    }

    static Term getBasicTermNotExist() {

        log.debug "getBasicTermNotExist() start"
        def random = new Random()
        def randomInt = random.nextInt()
        def term = Term.findByName(randomInt + "")

        while (term) {
            randomInt = random.nextInt()
            term = Term.findByName(randomInt + "")
        }

        term = new Term(name: randomInt + "", ontology: getBasicOntologyNotExist(), color: "0F00F0")
        term.ontology.save(flush: true)
        term.validate()
        log.debug "getBasicTermNotExist() end"
        term
    }

    static Software createOrGetBasicSoftware() {
        log.debug "createOrGetBasicSoftware()"
        def software = Software.findByName("AnotherBasicSoftware")
        if (!software) {

            software = new Software(name: "AnotherBasicSoftware", serviceName:"helloWorldJobService")
            software.validate()
            log.debug "software.errors=" + software.errors
            software.save(flush: true)
            log.debug "software.errors=" + software.errors
        }
        assert software != null
        software
    }

    static Software getBasicSoftwareNotExist() {

        log.debug "getBasicSoftwareNotExist() start"
        def random = new Random()
        def randomInt = random.nextInt()
        def software = Software.findByName(randomInt + "")

        while (software) {
            randomInt = random.nextInt()
            software = Software.findByName(randomInt + "")
        }

        software = new Software(name: randomInt + "",serviceName:"helloWorldJobService")
        software.validate()
        log.debug "getBasicSoftwareNotExist() end"
        software
    }

    static SoftwareParameter createOrGetBasicSoftwareParameter() {
        log.debug "createOrGetBasicSoftwareParameter()"
        Software software = createOrGetBasicSoftware()

        def parameter = SoftwareParameter.findBySoftware(software)
        if (!parameter) {

            parameter = new SoftwareParameter()
            parameter.name = "anotherParameter"
            parameter.software = software
            parameter.type = "String"

            parameter.validate()
            log.debug "SoftwareParameter.errors=" + parameter.errors
            parameter.save(flush: true)
            log.debug "SoftwareParameter.errors=" + parameter.errors
        }
        assert parameter != null
        parameter
    }

    static SoftwareParameter getBasicSoftwareParameterNotExist() {

        log.debug "getBasicSoftwareParameterNotExist() start"

        Software software = createOrGetBasicSoftware()

        def random = new Random()
        def randomInt = random.nextInt()
        def parameter = SoftwareParameter.findByNameAndSoftware(randomInt + "",software)

        while (parameter) {
            randomInt = random.nextInt()
            parameter = SoftwareParameter.findByNameAndSoftware(randomInt + "",software)
        }

        parameter = new SoftwareParameter(name: randomInt + "",software:software,type:"String")
        parameter.validate()
        log.debug "getBasicSoftwareParameterNotExist() end"
        parameter
    }

    static SoftwareProject createOrGetBasicSoftwareProject() {
        log.debug "createOrGetBasicSoftwareProject()"
        Software software = createOrGetBasicSoftware()
        Project project = createOrGetBasicProject()
        log.debug "software="+software+" project="+project

        SoftwareProject softproj = SoftwareProject.link(software,project)

        softproj.validate()
        log.debug "SoftwareParameter.errors=" + softproj.errors
        softproj.save(flush: true)
        log.debug "SoftwareParameter.errors=" + softproj.errors

        assert softproj != null
        softproj
    }

    static SoftwareProject getBasicSoftwareProjectNotExist() {

        log.debug "getBasicSoftwareProjectNotExist() start"

        Software software = getBasicSoftwareNotExist()
        Project project = getBasicProjectNotExist()
        software.save(flush:true)
        project.save(flush:true)

        SoftwareProject softproj = new SoftwareProject(software:software,project:project)


        softproj.validate()
        log.debug "getBasicSoftwareParameterNotExist() end"
        softproj
    }

    static Job createJobWithAlgoAnnotationTerm() {
        Project project = BasicInstance.getBasicProjectNotExist()
        project.save(flush: true)
        Ontology ontology = project.ontology
        println "ontology.terms()="+project.ontology.terms().collect{it.id}

        Term term1 = BasicInstance.getBasicTermNotExist()
        term1.ontology = ontology
        term1.save(flush: true)

        Term term2 = BasicInstance.getBasicTermNotExist()
        term2.ontology = ontology
        println "term2.ontology.id="+term2.ontology.id
        term2.save(flush: true)


        UserJob userJob = BasicInstance.getBasicUserJobNotExist()
        userJob.save(flush : true)
        Job job = userJob.job
        job.project = project
        println "project.ontology.id="+project.ontology.id
        println "ontology.terms()="+project.ontology.terms().collect{it.id}
        job.save(flush: true)
        println "created userjob.id=" + userJob.id
        AlgoAnnotationTerm algoAnnotationGood = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationGood.term = term1
        algoAnnotationGood.expectedTerm = term1
        algoAnnotationGood.userJob = userJob
        BasicInstance.checkDomain(algoAnnotationGood)
        BasicInstance.saveDomain(algoAnnotationGood)

        AlgoAnnotationTerm algoAnnotationBad = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationBad.term = term1
        algoAnnotationBad.expectedTerm = term2
        algoAnnotationBad.userJob = userJob
        BasicInstance.checkDomain(algoAnnotationBad)
        BasicInstance.saveDomain(algoAnnotationBad)
        AlgoAnnotationTerm.list().each {
            println it.userJob.id + " | " + it.term.id + " | " + it.expectedTerm.id
        }
        return job
    }



    static createUserJob(User user) {
       String username = new Date().toString()
       UserJob newUser = new UserJob()
       newUser.username = username
       newUser.password = "password"
       newUser.publicKey = user.publicKey
       newUser.privateKey = user.privateKey
       newUser.enabled = user.enabled
       newUser.accountExpired = user.accountExpired
       newUser.accountLocked = user.accountLocked
       newUser.passwordExpired = user.passwordExpired
       newUser.user = user
       newUser.generateKeys()
        newUser.save(flush:true)
        return newUser
    }

    /**
     * Compare Annotation expected data (in map) to annotation new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareAnnotation(map, json) {
        assert map.geom.replace(' ', '').equals(json.location.replace(' ', ''))
        assert toLong(map.user.id).equals(toLong(json.user))
    }

    static void compareReviewedAnnotation(map, json) {
        assert map.geom.replace(' ', '').equals(json.location.replace(' ', ''))
        assert toLong(map.user.id).equals(toLong(json.user))
        assert toLong(map.term.id).equals(toLong(json.term[0]))
    }

    /**
     * Compare Abstract image expected data (in map) to abstract image new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareAbstractImage(map, json) {
        assert map.filename.equals(json.filename)
        assert toLong(map.scanner.id).equals(toLong(json.scanner))
        //assert toLong(map.sample.id).equals(toLong(json.sample))
        assert map.path.equals(json.path)
        assert map.mime.extension.equals(json.mime)
    }

    /**
     * Compare image expected data (in map) to image new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareImageInstance(map, json) {
        assert toLong(map.baseImage.id).equals(toLong(json.baseImage))
        assert toLong(map.project.id).equals(toLong(json.project))
        assert toLong(map.user.id).equals(toLong(json.user))
    }

    /**
     * Compare project expected data (in map) to project new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareProject(map, json) {
        assert map.name.toUpperCase().equals(json.name)
        assert toLong(map.ontology.id).equals(toLong(json.ontology))
    }

    /**
     * Compare discipline expected data (in map) to discipline new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareDiscipline(map, json) {
        assert map.name.equals(json.name)
    }

    /**
     * Compare term expected data (in map) to term new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareTerm(map, json) {
        assert map.name.equals(json.name)
        assert map.comment.equals(json.comment)
        assert map.color.equals(json.color)
        assert toLong(map.ontology.id).equals(toLong(json.ontology))
    }

    /**
     * Compare jobdata expected data (in map) to jobdata new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareJobData(map, json) {
        assert map.key.equals(json.key)
        assert toLong(map.job.id).equals(toLong(json.job))
    }

    /**
     * Compare user expected data (in map) to user new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareUser(map, json) {
        assert map.firstname.equals(json.firstname)
        assert map.lastname.equals(json.lastname)
        assert map.email.equals(json.email)
        assert map.username.equals(json.username)
    }

    /**
     * Compare ontology expected data (in map) to ontology new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareOntology(map, json) {
        assert map.name.equals(json.name)
    }

    /**
     * Compare software expected data (in map) to software new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compareSoftware(map, json) {
        assert map.name.equals(json.name)
        assert map.serviceName.equals(json.serviceName)
    }


    static Double toDouble(String s) {
        if (s == null && s.equals("null")) return null
        else return Double.parseDouble(s)
    }

    static Double toDouble(Integer s) {
        if (s == null) return null
        else return Double.parseDouble(s.toString())
    }

    static Double toDouble(Double s) {
        return s
    }

    static Integer toLong(String s) {
        if (s == null && s.equals("null")) return null
        else return Integer.parseLong(s)
    }

    static Integer toLong(Long s) {
        return s
    }
}
