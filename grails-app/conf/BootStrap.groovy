import org.springframework.security.core.context.SecurityContextHolder as SCH

import be.cytomine.ViewPortToBuildXML
import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Instrument
import be.cytomine.processing.ImageFilter
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.project.Slide
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.social.UserPosition
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

import java.lang.management.ManagementFactory

import be.cytomine.data.*
import be.cytomine.image.server.*
import be.cytomine.ontology.*

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import be.cytomine.security.SecUser

class BootStrap {
    def springSecurityService
    def sequenceService
    def marshallersService
    def indexService
    def grailsApplication
    def storageService
    def messageSource
    def imagePropertiesService
    def countersService
    def triggerService
    def grantService
    def userGroupService
    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def JSONMinService



    static def development = "development"
    static def production = "production"
    static def test = "test"
    static def perf = "perf"


    def init = { servletContext ->

        //Register API Authentifier
        log.info "Current directory="+new File( 'test.html' ).absolutePath

        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.DIGEST_AUTH_FILTER.order + 1)
        log.info "###################" + grailsApplication.config.grails.serverURL + "##################"

        log.info "GrailsUtil.environment= " + GrailsUtil.environment + " BootStrap.development=" + BootStrap.development
        if (GrailsUtil.environment == BootStrap.development) { //scripts are not present in productions mode
            compileJS();
        }


        marshallersService.initMarshallers()
        sequenceService.initSequences()
        triggerService.initTrigger()
        indexService.initIndex()
        grantService.initGrant()

        grailsApplication.domainClasses.each {domainClass ->//iterate over the domainClasses
            if (domainClass.clazz.name.contains("be.cytomine")) {//only add it to the domains in my plugin

                domainClass.metaClass.retrieveErrors = {
                    def list = delegate?.errors?.allErrors?.collect {messageSource.getMessage(it, null)}
                    return list?.join('\n')
                }
            }
        }

