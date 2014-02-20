package be.cytomine.test

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.image.*
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.image.server.*
import be.cytomine.laboratory.Sample
import be.cytomine.ontology.*
import be.cytomine.processing.*
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.*
import be.cytomine.social.SharedAnnotation
import be.cytomine.utils.AttachedFile
import be.cytomine.utils.Description
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

    private static Log log = LogFactory.getLog(BasicInstanceBuilder.class)

    /**
     * Check if a domain is valide during test
     * @param domain Domain to check
     */
    static def checkDomain(def domain) {
        boolean validate = domain.validate()
        if(!validate) {
            println domain.errors
        }
        assert validate
        domain
    }

    /**
     *  Check if a domain is well saved during test
     * @param domain Domain to check
     */
    static def saveDomain(def domain) {
        domain.save(flush: true, failOnError:true)
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
        domain = domain.read(domain.id)
        boolean domainExist = domain && !domain.checkDeleted()
       assert domainExist == exist
        domainExist
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
            SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_USER"),true)
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
        Job job = getJobNotExist()
        job.project = project
        saveDomain(job)
        getSoftwareProjectNotExist(job.software,job.project,true)
        UserJob userJob = getUserJobNotExist()
        userJob.job = job
        userJob.user = getUser1()
        saveDomain(userJob)
    }

    static UserJob getUserJob(String username, String password) {
        UserJob user = UserJob.findByUsername(username)
        if (!user) {
            user = new UserJob(username: username, user:User.findByUsername(Infos.GOODLOGIN),password: password,enabled: true,job: getJob())
            user.generateKeys()
            saveDomain(user)
            try {
                User.findByUsername(Infos.GOODLOGIN).getAuthorities().each { secRole ->
                    SecUserSecRole.create(userJob, secRole)
                }
            } catch(Exception e) {
                log.warn(e)
            }
        }
        user
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
        save ? BasicInstanceBuilder.saveDomain(image) : BasicInstanceBuilder.checkDomain(image)
    }


    static NestedImageInstance getNestedImageInstance() {
        saveDomain(getNestedImageInstanceNotExist())
    }

    static NestedImageInstance getNestedImageInstanceNotExist(ImageInstance imageInstance = BasicInstanceBuilder.getImageInstance(), boolean save = false) {
        NestedImageInstance nestedImage = new NestedImageInstance(
                baseImage: getAbstractImageNotExist(true),
                parent: imageInstance,
                x: 10,
                y:20,
                project: imageInstance.project,
                //slide: BasicInstanceBuilder.getSlide(),
                user: imageInstance.user)
        save ? BasicInstanceBuilder.saveDomain(nestedImage) : BasicInstanceBuilder.checkDomain(nestedImage)
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
                image:getImageInstanceNotExist(job.project,true),
                user: user,
                project:job.project
        )
        save ? saveDomain(annotation) : checkDomain(annotation)
    }

