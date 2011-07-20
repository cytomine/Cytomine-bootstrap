import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.security.Group
import be.cytomine.security.UserGroup
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.project.Slide
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Polygon
import be.cytomine.image.server.RetrievalServer
import java.lang.management.ManagementFactory
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import grails.util.GrailsUtil
import be.cytomine.image.Mime
import be.cytomine.ontology.Term
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.image.AbstractImageGroup
import org.perf4j.StopWatch
import org.perf4j.LoggingStopWatch
import be.cytomine.data.BootStrapData
import be.cytomine.data.BootStrapData2
import be.cytomine.data.BootStrapData3
import be.cytomine.data.RestImportDataController

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

    static def development = "development"
    static def production = "production"
    static def test = "test"


    def init = { servletContext ->

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
                    def list = delegate?.errors?.allErrors?.collect{messageSource.getMessage(it,null)}
                    return list?.join('\n')
                }
            }
        }

        /* Print JVM infos like XMX/XMS */
        List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for(int i =0;i<inputArgs.size();i++) {
            println inputArgs.get(i)
        }

        StopWatch stopWatch = new LoggingStopWatch();
        initData(GrailsUtil.environment)
        countersService.updateCounters()
        updateImageProperties()
        stopWatch.stop("initData");
        //end of init
    }

    private def compileJS() {
        println "========= C O M P I L E == J S ========= "
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> println line }
        println "======================================== "
    }

    private def initData(String env) {


        createStorage(BootStrapData.storages)
        createGroups(BootStrapData.groupsSamples)
        createUsers(BootStrapData.usersSamples)
        createScanners(BootStrapData.scannersSamples)
        createMimes(BootStrapData.mimeSamples)
        createImageServers(BootStrapData.imageServerSamples)
        createRetrievalServers(BootStrapData.retrievalServerSamples)
        createOntology(BootStrapData.ontologySamples)
        createProjects(BootStrapData.projectSamples)

        /* Slides */
        if (env != BootStrap.test) {
            createSlidesAndAbstractImages(BootStrapData.ULGLBTDCells)
            createSlidesAndAbstractImages(BootStrapData2.CERVIXScans1)
            createSlidesAndAbstractImages(BootStrapData.PhillipsScans)
            createSlidesAndAbstractImages(BootStrapData.LBTDScans1)
            createSlidesAndAbstractImages(BootStrapData.LBTDScans4)
            createSlidesAndAbstractImages(BootStrapData.ZEBRA_CTL_Scans)
            createSlidesAndAbstractImages(BootStrapData3.ANAPATHScans)
        }

        if (env == BootStrap.production) {
            createSlidesAndAbstractImages(BootStrapData2.CERVIXScans2)
            createSlidesAndAbstractImages(BootStrapData2.CERVIXScans3)
            createSlidesAndAbstractImages(BootStrapData2.CERVIXScans4)
            createSlidesAndAbstractImages(BootStrapData2.CERVIXScans5)
            createSlidesAndAbstractImages(BootStrapData.LBTDScans2)
            createSlidesAndAbstractImages(BootStrapData.LBTDScans3)
        }

        if (env != BootStrap.test) {
            createTerms(BootStrapData.termSamples)
            createRelation(BootStrapData.relationSamples)
            createRelationTerm(BootStrapData.relationTermSamples)
            createAnnotations(BootStrapData.annotationSamples)
        }

        def destroy = {
        }
        //end of init
    }

    private def updateImageProperties() {
        def c = new  RestImportDataController()
        c.imageproperties()
    }

    /* Methods */

    def createStorage(storages) {
        println "createStorages"
        storages.each {
            if(Storage.findByName(it.name)) return

            def storage = new Storage(name : it.name, basePath : it.basePath, serviceUrl : it.serviceUrl)
            if (storage.validate()) {
                storage.save();
            } else {
                println("\n\n\n Errors in creating storage for ${it.name}!\n\n\n")
                storage.errors.each {
                    err -> println err
                }
            }

        }
    }

    def createSlidesAndAbstractImages(LBTDScans) {

        StopWatch stopWatch = new LoggingStopWatch();
        //Storage storage = Storage.findByName("cytomine")
        Group giga = Group.findByName('GIGA')
        User user = User.findByUsername("rmaree")
        LBTDScans.each { item ->
            if (!item.name) {
                item.name = new File(item.filename).getName()
            }
            if(AbstractImage.findByFilename(item.name)) return

            def slide
            if(item.slidename!=null)
                slide = Slide.findByName(item.slidename)

            if(!slide)
            {
                String slideName;
                if(item.slidename==null)
                {
                    slideName = "SLIDE "  + item.name
                }
                else
                {
                    slideName = item.slidename
                }

                //create one with slidename name
                slide = new Slide(name : slideName, order : item.order?:1)

                if (slide.validate()) {

                    slide.save(flush : true)
                }
            }
            def extension = item.extension ?: "jp2"

            def mime = Mime.findByExtension(extension)

            def scanner = Scanner.findByBrand("gigascan")

            Long lo =  new Long("1309250380");
            Long hi =  new Date().getTime()
            Random random = new Random()
            Long randomInt = ( Math.abs( random.nextLong() ) % ( hi.longValue() - lo.longValue() + 1 ) ) + lo.longValue();
            Date created = new Date(randomInt);


            AbstractImage image = new AbstractImage(
                    filename: item.name,
                    scanner : scanner,
                    slide : slide,
                    path : item.filename,
                    mime : mime,
                    created : created
            )

            if (image.validate()) {


                Project project = Project.findByName(item.study)
                assert(project != null)
                image.save(flush : true)
                //AbstractImageGroup.link(image,giga)


                project.groups().each { group ->
                    println "GROUP " + group.name + " IMAGE " + image.filename
                    AbstractImageGroup.link(image,group)
                }

                /*Storage.list().each { storage->
                    storageService.metadata(storage, image)
                }*/


                ImageInstance imageinstance = new ImageInstance(
                        baseImage : image,
                        user : user,
                        project : project,
                        slide : image.slide
                )
                if (imageinstance.validate()) {
                    imageinstance.save(flush:true)
                } else {
                    imageinstance.errors.each { println it }
                }


                Storage.list().each {
                    StorageAbstractImage.link(it, image)
                }
                //StorageAbstractImage.link(storage, image)

            } else {
                println("\n\n\n Errors in image boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> println err
                }

            }
        }
    }


    def createGroups(groupsSamples) {
        groupsSamples.each { item->
            if(Group.findByName(item.name)) return
            def group = new Group(name : item.name)
            if (group.validate()) {
                println "Creating group ${group.name}..."
                group.save(flush : true)
            }
            else {
                println("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> println err
                }
            }
        }
    }


    def createUsers(usersSamples) {
        def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority : "ROLE_USER").save(flush : true)
        def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority : "ROLE_ADMIN").save(flush : true)
        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (!user) {
                user = new User(
                        username : item.username,
                        firstname : item.firstname,
                        lastname : item.lastname,
                        email : item.email,
                        color : item.color,
                        password : springSecurityService.encodePassword(item.password),
                        enabled : true)
            } else {
                user.username = item.username;
                user.firstname = item.firstname;
                user.lastname = item.lastname;
                user.email = item.email;
                user.color = item.color;
                user.password = springSecurityService.encodePassword(item.password);
            }
            if (user.validate()) {
                println "Creating user ${user.username}..."
                // user.addToTransactions(new Transaction())
                user.save(flush : true)

                /* Create a special group the user */
                def userGroupName = item.username
                def userGroup = [
                        [name : userGroupName]
                ]
                createGroups(userGroup)
                Group group = Group.findByName(userGroupName)
                UserGroup.link(user, group)

                /* Handle groups */
                item.group.each { elem ->
                    group = Group.findByName(elem.name)
                    UserGroup.link(user, group)
                }

                /* Add Roles */
                SecUserSecRole.create(user, userRole)
                SecUserSecRole.create(user, adminRole)

            } else {
                println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createScanners(scannersSamples) {
        scannersSamples.each { item ->
            if(Scanner.findByBrandAndModel(item.brand, item.model)) return
            Scanner scanner = new Scanner(brand : item.brand, model : item.model)

            if (scanner.validate()) {
                println "Creating scanner ${scanner.brand} - ${scanner.model}..."
                scanner.save(flush : true)
            } else {
                println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                scanner.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createMimes(mimeSamples) {
        mimeSamples.each { item ->
            if(Mime.findByExtension(item.extension)) return
            Mime mime = new Mime(extension : item.extension,
                    mimeType : item.mimeType)
            if (mime.validate()) {
                println "Creating mime ${mime.extension} : ${mime.mimeType}..."
                mime.save(flush : true)
            } else {
                println("\n\n\n Errors in account boostrap for ${mime.extension} : ${mime.mimeType}!\n\n\n")
                mime.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createRetrievalServers(retrievalServerSamples) {
        retrievalServerSamples.each { item->
            if(RetrievalServer.findByUrl(item.url)) return
            RetrievalServer retrievalServer = new RetrievalServer( url : item.url, port : item.port, description : item.description)
            if (retrievalServer.validate()) {
                println "Creating retrieval server ${item.description}... "
                retrievalServer.save(flush:true)

            } else {
                println("\n\n\n Errors in retrieval server boostrap for ${item.description} !\n\n\n")
                item.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createImageServers(imageServerSamples) {
        imageServerSamples.each { item ->
            if(ImageServer.findByUrl(item.url)) return
            ImageServer imageServer = new ImageServer(
                    name : item.name,
                    url : item.url,
                    service : item.service,
                    className : item.className,
                    storage : Storage.findByName(item.storage))

            if (imageServer.validate()) {
                println "Creating image server ${imageServer.name}... : ${imageServer.url}"

                imageServer.save(flush : true)

                /* Link with MIME Types */
                item.extension.each { ext->
                    Mime mime = Mime.findByExtension(ext)
                    MimeImageServer.link(imageServer, mime)
                }
            } else {
                println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                imageServer.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createProjects(projectSamples) {
        projectSamples.each { item->
            if(Project.findByName(item.name)) return
            def ontology = Ontology.findByName(item.ontology)
            def project = new Project(
                    name : item.name,
                    ontology : ontology,
                    created : new Date(),
                    updated : item.updated,
                    deleted : item.deleted
            )
            if (project.validate()){
                println "Creating project  ${project.name}..."

                project.save(flush : true)

                /* Handle groups */
                item.groups.each { elem ->
                    Group group = Group.findByName(elem.name)
                    ProjectGroup.link(project, group)
                }



            } else {
                println("\n\n\n Errors in project boostrap for ${item.name}!\n\n\n")
                project.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createSlides(slideSamples) {
        slideSamples.each {item->
            if(Slide.findByName( item.name)) return
            def slide = new Slide(name : item.name, order : item.order)

            if (slide.validate()) {
                println "Creating slide  ${item.name}..."

                slide.save(flush : true)

                /* Link to projects */
                /*item.projects.each { elem ->
                    Project project = Project.findByName(elem.name)
                    ProjectSlide.link(project, slide)
                }*/


            } else {
                println("\n\n\n Errors in slide boostrap for ${item.name}!\n\n\n")
                slide.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createScans(scanSamples, slides) {
        scanSamples.each { item ->
            if (AbstractImage.findByPath(item.path)) return
            def extension = item.extension ?: "jp2"
            def mime = Mime.findByExtension(extension)

            def scanner = Scanner.findByBrand("gigascan")
            def user = User.findByUsername("lrollus")


            Random random = new Random()
            Long randomInt = random.nextLong()
            Date created = new Date(randomInt);

            //  String path
            //Mime mime
            def image = new AbstractImage(
                    filename: item.filename,
                    path : item.path,
                    mime : mime,
                    scanner : scanner,
                    slide : slides[item.slide],
                    created :created
            )

            if (image.validate()) {
                println "Creating image : ${image.filename}..."

                image.save(flush : true)
/*
            *//* Link to projects *//*
            item.annotations.each { elem ->
              Annotation annotation = Annotation.findByName(elem.name)
              println 'ScanAnnotation:' + image.filename + " " + annotation.name
              ScanAnnotation.link(image, annotation)
              println 'ScanAnnotation: OK'
            }*/
            } else {
                println("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createAnnotations(annotationSamples) {


        def annotation = null
        GeometryFactory geometryFactory = new GeometryFactory()
        annotationSamples.each { item ->
            if(Annotation.findByName(item.name)) return
            /* Read spatial data an create annotation*/
            def geom
            if(item.location[0].startsWith('POINT'))
            {
                //point
                geom = new WKTReader().read(item.location[0]);
            }
            else
            {
                //multipolygon
                Polygon[] polygons = new Polygon[(item.location).size()];
                int i=0
                (item.location).each {itemPoly ->
                    polygons[i] =  new WKTReader().read(itemPoly);
                    i++;
                }
                geom = geometryFactory.createMultiPolygon(polygons)
            }
            def scanParent = AbstractImage.findByFilename(item.scan.filename)
            def imageParent = ImageInstance.findByBaseImage(scanParent)


            def user = User.findByUsername(item.user)
            println "user " + item.user +"=" + user.username

            annotation = new Annotation(name: item.name, location:geom, image:imageParent,user:user)


            /* Save annotation */
            if (annotation.validate()) {
                println "Creating annotation : ${annotation.name}..."

                annotation.save(flush : true)

                item.term.each {  term ->
                    println "add Term " + term
                    //annotation.addToTerm(Term.findByName(term))
                    AnnotationTerm.link(annotation, Term.findByName(term))
                }


            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                annotation.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createOntology(ontologySamples) {
        ontologySamples.each { item ->
            if(Ontology.findByName(item.name)) return
            User user = User.findByUsername(item.user)
            def ontology = new Ontology(name:item.name,user:user)
            println "create ontology="+ ontology.name

            if(ontology.validate()) {
                println "Creating ontology : ${ontology.name}..."
                ontology.save(flush : true)
            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                ontology.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createTerms(termSamples) {
        println "createTerms"
        termSamples.each { item ->
            if(Term.findByNameAndOntology(item.name,Ontology.findByName(item.ontology.name))) return
            def term = new Term(name:item.name,comment:item.comment,ontology:Ontology.findByName(item.ontology.name),color:item.color)
            println "create term="+ term.name

            if(term.validate()) {
                println "Creating term : ${term.name}..."
                term.save(flush : true)


                /*  item.ontology.each {  ontology ->
                  println "add Ontology " + ontology.name
                  //annotation.addToTerm(Term.findByName(term))
                  TermOntology.link(term, Ontology.findByName(ontology.name),ontology.color)
                }*/

            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                term.errors.each {
                    err -> println err
                }

            }
        }
    }


    def createRelation(relationsSamples) {
        println "createRelation"
        relationsSamples.each { item ->
            if(Relation.findByName(item.name)) return
            def relation = new Relation(name:item.name)
            println "create relation="+ relation.name

            if(relation.validate()) {
                println "Creating relation : ${relation.name}..."
                relation.save(flush : true)

            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createRelationTerm(relationTermSamples) {
        relationTermSamples.each {item->
            def ontology = Ontology.findByName(item.ontology);
            def relation = Relation.findByName(item.relation)
            def term1 = Term.findByNameAndOntology(item.term1, ontology)
            def term2 = Term.findByNameAndOntology(item.term2, ontology)

            if(!RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)) {
                println "Creating term/relation  ${relation.name}:${item.term1}/${item.term2}..."
                RelationTerm.link(relation, term1, term2)
            }

        }
    }

}
