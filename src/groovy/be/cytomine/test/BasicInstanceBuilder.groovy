package be.cytomine.test

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.server.*
import be.cytomine.laboratory.Sample
import be.cytomine.ontology.*
import be.cytomine.processing.*
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.*
import be.cytomine.social.SharedAnnotation
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.UserAnnotationAPI
import com.vividsolutions.jts.io.WKTReader
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/02/11
 * Time: 13:37
 * Build sample domain data
 */
class BasicInstanceBuilder {

    def springSecurityService
    def secRoleService

    private static Log log = LogFactory.getLog(BasicInstanceBuilder.class)

    /**
     * Check if a domain is valide during test
     * @param domain Domain to check
     */
    static def checkDomain(def domain) {
        log.debug "#### checkDomain=" + domain.class
        if(!domain.validate()) {
            log.warn domain.class.name+".errors=" + domain.errors
            assert false
        }
        domain
    }

    /**
     *  Check if a domain is well saved during test
     * @param domain Domain to check
     */
    static def saveDomain(def domain) {
        log.debug "#### saveDomain=" + domain.class
        checkDomain(domain)
        if(!domain.save(flush: true)) {
            log.warn domain.class+".errors=" + domain.errors
            assert false
        }
        assert domain != null
        domain
    }

    /**
     * Compare  expected data (in map) to  new data (json)
     * This method is used in update test method to check if data are well changed
     * @param map Excpected data
     * @param json New Data
     */
    static void compare(map, json) {

        println "map=$map"
        println "json=$json"
        map.each {
            def propertyName = it.key
            def propertyValue = it.value

            println "propertyName=$propertyName"
            println "propertyValue=$propertyValue"
            def compareValue = json[it.key]
            println "compareValue=$compareValue"
            assert toString(propertyValue).equals(toString(compareValue))
        }
    }

    static String toString(def data) {
        return data + ""
    }


    static boolean checkIfDomainExist(def domain, boolean exist=true) {
        try {
            domain.refresh()
        } catch(Exception e) {}
        log.info "Check if domain ${domain.class} exist=${exist}"
       assert ((domain.read(domain.id)!=null) == exist)
    }

    static void checkIfDomainsExist(def domains) {
        domains.each {
            checkIfDomainExist(it,true)
        }
    }

    static void checkIfDomainsNotExist(def domains) {
        domains.each {
            checkIfDomainExist(it,false)
        }
    }