//    static AlgoAnnotationTerm createAlgoAnnotationTerm(Job job, AnnotationDomain annotation, UserJob userJob) {
//        AlgoAnnotationTerm at = getAlgoAnnotationTermNotExist()
//        at.project = job.project
//        at.annotationIdent = annotation.id
//        at.domainClassName = annotation.class.getName()
//        at.userJob = userJob
//        checkDomain(at)
//        saveDomain(at)
//        at
//    }



    //CytomineDomain annotation = (useAlgoAnnotation? saveDomain(getUserAnnotationNotExist()) :  saveDomain(getAlgoAnnotationNotExist()))

    static AlgoAnnotationTerm getAlgoAnnotationTerm(Job job = getJob(), AnnotationDomain annotation, UserJob user = getUserJob()) {
        def term = getTermNotExist()
        term.ontology = annotation.project.ontology
        saveDomain(term)
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
        def term = getTermNotExist()
        term.ontology = annotation.project.ontology
        saveDomain(term)
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

    static AlgoAnnotation getAlgoAnnotationNotExist(ImageInstance image, String polygon, UserJob user, Term term) {
        AlgoAnnotation annotation = new AlgoAnnotation(
                location: new WKTReader().read(polygon),
                image:image,
                user: user,
                project:project
        )
        annotation = saveDomain(annotation)


       def at = getAlgoAnnotationTermNotExist(user.job,user,annotation,true)
        at.term = term
        at.userJob = user
        saveDomain(at)
        annotation
    }

    static ReviewedAnnotation createReviewAnnotation(ImageInstance image) {
        ReviewedAnnotation review = getReviewedAnnotationNotExist()
        review.project = image.project
        review.image = image
        saveDomain(review)
        review
    }

    static ReviewedAnnotation createReviewAnnotation(AnnotationDomain annotation) {
        ReviewedAnnotation review = getReviewedAnnotationNotExist()
        review.project = annotation.project
        review.image = annotation.image
        review.location = annotation.location
        review.putParentAnnotation(annotation)
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
        def term = getTermNotExist()
        term.ontology = annotation.project.ontology
        term = saveDomain(term)
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
        save ? saveDomain(annotation) : checkDomain(annotation)
    }


    static RoiAnnotation getRoiAnnotation() {
        def annotation = RoiAnnotation.findOrCreateWhere(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image: getImageInstance(),
                user: getUser1(),
                project:getImageInstance().project
        )
        saveDomain(annotation)
    }

    static RoiAnnotation getRoiAnnotationNotExist(ImageInstance image = getImageInstance(),boolean save = false) {
        RoiAnnotation annotation = new RoiAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: getUser1(),
                project:image.project
        )
        save ? saveDomain(annotation) : checkDomain(annotation)
    }

    static RoiAnnotation getRoiAnnotationNotExist(ImageInstance image = getImageInstance(),User user,boolean save = false) {
        RoiAnnotation annotation = new RoiAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: user,
                project:image.project
        )
        save ? saveDomain(annotation) : checkDomain(annotation)
    }

    static UserAnnotation getUserAnnotationNotExist(ImageInstance image, String polygon, User user, Term term) {
        UserAnnotation annotation = new UserAnnotation(
                location: new WKTReader().read(polygon),
                image:image,
                user: user,
                project:project
        )
        annotation = saveDomain(annotation)


       def at = getAnnotationTermNotExist(annotation,true)
        at.term = term
        at.user = user
        saveDomain(at)
        annotation
    }

    static UserAnnotation getUserAnnotationNotExist(ImageInstance image, User user, Term term) {
        UserAnnotation annotation = new UserAnnotation(
                location: new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"),
                image:image,
                user: user,
                project:image.project
        )
        annotation = saveDomain(annotation)

       if(term) {
           def at = getAnnotationTermNotExist(annotation,true)
            at.term = term
            at.user = user
            saveDomain(at)
       }

        annotation
    }

    static ReviewedAnnotation getReviewedAnnotationNotExist(ImageInstance image, String polygon, User user, Term term) {
        def annotation = getUserAnnotationNotExist(image,polygon,user,term)

            def reviewedAnnotation = ReviewedAnnotation.findOrCreateWhere(
                    location: annotation.location,
                    image: annotation.image,
                    user: user,
                    project:annotation.project,
                    status : 0,
                    reviewUser: user
            )
        reviewedAnnotation.putParentAnnotation(annotation)
        reviewedAnnotation.addToTerms(term)
        saveDomain(reviewedAnnotation)
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

    static AttachedFile getAttachedFileNotExist(boolean save = false) {
        getAttachedFileNotExist("test/functional/be/cytomine/utils/simpleFile.txt",save)
    }

    static AttachedFile getAttachedFileNotExist(String file,boolean save = false) {
        def attachedFile = new AttachedFile()
        def project = getProjectNotExist(true)
        attachedFile.domainClassName = project.class.name
        attachedFile.domainIdent = project.id
        File f = new File(file)
        attachedFile.filename = f.name
        attachedFile.data = f.bytes
        save ? saveDomain(attachedFile) : checkDomain(attachedFile)
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
        def image = new AbstractImage(filename: getRandomString(), scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath", width: 1600, height: 1200)
        save ? saveDomain(image) : checkDomain(image)
    }

    static AbstractImage getAbstractImageNotExist(String filename, boolean save = false) {
        def image = new AbstractImage(filename: filename, scanner: getScanner(), sample: null, mime: getMime(), path: "pathpathpath", width: 1600, height: 1200)
        save ? saveDomain(image) : checkDomain(image)
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

    static UploadedFile getUploadedFile() {
        def uploadedFile = UploadedFile.findByFilename("BASICFILENAME")
        if (!uploadedFile) {
            uploadedFile = new UploadedFile(
                    user: getUser1(),
                    projects:[getProject().id],
                    storages: [getStorage().id],
                    filename: "BASICFILENAME",
                    originalFilename: "originalFilename",
                    convertedFilename:"originalFilenameConv",
                    ext: "tiff",
                    convertedExt: "tiff",
                    path: "path",
                    contentType: "tiff/ddd",
                    size: 1232l
            )
            saveDomain(uploadedFile)
        }
        uploadedFile
    }

    static UploadedFile getUploadedFileNotExist(boolean save = false) {
        UploadedFile uploadedFile = new UploadedFile(
                user: getUser1(),
                projects:[getProject().id],
                storages: [getStorage().id],
                filename: getRandomString(),
                originalFilename: "originalFilename",
                convertedFilename:"originalFilenameConv",
                ext: "tiff",
                convertedExt: "tiff",
                path: "path",
                contentType: "tiff/ddd",
                size: 1232l
        )
        save ? saveDomain(uploadedFile) : checkDomain(uploadedFile)
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

    static JobTemplate getJobTemplate() {
        def job = JobTemplate.findByProjectAndSoftwareAndName(getProject(),getSoftware(),"jobtemplate")
        if(!job) {
            job = new JobTemplate(project:getProject(),software:getSoftware(), name:"jobtemplate")
            saveDomain(job)
        }
        job
    }

    static JobTemplate getJobTemplateNotExist(boolean save = false) {
        JobTemplate job =  new JobTemplate(software:saveDomain(getSoftwareNotExist()), project : saveDomain(getProjectNotExist()), name:getRandomString())
        save ? saveDomain(job) : checkDomain(job)
    }

    static JobTemplateAnnotation getJobTemplateAnnotation() {
        def job = JobTemplateAnnotation.findByJobTemplateAndAnnotationIdent(getJobTemplate(),getRoiAnnotation().id)
        if(!job) {
            job = new JobTemplateAnnotation(jobTemplate: getJobTemplate())
            job.setAnnotation(getRoiAnnotation())
            saveDomain(job)
        }
        job
    }

    static JobTemplateAnnotation getJobTemplateAnnotationNotExist(boolean save = false) {
        RoiAnnotation annotation = saveDomain(getRoiAnnotation())
        JobTemplateAnnotation jobTemplateAnnotation =  new JobTemplateAnnotation(jobTemplate:saveDomain(getJobTemplate()))
        jobTemplateAnnotation.setAnnotation(getRoiAnnotation())
        save ? saveDomain(jobTemplateAnnotation) : checkDomain(jobTemplateAnnotation)
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
            project = new Project(name: name, ontology: getOntology(), discipline: getDiscipline())
            saveDomain(project)
            try {
                Infos.addUserRight(Infos.GOODLOGIN,project)
            } catch(Exception e) {}
        }
        project
    }

    static Project getProjectNotExist(Ontology ontology,boolean save = false) {
        Project project = new Project(name: getRandomString(), ontology: ontology, discipline: getDiscipline()  )
        if(save) {
            saveDomain(project)
            Infos.addUserRight(Infos.GOODLOGIN,project)
        } else{
            checkDomain(project)
        }
        return project
    }

    static Project getProjectNotExist(boolean save = false) {
        getProjectNotExist(saveDomain(getOntologyNotExist()),save)
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

    static Property getAnnotationProperty() {
        def annotation = getUserAnnotation()
        def annotationProperty = Property.findByDomainIdentAndKey(annotation.id,'MyKeyBasic')
        if (!annotationProperty) {
            annotationProperty = new Property(domain: annotation, key: 'MyKeyBasic', value:"MyValueBasic")
            saveDomain(annotationProperty)
        }
        annotationProperty
    }

    static Property getAnnotationPropertyNotExist(UserAnnotation annotation = getUserAnnotation(), boolean save = false) {
        def annotationProperty = new Property(domain: annotation, key: getRandomString(),value: "MyValueBasic")
        save? saveDomain(annotationProperty) : checkDomain(annotationProperty)
    }

    static Property getProjectProperty() {
        def project = getProject()
        def projectProperty = Property.findByDomainIdentAndKey(project.id,'MyKeyBasic')
        if (!projectProperty) {
            projectProperty = new Property(domain: project, key: 'MyKeyBasic', value:"MyValueBasic")
            saveDomain(projectProperty)
        }
        projectProperty
    }

    static Property getProjectPropertyNotExist(Project project = getProject(), boolean save = false) {
        def projectProperty = new Property(domain: project, key: getRandomString(),value: "MyValueBasic")
        save? saveDomain(projectProperty) : checkDomain(projectProperty)
    }

    static Property getImageInstanceProperty() {
        def imageInstance = getImageInstance()
        def imageInstanceProperty = Property.findByDomainIdentAndKey(imageInstance.id,'MyKeyBasic')
        if (!imageInstanceProperty) {
            imageInstanceProperty = new Property(domain: imageInstance, key: 'MyKeyBasic', value:"MyValueBasic")
            saveDomain(imageInstanceProperty)
        }
        imageInstanceProperty
    }

    static Property getImageInstancePropertyNotExist(ImageInstance imageInstance = getImageInstance(), boolean save = false) {
        def imageInstanceProperty = new Property(domain: imageInstance, key: getRandomString(),value: "MyValueBasic")
        save? saveDomain(imageInstanceProperty) : checkDomain(imageInstanceProperty)
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

    static User getGhest(String username, String password) {
        def user = SecUser.findByUsername(username)
        if (!user) {
            user = new User(username: username,firstname: "Basic",lastname: "User",email: "Basic@User.be",password: password,enabled: true)
            user.generateKeys()
            saveDomain(user)
            try {
               SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_GUEST"),true)
            } catch(Exception e) {
                log.warn(e)
            }
        }
        user
    }

    static User getUserNotExist(boolean save = false) {
       User user = new User(username: getRandomString(),firstname: "BasicNotExist",lastname: "UserNotExist",email: "BasicNotExist@User.be",password: "password",enabled: true)
        user.generateKeys()
        if(save) {
            saveDomain(user)
            SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_USER"),true)
        } else {
            checkDomain(user)
        }

        user
    }

    static User getGhestNotExist(boolean save = false) {
       User user = new User(username: getRandomString(),firstname: "BasicNotExist",lastname: "UserNotExist",email: "BasicNotExist@User.be",password: "password",enabled: true)
        user.generateKeys()
        if(save) {
            saveDomain(user)
            SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_GUEST"),true)
        } else {
            checkDomain(user)
        }

        user
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
            Infos.addUserRight(User.findByUsername(Infos.GOODLOGIN),storage)
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

    static Description getDescriptionNotExist(CytomineDomain domain,boolean save = false) {
        Description description = new Description(domainClassName: domain.class.name, domainIdent: domain.id, data: "A description for this domain!")
        save ? saveDomain(description) : checkDomain(description)
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


    static ImageSequence getImageSequence() {
        ImageSequence imageSequence = ImageSequence.findByImageGroup(getImageGroup())
        if(!imageSequence) {
            imageSequence = new ImageSequence(image:getImageInstanceNotExist(imageGroup.project,true),zStack:0,slice: 0, time:0,channel:0,imageGroup:imageGroup)
            imageSequence = saveDomain(imageSequence)
        }
        imageSequence
    }

    static ImageSequence getImageSequenceNotExist(boolean save = false) {
        def project = getProjectNotExist(true)
        def image = getImageInstanceNotExist(project,true)
        def group =  getImageGroupNotExist(project,true)
        ImageSequence seq = new ImageSequence(image:image,slice: 0, zStack:0,time:0,channel:2,imageGroup:group)
        save ? saveDomain(seq) : checkDomain(seq)
    }

    static ImageSequence getImageSequence(ImageInstance image,Integer channel, Integer zStack, Integer slice, Integer time,ImageGroup imageGroup,boolean save = false) {
        ImageSequence seq = new ImageSequence(image:image,zStack:zStack,time:time,channel:channel,slice:slice,imageGroup:imageGroup)
        save ? saveDomain(seq) : checkDomain(seq)
    }

    static ImageGroup getImageGroup() {
        ImageGroup imageGroup = ImageGroup.findByName("imagegroupname")
        if(!imageGroup) {
            imageGroup = new ImageGroup(project: project, name:"imagegroupname" )
            imageGroup = saveDomain(imageGroup)
        }
        imageGroup
    }

    static ImageGroup getImageGroupNotExist(Project project = getProject(), boolean save = false) {
        ImageGroup imageGroup = new ImageGroup(project: project)
        save ? saveDomain(imageGroup) : checkDomain(imageGroup)
    }

    static def getMultiDimensionalDataSet(def channel,def zStack,def slice,def time) {
        Project project = getProjectNotExist(true)
        ImageGroup group = getImageGroupNotExist(project,true)

        def data = []

        channel.eachWithIndex { c,ci ->
            zStack.eachWithIndex { z,zi ->
                slice.eachWithIndex { s,si ->
                    time.eachWithIndex { t,ti ->
                        String filename = c+"-"+z+"-"+s+"-"+t+"-"+System.currentTimeMillis()
                        def abstractImage = getAbstractImageNotExist(filename,true)
                        def imageInstance = getImageInstanceNotExist(project,true)
                        imageInstance.baseImage = abstractImage
                        saveDomain(imageInstance)

                        ImageSequence seq = getImageSequence(imageInstance,ci,zi,si,ti,group,true)
                        data << seq
                    }

                }
            }
        }
//        assert data.first().slice==0
//        assert data.first().zStack==0
//        assert data.first().time==0
//        assert data.first().channel==0
//        assert data.last().zStack==2
//        assert data.last().slice==2
//        assert data.last().time==2
//        assert data.last().channel==2
        return data
    }


    public static ImageInstance initImage() {

        String urlImageServer = "http://is31.cytomine.be"

        ImageServer imageServer = ImageServer.findByUrl(urlImageServer)
        if(!imageServer) {
            imageServer = new ImageServer()
            imageServer.className = "IIPResolver"
            imageServer.name = "IIP-Openslide2"
            imageServer.service = "/fcgi-bin/iipsrv.fcgi"
            imageServer.url =  urlImageServer
            imageServer.available = true
            BasicInstanceBuilder.saveDomain(imageServer)
        }

        Mime mime = Mime.findByExtension("tif")
        if(!mime) {
            mime = new Mime()
            mime.mimeType = "image/tiff"
            mime.extension = "tif"
            BasicInstanceBuilder.saveDomain(mime)
        }

        MimeImageServer mimeImageServer = MimeImageServer.findByMimeAndImageServer(mime,imageServer)
        if(!mimeImageServer) {
            mimeImageServer = new MimeImageServer()
            mimeImageServer.mime = mime
            mimeImageServer.imageServer = imageServer
            BasicInstanceBuilder.saveDomain(mimeImageServer)
        }

        Storage storage = Storage.findByName("lrollus test storage")
        if(!storage) {
            storage = new Storage()
            storage.basePath = "/data/test.cytomine.be/1"
            storage.name = "lrollus test storage"
            storage.ip = "10.3.1.136" // still used?
            storage.password = "toto"
            storage.port = 22
            storage.username = "username"
            storage.user = BasicInstanceBuilder.getUser1()
            BasicInstanceBuilder.saveDomain(storage)
        }

        ImageServerStorage imageServerStorage = ImageServerStorage.findByImageServerAndStorage(imageServer,storage)
        if(!imageServerStorage) {
            imageServerStorage = new ImageServerStorage()
            imageServerStorage.storage = storage
            imageServerStorage.imageServer = imageServer
            BasicInstanceBuilder.saveDomain(imageServerStorage)
        }

        AbstractImage abstractImage = AbstractImage.findByFilename("1383567901006/test.tif")
        if(!abstractImage) {
            abstractImage = new AbstractImage()
            abstractImage.filename = "1383567901006/test.tif"
            abstractImage.originalFilename = "test.tif"
            abstractImage.path = "1383567901006/test.tif"
            abstractImage.width = 25088
            abstractImage.height = 37888
            abstractImage.magnification = 8
            abstractImage.resolution = 0.65d
            abstractImage.mime = mime
            abstractImage.originalFilename = "test01.jpg"
            BasicInstanceBuilder.saveDomain(abstractImage)
        }

        StorageAbstractImage storageAbstractImage =  StorageAbstractImage.findByStorageAndAbstractImage(storage,abstractImage)
        if(!storageAbstractImage) {
            storageAbstractImage = new StorageAbstractImage()
            storageAbstractImage.abstractImage = abstractImage
            storageAbstractImage.storage = storage
            BasicInstanceBuilder.saveDomain(storageAbstractImage)
        }

        Project project = Project.findByName("testimage")
        if(!project) {
            project = BasicInstanceBuilder.getProjectNotExist(true)
            project.name = "testimage"
            BasicInstanceBuilder.saveDomain(project)
        }

        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(abstractImage,project)
        if(!imageInstance) {
            imageInstance = new ImageInstance()
            imageInstance.baseImage = abstractImage
            imageInstance.project = project
            imageInstance.user = BasicInstanceBuilder.getUser1()
            BasicInstanceBuilder.saveDomain(imageInstance)
        }


        ProcessingServer processingServer = ProcessingServer.findByUrl("http://image.cytomine.be")
        if(!processingServer) {
            processingServer = new  ProcessingServer()
            processingServer.url = "http://image.cytomine.be"
            BasicInstanceBuilder.saveDomain(processingServer)
        }

        ReviewedAnnotation.findAllByImage(imageInstance).each {
            it.delete(flush: true)
        }
        UserAnnotation.findAllByImage(imageInstance).each {
            AnnotationTerm.findAllByUserAnnotation(it).each { at ->
                at.delete(flush:true)
            }
            it.delete(flush: true)
        }

        return imageInstance

    }
}