        /* Print JVM infos like XMX/XMS */
        List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < inputArgs.size(); i++) {
            log.info inputArgs.get(i)
        }

        if (GrailsUtil.environment == BootStrap.test) { //scripts are not present in productions mode and dev mode
            initData(GrailsUtil.environment)
        }

        log.info "create scanner"
        createScanner()
        log.info "create user"
        createBasicUser()
        log.info "create discipline"
        createDiscipline()

        log.info "fill project for position (may take some time...)"
        UserPosition.findAllByProjectIsNull().each {
            it.project = it.image.project
            if(!it.updated) it.updated = it.created
            it.save()
        }


        //countersService.updateCounters()
        //updateImageProperties()
        //generateAbstractImageOriginalFilename()
        /*
        createProjectGrant()
        createProjectOwner()
        createAnnotationGrant()
        */
        //end of init
    }

    public void createBasicUser() {
        if (User.list().isEmpty()) {
            User user = new User()
            user.username = "admin"
            user.firstname = "Admin"
            user.lastname = "Admin"
            user.password = "test"
            user.email = "admin@cytominetest.be"
            user.enabled = true
            user.accountExpired = false
            user.accountLocked = false
            user.passwordExpired = false
            user.generateKeys()
            log.info "save user = " + user.save(flush: true)

            def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
            def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)

            SecUserSecRole.create(user, userRole)
            SecUserSecRole.create(user, adminRole)
        }


        if(Group.list().isEmpty()) {
            Group group = new Group()
            group.name = "admin"
            log.info "save admin = " + group.save(flush: true)
        }

    }

    public void createScanner() {
        if (!Instrument.findByBrand('gigascan2')) {
            Instrument scanner = new Instrument(brand: "gigascan2", model: "MODEL2")
            log.info "save scanner = " + scanner.save(flush: true)
        }
    }

    public void createDiscipline() {
        if (Discipline.list().isEmpty()) {
            Discipline cyto = new Discipline(name: "CYTOLOGY")
            log.info "validate cyto = " + cyto.validate()
            log.info "save cyto = " + cyto.save(flush: true)
            Discipline histo = new Discipline(name: "HISTOLOGY")
            log.info "save histo = " + histo.save(flush: true)
        }
    }

    private def compileJS() {

        log.info "========= C O M P I L E == J S ========= "
        ViewPortToBuildXML.process()
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        log.info "======================================== "
    }

    private def initData(String env) {

        createStorage(BootStrapData.storages)
        createImageFilters(BootStrapData.imageFiltersSamples)
        //createGroups(BootStrapData.groupsSamples)
        createUsers(BootStrapData.usersSamples)
        createScanners(BootStrapData.scannersSamples)
        createMimes(BootStrapData.mimeSamples)
        createImageServers(BootStrapData.imageServerSamples)
        createRetrievalServers(BootStrapData.retrievalServerSamples)

        /* Slides */
        if (env != BootStrap.test && env != BootStrap.perf) {
            createOntology(BootStrapData.ontologySamples)
            createProjects(BootStrapData.projectSamples)
            createSoftware(BootStrapData.softwareSamples)
            createDiscipline(BootStrapData.disciplineSamples)
            createSlidesAndAbstractImages(ImageData.ULBAnapathASP_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathFrottisEBUS_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathFrottisPAPA_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBACB_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBADQ_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBApapa_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathTPP_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDNEO13_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGTESTPHILIPS_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDNEO04_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDLBA_DATA)
            createSlidesAndAbstractImages(ImageData4.ULGBMGGZEBRACTL_DATA)
            createSlidesAndAbstractImages(ImageData4.BOTA)
        }

        if (env == BootStrap.production) {
            createSlidesAndAbstractImages(ImageData2.CELLSOLUTIONSBESTCYTECERVIX_DATA)
            createSlidesAndAbstractImages(ImageData5.CELLSOLUTIONSBESTCYTECERVIX_DATA)
        }

        if (env != BootStrap.test && env != BootStrap.perf) {
            createTerms(BootStrapData.termSamples)
            createRelation(BootStrapData.relationSamples)
            createRelationTerm(BootStrapData.relationTermSamples)
            createAnnotations(BootStrapData.annotationSamples)
        }

        if(env == BootStrap.test) {
            Project project = BasicInstance.createOrGetBasicProject()
            Infos.addUserRight(Infos.GOODLOGIN,project)
//            BootStrapData.class.getDeclaredFields().each {
//                print it.class
//            }
//            ImageData.class.getDeclaredFields().each {
//                print it.class
//            }
//            ImageData2.class.getDeclaredFields().each {
//                print it.class
//            }
//            ImageData3.class.getDeclaredFields().each {
//                print it.class
//            }
//            ImageData4.class.getDeclaredFields().each {
//                print it.class
//            }
        }

        //end of init
    }

    private def generateAbstractImageOriginalFilename () {
        AbstractImage.findAllByOriginalFilenameIsNull().each { image ->
            String filename = image.getFilename()
            filename = filename.replace(".vips.tiff", "")
            filename = filename.replace(".vips.tif", "")
            if (filename.lastIndexOf("/") != -1 && filename.lastIndexOf("/") != filename.size())
                filename = filename.substring(filename.lastIndexOf("/")+1, filename.size())

            log.info image.getFilename() + " => " + filename
            image.originalFilename = filename
            image.save(flush : true)
        }
    }

    private def createProjectGrant() {
        //Remove admin ritht for non-giga user
        log.info "createProjectGrant..."
        List<User> usersList = User.list()
        usersList.each { user ->
            if (!user.username.equals("lrollus") && !user.username.equals("stevben") && !user.username.equals("rmaree")) {
                SecRole admin = SecRole.findByAuthority("ROLE_ADMIN")
                SecUserSecRole.remove(user, admin, true)
            }

        }
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        List<Project> projects = Project.list()
        projects.each { project ->
            log.info "createProjectGrant for project $project.name..."
            def objectACL = AclObjectIdentity.findByObjectId(project.id)

            if (!objectACL) {
                try {
                    //Create object security id for each project
                    aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(project))
                    //For each project, create ADMIN grant for each user

                    List<User> users = []
                    ProjectGroup.findAllByProject(project).each {
                        Group group = it.group
                        group.users().each { user ->
                            if(!users.contains(user))
                                users.add(user)
                        }

                    }
                    users.each { user ->
                        log.info "add user $user.username..."
                        aclUtilService.addPermission(project, user.username, ADMINISTRATION)
                    }
                } catch (Exception e) { e.printStackTrace()}
            }
        }
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    private def createProjectOwner() {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        changeOwner("BOTANIQUE-LEAVES","aempain")
        changeOwner("ROSTOCK-HJTHIESEN-KIDNEY","rmaree")
        changeOwner("ULG-LBTD-LBA","dcataldo")
        changeOwner("ULB-ANAPATH-TPP-CB","isalmon")
        changeOwner("ULB-ANAPATH-FROTTIS-EBUS","isalmon")
        changeOwner("ULB-ANAPATH-PCT-CB","isalmon")
        changeOwner("ULB-ANAPATH-ASP-CB","isalmon")
        changeOwner("ULB-ANAPATH-LBA-DQ","isalmon")
        changeOwner("ULG-BMGG-ZEBRA_CTL","stern")
        changeOwner("ULG-LBTD-NEO13","dcataldo")
        changeOwner("ULB-ANAPATH-FROTTIS-PAPA","isalmon")
        changeOwner("ULB-ANAPATH-ASP","isalmon")
        changeOwner("ULG-LBTD-NEO04","dcataldo")
        changeOwner("XCELLSOLUTIONS-BESTCYTE-CERVIX","rmaree")
        changeOwner("ULB-ANAPATH-LBA-PAPA","isalmon")
        changeOwner("ULB-ANAPATH-LBA-CB","isalmon")
        changeOwner("ZEBRA_CTL","stern")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    private void changeOwner(String projectName, String username) {
        Project project = Project.findByName(projectName)
        if(project) {
            log.info "Project " + project.name + " id=" + project.id  +" will be owned by " + username
            aclUtilService.changeOwner project, username
        } else {
            log.info "Project not found " + projectName
        }
//        AclObjectIdentity acl = AclObjectIdentity.findByObjectId(project.id)
        //        acl.owner = AclSid.findBySid(username)
        //        acl.save(flush:true)

    }







    private def updateImageProperties() {
        def c = new ImportController()
        c.imageproperties()
    }

    /* Methods */

    def createImageFilters(imageFilters) {
        imageFilters.each { item ->
            if (ImageFilter.findByName(item.name) != null) return
            ImageFilter imageFilter = new ImageFilter(name: item.name, baseUrl: item.baseUrl)
            if (imageFilter.validate()) {
                imageFilter.save();
            } else {
                log.info("\n\n\n Errors in creating imageFilter for ${it.name}!\n\n\n")
                imageFilter.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createStorage(storages) {
        log.info "createStorages"
        storages.each {
            if (Storage.findByName(it.name)) {
                def storage = Storage.findByName(it.name)
                storage.basePath = it.basePath
                storage.serviceUrl = it.serviceUrl
                storage.username = it.username
                storage.password = it.password
                storage.ip = it.ip
                storage.port = it.port
                storage.save()
            }
            else {
                def storage = new Storage(name: it.name, basePath: it.basePath, serviceUrl: it.serviceUrl, username: it.username, password: it.password, ip: it.ip, port: it.port)
                if (storage.validate()) {
                    storage.save();
                } else {
                    log.info("\n\n\n Errors in creating storage for ${it.name}!\n\n\n")
                    storage.errors.each {
                        err -> log.info err
                    }
                }
            }
        }
    }

    def createSlidesAndAbstractImages(abstractImages) {

        //Storage storage = Storage.findByName("cytomine")
        User user = User.findByUsername("rmaree")
        abstractImages.each { item ->
            if (!item.name) {
                item.name = new File(item.filename).getName()
            }
            if (AbstractImage.findByFilename(item.name)) return

            def slide
            if (item.slidename != null)
                slide = Slide.findByName(item.slidename)

            if (!slide) {
                String slideName;
                if (item.slidename == null) {
                    slideName = "SLIDE " + item.name
                }
                else {
                    slideName = item.slidename
                }

                //create one with slidename name
                slide = new Slide(name: slideName, order: item.order ?: 1)

                if (slide.validate()) {

                    slide.save(flush: true)
                }
            }
            def extension = item.extension ?: "jp2"

            def mime = Mime.findByExtension(extension)

            def scanner = Instrument.findByBrand("gigascan")

            Long lo = new Long("1309250380");
            Long hi = new Date().getTime()
            Random random = new Random()
            Long randomInt = (Math.abs(random.nextLong()) % (hi.longValue() - lo.longValue() + 1)) + lo.longValue();
            Date created = new Date(randomInt);


            AbstractImage image = new AbstractImage(
                    filename: item.name,
                    scanner: scanner,
                    slide: slide,
                    width: item.width,
                    height: item.height,
                    magnification: item.magnification,
                    resolution: item.resolution,
                    path: item.filename,
                    mime: mime,
                    created: created
            )

            if (image.validate()) {


                Project project = Project.findByName(item.study)
                //assert(project != null)
                image.save(flush: true)
                //AbstractImageGroup.link(image,giga)

                if (project != null) {
                    project.groups().each { group ->
                        log.info "GROUP " + group.name + " IMAGE " + image.filename
                        AbstractImageGroup.link(image, group)
                    }

                    /*Storage.list().each { storage->
                        storageService.metadata(storage, image)
                    }*/


                    ImageInstance imageinstance = new ImageInstance(
                            baseImage: image,
                            user: user,
                            project: project,
                            slide: image.slide
                    )
                    if (imageinstance.validate()) {
                        imageinstance.save(flush: true)
                    } else {
                        imageinstance.errors.each { log.info it }
                    }

                } else { //link with stevben by default
                    Group group = Group.findByName("stevben")
                    AbstractImageGroup.link(image, group)
                }


                Storage.list().each {
                    StorageAbstractImage.link(it, image)
                }
                //StorageAbstractImage.link(storage, image)

            } else {
                log.info("\n\n\n Errors in image boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> log.info err
                }

            }
        }
    }


    def createGroups(groupsSamples) {
        groupsSamples.each { item ->
            if (Group.findByName(item.name)) return
            def group = new Group(name: item.name)
            if (group.validate()) {
                log.info "Creating group ${group.name}..."
                group.save(flush: true)
                log.info "Creating group ${group.name}... OK"
            }
            else {
                log.info("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> log.info err
                }
            }
        }
    }


    def createUsers(usersSamples) {
        SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)
        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)
            user.generateKeys()

            println "# user="+user.username + " " + user.id
            SecUser.list().each {
                println "### user="+it.username + " " + it.publicKey+ " " + user.privateKey
            }


            log.info "Before validating ${user.username}..."
            if (user.validate()) {
                log.info "Creating user ${user.username}..."
                // user.addToTransactions(new Transaction())
                //user.encodePassword()

                println "# user="+user.username + " " + user.id
                SecUser.list().each {
                    println "### user="+it.username + " " + it.publicKey+ " " + user.privateKey
                }
                try {user.save(flush: true) } catch(Exception e) {println e}


                SecUser.list().each {
                    println "###TheEnd user="+it.username + " " + it.publicKey+ " " + user.privateKey
                }


                log.info "Save ${user.username}..."

                /* Create a special group the user */
                def userGroupName = item.username
                def userGroup = [
                        [name: userGroupName]
                ]
                createGroups(userGroup)
                Group group = Group.findByName(userGroupName)
                userGroupService.link(user, group)

                /* Handle groups */
                item.group.each { elem ->
                    def newGroup = [
                            [name: elem.name]
                    ]
                    createGroups(newGroup)
                    log.info "Fetch group " + elem.name
                    group = Group.findByName(elem.name)
                    userGroupService.link(user, group)
                }

                /* Add Roles */
                item.roles.each { authority ->
                    log.info "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createMimes(mimeSamples) {
        mimeSamples.each { item ->
            if (Mime.findByExtension(item.extension)) return
            Mime mime = new Mime(extension: item.extension,
                    mimeType: item.mimeType)
            if (mime.validate()) {
                log.info "Creating mime ${mime.extension} : ${mime.mimeType}..."
                mime.save(flush: true)
            } else {
                log.info("\n\n\n Errors in account boostrap for ${mime.extension} : ${mime.mimeType}!\n\n\n")
                mime.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createRetrievalServers(retrievalServerSamples) {
        retrievalServerSamples.each { item ->
            if (RetrievalServer.findByDescription(item.description)) return
            RetrievalServer retrievalServer = new RetrievalServer(url: item.url, description: item.description)
            if (retrievalServer.validate()) {
                log.info "Creating retrieval server ${item.description}... "
                retrievalServer.save(flush: true)

            } else {
                log.info("\n\n\n Errors in retrieval server boostrap for ${item.description} !\n\n\n")
                item.errors.each {
                    err -> log.info err
                }
            }
        }
    }


    def createScanners(scannersSamples) {
         scannersSamples.each { item ->
             if (Instrument.findByBrandAndModel(item.brand, item.model)) return
             Instrument scanner = new Instrument(brand: item.brand, model: item.model)
             if (scanner.validate()) {
                 log.info "Creating scanner ${scanner.brand} - ${scanner.model}..."
                 scanner.save(flush: true)
             } else {
                 log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                 scanner.errors.each {
                     err -> log.info err
                 }
             }
         }
     }

    def createImageServers(imageServerSamples) {
        imageServerSamples.each { item ->
            def imageServer = ImageServer.findByName(item.name)
            if (imageServer) { //exist => update
                /*imageServer.url = item.url
                imageServer.service = item.service
                imageServer.className = item.className
                imageServer.storage = Storage.findByName(item.storage)
                imageServer.save()
                item.extension.each { ext->
                    Mime mime = Mime.findByExtension(ext)
                    if (!MimeImageServer.findByImageServerAndMime(imageServer, mime)){
                        MimeImageServer.link(imageServer, mime)
                    }
                }
                return*/
            } else {
                imageServer = new ImageServer(
                        name: item.name,
                        url: item.url,
                        service: item.service,
                        className: item.className,
                        storage: Storage.findByName(item.storage),
                        available: true)

                if (imageServer.validate()) {
                    log.info "Creating image server ${imageServer.name}... : ${imageServer.url}"
                    imageServer.save(flush: true)

                    /* Link with MIME Types */
                    item.extension.each { ext ->
                        Mime mime = Mime.findByExtension(ext)
                        MimeImageServer.link(imageServer, mime)
                    }
                } else {
                    log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                    imageServer.errors.each {
                        err -> log.info err
                    }
                }
            }
        }
    }

    def createProjects(projectSamples) {

        //Comment: DON'T ADD AGAIN A PROJECT A USER RENAME IT
        //=> What should we do with bootsrap data?

        //        projectSamples.each { item->
        //            if(Project.findByNameIlike(item.name)) return
        //            def ontology = Ontology.findByName(item.ontology)
        //            def project = new Project(
        //                    name : item.name.toString().toUpperCase(),
        //                    ontology : ontology,
        //                    created : new Date(),
        //                    updated : item.updated,
        //                    deleted : item.deleted
        //            )
        //            if (project.validate()){
        //                log.info "Creating project  ${project.name}..."
        //
        //                project.save(flush : true)
        //
        //                /* Handle groups */
        //                item.groups.each { elem ->
        //                    Group group = Group.findByName(elem.name)
        //                    ProjectGroup.link(project, group)
        //                }
        //
        //
        //
        //            } else {
        //                log.info("\n\n\n Errors in project boostrap for ${item.name}!\n\n\n")
        //                project.errors.each {
        //                    err -> log.info err
        //                }
        //            }
        //        }
    }

    def createSlides(slideSamples) {
        slideSamples.each {item ->
            if (Slide.findByName(item.name)) return
            def slide = new Slide(name: item.name, order: item.order)

            if (slide.validate()) {
                log.info "Creating slide  ${item.name}..."

                slide.save(flush: true)

                /* Link to projects */
                /*item.projects.each { elem ->
                    Project project = Project.findByName(elem.name)
                    ProjectSlide.link(project, slide)
                }*/


            } else {
                log.info("\n\n\n Errors in slide boostrap for ${item.name}!\n\n\n")
                slide.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createScans(scanSamples, slides) {
        scanSamples.each { item ->
            if (AbstractImage.findByPath(item.path)) return
            def extension = item.extension ?: "jp2"
            def mime = Mime.findByExtension(extension)
            Random random = new Random()
            Long randomInt = random.nextLong()
            Date created = new Date(randomInt);

            //  String path
            //Mime mime
            def image = new AbstractImage(
                    filename: item.filename,
                    path: item.path,
                    mime: mime,
                    scanner: null,
                    slide: slides[item.slide],
                    created: created
            )

            if (image.validate()) {
                log.info "Creating image : ${image.filename}..."

                image.save(flush: true)
/*
            *//* Link to projects *//*
            item.annotations.each { elem ->
              Annotation annotation = Annotation.findByName(elem.name)
              log.info 'ScanAnnotation:' + image.filename + " " + annotation.name
              ScanAnnotation.link(image, annotation)
              log.info 'ScanAnnotation: OK'
            }*/
            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createAnnotations(annotationSamples) {

        if (!UserAnnotation.list().isEmpty()) return
        UserAnnotation userAnnotation = null
        GeometryFactory geometryFactory = new GeometryFactory()
        annotationSamples.each { item ->

            /* Read spatial data an create annotation*/
            def geom
            if (item.location[0].startsWith('POINT')) {
                //point
                geom = new WKTReader().read(item.location[0]);
            }
            else {
                //multipolygon
                Polygon[] polygons = new Polygon[(item.location).size()];
                int i = 0
                (item.location).each {itemPoly ->
                    polygons[i] = new WKTReader().read(itemPoly);
                    i++;
                }
                geom = geometryFactory.createMultiPolygon(polygons)
            }
            def scanParent = AbstractImage.findByFilename(item.scan.filename)
            def imageParent = ImageInstance.findByBaseImage(scanParent)


            def user = User.findByUsername(item.user)
            log.info "user " + item.user + "=" + user.username

            userAnnotation = new UserAnnotation(location: geom, image: imageParent, user: user)

            /* Save annotation */
            if (userAnnotation.validate()) {
                log.info "Creating userannotation : ${userAnnotation.name}..."

                userAnnotation.save(flush: true)

                item.term.each {  term ->
                    log.info "add Term " + term
                    //annotation.addToTerm(Term.findByName(term))
                    AnnotationTerm.link(userAnnotation, Term.findByName(term))
                }


            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                userAnnotation.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    def createOntology(ontologySamples) {
        ontologySamples.each { item ->
            if (Ontology.findByName(item.name)) return
            User user = User.findByUsername(item.user)
            def ontology = new Ontology(name: item.name, user: user)
            log.info "create ontology=" + ontology.name

            if (ontology.validate()) {
                log.info "Creating ontology : ${ontology.name}..."
                ontology.save(flush: true)
            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                ontology.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    def createTerms(termSamples) {
        log.info "createTerms"
        termSamples.each { item ->
            if (Term.findByNameAndOntology(item.name, Ontology.findByName(item.ontology.name))) return
            def term = new Term(name: item.name, comment: item.comment, ontology: Ontology.findByName(item.ontology.name), color: item.color)
            log.info "create term=" + term.name

            if (term.validate()) {
                log.info "Creating term : ${term.name}..."
                term.save(flush: true)

                /*  item.ontology.each {  ontology ->
                  log.info "add Ontology " + ontology.name
                  //annotation.addToTerm(Term.findByName(term))
                  TermOntology.link(term, Ontology.findByName(ontology.name),ontology.color)
                }*/

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                term.errors.each {
                    err -> log.info err
                }

            }
        }
    }


    def createRelation(relationsSamples) {
        log.info "createRelation"
        relationsSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            log.info "create relation=" + relation.name

            if (relation.validate()) {
                log.info "Creating relation : ${relation.name}..."
                relation.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    def createRelationTerm(relationTermSamples) {
        relationTermSamples.each {item ->
            def ontology = Ontology.findByName(item.ontology);
            def relation = Relation.findByName(item.relation)
            def term1 = Term.findByNameAndOntology(item.term1, ontology)
            def term2 = Term.findByNameAndOntology(item.term2, ontology)

            if (!RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)) {
                log.info "Creating term/relation  ${relation.name}:${item.term1}/${item.term2}..."
                RelationTerm.link(relation, term1, term2)
            }

        }
    }

    def createSoftware(softwareSamples) {
        log.info "createRelation"
        softwareSamples.each { item ->
            if (Software.findByName(item.name)) return
            Software software = new Software(name: item.name, serviceName: "wrongService")
            log.info "create software=" + software.name

            if (software.validate()) {
                log.info "Creating software : ${software.name}..."
                software.save(flush: true)

                if (Job.findAllBySoftware(software).isEmpty()) {
                    Job job = new Job(user: User.findByUsername("lrollus"), software: software)
                    job.save(flush: true)
                }

            } else {
                log.info("\n\n\n Errors in account boostrap for ${software.name}!\n\n\n")
                software.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    static def disciplineSamples = [
            [name: "IMMUNOHISTOCHEMISTRY"],
            [name: "CYTOLOGY"],
            [name: "HISTOLOGY"]
    ]

    def createDiscipline(disciplineSamples) {
        log.info "createDiscipline"
        disciplineSamples.each { item ->
            if (Discipline.findByName(item.name)) return
            Discipline discipline = new Discipline(name: item.name)
            log.info "create discipline=" + discipline.name

            if (discipline.validate()) {
                log.info "Creating discipline : ${discipline.name}..."
                discipline.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${discipline.name}!\n\n\n")
                discipline.errors.each {
                    err -> log.info err
                }

            }
        }

        mapProjectDiscipline("ROSTOCK-HJTHIESEN-KIDNEY", "IMMUNOHISTOCHEMISTRY")

        mapProjectDiscipline("ULB-ANAPATH-ASP", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-FROTTIS-EBUS", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-FROTTIS-PAPA", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-CB", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-DQ", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-PAPA", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-TPP", "CYTOLOGY")

        mapProjectDiscipline("ULG-LBTD-LBA", "CYTOLOGY")
        mapProjectDiscipline("ULG-LBTD-NEO04", "HISTOLOGY")
        mapProjectDiscipline("ULG-LBTD-NEO13", "HISTOLOGY")

        mapProjectDiscipline("XCELLSOLUTIONS-BESTCYTE-CERVIX", "CYTOLOGY")


    }

    void mapProjectDiscipline(String projectName, String disciplineName) {
        Project project = Project.findByNameIlike(projectName)
        if (!project || project.discipline) return
        project.setDiscipline(Discipline.findByNameIlike(disciplineName))
        project.save(flush: true)
    }


}