    static User getUser() {
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


    static User getUser1() {
        return User.findByUsername("lrollus")
    }


    static User getUser2() {
        return User.findByUsername("stevben")
    }




    static UserJob getUserJob(Project project) {
        Job job = BasicInstanceBuilder.getJobNotExist()
        if(project) job.project = project
        saveDomain(job)
        BasicInstanceBuilder.getSoftwareProjectNotExist(job.software,job.project,true)
        UserJob userJob = BasicInstanceBuilder.getUserJob()
        userJob.job = job
        userJob.user = BasicInstanceBuilder.getUser1()
        saveDomain(userJob)
    }


    static UserJob getUserJob() {
        log.debug "getUserJob()"
        UserJob userJob = UserJob.findByUsername("BasicUserJob")
        if (!userJob) {
            userJob = new UserJob(username: "BasicUserJob",password: "PasswordUserJob",enabled: true,user : User.findByUsername(Infos.GOODLOGIN),job: getJob())
            userJob.generateKeys()
            saveDomain(userJob)
            User.findByUsername(Infos.GOODLOGIN).getAuthorities().each { secRole ->
                SecUserSecRole.create(userJob, secRole)
            }
        }
        userJob
    }

    static String getRandomString() {
        def random = new Random()
        new Date().time.toString() + random.nextInt()
    }

    static UserJob getUserJobNotExist(boolean save = false) {
        log.debug "getUserJobNotExist()"
        def user = getUser1()
        def job = getJobNotExist(true)

        UserJob userJob = new UserJob(username:getRandomString(),password: "PasswordUserJob",enabled: true,user : user,job: job)
        userJob.generateKeys()

        if(save) {
            saveDomain(userJob)
            userJob.user.getAuthorities().each { secRole ->
                SecUserSecRole.create(user, secRole)
            }
        } else{
            checkDomain(userJob)
        }
        userJob
    }

    static User getAdmin(String username, String password) {
        User user = getUser(username,password)
            try {
               SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_ADMIN"))
            } catch(Exception e) {
                log.warn(e)
            }
        assert user != null
        user
    }

    static ImageInstance getImageInstance() {
        log.info "getImageInstance()"
//
//        def images = ImageInstance.list([max: 1, sort: 'id'])
//        def image
//        if (images.isEmpty()) {
//            images << saveDomain(getImageInstanceNotExist())
//        }
//        images.first()
        saveDomain(getImageInstanceNotExist())
    }

    static ImageInstance getImageInstanceNotExist(Project project = BasicInstanceBuilder.getProject(), boolean save = false) {
        log.info "getImageInstanceNotExist()"
        ImageInstance image = new ImageInstance(
                baseImage: saveDomain(BasicInstanceBuilder.getAbstractImageNotExist()),
                project: project,
                //slide: BasicInstanceBuilder.getSlide(),
                user: getUser1())

        println image.user
        save ? BasicInstanceBuilder.saveDomain(image) : BasicInstanceBuilder.checkDomain(image)
    }


    static AlgoAnnotation getAlgoAnnotation() {
        log.debug "getAlgoAnnotation()"
        def annotation = AlgoAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: getImageInstance(),
                user: getUserJob(),
                project:getImageInstance().project
        )
        saveDomain(annotation)
    }


    static AlgoAnnotation getAlgoAnnotationNotExist(Job job = getJob(), UserJob user = getUserJob(),boolean save = false) {
        log.debug "getAlgoAnnotationNotExist()"
        AlgoAnnotation annotation = new AlgoAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:getImageInstance(),
                user: user,
                project:getImageInstance().project
        )
        save ? saveDomain(annotation) : checkDomain(annotation)
    }

