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
import com.vividsolutions.jts.io.WKTReader
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


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
        map.each {
            def propertyName = it.key
            def propertyValue = it.value
            def compareValue = json[it.key]
            assert toString(propertyValue).equals(toString(compareValue))
        }
    }

    static String toString(def data) {
        try {
            return data.toString()
        } catch(Exception e) {
            return data+""
        }
    }


    static boolean checkIfDomainExist(def domain, boolean exist=true) {
        try {
            domain.refresh()
        } catch(Exception e) {}
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
            saveDomain(user)
        }
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
        user
    }

    static ImageInstance getImageInstance() {
        saveDomain(getImageInstanceNotExist())
    }

    static ImageInstance getImageInstanceNotExist(Project project = BasicInstanceBuilder.getProject(), boolean save = false) {
        ImageInstance image = new ImageInstance(
                baseImage: saveDomain(BasicInstanceBuilder.getAbstractImageNotExist()),
                project: project,
                //slide: BasicInstanceBuilder.getSlide(),
                user: getUser1())

        println image.user
        save ? BasicInstanceBuilder.saveDomain(image) : BasicInstanceBuilder.checkDomain(image)
    }


    static AlgoAnnotation getAlgoAnnotation() {
        def annotation = AlgoAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: getImageInstance(),
                user: getUserJob(),
                project:getImageInstance().project
        )
        saveDomain(annotation)
    }


    static AlgoAnnotation getAlgoAnnotationNotExist(Job job = getJob(), UserJob user = getUserJob(),boolean save = false) {
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
        def term = saveDomain(getTermNotExist())

        def algoannotationTerm = new AlgoAnnotationTerm(term:term,expectedTerm:term,userJob:user,rate:0)
        algoannotationTerm.setAnnotation(annotation)
        saveDomain(algoannotationTerm)
    }

    static AlgoAnnotationTerm getAlgoAnnotationTerm(boolean useAlgoAnnotation) {
        getAlgoAnnotationTerm(getJob(),getUserJob(),useAlgoAnnotation)
    }

    static AlgoAnnotationTerm getAlgoAnnotationTerm(Job job = getJob(), UserJob user = getUserJob(),boolean useAlgoAnnotation = false) {
        def annotation = (useAlgoAnnotation? saveDomain(getAlgoAnnotationNotExist()) :  saveDomain(getUserAnnotationNotExist()))
        getAlgoAnnotationTerm(job,annotation,user)
    }

    //getAlgoAnnotationTermForAlgoAnnotation
    static AlgoAnnotationTerm getAlgoAnnotationTermNotExist(Job job = getJob(),UserJob userJob = getUserJob(),AnnotationDomain annotation = saveDomain(getUserAnnotationNotExist()),boolean save = false) {
        def term = saveDomain(getTermNotExist())
        def algoannotationTerm = new AlgoAnnotationTerm(term:term,userJob:userJob, expectedTerm: term, rate:1d)
        algoannotationTerm.setAnnotation(annotation)
        algoannotationTerm
    }

    static AlgoAnnotationTerm getAlgoAnnotationTermNotExistForAlgoAnnotation() {
        def term = saveDomain(getTermNotExist())
        def annotation = saveDomain(getAlgoAnnotationNotExist())
        def user = saveDomain(getUserJobNotExist())
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
        def annotation = saveDomain(getUserAnnotationNotExist())
        def term = saveDomain(getTermNotExist())
        def user = getUser1()
        saveDomain(new AnnotationTerm(userAnnotation: annotation, term: term,user: user))
    }

    static AnnotationTerm getAnnotationTermNotExist(UserAnnotation annotation=saveDomain(getUserAnnotationNotExist()),boolean save=false) {
        def term = getTermNotExist()
        term.ontology = annotation.project.ontology
        saveDomain(term)
        def user = getUser1()
        def annotationTerm = new AnnotationTerm(userAnnotation:annotation,term:term,user:user)
        save ? saveDomain(annotationTerm) : checkDomain(annotationTerm)
    }

    static UserAnnotation getUserAnnotation() {
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
        def sharedannotation = SharedAnnotation.findOrCreateWhere(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: getUserAnnotation()
        )
        saveDomain(sharedannotation)
    }

    static SharedAnnotation getSharedAnnotationNotExist(boolean save = false) {
        def sharedannotation = new SharedAnnotation(
                sender: User.findByUsername(Infos.GOODLOGIN),
                comment: "This is a test",
                userAnnotation: getUserAnnotation()
        )
        save ? saveDomain(sharedannotation) : checkDomain(sharedannotation)
    }


    static ImageFilter getImageFilter() {
       def imagefilter = ImageFilter.findByName("imagetest")
       if(!imagefilter) {
           imagefilter = new ImageFilter(name:"imagetest",baseUrl:"baseurl",processingServer:getProcessingServer())
           saveDomain(imagefilter)
       }
        imagefilter
    }

    static ImageFilter getImageFilterNotExist(boolean save = false) {
       def imagefilter = new ImageFilter(name:"imagetest"+new Date(),baseUrl:"baseurl",processingServer:getProcessingServer())
        save ? saveDomain(imagefilter) : checkDomain(imagefilter)
    }

    static ImageFilterProject getImageFilterProject() {
       def imageFilterProject = ImageFilterProject.find()
        if(!imageFilterProject) {
            imageFilterProject = new ImageFilterProject(imageFilter:getImageFilter(),project:getProject())
            saveDomain(imageFilterProject)
        }
        return imageFilterProject
    }

    static ImageFilterProject getImageFilterProjectNotExist(boolean save = false) {
        def imagefilter = saveDomain(getImageFilterNotExist())
        def project = saveDomain(getProjectNotExist())
        def ifp = new ImageFilterProject(imageFilter: imagefilter, project: project)

        save ? saveDomain(ifp) : checkDomain(ifp)
    }

    static AbstractImage getAbstractImage() {
        AbstractImage image = AbstractImage.findByFilename("filename")
        if (!image) {
            image = new AbstractImage(filename: "filename", scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath")
        }
        saveDomain(image)
    }

    static AbstractImage getAbstractImageNotExist(boolean save = false) {
        def image = new AbstractImage(filename: getRandomString(), scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath")
        save ? saveDomain(image) : checkDomain(image)
    }

    static AbstractImageGroup getAbstractImageGroup() {
        def abstractImage = saveDomain(getAbstractImageNotExist())
        def group = saveDomain(getGroupNotExist())
        saveDomain(new AbstractImageGroup(abstractImage:abstractImage, group:group))
    }

    static AbstractImageGroup getAbstractImageGroupNotExist(boolean save = false) {
        def group = saveDomain(getGroupNotExist())
        def abstractImage = saveDomain(getAbstractImageNotExist())
        def abstractImageGroup = new AbstractImageGroup(abstractImage: abstractImage, group: group)
        save ? saveDomain(abstractImageGroup) : checkDomain(abstractImageGroup)
    }

    static StorageAbstractImage getStorageAbstractImage() {
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
        def ps = ProcessingServer.findByUrl("processing_server_url")
        if (!ps) {
            ps = new ProcessingServer(url: "processing_server_url")
            saveDomain(ps)
        }
        ps
    }

    static Discipline getDiscipline() {
        def discipline = Discipline.findByName("BASICDISCIPLINE")
        if (!discipline) {
            discipline = new Discipline(name: "BASICDISCIPLINE")
            saveDomain(discipline)
        }
        discipline
    }

    static Discipline getDisciplineNotExist() {
        Discipline discipline = new Discipline(name: getRandomString())
        checkDomain(discipline)
    }

    static AnnotationFilter getAnnotationFilter() {
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
        def annotationFilter = new AnnotationFilter(name:getRandomString(),project:getProject(),user: getUser1())
        annotationFilter.addToTerms(getTerm())
        annotationFilter.addToUsers(getUser1())
        checkDomain(annotationFilter)
    }

    static Sample getSample() {
        def sample = Sample.findByName("BASICSAMPLE")
        if (!sample) {
            sample = new Sample(name: "BASICSAMPLE")
        }
        saveDomain(sample)
    }

    static Sample getSampleNotExist() {
        def sample = new Sample(name: getRandomString())
        checkDomain(sample)
    }


    static Job getJob() {
        def job = Job.findByProjectAndSoftware(getProject(),getSoftware())
        if(!job) {
            job = new Job(project:getProject(),software:getSoftware())
            saveDomain(job)
        }
        job
    }

    static Job getJobNotExist(boolean save = false) {
        Job job =  new Job(software:saveDomain(getSoftwareNotExist()), project : saveDomain(getProjectNotExist()))
        save ? saveDomain(job) : checkDomain(job)
    }


    static JobData getJobDataNotExist() {
        JobData jobData =  new JobData(job:saveDomain(getJobNotExist()), key : "TESTKEY", filename: "filename.jpg")
        checkDomain(jobData)
    }


    static JobData getJobData() {
        saveDomain(getJobDataNotExist())
    }

    static JobParameter getJobParameter() {
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
         def job = saveDomain(getJobNotExist())
         def softwareParam = saveDomain(getSoftwareParameterNotExist() )
         def jobParameter = new JobParameter(value: "toto", job:job,softwareParameter:softwareParam)
         checkDomain(jobParameter)
     }

    static Ontology getOntology() {
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
        def relation = Relation.findByName("BasicRelation")
        if (!relation) {
            relation = new Relation(name: "BasicRelation")
            saveDomain(relation)
        }
        relation
    }

    static Relation getRelationNotExist() {
        def relation = new Relation(name: getRandomString())
        checkDomain(relation)
    }

    static RelationTerm getRelationTerm() {
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
        def relation = saveDomain(getRelationNotExist())
        def term1 = saveDomain(getTermNotExist())
        def term2 = saveDomain(getTermNotExist())
        term2.ontology = term1.ontology
        saveDomain(term2)
        def relationTerm = new RelationTerm(relation: relation, term1: term1, term2: term2)
        checkDomain(relationTerm)
    }

    static Mime getMime() {
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
        def annotation = getUserAnnotation()
        def annotationProperty = AnnotationProperty.findByAnnotationIdentAndKey(annotation.id,'MyKeyBasic')
        if (!annotationProperty) {
            annotationProperty = new AnnotationProperty(annotation: annotation, key: 'MyKeyBasic', value:"MyValueBasic")
            saveDomain(annotationProperty)
        }
        annotationProperty
    }

    static AnnotationProperty getAnnotationPropertyNotExist(UserAnnotation annotation = getUserAnnotation(), boolean save = false) {
        def annotationProperty = new AnnotationProperty(annotation: annotation, key: getRandomString(),value: "MyValueBasic")
        save? saveDomain(annotationProperty) : checkDomain(annotationProperty)
    }

    static Instrument getScanner() {
        Instrument scanner = new Instrument(brand: "brand", model: "model")
        saveDomain(scanner)
    }

    static Instrument getNewScannerNotExist(boolean save  = false) {
        def scanner = new Instrument(brand: "newBrand", model: getRandomString())
        save? saveDomain(scanner) : checkDomain(scanner)
    }

    static Sample getSlide() {
        def name = "BasicSlide".toUpperCase()
        def slide = Sample.findByName(name)
        if (!slide) {
            slide = new Sample(name: name)
            saveDomain(slide)
        }
        slide
    }

    static Sample getSlideNotExist() {
        def slide = new Sample(name: getRandomString())
        checkDomain(slide)
    }

    static User getUser(String username, String password) {
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

    static User getUserNotExist(boolean save = false) {
       User user = new User(username: getRandomString(),firstname: "BasicNotExist",lastname: "UserNotExist",email: "BasicNotExist@User.be",password: "password",enabled: true)
        user.generateKeys()
        save ? saveDomain(user) :  checkDomain(user)
    }

    static Group getGroup() {
        def name = "BasicGroup".toUpperCase()
        def group = Group.findByName(name)
        if (!group) {
            group = new Group(name: name)
            saveDomain(group)
        }
        group
    }

    static Group getGroupNotExist() {
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
        Storage storage = new Storage(name: getRandomString(), basePath: getRandomString(), ip: getRandomString(), port: 22, user: getUser1())
        checkDomain(storage)
        storage
    }

    static Term getTerm() {
        def term = Term.findByName("BasicTerm")
        if (!term) {
            term = new Term(name: "BasicTerm", ontology: getOntology(), color: "FF0000")
            saveDomain(term)
        }
        term
    }

    static Term getAnotherBasicTerm() {
        def term = Term.findByName("AnotherBasicTerm")
        if (!term) {
            term = new Term(name: "AnotherBasicTerm", ontology: getOntology(), color: "F0000F")
            saveDomain(term)
        }
        term
    }

    static Term getTermNotExist(boolean save = false) {
        getTermNotExist(saveDomain(getOntologyNotExist()), save)
    }

    static Term getTermNotExist(Ontology ontology,boolean save = false) {
        Term term = new Term(name: getRandomString(), ontology: ontology, color: "0F00F0")
        save ? saveDomain(term) :  checkDomain(term)
    }

    static Software getSoftware() {
        def software = Software.findByName("AnotherBasicSoftware")
        if (!software) {
            software = new Software(name: "AnotherBasicSoftware", serviceName:"helloWorldJobService")
            saveDomain(software)
        }
        software
    }

    static Software getSoftwareNotExist() {
        def software = new Software(name: getRandomString(),serviceName:"helloWorldJobService")
        software.validate()
        software
    }

    static SoftwareParameter getSoftwareParameter() {
        Software software = getSoftware()
        def parameter = SoftwareParameter.findBySoftware(software)
        if (!parameter) {
            parameter = new SoftwareParameter(name:"anotherParameter",software:software,type:"String")
            saveDomain(parameter)
        }
        parameter
    }

    static SoftwareParameter getSoftwareParameterNotExist() {
        Software software = getSoftware()
        def parameter =   new SoftwareParameter(name: getRandomString(),software:software,type:"String")
        checkDomain(parameter)
    }

    static ImageServer getImageServer() {

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
        SoftwareProject softproj = new SoftwareProject(software:getSoftware(),project:getProject())
        saveDomain(softproj)
    }

    static SoftwareProject getSoftwareProjectNotExist(Software software = saveDomain(getSoftwareNotExist()), Project project = saveDomain(getProjectNotExist()), boolean save = false) {
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