//    static AlgoAnnotationTerm createAlgoAnnotationTerm(Job job, AnnotationDomain annotation, UserJob userJob) {
//        AlgoAnnotationTerm at = getAlgoAnnotationTermNotExist()
//        at.project = job.project
//        at.annotationIdent = annotation.id
//        at.annotationClassName = annotation.class.getName()
//        at.userJob = userJob
//        checkDomain(at)
//        saveDomain(at)
//        at
//    }



    //CytomineDomain annotation = (useAlgoAnnotation? saveDomain(getUserAnnotationNotExist()) :  saveDomain(getAlgoAnnotationNotExist()))

    static AlgoAnnotationTerm getAlgoAnnotationTerm(Job job = getJob(), AnnotationDomain annotation, UserJob user = getUserJob()) {
        log.debug "getAlgoAnnotationTerm()"
        def term = saveDomain(getTermNotExist())

        def algoannotationTerm = new AlgoAnnotationTerm(term:term,expectedTerm:term,userJob:user,rate:0)
        algoannotationTerm.setAnnotation(annotation)
        saveDomain(algoannotationTerm)
    }

    static AlgoAnnotationTerm getAlgoAnnotationTerm(boolean useAlgoAnnotation) {
        getAlgoAnnotationTerm(getJob(),getUserJob(),useAlgoAnnotation)
    }

    static AlgoAnnotationTerm getAlgoAnnotationTerm(Job job = getJob(), UserJob user = getUserJob(),boolean useAlgoAnnotation = false) {
        log.debug "getAlgoAnnotationTerm()"
        def annotation = (useAlgoAnnotation? saveDomain(getAlgoAnnotationNotExist()) :  saveDomain(getUserAnnotationNotExist()))
        getAlgoAnnotationTerm(job,annotation,user)
    }

    //getAlgoAnnotationTermForAlgoAnnotation
    static AlgoAnnotationTerm getAlgoAnnotationTermNotExist(Job job = getJob(),UserJob userJob = getUserJob(),AnnotationDomain annotation = saveDomain(getUserAnnotationNotExist()),boolean save = false) {
        log.debug "getAnnotationTermNotExist()"
        def term = saveDomain(getTermNotExist())
        def algoannotationTerm = new AlgoAnnotationTerm(term:term,userJob:userJob, expectedTerm: term, rate:1d)
        algoannotationTerm.setAnnotation(annotation)
        algoannotationTerm
    }

    static AlgoAnnotationTerm getAlgoAnnotationTermNotExistForAlgoAnnotation() {
        log.debug "getAlgoAnnotationTermNotExistForAlgoAnnotation()"
        def term = getTermNotExist()
        term.save(flush: true)
        assert term != null
        def annotation = getAlgoAnnotationNotExist()
        annotation.save(flush: true)
        assert annotation != null
        def user = getUserJobNotExist()
        user.save(flush: true)
        assert user != null

        def algoannotationTerm = new AlgoAnnotationTerm(term:term,userJob:user, expectedTerm: term, rate:1d)
        algoannotationTerm.setAnnotation(annotation)
        algoannotationTerm
    }

    static ReviewedAnnotation createReviewAnnotation(ImageInstance image) {
        ReviewedAnnotation review = getReviewedAnnotationNotExist()
        review.project = image.project
        review.image = image
        checkDomain(review)
        saveDomain(review)
        review
    }

    static ReviewedAnnotation getReviewedAnnotation() {
         log.debug "getReviewedAnnotation()"
         def basedAnnotation = saveDomain(getUserAnnotationNotExist())
         def image = getImageInstance()
         def annotation = ReviewedAnnotation.findOrCreateWhere(
                 location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                 image: image,
                 user: getUser1(),
                 project:image.project,
                 status : 0,
                 reviewUser: getUser1()
         )
         annotation.putParentAnnotation(basedAnnotation)
         saveDomain(annotation)

         def term = getTerm()
         term.ontology = image.project.ontology
         checkDomain(term)
         saveDomain(term)

         annotation.addToTerms(term)
         checkDomain(annotation)
         saveDomain(annotation)
         annotation
     }

     static ReviewedAnnotation getReviewedAnnotationNotExist() {
         log.debug "getReviewedAnnotation()"
         def basedAnnotation = saveDomain(getUserAnnotationNotExist())

         def annotation = ReviewedAnnotation.findOrCreateWhere(
                 location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                 image: getImageInstance(),
                 user: getUser1(),
                 project:getImageInstance().project,
                 status : 0,
                 reviewUser: getUser1()
         )
         annotation.putParentAnnotation(basedAnnotation)
         checkDomain(annotation)
     }

    static AnnotationTerm getAnnotationTerm() {
        log.debug "getAnnotationTerm()"
        def annotation = saveDomain(getUserAnnotationNotExist())
        def term = saveDomain(getTermNotExist())
        def user = getUser1()
        saveDomain(new AnnotationTerm(userAnnotation: annotation, term: term,user: user))
    }

    static AnnotationTerm getAnnotationTermNotExist(UserAnnotation annotation=saveDomain(getUserAnnotationNotExist()),boolean save=false) {
        log.debug "getAnnotationTermNotExist()"
        def term = getTermNotExist()
        term.ontology = annotation.project.ontology
        saveDomain(term)
        def user = getUser1()
        def annotationTerm = new AnnotationTerm(userAnnotation:annotation,term:term,user:user)
        save ? saveDomain(annotationTerm) : checkDomain(annotationTerm)
    }

    static UserAnnotation getUserAnnotation() {
        log.debug "getUserAnnotation()"
        def annotation = UserAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: getImageInstance(),
                user: getUser1(),
                project:getImageInstance().project
        )
        saveDomain(annotation)
    }

    static UserAnnotation getUserAnnotationNotExist(Project project = getImageInstance().project, boolean save = false) {
        getUserAnnotationNotExist(project,getImageInstance(),save)
    }

    static UserAnnotation getUserAnnotationNotExist(Project project = getImageInstance().project, ImageInstance image,boolean save = false) {
        log.debug "getUserAnnotationNotExist()"
        UserAnnotation annotation = new UserAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: getUser1(),
                project:project
        )
        println annotation.user
        save ? saveDomain(annotation) : checkDomain(annotation)
    }


    static SharedAnnotation getSharedAnnotation() {
        log.debug "getSharedAnnotation()"
        def sharedannotation = SharedAnnotation.findOrCreateWhere(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: getUserAnnotation()
        )
        saveDomain(sharedannotation)
    }

    static SharedAnnotation getSharedAnnotationNotExist(boolean save = false) {
        log.debug "getSharedAnnotationNotExist()"
        def sharedannotation = new SharedAnnotation(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: getUserAnnotation()
        )
        save ? saveDomain(sharedannotation) : checkDomain(sharedannotation)
    }


    static ImageFilter getImageFilter() {
        log.debug "getImageFilter()"
       def imagefilter = ImageFilter.findByName("imagetest")
       if(!imagefilter) {
           imagefilter = new ImageFilter(name:"imagetest",baseUrl:"baseurl",processingServer:getProcessingServer())
           saveDomain(imagefilter)
       }
        imagefilter
    }

    static ImageFilter getImageFilterNotExist(boolean save = false) {
        log.debug "getImageFilterNotExist()"
       def imagefilter = new ImageFilter(name:"imagetest"+new Date(),baseUrl:"baseurl",processingServer:getProcessingServer())
        save ? saveDomain(imagefilter) : checkDomain(imagefilter)
    }

    static ImageFilterProject getImageFilterProject() {
        log.debug "getImageFilterProject()"
       def imageFilterProject = ImageFilterProject.find()
        if(!imageFilterProject) {
            imageFilterProject = new ImageFilterProject(imageFilter:getImageFilter(),project:getProject())
            saveDomain(imageFilterProject)
        }
        return imageFilterProject
    }

    static ImageFilterProject getImageFilterProjectNotExist(boolean save = false) {
        log.debug "getImageFilterProjectNotExist()"
        def imagefilter = saveDomain(getImageFilterNotExist())
        def project = saveDomain(getProjectNotExist())
        def ifp = new ImageFilterProject(imageFilter: imagefilter, project: project)

        save ? saveDomain(ifp) : checkDomain(ifp)
    }

    static AbstractImage getAbstractImage() {
        log.debug "getAbstractImage()"
        AbstractImage image = AbstractImage.findByFilename("filename")
        if (!image) {
            image = new AbstractImage(filename: "filename", scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath")
        }
        saveDomain(image)
    }

    static AbstractImage getAbstractImageNotExist(boolean save = false) {
        log.debug "getImageNotExist()"
        def image = new AbstractImage(filename: getRandomString(), scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath")
        save ? saveDomain(image) : checkDomain(image)
    }

    static AbstractImageGroup getAbstractImageGroup() {
        log.debug "getAbstractImageGroup()"
        def abstractImage = saveDomain(getAbstractImageNotExist())
        def group = saveDomain(getGroupNotExist())
        saveDomain(new AbstractImageGroup(abstractImage:abstractImage, group:group))
    }

    static AbstractImageGroup getAbstractImageGroupNotExist(boolean save = false) {
        log.debug "getAbstractImageGroupNotExist()"
        def group = saveDomain(getGroupNotExist())
        def abstractImage = saveDomain(getAbstractImageNotExist())
        def abstractImageGroup = new AbstractImageGroup(abstractImage: abstractImage, group: group)
        save ? saveDomain(abstractImageGroup) : checkDomain(abstractImageGroup)
    }

    static StorageAbstractImage getStorageAbstractImage() {
        log.debug("getStorageAbstractImage()")
        def storage = getStorage()
        def abstractImage = getAbstractImage()
        StorageAbstractImage sai = StorageAbstractImage.findByStorageAndAbstractImage(storage, abstractImage)
        if (!sai) {
            sai = new StorageAbstractImage(storage : storage, abstractImage : abstractImage)
            saveDomain(sai)
        }
        sai
    }

    static ProcessingServer getProcessingServer() {
        log.debug "getProcessingServer()"
        def ps = ProcessingServer.findByUrl("processing_server_url")
        if (!ps) {
            ps = new ProcessingServer(url: "processing_server_url")
            saveDomain(ps)
        }
        ps
    }

    static Discipline getDiscipline() {
        log.debug "getDiscipline()"
        def discipline = Discipline.findByName("BASICDISCIPLINE")
        if (!discipline) {
            discipline = new Discipline(name: "BASICDISCIPLINE")
            saveDomain(discipline)
        }
        discipline
    }

    static Discipline getDisciplineNotExist() {
        log.debug "getDisciplineNotExist()"
        Discipline discipline = new Discipline(name: getRandomString())
        checkDomain(discipline)
    }

    static AnnotationFilter getAnnotationFilter() {
        log.debug "getAnnotationFilter()"
        def filter = AnnotationFilter.findByName("BASICFILTER")
        if (!filter) {
            filter = new AnnotationFilter(name:"BASICFILTER",project:getProject(),user:getUser1())
            saveDomain(filter)
            filter.addToTerms(getTerm())
            filter.addToUsers(getUser1())
            saveDomain(filter)
        }
        filter
    }

    static AnnotationFilter getAnnotationFilterNotExist() {
        log.debug "getAnnotationFilterNotExist()"
        def annotationFilter = new AnnotationFilter(name:getRandomString(),project:getProject(),user: getUser1())
        annotationFilter.addToTerms(getTerm())
        annotationFilter.addToUsers(getUser1())
        checkDomain(annotationFilter)
    }

    static Sample getSample() {
        log.debug "getSample()"
        def sample = Sample.findByName("BASICSAMPLE")
        if (!sample) {
            sample = new Sample(name: "BASICSAMPLE")
        }
        saveDomain(sample)
    }

    static Sample getSampleNotExist() {
        log.debug "getSampleNotExist()"
        def sample = new Sample(name: getRandomString())
        checkDomain(sample)
    }


    static Job getJob() {
        log.debug  "getJob()"
        def job = Job.findByProjectAndSoftware(getProject(),getSoftware())
        if(!job) {
            job = new Job(project:getProject(),software:getSoftware())
            saveDomain(job)
        }
        job
    }

    static Job getJobNotExist(boolean save = false) {
        log.debug "getJobNotExist()"
        Job job =  new Job(software:saveDomain(getSoftwareNotExist()), project : saveDomain(getProjectNotExist()))
        save ? saveDomain(job) : checkDomain(job)
    }


    static JobData getJobDataNotExist() {
        log.debug "getJobDataNotExist()"
        JobData jobData =  new JobData(job:saveDomain(getJobNotExist()), key : "TESTKEY", filename: "filename.jpg")
        checkDomain(jobData)
    }


    static JobData getJobData() {
        log.debug  "getJobData()"
        saveDomain(getJobDataNotExist())
    }

    static JobParameter getJobParameter() {
         log.debug "getJobparameter()"
         def job = getJob()
         def softwareParam = getSoftwareParameter()
         def jobParameter = JobParameter.findByJobAndSoftwareParameter(job,softwareParam)
         if (!jobParameter) {
             jobParameter = new JobParameter(value: "toto", job:job,softwareParameter:softwareParam)
             saveDomain(jobParameter)
         }
         jobParameter
     }

     static JobParameter getJobParameterNotExist() {
         log.debug "getJobparameterNotExist()"
         def job = saveDomain(getJobNotExist())
         def softwareParam = saveDomain(getSoftwareParameterNotExist() )
         def jobParameter = new JobParameter(value: "toto", job:job,softwareParameter:softwareParam)
         checkDomain(jobParameter)
     }

    static Ontology getOntology() {
        log.debug "getOntology()"
        def ontology = Ontology.findByName("BasicOntology")
        if (!ontology) {
            ontology = new Ontology(name: "BasicOntology", user: getUser1())
            saveDomain(ontology)
            def term = getTermNotExist()
            term.ontology = ontology
            saveDomain(term)
        }
        ontology
    }

    static Ontology getOntologyNotExist(boolean save = false) {
        log.debug "getOntologyNsotExist()"
        Ontology ontology = new Ontology(name: getRandomString() + "", user: getUser1())
        save ? saveDomain(ontology) : checkDomain(ontology)
        if (save) {
            Term term = getTermNotExist(true)
            term.ontology = ontology
            saveDomain(ontology)
        }
        ontology
    }

    static Project getProject() {
        log.debug "getProject()"
        def name = "BasicProject".toUpperCase()
        def project = Project.findByName(name)
        if (!project) {
            project = new Project(name: name, ontology: getOntology(), discipline: getDiscipline(), description: "BasicDescription")
            saveDomain(project)
            try {
                Infos.addUserRight(Infos.GOODLOGIN,project)
            } catch(Exception e) {}
        }
        project
    }


    static Project getProjectNotExist(boolean save = false) {
        log.debug "getProjectNotExist()"
        Project project = new Project(name: getRandomString(), ontology: saveDomain(getOntologyNotExist()), discipline: getDiscipline(), description: "BasicDescription" )
        if(save) {
            saveDomain(project)
            Infos.addUserRight(Infos.GOODLOGIN,project)
        } else{
            checkDomain(project)
        }
        return project
    }


    static Relation getRelation() {
        log.debug "getRelation()"
        def relation = Relation.findByName("BasicRelation")
        if (!relation) {
            relation = new Relation(name: "BasicRelation")
            saveDomain(relation)
        }
        relation
    }

    static Relation getRelationNotExist() {
        log.debug "getRelationNotExist()"
        def relation = new Relation(name: getRandomString())
        checkDomain(relation)
    }




    static RelationTerm getRelationTerm() {
        log.debug "getRelationTerm()"
        def relation = getRelation()
        def term1 = getTerm()
        def term2 = getAnotherBasicTerm()

        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) {
            relationTerm = new RelationTerm(relation:relation, term1:term1, term2:term2)
            saveDomain(relationTerm)
        }
        relationTerm
    }

    static RelationTerm getRelationTermNotExist() {
        log.debug "getRelationTermNotExist()"
        def relation = saveDomain(getRelationNotExist())
        def term1 = saveDomain(getTermNotExist())
        def term2 = saveDomain(getTermNotExist())
        term2.ontology = term1.ontology
        saveDomain(term2)
        def relationTerm = new RelationTerm(relation: relation, term1: term1, term2: term2)
        checkDomain(relationTerm)
    }

    static Mime getMime() {
        log.debug "getMime1()"
        def mime = Mime.findByExtension("tif")
        if(!mime) {
            mime = new Mime(extension:"tif",mimeType: "tif")
            saveDomain(mime)
            def mis = new MimeImageServer(imageServer: getImageServer(),mime:mime)
            saveDomain(mis)
        }
        mime.refresh()
        mime.imageServers()
        mime
    }

    static Mime getMimeNotExist() {
        log.debug "getMimeNotExist()"
        def mime = Mime.findByMimeType("mimeT");
        if (mime == null) {
            mime = new Mime(extension: "ext", mimeType: "mimeT")
            saveDomain(mime)
        }
        mime
    }

    static AnnotationProperty createAnnotationProperty(AnnotationDomain annotation) {
        AnnotationProperty ap = getAnnotationPropertyNotExist()
        ap.annotationIdent = annotation.id
        ap.annotationClassName = annotation.class.getName()
        ap.annotation = annotation
        checkDomain(ap)
        saveDomain(ap)
        ap
    }

    static AnnotationProperty getAnnotationProperty() {
        log.debug "getAnnotationProperty()"
        def annotation = getUserAnnotation()
        def annotationProperty = AnnotationProperty.findByAnnotationIdentAndKey(annotation.id,'MyKeyBasic')
        if (!annotationProperty) {
            annotationProperty = new AnnotationProperty(annotation: annotation, key: 'MyKeyBasic', value:"MyValueBasic")
            saveDomain(annotationProperty)
        }
        annotationProperty
    }

    static AnnotationProperty getAnnotationPropertyNotExist(UserAnnotation annotation = getUserAnnotation(), boolean save = false) {
        log.debug "getAnnotationProperty()"
        def annotationProperty = new AnnotationProperty(annotation: annotation, key: getRandomString(),value: "MyValueBasic")
        save? saveDomain(annotationProperty) : checkDomain(annotationProperty)
    }

    static Instrument getScanner() {
        log.debug "getScanner()"
        Instrument scanner = new Instrument(brand: "brand", model: "model")
        saveDomain(scanner)
    }

    static Instrument getNewScannerNotExist() {
        log.debug "getNewScannerNotExist()"
        def scanner = new Instrument(brand: "newBrand", model: getRandomString())
        saveDomain(scanner)
    }

    static Sample getSlide() {
        log.debug "getSlide()"
        def name = "BasicSlide".toUpperCase()
        def slide = Sample.findByName(name)
        if (!slide) {
            slide = new Sample(name: name)
            saveDomain(slide)
        }
        slide
    }

    static Sample getSlideNotExist() {
        log.debug "getSlideNotExist()"
        def slide = new Sample(name: getRandomString())
        checkDomain(slide)
    }

    static User getUser(String username, String password) {
        log.debug "getUser()"
        def user = SecUser.findByUsername(username)
        if (!user) {
            user = new User(username: username,firstname: "Basic",lastname: "User",email: "Basic@User.be",password: password,enabled: true)
            user.generateKeys()
            saveDomain(user)
            try {
               SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_USER"),true)
            } catch(Exception e) {
                log.warn(e)
            }
        }
        user
    }

    static User getUserNotExist() {
        log.debug "getUserNotExist()"
       User user = new User(username: getRandomString(),firstname: "BasicNotExist",lastname: "UserNotExist",email: "BasicNotExist@User.be",password: "password",enabled: true)
        user.generateKeys()
        checkDomain(user)
    }

    static Group getGroup() {
        log.debug "getGroup()"
        def name = "BasicGroup".toUpperCase()
        def group = Group.findByName(name)
        if (!group) {
            group = new Group(name: name)
            saveDomain(group)
        }
        group
    }

    static Group getGroupNotExist() {
        log.debug "getGroupNotExist()"
        Group group = new Group(name: getRandomString())
        checkDomain(group)
    }

    static Storage getStorage() {
        def storage = Storage.findByName("bidon")
        if(!storage) {
            storage = new Storage(name:"bidon",basePath:"storagepath",ip:"192.168.0.0",user: getUser1(),port: 123)
            saveDomain(storage)
        }
        return storage
    }

    static Storage getStorageNotExist() {
        log.debug "getStorageNotExist()"
        Storage storage = new Storage(name: getRandomString(), basePath: getRandomString(), ip: getRandomString(), port: 22, user: getUser1())
        checkDomain(storage)
        storage
    }

    static Term getTerm() {
        log.debug "getTerm()"
        def term = Term.findByName("BasicTerm")
        if (!term) {
            term = new Term(name: "BasicTerm", ontology: getOntology(), color: "FF0000")
            saveDomain(term)
        }
        term
    }

    static Term getAnotherBasicTerm() {
        log.debug "getTerm()"
        def term = Term.findByName("AnotherBasicTerm")
        if (!term) {
            term = new Term(name: "AnotherBasicTerm", ontology: getOntology(), color: "F0000F")
            saveDomain(term)
        }
        term
    }

    static Term getTermNotExist(boolean save = false) {
        log.debug "getTermNotExist()"
        getTermNotExist(saveDomain(getOntologyNotExist()), save)
    }

    static Term getTermNotExist(Ontology ontology,boolean save = false) {
        log.debug "getTermNotExist()"
        Term term = new Term(name: getRandomString(), ontology: ontology, color: "0F00F0")
        save ? saveDomain(term) :  checkDomain(term)
    }

    static Software getSoftware() {
        log.debug "getSoftware()"
        def software = Software.findByName("AnotherBasicSoftware")
        if (!software) {
            software = new Software(name: "AnotherBasicSoftware", serviceName:"helloWorldJobService")
            saveDomain(software)
        }
        software
    }

    static Software getSoftwareNotExist() {
        log.debug "getSoftwareNotExist()"
        def software = new Software(name: getRandomString(),serviceName:"helloWorldJobService")
        software.validate()
        software
    }

    static SoftwareParameter getSoftwareParameter() {
        log.debug "getSoftwareParameter()"
        Software software = getSoftware()
        def parameter = SoftwareParameter.findBySoftware(software)
        if (!parameter) {
            parameter = new SoftwareParameter(name:"anotherParameter",software:software,type:"String")
            saveDomain(parameter)
        }
        parameter
    }

    static SoftwareParameter getSoftwareParameterNotExist() {
        log.debug "getSoftwareParameterNotExist()"
        Software software = getSoftware()
        def parameter =   new SoftwareParameter(name: getRandomString(),software:software,type:"String")
        checkDomain(parameter)
    }

    static ImageServer getImageServer() {
        log.debug "getImageServer()"

        def imageServer = ImageServer.findByName("bidon")
        if (!imageServer) {
            imageServer = new ImageServer(name:"bidon",url:"http://bidon.server.com/",service:"service",className:"sample",available:true)
            saveDomain(imageServer)
        }

        def storage = getStorage()

        def imageServerStorage = ImageServerStorage.findByImageServerAndStorage(imageServer, storage)
        if (!imageServerStorage) {
            imageServerStorage = new ImageServerStorage(imageServer: imageServer, storage : storage)
            saveDomain(imageServerStorage)
        }
        imageServer
    }

    static SoftwareProject getSoftwareProject() {
        log.debug "getSoftwareProject()"
        SoftwareProject softproj = new SoftwareProject(software:getSoftware(),project:getProject())
        saveDomain(softproj)
    }

    static SoftwareProject getSoftwareProjectNotExist(Software software = saveDomain(getSoftwareNotExist()), Project project = saveDomain(getProjectNotExist()), boolean save = false) {
        log.debug "getSoftwareProjectNotExist()"
        SoftwareProject softproj = new SoftwareProject(software:software,project:project)
        save ? saveDomain(softproj) : checkDomain(softproj)
    }

    static Job createJobWithAlgoAnnotationTerm() {
         Project project = getProjectNotExist(true)
         Ontology ontology = project.ontology

         Term term1 = getTermNotExist(ontology,true)
         Term term2 = getTermNotExist(ontology,true)


         UserJob userJob = getUserJobNotExist(true)
         Job job = userJob.job
         job.project = project
         saveDomain(job)
         AlgoAnnotationTerm algoAnnotationGood = getAlgoAnnotationTermNotExist()
         algoAnnotationGood.term = term1
         algoAnnotationGood.expectedTerm = term1
         algoAnnotationGood.userJob = userJob
         saveDomain(algoAnnotationGood)

         AlgoAnnotationTerm algoAnnotationBad = getAlgoAnnotationTermNotExist()
         algoAnnotationBad.term = term1
         algoAnnotationBad.expectedTerm = term2
         algoAnnotationBad.userJob = userJob
         saveDomain(algoAnnotationBad)
         return job
     }
}
