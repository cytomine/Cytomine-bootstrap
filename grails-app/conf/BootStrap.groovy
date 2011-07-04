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

class BootStrap {
    def springSecurityService
    def sequenceService
    def marshallersService
    def grailsApplication
    def storageService
    def messageSource

    static def development = "development"
    static def production = "production"
    static def test = "test"


    def init = { servletContext ->

        marshallersService.initMarshallers()
        sequenceService.initSequences()

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



        /*switch(GrailsUtil.environment) {
            case "development":
                initData(true)
                break
            case "production":
                initData(true)
                break
            case "test":
                initData(false)
                break
        }*/
      StopWatch stopWatch = new LoggingStopWatch();
        initData(GrailsUtil.environment)
      stopWatch.stop("initData");
        //end of init
    }



    private def initData(String env) {
        /* AIS Storages */
        def storages = [
                [name : "cytomine", basePath : "/home/aisstorage/store", serviceUrl : "ais://"],
                [name : "cytomin0", basePath : "/media/datalvm/aisstorage/store", serviceUrl : "ais://"]
                //etc
        ]
        createStorage(storages)

        /* Groups */
        def groupsSamples = [
                [name : "LBTD"],
                [name : "ANAPATH"] ,
                [name : "OTHER"],
                [name : "CERVIX"],
                [name : "GIGA"]
        ]
        createGroups(groupsSamples)

        /* Users */
        def usersSamples = [
                [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group :[[ name :"GIGA"],[ name :"LBTD"], [name : "OTHER"],[name : "ANAPATH"],[name : "CERVIX"]], password : 'password', color : "#FF0000"],
                [username : 'lrollus', firstname : 'Loïc', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[ name :"GIGA"],[ name :"LBTD"], [name : "OTHER"],[name : "ANAPATH"],[name : "CERVIX"]], password : 'password', color : "#00FF00"],
                [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[ name :"GIGA"],[ name :"LBTD"], [name : "OTHER"],[name : "ANAPATH"],[name : "CERVIX"]], password : 'password', color : "#0000FF"] ,
                [username : 'demo', firstname : 'Jean', lastname : 'Dupont', email : 'mymail@ulg.ac.be', group : [[ name :"GIGA"],[ name :"LBTD"], [name : "OTHER"],[name : "ANAPATH"],[name : "CERVIX"]], password : 'demodemo', color : "#00FFFF"],
                [username : 'lbtd', firstname : 'LB', lastname : 'TD', email : 'mymail@ulg.ac.be', group : [[ name :"LBTD"]], password : 'lbtd', color : "#00FFFF"],
                [username : 'anapath', firstname : 'Ana', lastname : 'Path', email : 'mymail@ulg.ac.be', group : [[ name :"ANAPATH"]], password : 'anapath', color : "#00FFFF"]

        ]
        createUsers(usersSamples)


        /* Scanners */
        def scannersSamples = [
                [brand : "gigascan", model : "MODEL1"]
        ]
        createScanners(scannersSamples)


        /* MIME Types */
        def mimeSamples = [
                /*[extension : "jp2", mimeType : "image/jp2"],*/
                [extension : "tif", mimeType : "image/tiff"],
                [extension : "tiff", mimeType : "image/tiff"],
                /*[extension : "gdal", mimeType : "gdalType"],*/
                [extension : "vms", mimeType : "openslide/vms"],
                [extension : "mrxs", mimeType : "openslide/mrxs"],
                [extension : "svs", mimeType : "openslide/svs"]
        ]
        createMimes(mimeSamples)


        /* Image Server */
        def imageServerSamples =  [
                /*[
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is1.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is2.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is3.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is4.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is5.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is6.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is7.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is8.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is9.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'Adore-Djatoka',
                        'url' : 'http://is10.cytomine.be:38',
                        'service' : '/adore-djatoka/resolver',
                        'className' : 'DjatokaResolver',
                        'extension' : ['jp2'],
                        'storage' : 'cytomin0'
                ],*/
                /*[
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is1.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'

                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is2.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is3.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is4.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is5.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is6.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is7.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is8.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is9.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is10.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide',
                        'url' : 'http://is10.cytomine.be:48',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif'],
                        'storage' : 'cytomin0'
                ],*/
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is1.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'

                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is2.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is3.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is4.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is5.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is6.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is7.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is8.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is9.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is10.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ],
                [
                        'name' : 'IIP-Openslide2',
                        'url' : 'http://is10.cytomine.be:888',
                        'service' : '/fcgi-bin/iipsrv.fcgi',
                        'className' : 'IPPResolver',
                        'extension' : ['mrxs','vms', 'tif', 'tiff'],
                        'storage' : 'cytomin0'
                ]


        ]
        createImageServers(imageServerSamples)

        def retrievalServerSamples = [
                [
                        'url' : '139.165.108.28',
                        'port' : 1230,
                        'description' : 'marée'
                ]
        ]
        createRetrievalServers(retrievalServerSamples)

        def ontologySamples = [
                [name: "ATEST", user:'lrollus'],
                /* ANAPATH */
                [name: "LBA",user:'lrollus'],
                [name: "ASP",user:'lrollus'],
                [name: "Frottis",user:'stevben'],
                /* LBTD */
                [name : "Tissus",user:'stevben'],
                [name : "Cellules",user:'stevben'],
                /* PAP*/
                [name : "PAP", user : 'stevben']


        ]
        createOntology(ontologySamples)

        /* Projects */
        def projectSamples = [
                [name : "LBTD NEO4",  groups : [[ name :"LBTD"]],ontology: "Tissus"],
                [name : "LBTD NEO13",  groups : [[ name :"LBTD"]],ontology: "Tissus"],
                //[name : "LBTD",  groups : [[ name :"GIGA"]],ontology: "Tissus"],
                [name : "ANAPATH",  groups : [[ name :"ANAPATH"]],ontology: "LBA"],
                [name : "OTHER",  groups : [[ name :"OTHER"]],ontology: "Cellules"] ,
                [name : "CERVIX",  groups : [[ name :"CERVIX"]],ontology: "PAP"],
                [name : "PHILIPS",  groups : [[ name :"CERVIX"]],ontology: "Tissus"]
                // [name : "NEO13", groups : [[ name :"GIGA"]]],
                // [name : "NEO4",  groups : [[ name :"GIGA"]]]

        ]

        createProjects(projectSamples)

        /* Slides */
        def slideSamples = [
                /*[name : "testslide", order : 8, projects : [[name : "GIGA-DEV"]]],
    [name : "testslide2", order : 8, projects : [[name : "GIGA-DEV"]]],
    [name : "testslide3", order : 8, projects : [[name : "GIGA-DEV"]]],
    [name : "testslide4", order : 8, projects : [[name : "GIGA-DEV2"]]],
    [name : "testslide5", order : 8, projects : [[name : "GIGA-DEV"]]],
    [name : "testslide6", order : 8, projects : [[name : "ANAPATH"]]]  */
        ]
        def slides = createSlides(slideSamples)



        if (env != BootStrap.test) createSlidesAndAbstractImages(BootStrapData.ANAPATHScans)

        if (env != BootStrap.test) createSlidesAndAbstractImages(BootStrapData2.CERVIXScans1)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData2.CERVIXScans2)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData2.CERVIXScans3)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData2.CERVIXScans4)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData2.CERVIXScans5)
        if (env != BootStrap.test) createSlidesAndAbstractImages(BootStrapData2.PhillipsScans)

        if (env != BootStrap.test) createSlidesAndAbstractImages(BootStrapData.LBTDScans1)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData.LBTDScans2)
        if (env == BootStrap.production) createSlidesAndAbstractImages(BootStrapData.LBTDScans3)
        if (env != BootStrap.test) createSlidesAndAbstractImages(BootStrapData.LBTDScans4)

        def termSamples = [

                /* Ontology 1 */
                [name: "Cell in vivo",comment:"",ontology:[name:"ATEST"],color:"#4b5de4"],
                [name: "Cell ex vivo",comment:"",ontology:[name:"ATEST"],color:"#d8b83f"],
                [name: "Cell",comment:"A comment for cell",ontology:[name:"ATEST"],color:"#ff5800"],
                [name: "Cell within a living organism",comment:"",ontology:[name:"ATEST"],color:"#0085cc"],
                /* LBA */
                [name: "Macrophage",comment:"",ontology:[name:"LBA"],color:"#c747a3"],
                [name: "Polynucléaire neutrophile",comment:"",ontology:[name:"LBA"],color:"#cddf54"],
                [name: "Lymphocytes",comment:"",ontology:[name:"LBA"],color:"#fbd178"],
                [name: "Cellules bronchiques ciliées",comment:"",ontology:[name:"LBA"],color:"#26b4e3"],
                [name: "Cellules malpighiennes",comment:"",ontology:[name:"LBA"],color:"#bd70c7"],
                [name: "Autre",comment:"",ontology:[name:"LBA"],color:"#4bb2c5"],
                [name: "Bactérie",comment:"",ontology:[name:"LBA"],color:"#eaa228"],
                [name: "Champignon",comment:"",ontology:[name:"LBA"],color:"#0085cc"],
                [name: "Mucus",comment:"",ontology:[name:"LBA"],color:"#4b5de4"],
                [name: "Artéfact",comment:"",ontology:[name:"LBA"],color:"#ff5800"],
                /* ASP */
                [name: "Cellules bronchiques ciliées",comment:"",ontology:[name:"ASP"],color:"#fbd178"],
                [name: "Cellules muco-sécréantes",comment:"",ontology:[name:"ASP"],color:"#26b4e3"],
                [name: "Cellules tumorales",comment:"",ontology:[name:"ASP"],color:"#bd70c7"],
                [name: "Carcinome épidermoïde",comment:"",ontology:[name:"ASP"],color:"#cddf54"],
                [name: "Adénocarcinome glandulaire",comment:"",ontology:[name:"ASP"],color:"#eaa228"],
                [name: "Small cell carcinoma",comment:"",ontology:[name:"ASP"],color:"#c5b47f"],
                [name: "Cellules malpighiennes",comment:"",ontology:[name:"ASP"],color:"#579575"],
                [name: "Autre",comment:"",ontology:[name:"ASP"],color:"#839557"],
                [name: "Bactérie",comment:"",ontology:[name:"ASP"],color:"#958c12"],
                [name: "Champignon",comment:"",ontology:[name:"ASP"],color:"#953579"],
                [name: "Mucus",comment:"",ontology:[name:"ASP"],color:"#4b5de4"],
                [name: "Artéfact",comment:"",ontology:[name:"ASP"],color:"#d8b83f"],
                /* Frottis */
                [name: "Cellules tumorales",comment:"",ontology:[name:"Frottis"],color:"#ff5800"],
                [name: "Carcinome épidermoïde",comment:"",ontology:[name:"Frottis"],color:"#0085cc"],
                [name: "Adénocarcinome glandulaire",comment:"",ontology:[name:"Frottis"],color:"#c747a3"],
                [name: "Small cell carcinoma",comment:"",ontology:[name:"Frottis"],color:"#cddf54"],
                [name: "Cellules bronchiques ciliées",comment:"",ontology:[name:"Frottis"],color:"#fbd178"],
                [name: "Cellules muco-sécréantes",comment:"",ontology:[name:"Frottis"],color:"#26b4e3"],
                [name: "Cellules malpighiennes",comment:"",ontology:[name:"Frottis"],color:"#bd70c7"],
                [name: "Autre",comment:"",ontology:[name:"Frottis"],color:"#4bb2c5"],
                [name: "Bactérie",comment:"",ontology:[name:"Frottis"],color:"#eaa228"],
                [name: "Champignon",comment:"",ontology:[name:"Frottis"],color:"#c5b47f"],
                [name: "Mucus",comment:"",ontology:[name:"Frottis"],color:"#579575"],
                [name: "Artéfact",comment:"",ontology:[name:"Frottis"],color:"#839557"],
                /* Tissus */
                [name: "Tumeurs",comment:"",ontology:[name:"Tissus"],color:"#958c12"],
                [name: "Adénocarcinomes",comment:"",ontology:[name:"Tissus"],color:"#953579"],
                [name: "Tumeurs épidermoïdes",comment:"",ontology:[name:"Tissus"],color:"#4b5de4"],
                [name: "Vaisseaux",comment:"",ontology:[name:"Tissus"],color:"#d8b83f"],
                [name: "Vaisseau sanguin",comment:"",ontology:[name:"Tissus"],color:"#ff5800"],
                [name: "Vaisseau lymphatique",comment:"",ontology:[name:"Tissus"],color:"#0085cc"],
                [name: "Bronche",comment:"",ontology:[name:"Tissus"],color:"#c747a3"],
                [name: "Foyer d'inflammation",comment:"",ontology:[name:"Tissus"],color:"#cddf54"],
                [name: "Marquage",comment:"",ontology:[name:"Tissus"],color:"#fbd178"],
                [name: "Collagen",comment:"",ontology:[name:"Tissus"],color:"#26b4e3"],
                [name: "Cellule en prolifération",comment:"",ontology:[name:"Tissus"],color:"#bd70c7"],
                [name: "Alpha-smooth muscle actin",comment:"",ontology:[name:"Tissus"],color:"#4bb2c5"],
                [name: "Muscle",comment:"",ontology:[name:"Tissus"],color:"#eaa228"],
                [name: "Globule rouge",comment:"",ontology:[name:"Tissus"],color:"#42426F"],
                [name: "Cartilage",comment:"",ontology:[name:"Tissus"],color:"#c5b47f"],
                [name: "Artefact",comment:"",ontology:[name:"Tissus"],color:"#579575"],
                [name: "Unknown",comment:"",ontology:[name:"Tissus"],color:"#839557"],
                /* Cellules */
                [name: "Macrophages",comment:"",ontology:[name:"Cellules"],color:"#958c12"],
                [name: "Eosinophiles",comment:"",ontology:[name:"Cellules"],color:"#953579"],
                [name: "Neutrophiles",comment:"",ontology:[name:"Cellules"],color:"#4b5de4"],
                [name: "Cellules épithéliales",comment:"",ontology:[name:"Cellules"],color:"#d8b83f"],
                [name: "Lymphocytes",comment:"",ontology:[name:"Cellules"],color:"#ff5800"],
                [name: "Red Blood Cells",comment:"",ontology:[name:"Cellules"],color:"#0085cc"],
                [name: "Artefacts",comment:"",ontology:[name:"Cellules"],color:"#cddf54"],
                /* PAP */
                [name: "Squamous",comment:"",ontology:[name:"PAP"],color:"#958c12"],
                [name: "Intermediate/Superficial",comment:"Class 1",ontology:[name:"PAP"],color:"#953579"],
                [name: "T-Zone",comment:"",ontology:[name:"PAP"],color:"#4bb2c5"],
                [name: "Glandular endocervical",comment:"Class 8",ontology:[name:"PAP"],color:"#4b5de4"],
                [name: "Parabasal",comment:"Class 7",ontology:[name:"PAP"],color:"#d8b83f"],
                [name: "Atypical",comment:"Class26",ontology:[name:"PAP"],color:"#ff5800"],
                [name: "Halos",comment:"Class 32",ontology:[name:"PAP"],color:"#0085cc"],
                [name: "High N/C",comment:"Class 47",ontology:[name:"PAP"],color:"#c747a3"],
                [name: "Elongated",comment:"Class 45",ontology:[name:"PAP"],color:"#cddf54"],
                [name: "Clusters",comment:"Class 42",ontology:[name:"PAP"],color:"#fbd178"],
                [name: "Organisms",comment:"",ontology:[name:"PAP"],color:"#26b4e3"],
                [name: "Fungus",comment:"Class 51",ontology:[name:"PAP"],color:"#bd70c7"],



        ]

        if (env != BootStrap.test) createTerms(termSamples)

        def relationSamples = [
                [name: RelationTerm.names.PARENT],
                [name: RelationTerm.names.SYNONYM],
        ]
        createRelation(relationSamples)


        def relationTermSamples = [
                /* Ontology 1 */
                [relation: RelationTerm.names.PARENT, term1:"Cell within a living organism", term2: "Cell in vivo", ontology : "ATEST"],
                /* LBA */
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Bactérie", ontology : "LBA"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Champignon", ontology : "LBA"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Mucus", ontology : "LBA"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Artéfact", ontology : "LBA"],
                /* ASP */
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Bactérie", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Champignon", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Mucus", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Artéfact", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Carcinome épidermoïde", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Adénocarcinome glandulaire", ontology : "ASP"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Small cell carcinoma", ontology : "ASP"],
                /* Frottis */
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Bactérie", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Champignon", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Mucus", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Autre", term2: "Artéfact", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Carcinome épidermoïde", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Adénocarcinome glandulaire", ontology : "Frottis"],
                [relation: RelationTerm.names.PARENT,term1:"Cellules tumorales", term2: "Small cell carcinoma", ontology : "Frottis"],
                /* Tissus */
                [relation: RelationTerm.names.PARENT,term1:"Tumeurs", term2: "Adénocarcinomes", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Tumeurs", term2: "Tumeurs épidermoïdes", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Vaisseaux", term2: "Vaisseau sanguin", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Vaisseaux", term2: "Vaisseau lymphatique", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Marquage", term2: "Collagen", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Marquage", term2: "Cellule en prolifération", ontology : "Tissus"],
                [relation: RelationTerm.names.PARENT,term1:"Marquage", term2: "Alpha-smooth muscle actin", ontology : "Tissus"],
                /* PAP */
                [relation: RelationTerm.names.PARENT,term1:"Squamous", term2: "Intermediate/Superficial", ontology : "PAP"],
                [relation: RelationTerm.names.PARENT,term1:"T-Zone", term2: "Glandular endocervical", ontology : "PAP"],
                [relation: RelationTerm.names.PARENT,term1:"T-Zone", term2: "Parabasal", ontology : "PAP"],
                [relation: RelationTerm.names.PARENT,term1:"Organisms", term2: "Fungus", ontology : "PAP"]
        ]
        if (env != BootStrap.test) createRelationTerm(relationTermSamples)



        /* Annotations */
        def annotationSamples = [
                //[name : "annot3", location : ["POLYGON((2000 1000, 30 0, 40 10, 30 20, 2000 1000))","POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))"], image: [filename: "Boyden - essai _10x_02"]],
                //[name : "annot2", location : ["POLYGON((20 10, 30 50, 40 10, 30 20, 20 10))"],image: [filename: "Boyden - essai _10x_02"]]
                // [name : "annot3", location : ["POINT(10000 10000)"], scan: [filename: "Aperio - 003"],term:["Bactérie","Champignon"], user:"lrollus"],
                // [name : "", location : ["POINT(5000 5000)"],scan: [filename: "Aperio - 003"],user:"lrollus",term:["Champignon"]],
                /*[name : "Annotation test 1", location : ["POLYGON ((13020.5 25292, 13172.5 25360, 13320.5 25364, 13428.5 25376, 13500.5 25256, 13524.5 25200, 13368.5 25156, 13264.5 25152, 13156.5 25120, 13068.5 25084, 12968.5 25092, 12908.5 25140, 12832.5 25232, 12836.5 25284, 13020.5 25292))"],scan: [filename: "NEO4_HPg_INH_1.60__01.tif.jp2"],term:["Globule rouge","Bronche"],user:"lrollus"],
                [name : "Annotation test 2", location : ["POLYGON ((14600.5 24448, 14692.5 24612, 14724.5 24872, 14996.5 24544, 15012.5 24412, 15112.5 24284, 15496.5 24372, 16292.5 24352, 16632.5 24332, 16808.5 23692, 16696.5 23584, 15564.5 23516, 15412.5 23540, 14784.5 23632, 14540.5 23872, 14464.5 24140, 14348.5 24308, 14344.5 24320, 14600.5 24448))"],term:["Bronche"],scan: [filename: "NEO4_HPg_INH_1.60__01.tif.jp2"],user:"lrollus"],

                [
                        name : "Annotation COMPRESS 0",
                        location : ["POLYGON ((10864 6624, 10864 6752, 10928 6816, 10928 6848, 10992 6912, 10992 7008, 11024 7040, 11024 7296, 11056 7328, 11056 7904, 11088 7968, 11088 8064, 11120 8096, 11120 8160, 11152 8192, 11184 8256, 11184 8320, 11216 8384, 11248 8416, 11248 8512, 11280 8576, 11280 8640, 11312 8704, 11312 8768, 11344 8768, 11344 8864, 11376 8896, 11376 9056, 11408 9056, 11408 9120, 11376 9184, 11376 9216, 11312 9280, 11312 9376, 11344 9472, 11344 9696, 11376 9728, 11376 9792, 11504 9920, 11568 9952, 11600 9952, 11664 9984, 11728 9984, 11760 10048, 11792 10048, 11792 10112, 11760 10144, 11760 10272, 11792 10272, 11792 10464, 11760 10528, 11760 10624, 11728 10656, 11728 10720, 11696 10752, 11696 10784, 11632 10912, 11536 11008, 11536 11040, 11376 11200, 11376 11232, 11312 11360, 11312 11424, 11344 11456, 11344 11488, 11376 11552, 11440 11616, 11440 11648, 11472 11680, 11472 11712, 11504 11712, 11504 11744, 11568 11744, 11568 11776, 11600 11808, 11600 11840, 11664 11904, 11664 11936, 11696 11936, 11696 12000, 11728 12000, 11728 12064, 11888 12224, 12016 12288, 12048 12352, 12048 12480, 12080 12512, 12080 12576, 12112 12608, 12112 12672, 12144 12736, 12144 12768, 12176 12832, 12176 12896, 12208 12928, 12208 12960, 12240 13024, 12592 13376, 12592 13408, 12656 13472, 12656 13568, 12720 13632, 12720 13696, 12752 13728, 12752 13792, 12816 13856, 12848 13920, 12944 14016, 13072 14112, 13168 14176, 13264 14272, 13296 14272, 13360 14336, 13392 14400, 13584 14592, 13584 14752, 13616 14816, 13648 14912, 13712 15040, 13712 15168, 13744 15232, 13744 15328, 13776 15360, 13776 15424, 13808 15456, 13808 15520, 13840 15584, 13840 15744, 13872 15808, 13872 15840, 13936 15968, 13968 16000, 13968 16064, 14000 16128, 14000 16160, 14032 16224, 14032 16256, 14064 16320, 14064 16384, 14096 16448, 14128 16544, 14160 16608, 14224 16672, 14224 16704, 14288 16768, 14352 16800, 14352 16832, 14384 16864, 14384 16896, 14416 16928, 14448 16992, 14576 17120, 14672 17120, 14704 17152, 14768 17184, 14864 17184, 14896 17216, 15184 17216, 15216 17248, 15312 17248, 15312 17280, 15376 17312, 15440 17376, 15600 17376, 15600 17408, 15664 17440, 15760 17440, 15824 17472, 16400 17472, 16496 17440, 16560 17440, 16592 17408, 16784 17408, 16784 17376, 16848 17344, 16944 17280, 17072 17248, 17104 17216, 17168 17184, 17264 17120, 17360 17088, 17488 17024, 17552 16960, 17584 16896, 17616 16896, 17648 16832, 17680 16832, 17712 16800, 17776 16672, 17776 16608, 17808 16576, 17840 16512, 17872 16480, 17968 16320, 18000 16256, 18064 16192, 18096 16128, 18096 16064, 18160 16064, 18224 16000, 18256 15936, 18320 15936, 18512 15744, 18544 15744, 18576 15680, 18608 15648, 18608 15616, 18640 15584, 18704 15552, 18768 15488, 18768 15456, 18800 15392, 18832 15360, 18832 15296, 18864 15232, 18896 15200, 18896 15040, 18864 15040, 18864 15008, 18832 15008, 18768 14944, 18736 14944, 18704 14912, 18640 14880, 18576 14880, 18576 14848, 18544 14848, 18512 14816, 18512 14688, 18544 14688, 18576 14624, 18672 14592, 18768 14528, 18864 14496, 18992 14496, 19024 14496, 19056 14528, 19056 14592, 19088 14592, 19088 14560, 19120 14560, 19152 14496, 19152 14304, 19184 14144, 19216 14080, 19216 13984, 19248 13920, 19248 13888, 19280 13824, 19344 13760, 19344 13696, 19376 13632, 19408 13600, 19408 13312, 19440 13248, 19440 12448, 19472 12384, 19472 11616, 19440 11584, 19440 11232, 19472 11200, 19472 11136, 19536 11008, 19568 11008, 19600 10944, 19600 10880, 19632 10848, 19632 10752, 19664 10752, 19664 10592, 19632 10528, 19632 10336, 19600 10272, 19600 10144, 19568 10080, 19568 9984, 19536 9920, 19536 9792, 19504 9728, 19504 9600, 19472 9536, 19472 9472, 19440 9376, 19440 9216, 19408 9184, 19376 9088, 19376 8928, 19312 8864, 19312 8832, 19280 8768, 19280 8704, 19248 8672, 19248 8608, 19216 8512, 19216 8480, 19184 8416, 19184 8384, 19152 8288, 19120 8224, 19120 8032, 19088 7968, 19088 7904, 19056 7808, 19056 7712, 19024 7648, 19024 7424, 18992 7392, 18992 7264, 18960 7232, 18928 7168, 18896 7136, 18864 7072, 18864 6976, 18832 6944, 18800 6880, 18800 6688, 18768 6624, 18736 6592, 18736 6528, 18704 6496, 18704 6400, 18640 6304, 18608 6208, 18576 6176, 18544 6112, 18512 6080, 18480 6016, 18416 5952, 18320 5760, 18256 5664, 18224 5632, 18192 5536, 18160 5472, 18128 5440, 18128 5376, 18096 5312, 18064 5280, 18032 5216, 17968 5152, 17936 5056, 17904 5056, 17840 4928, 17744 4832, 17712 4768, 17648 4672, 17616 4576, 17584 4448, 17552 4416, 17552 4320, 17520 4256, 17488 4160, 17456 4128, 17456 4096, 17392 4000, 17360 3936, 17328 3776, 17296 3712, 17232 3648, 17200 3552, 17104 3456, 17072 3392, 17008 3360, 17008 3296, 16944 3232, 16912 3136, 16848 3072, 16816 3008, 16784 2912, 16752 2848, 16688 2656, 16656 2592, 16656 2464, 16624 2432, 16592 2368, 16592 2304, 16496 2208, 16432 2080, 16336 2016, 16272 1984, 16240 1920, 16176 1856, 16016 1856, 15888 1760, 15824 1728, 15760 1728, 15664 1696, 15344 1696, 15344 1728, 15312 1728, 15312 1760, 15248 1760, 15152 1824, 15088 1856, 15024 1856, 14992 1888, 14928 1920, 14864 1984, 14800 2080, 14672 2208, 14608 2304, 14544 2368, 14544 2432, 14512 2464, 14448 2464, 14416 2496, 14096 2496, 14000 2464, 13936 2464, 13840 2432, 13552 2432, 13456 2400, 13168 2400, 13072 2368, 12944 2336, 12816 2336, 12720 2304, 12624 2240, 12560 2208, 12496 2208, 12432 2144, 12336 2112, 12272 2112, 12176 2048, 11728 1824, 11664 1760, 11632 1760, 11632 1728, 11600 1696, 11600 1664, 11536 1632, 11504 1568, 11440 1472, 11376 1408, 11280 1344, 11216 1248, 11120 1152, 11088 1088, 10992 1056, 10928 960, 10864 928, 10832 864, 10768 768, 10736 736, 10672 704, 10544 576, 10448 544, 9872 544, 9776 608, 9712 672, 9584 832, 9520 928, 9456 992, 9424 1056, 9392 1088, 9328 1184, 9296 1248, 9232 1312, 9232 1376, 9200 1408, 9200 1472, 9104 1568, 9072 1632, 9072 1728, 9040 1792, 9040 1888, 9072 1984, 9072 2848, 9104 2976, 9104 3200, 9136 3328, 9168 3360, 9200 3488, 9200 3584, 9232 3680, 9296 3744, 9296 3872, 9328 3968, 9328 4096, 9392 4192, 9392 4288, 9456 4352, 9456 4448, 9520 4576, 9584 4672, 9616 4736, 9648 4832, 9712 4928, 9744 5056, 9776 5120, 9808 5152, 9840 5216, 9936 5312, 10032 5376, 10096 5408, 10128 5472, 10192 5536, 10224 5632, 10288 5664, 10320 5728, 10384 5824, 10480 6016, 10544 6080, 10576 6144, 10640 6176, 10672 6272, 10704 6272, 10704 6304, 10736 6304, 10736 6368, 10768 6400, 10768 6432, 10864 6624))"],
                        scan: [filename: "NEO_4_Curcu_INH_1.70_3_8_01.tif"],
                        term:["Bronche","Collagen"],
                        user:"lrollus"
                ],
                [
                        name : "Annotation COMPRESS 25",
                        location : ["POLYGON ((10864 6624, 10864 6752, 10992 6912, 11024 7040, 11088 8064, 11248 8416, 11312 8768, 11344 8768, 11376 8896, 11376 9056, 11408 9056, 11376 9216, 11312 9280, 11344 9696, 11376 9792, 11504 9920, 11728 9984, 11792 10048, 11760 10144, 11792 10464, 11728 10720, 11632 10912, 11376 11200, 11312 11424, 11472 11712, 11568 11744, 11600 11840, 11696 11936, 11728 12064, 11888 12224, 12016 12288, 12176 12896, 12240 13024, 12656 13472, 12656 13568, 12720 13632, 12752 13792, 12944 14016, 13296 14272, 13584 14592, 13584 14752, 13712 15040, 13744 15328, 13840 15584, 13840 15744, 13968 16000, 14160 16608, 14352 16800, 14448 16992, 14576 17120, 14672 17120, 14896 17216, 15312 17248, 15440 17376, 15600 17376, 15664 17440, 15824 17472, 16400 17472, 16592 17408, 16784 17408, 16944 17280, 17072 17248, 17488 17024, 17712 16800, 17776 16608, 18064 16192, 18096 16064, 18160 16064, 18256 15936, 18320 15936, 18544 15744, 18608 15616, 18768 15488, 18832 15360, 18896 15200, 18896 15040, 18768 14944, 18576 14880, 18512 14816, 18512 14688, 18576 14624, 18864 14496, 19024 14496, 19056 14592, 19152 14496, 19216 13984, 19408 13600, 19408 13312, 19440 13248, 19440 12448, 19472 12384, 19440 11232, 19600 10944, 19632 10752, 19664 10752, 19664 10592, 19440 9376, 19440 9216, 19376 9088, 19376 8928, 19312 8864, 19120 8224, 19120 8032, 19024 7648, 18992 7264, 18864 7072, 18864 6976, 18800 6880, 18800 6688, 18736 6592, 18704 6400, 18224 5632, 18096 5312, 17648 4672, 17552 4416, 17552 4320, 17360 3936, 17328 3776, 17200 3552, 17008 3360, 17008 3296, 16848 3072, 16656 2592, 16656 2464, 16592 2304, 16432 2080, 16272 1984, 16176 1856, 16016 1856, 15824 1728, 15344 1696, 15312 1760, 14928 1920, 14544 2368, 14544 2432, 14416 2496, 14096 2496, 13840 2432, 13168 2400, 12816 2336, 12496 2208, 12432 2144, 12272 2112, 11632 1760, 11600 1664, 11536 1632, 11440 1472, 11280 1344, 11088 1088, 10992 1056, 10768 768, 10544 576, 10448 544, 9872 544, 9776 608, 9232 1312, 9200 1472, 9072 1632, 9040 1888, 9072 1984, 9104 3200, 9232 3680, 9296 3744, 9328 4096, 9392 4192, 9392 4288, 9456 4352, 9456 4448, 9776 5120, 9936 5312, 10096 5408, 10224 5632, 10288 5664, 10480 6016, 10640 6176, 10672 6272, 10736 6304, 10864 6624))"],
                        scan: [filename: "NEO_4_Curcu_INH_1.70_3_8_01.tif"],
                        term:["Globule rouge","Collagen"],
                        user:"stevben"
                ],
                [
                        name : "Annotation COMPRESS 100",
                        location : ["POLYGON ((10864 6624, 11408 9056, 11344 9696, 11792 10048, 11728 10720, 11312 11424, 12016 12288, 12176 12896, 12752 13792, 13584 14592, 14160 16608, 14576 17120, 15824 17472, 16784 17408, 17488 17024, 18096 16064, 18832 15360, 18896 15040, 18512 14688, 19152 14496, 19440 13248, 19440 11232, 19664 10592, 18704 6400, 17648 4672, 16432 2080, 15824 1728, 15344 1696, 14928 1920, 14416 2496, 12816 2336, 11632 1760, 10544 576, 9776 608, 9072 1632, 9104 3200, 9776 5120, 10864 6624))"],
                        scan: [filename: "NEO_4_Curcu_INH_1.70_3_8_01.tif"],
                        term:["Artefact"],
                        user:"demo"
                ],
                [
                        name : "Annotation COMPRESS 500",
                        location : ["POLYGON ((10864 6624, 11792 10048, 11312 11424, 13584 14592, 14160 16608, 15824 17472, 17488 17024, 19152 14496, 19664 10592, 18704 6400, 16432 2080, 15344 1696, 14416 2496, 12816 2336, 9776 608, 9104 3200, 10864 6624))"],
                        scan: [filename: "NEO_4_Curcu_INH_1.70_3_8_01.tif"],
                        term:["Collagen"],
                        user:"rmaree"
                ] */


        ]
        if (env != BootStrap.test) createAnnotations(annotationSamples)



        def destroy = {
        }
        //end of init
    }

    /* Methods */

    def createStorage(storages) {
        println "createStorages"
        storages.each {
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
      Storage storage = Storage.findByName("cytomine")
      Group giga = Group.findByName('GIGA')
      User user = User.findByUsername("demo")
        int nbreImage = 0
        LBTDScans.each { item ->
            nbreImage++;

            println "image " +nbreImage + " / " +LBTDScans.size() + " image " + item.filename;

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
                image.save(flush : true)
                AbstractImageGroup.link(image,giga)


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
                        project : project
                )

                imageinstance.save(flush:true)
                /*Storage.list().each {
                    StorageAbstractImage.link(it, image)
                }*/
                StorageAbstractImage.link(storage, image)

            } else {
                println("\n\n\n Errors in image boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> println err
                }

            }


        }
    }


    def createGroups(groupsSamples) {
        def groups = Group.list() ?: []
        //if (!groups) {
        groupsSamples.each { item->
            def group = new Group(name : item.name)
            if (group.validate()) {
                println "Creating group ${group.name}..."

                group.save(flush : true)

                groups << group
            }
            else {
                println("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> println err
                }
            }
        }
        //}
    }


    def createUsers(usersSamples) {
        def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority : "ROLE_USER").save(flush : true)
        def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority : "ROLE_ADMIN").save(flush : true)

        def users = User.list() ?: []
        if (!users) {
            usersSamples.each { item ->
                User user = new User(
                        username : item.username,
                        firstname : item.firstname,
                        lastname : item.lastname,
                        email : item.email,
                        color : item.color,
                        password : springSecurityService.encodePassword(item.password),
                        enabled : true)
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

                    users << user
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                    user.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createScanners(scannersSamples) {
        def scanners = Scanner.list() ?: []
        if (!scanners) {
            scannersSamples.each { item ->
                Scanner scanner = new Scanner(brand : item.brand, model : item.model)

                if (scanner.validate()) {
                    println "Creating scanner ${scanner.brand} - ${scanner.model}..."

                    scanner.save(flush : true)

                    scanners << scanner
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                    scanner.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createMimes(mimeSamples) {
        def mimes = Mime.list() ?: []
        if (!mimes) {
            mimeSamples.each { item ->
                Mime mime = new Mime(extension : item.extension,
                        mimeType : item.mimeType)
                if (mime.validate()) {
                    println "Creating mime ${mime.extension} : ${mime.mimeType}..."

                    mime.save(flush : true)


                    mimes << mime
                } else {
                    println("\n\n\n Errors in account boostrap for ${mime.extension} : ${mime.mimeType}!\n\n\n")
                    mime.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createRetrievalServers(retrievalServerSamples) {
        def retrievalServers = RetrievalServer.list() ?: []
        if (!retrievalServers) {
            retrievalServerSamples.each { item->
                RetrievalServer retrievalServer = new RetrievalServer( url : item.url, port : item.port, description : item.description)
                if (retrievalServer.validate()) {
                    println "Creating retrieval server ${item.description}... "

                    retrievalServer.save(flush:true)

                    retrievalServers <<  retrievalServer
                } else {
                    println("\n\n\n Errors in retrieval server boostrap for ${item.description} !\n\n\n")
                    item.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createImageServers(imageServerSamples) {
        def imageServers = ImageServer.list() ?: []
        if (!imageServers) {
            imageServerSamples.each { item ->
                ImageServer imageServer = new ImageServer(
                        name : item.name,
                        url : item.url,
                        service : item.service,
                        className : item.className,
                        storage : Storage.findByName(item.storage))

                if (imageServer.validate()) {
                    println "Creating image server ${imageServer.name}... : ${imageServer.url}"

                    imageServer.save(flush : true)

                    imageServers << imageServer

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
    }

    def createProjects(projectSamples) {
        def projects = Project.list() ?: []
        if (!projects) {
            projectSamples.each { item->
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

                    projects << project

                } else {
                    println("\n\n\n Errors in project boostrap for ${item.name}!\n\n\n")
                    project.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createSlides(slideSamples) {
        def slides = Slide.list() ?: []
        if (!slides) {
            slideSamples.each {item->
                def slide = new Slide(name : item.name, order : item.order)

                if (slide.validate()) {
                    println "Creating slide  ${item.name}..."

                    slide.save(flush : true)

                    /* Link to projects */
                    /*item.projects.each { elem ->
                        Project project = Project.findByName(elem.name)
                        ProjectSlide.link(project, slide)
                    }*/

                    slides << slide

                } else {
                    println("\n\n\n Errors in slide boostrap for ${item.name}!\n\n\n")
                    slide.errors.each {
                        err -> println err
                    }
                }
            }
        }
        return slides
    }

    def createScans(scanSamples, slides) {
        def images = AbstractImage.list() ?: []
        if (!images) {
            scanSamples.each { item ->
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



                    images << image
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
                    image.errors.each {
                        err -> println err
                    }

                }
            }
        }
    }

    def createAnnotations(annotationSamples) {
        def annotations = Annotation.list() ?: []
        if (!annotations) {
            def annotation = null
            GeometryFactory geometryFactory = new GeometryFactory()
            annotationSamples.each { item ->
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

                    annotations << annotation
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                    annotation.errors.each {
                        err -> println err
                    }

                }
            }
        }
    }

    def createOntology(ontologySamples) {
        println "createOntology"
        def ontologies =   Ontology.list()?:[]
        if(!ontologies) {
            def ontology = null
            ontologySamples.each { item ->
                User user = User.findByUsername(item.user)
                ontology = new Ontology(name:item.name,user:user)
                println "create ontology="+ ontology.name

                if(ontology.validate()) {
                    println "Creating ontology : ${ontology.name}..."
                    ontology.save(flush : true)

                    ontologies << ontology
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                    ontology.errors.each {
                        err -> println err
                    }

                }
            }
        }
    }

    def createTerms(termSamples) {
        println "createTerms"
        def terms =   Term.list()?:[]
        if(!terms) {
            def term = null
            termSamples.each { item ->
                term = new Term(name:item.name,comment:item.comment,ontology:Ontology.findByName(item.ontology.name),color:item.color)
                println "create term="+ term.name

                if(term.validate()) {
                    println "Creating term : ${term.name}..."
                    term.save(flush : true)


                    /*  item.ontology.each {  ontology ->
                      println "add Ontology " + ontology.name
                      //annotation.addToTerm(Term.findByName(term))
                      TermOntology.link(term, Ontology.findByName(ontology.name),ontology.color)
                    }*/

                    terms << term
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                    term.errors.each {
                        err -> println err
                    }

                }
            }
        }
    }


    def createRelation(relationsSamples) {
        println "createRelation"
        def relations =   Relation.list()?:[]
        if(!relations) {
            def relation = null
            relationsSamples.each { item ->
                relation = new Relation(name:item.name)
                println "create relation="+ relation.name

                if(relation.validate()) {
                    println "Creating relation : ${relation.name}..."
                    relation.save(flush : true)

                    relations << relation
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                    relation.errors.each {
                        err -> println err
                    }

                }
            }
        }
    }

    def createRelationTerm(relationTermSamples) {
        def relationTerm = RelationTerm.list() ?: []

        if (!relationTerm) {
            relationTermSamples.each {item->
                def ontology = Ontology.findByName(item.ontology);
                def relation = Relation.findByName(item.relation)
                def term1 = Term.findByNameAndOntology(item.term1, ontology)
                def term2 = Term.findByNameAndOntology(item.term2, ontology)

                println "Creating term/relation  ${relation.name}:${item.term1}/${item.term2}..."
                RelationTerm.link(relation, term1, term2)

            }
        }

    }

}
class BootStrapData {
    static def ANAPATHScans = [
            //ANAPATH
            [filename: '/home/stevben/Slides/ANAPATH/vms/01c02157_lba-2011-01-2523.21.42_clip.vms',name: '01c02157_lba-2011-01-2523.21.42_clip.vms',study:'ANAPATH', extension:"vms"],
            [filename: '/home/stevben/Slides/ANAPATH/vms/10C12080-LBAPap-2010-12-0912.18.51_clip.vms', name: '10C12080-LBAPap-2010-12-0912.18.51_clip.vms',study:'ANAPATH', extension:"vms"],
            [filename: '/home/stevben/Slides/ANAPATH/vms/OVA17cyto-2010-11-1513.09.42_clip.vms', name : 'OVA17cyto-2010-11-1513.09.42_clip.vms',study:'ANAPATH', extension:"vms"],
    ]

    static def LBTDScans1 = [



            /* [filename: 'Boyden - essai _10x_02',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2',slide : 0],
       [filename: 'Aperio - 003',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2',slide : 0 ],
       [filename: 'Aperio - 2005900969-2', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2',slide : 0 ],
       [filename: 'bottom-nocompression', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/bottom-nocompression-crop-8levels-256.jp2',slide : 0 ],
       [filename: '70pc_cropnew', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/PhDelvenne/2_02_JPEG_70pc_cropnew.jp2',slide : 0 ],
       [filename: 'Agar seul 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-1.jp2',slide : 0 ],
       [filename: 'Agar seul 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-2.jp2',slide : 0 ],
       [filename: 'Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-1.jp2',slide : 1 ],
       [filename: 'Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-2.jp2',slide : 1 ],
       [filename: 'Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-3.jp2',slide : 1 ],
       [filename: 'Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-4.jp2',slide : 1 ],
       [filename: 'Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-5.jp2',slide : 1 ],
       [filename: 'Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-6.jp2',slide : 1 ],
       [filename: 'Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-7.jp2',slide : 1 ],
       [filename: 'Curcu non soluble 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-1.jp2',slide : 2 ],
       [filename: 'Curcu non soluble 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-2.jp2',slide : 2 ],
       [filename: 'Curcu non soluble 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-3.jp2',slide : 2 ],
       [filename: 'Curcu non soluble 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-4.jp2',slide : 2 ],
       [filename: 'Curcu non soluble 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-5.jp2',slide : 2 ],
       [filename: 'Gemzar 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-1.jp2',slide : 3 ],
       [filename: 'Gemzar 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-2.jp2',slide : 3 ],
       [filename: 'Gemzar 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-3.jp2',slide : 3 ],
       [filename: 'Gemzar 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-4.jp2',slide : 3 ],
       [filename: 'Gemzar 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-5.jp2',slide : 3 ],
       [filename: 'Gemzar 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-6.jp2',slide : 3 ],
       [filename: 'Gemzar 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-7.jp2',slide : 3 ],
       [filename: 'Gemzar 8', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-8.jp2',slide : 3 ],
       [filename: 'Gemzar + Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-1.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-2.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-3.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-4.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-5.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-6.jp2',slide : 4 ],
       [filename: 'Gemzar + Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-7.jp2',slide : 4 ],
       [filename: 'HPg 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-1.jp2',slide :5 ],
       [filename: 'HPg 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-3.jp2',slide :5 ],
       [filename: 'HPg 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-4.jp2',slide :5 ],
       [filename: 'HPg 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-5.jp2',slide :5 ],
       [filename: 'HPg 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-6.jp2',slide :5 ],
       [filename: 'HPg 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-7.jp2',slide :5 ] */





            //OTHER
            /*[filename: 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2', name: 'Boyden - essai _10x_02', study:'OTHER'],
        [filename: 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2',name:'Aperio - 003',study:'OTHER'],
        [filename: 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2', name: 'Aperio - 2005900969-2', study:'OTHER'],
        [filename: 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/PhDelvenne/2_02_JPEG_70pc_cropnew.jp2', name:'70pc_cropnew', study:'OTHER' ],*/


            //NEO13
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/ImageNEO13_CNS_5.10_5_4_01.tif.vips.tif',name:'ImageNEO13_CNS_5.10_5_4_01.tif.vips.tif',study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/ImageNEO13_CNS_5.1_5_3_01.tif.vips.tif',name:'ImageNEO13_CNS_5.1_5_3_01.tif.vips.tif',study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/ImageNEO13_CNS_5.20_5_5_01.tif.vips.tif',name:'ImageNEO13_CNS_5.20_5_5_01.tif.vips.tif',study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.10_5_1_01.tif.vips.tif',name:'NEO13_CNS_1.10_5_1_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.1_4_10_01.tif.vips.tif',name:'NEO13_CNS_1.1_4_10_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.20_5_2_01.tif.vips.tif',name:'NEO13_CNS_1.20_5_2_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.30_5_3_01.tif.vips.tif',name:'NEO13_CNS_1.30_5_3_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.40_5_4_01.tif.vips.tif',name:'NEO13_CNS_1.40_5_4_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.50_5_5_01.tif.vips.tif',name:'NEO13_CNS_1.50_5_5_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.60_5_6_01.tif.vips.tif',name:'NEO13_CNS_1.60_5_6_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_1.70_5_7_01.tif.vips.tif',name:'NEO13_CNS_1.70_5_7_01.tif.vips.tif',slidename:'NEO13_CNS_1',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.10_4_3_01.tif.vips.tif',name:'NEO13_CNS_2.10_4_3_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.1_4_2_01.tif.vips.tif',name:'NEO13_CNS_2.1_4_2_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.20_4_4_01.tif.vips.tif',name:'NEO13_CNS_2.20_4_4_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.30_4_5_01.tif.vips.tif',name:'NEO13_CNS_2.30_4_5_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.40_4_6_01.tif.vips.tif',name:'NEO13_CNS_2.40_4_6_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.50_4_7_01.tif.vips.tif',name:'NEO13_CNS_2.50_4_7_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.60_4_8_01.tif.vips.tif',name:'NEO13_CNS_2.60_4_8_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_2.70_4_9_01.tif.vips.tif',name:'NEO13_CNS_2.70_4_9_01.tif.vips.tif',slidename:'NEO13_CNS_2',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.10_3_5_01.tif.vips.tif',name:'NEO13_CNS_3.10_3_5_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.1_3_4_01.tif.vips.tif',name:'NEO13_CNS_3.1_3_4_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.20_3_6_01.tif.vips.tif',name:'NEO13_CNS_3.20_3_6_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.30_3_7_01.tif.vips.tif',name:'NEO13_CNS_3.30_3_7_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.40_3_8_01.tif.vips.tif',name:'NEO13_CNS_3.40_3_8_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.50_3_9_01.tif.vips.tif',name:'NEO13_CNS_3.50_3_9_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.60_3_10_01.tif.vips.tif',name:'NEO13_CNS_3.60_3_10_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_3.70_4_1_01.tif.vips.tif',name:'NEO13_CNS_3.70_4_1_01.tif.vips.tif',slidename:'NEO13_CNS_3',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.10_2_7_01.tif.vips.tif',name:'NEO13_CNS_4.10_2_7_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.1_2_6_01.tif.vips.tif',name:'NEO13_CNS_4.1_2_6_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.20_2_8_01.tif.vips.tif',name:'NEO13_CNS_4.20_2_8_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.30_2_9_01.tif.vips.tif',name:'NEO13_CNS_4.30_2_9_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.40_2_10_01.tif.vips.tif',name:'NEO13_CNS_4.40_2_10_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.50_3_1_01.tif.vips.tif',name:'NEO13_CNS_4.50_3_1_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.60_3_2_01.tif.vips.tif',name:'NEO13_CNS_4.60_3_2_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_4.70_3_3_01.tif.vips.tif',name:'NEO13_CNS_4.70_3_3_01.tif.vips.tif',slidename:'NEO13_CNS_4',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_5.30_1_3_01.tif.vips.tif',name:'NEO13_CNS_5.30_1_3_01.tif.vips.tif',slidename:'NEO13_CNS_5',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_5.40_1_4_01.tif.vips.tif',name:'NEO13_CNS_5.40_1_4_01.tif.vips.tif',slidename:'NEO13_CNS_5',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_5.50_1_5_01.tif.vips.tif',name:'NEO13_CNS_5.50_1_5_01.tif.vips.tif',slidename:'NEO13_CNS_5',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_5.60_1_6_01.tif.vips.tif',name:'NEO13_CNS_5.60_1_6_01.tif.vips.tif',slidename:'NEO13_CNS_5',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_5.70_1_7_01.tif.vips.tif',name:'NEO13_CNS_5.70_1_7_01.tif.vips.tif',slidename:'NEO13_CNS_5',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.10_1_9_01.tif.vips.tif',name:'NEO13_CNS_6.10_1_9_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.1_1_8_01.tif.vips.tif',name:'NEO13_CNS_6.1_1_8_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.20_1_10_01.tif.vips.tif',name:'NEO13_CNS_6.20_1_10_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.30_2_1_01.tif.vips.tif',name:'NEO13_CNS_6.30_2_1_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.40_2_2_01.tif.vips.tif',name:'NEO13_CNS_6.40_2_2_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.50_2_3_01.tif.vips.tif',name:'NEO13_CNS_6.50_2_3_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.60_2_4_01.tif.vips.tif',name:'NEO13_CNS_6.60_2_4_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/NEO13_CNS_6.70_2_5_01.tif.vips.tif',name:'NEO13_CNS_6.70_2_5_01.tif.vips.tif',slidename:'NEO13_CNS_6',order:70,study:'LBTD NEO13', extension :'tif']
    ]
    static def LBTDScans2 = [
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_1.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_1.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_1',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_2.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_2.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_2',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_3.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_3.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_3',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_4.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_4.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_4',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_5.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_5.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_5',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.10_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.10_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.1_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.1_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.20_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.20_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.30_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.30_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.40_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.40_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.50_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.50_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.60_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.60_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/NEO_13_Curcu_90pc_6.70_01.tif.vips.tif',name:'NEO_13_Curcu_90pc_6.70_01.tif.vips.tif',slidename:'NEO_13_Curcu_90pc_6',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.10_2_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.10_2_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.1_1_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.1_1_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.20_3_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.20_3_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.30_4_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.30_4_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.40_5_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.40_5_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.50_6_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.50_6_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.60_7_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.60_7_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_1.70_8_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_1.70_8_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_1',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.10_10_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.10_10_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.1_9_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.1_9_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.20_1_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.20_1_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.30_2_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.30_2_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.40_3_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.40_3_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.50_4_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.50_4_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.60_5_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.60_5_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_2.70_6_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_2.70_6_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_2',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.10_8_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.10_8_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.1_7_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.1_7_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.20_9_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.20_9_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.30_10_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.30_10_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.40_1_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.40_1_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO13_CURCU_93pc_3.50_1_1_01.tif.vips.tif',name:'NEO13_CURCU_93pc_3.50_1_1_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_3.60_3_01.tif.vips.tif',name:'NEO_13_Curcu_93pc_3.60_3_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO13_CURCU_93pc_3.70_1_2_01.tif.vips.tif',name:'NEO13_CURCU_93pc_3.70_1_2_01.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_3',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.10.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.10.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.1.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.1.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.20.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.20.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.30.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.30.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.40.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.40.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.50.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.50.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.60.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.60.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_4.70.tif.vips.tif',name:'NEO_13_Curcu_93pc_4.70.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_4',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.10.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.10.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.1.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.1.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.20.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.20.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.30.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.30.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.40.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.40.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.50.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.50.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.60.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.60.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_5.70.tif.vips.tif',name:'NEO_13_Curcu_93pc_5.70.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_5',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.10.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.10.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.1.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.1.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.20.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.20.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.30.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.30.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.40.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.40.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.50.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.50.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.60.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.60.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/NEO_13_Curcu_93pc_6.70.tif.vips.tif',name:'NEO_13_Curcu_93pc_6.70.tif.vips.tif',slidename:'NEO_13_Curcu_93pc_6',order:70,study:'LBTD NEO13', extension :'tif']
    ]
    static def LBTDScans3 = [
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.20_1_1_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.20_1_1_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:20,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.30_1_2_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.30_1_2_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:30,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.40_1_3_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.40_1_3_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:40,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.50_1_4_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.50_1_4_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:50,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.60_1_5_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.60_1_5_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:60,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_2.70_1_6_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_2.70_1_6_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_2',order:70,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.10_1_8_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.10_1_8_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:10,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.1_1_7_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.1_1_7_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:1,study:'LBTD NEO13', extension :'tif'		],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.20_1_9_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.20_1_9_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:20,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.30_1_10_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.30_1_10_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.40_2_1_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.40_2_1_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:40,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.50_2_2_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.50_2_2_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:50,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.60_2_3_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.60_2_3_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:60,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_3.70_2_4_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_3.70_2_4_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_3',order:70,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.10_2_6_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.10_2_6_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:10,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.1_2_5_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.1_2_5_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4'  ,order:1,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.20_2_7_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.20_2_7_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:20,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.30_2_8_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.30_2_8_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:30,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.40_2_9_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.40_2_9_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:40,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.50_2_10_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.50_2_10_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.60_3_1_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.60_3_1_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:60,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_4.70_3_2_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_4.70_3_2_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_4',order:70,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.10_3_4_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.10_3_4_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:10,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.1_3_3_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.1_3_3_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5'  ,order:1,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.20_3_5_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.20_3_5_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:20,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.30_3_6_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.30_3_6_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:30,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.40_3_7_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.40_3_7_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:40,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.50_3_8_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.50_3_8_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:50,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_5.70_3_10_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_5.70_3_10_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_5',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.10_4_2_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.10_4_2_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6,',order:10,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.1_4_1_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.1_4_1_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:1,study:'LBTD NEO13', extension :'tif'		],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.20_4_3_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.20_4_3_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:20,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.30_4_4_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.30_4_4_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:30,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.40_4_5_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.40_4_5_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:40,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.50_4_6_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.50_4_6_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:50,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.60_4_7_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.60_4_7_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:60,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/ImageNEO_13_Curcu_99pc_6.70_4_8_01.tif.vips.tif',name:'ImageNEO_13_Curcu_99pc_6.70_4_8_01.tif.vips.tif',slidename:'ImageNEO_13_Curcu_99pc_6',order:70,study:'LBTD NEO13', extension :'tif'	],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/NEO_13_Curcu_99pc_2.10.tif.vips.tif',name:'NEO_13_Curcu_99pc_2.10.tif.vips.tif',slidename:'NEO_13_Curcu_99pc_2',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/NEO_13_Curcu_99pc_2.1.tif.vips.tif',name:'NEO_13_Curcu_99pc_2.1.tif.vips.tif',slidename:'NEO_13_Curcu_99pc_2',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO13_HPg_1.10_5_9_01.tif.vips.tif',name:'NEO13_HPg_1.10_5_9_01.tif.vips.tif',slidename:'NEO13_HPg_1',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO13_HPg_1.1_5_8_01.tif.vips.tif',name:'NEO13_HPg_1.1_5_8_01.tif.vips.tif',slidename:'NEO13_HPg_1',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO13_HPg_1.20_5_10_01.tif.vips.tif',name:'NEO13_HPg_1.20_5_10_01.tif.vips.tif',slidename:'NEO13_HPg_1',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_1.30__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_1.30__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_1',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_1.40__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_1.40__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_1',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_1.50__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_1.50__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_1',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_1.60__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_1.60__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_1',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_1.70__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_1.70__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_1',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.10__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.10__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.1__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.1__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.20__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.20__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.30__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.30__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.40__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.40__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.50__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.50__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.60__HE01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.60__HE01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_2.70__01.tif.vips.tif',name:'NEO_13_HPg_Gav_2.70__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_2',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.10__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.10__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.1__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.1__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.20__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.20__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.30__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.30__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.40__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.40__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.50__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.50__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.60__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.60__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_3.70__01.tif.vips.tif',name:'NEO_13_HPg_Gav_3.70__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_3',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.10__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.10__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.1__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.1__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.20__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.20__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.30__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.30__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.40__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.40__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.50__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.50__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.60__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.60__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_4.70__01.tif.vips.tif',name:'NEO_13_HPg_Gav_4.70__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_4',order:70,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.10__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.10__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:10,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.1__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.1__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:1,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.20__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.20__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:20,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.30__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.30__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:30,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.40__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.40__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:40,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.50__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.50__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:50,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.60__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.60__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:60,study:'LBTD NEO13', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/NEO_13_HPg_Gav_5.70__01.tif.vips.tif',name:'NEO_13_HPg_Gav_5.70__01.tif.vips.tif',slidename:'NEO_13_HPg_Gav_5',order:70,study:'LBTD NEO13', extension :'tif']
    ]
    static def LBTDScans4 = [
            //NEO4
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.10_3_2_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.10_3_2_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.1_3_1_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.1_3_1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.20_3_3_01.vips.tif.vips.tif',name:'NEO_4_Curcu_INH_1.20_3_3_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.30_3_4_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.30_3_4_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.40_3_5_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.40_3_5_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.50_3_6_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.50_3_6_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.60_3_7_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.60_3_7_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_1.70_3_8_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.70_3_8_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_2.10_3_10_01.tif.vips.tif',name:'NEO_4_Curcu_INH_2.10_3_10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_2.1_3_9_01.tif.vips.tif',name:'NEO_4_Curcu_INH_2.1_3_9_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_2.20_4_1_01.tif.vips.tif',name:'NEO_4_Curcu_INH_2.20_4_1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_2.30_4_2_01.tif.vips.tif',name:'NEO_4_Curcu_INH_2.30_4_2_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO_4_Curcu_INH_2.40_1_1_01HE.tif.vips.tif',name:'NEO_4_Curcu_INH_2.40_1_1_01HE.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_2.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_2.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_2.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_2.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_2.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_2.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_2',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.40_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.40_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_3.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_3.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_3',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.40_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.40_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_4.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_4.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_4',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.40_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.40_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_5.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_5.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_5',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.40_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.40_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_6.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_6.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_6',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.40_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.40_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.50_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.50_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.60_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.60_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_7.70_01.tif.vips.tif',name:'NEO4_CURCU_INH_7.70_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_7',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_8.10_01.tif.vips.tif',name:'NEO4_CURCU_INH_8.10_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_8.1_01.tif.vips.tif',name:'NEO4_CURCU_INH_8.1_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_8.20_01.tif.vips.tif',name:'NEO4_CURCU_INH_8.20_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_8.30_01.tif.vips.tif',name:'NEO4_CURCU_INH_8.30_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/NEO4_CURCU_INH_8.4001.tif.vips.tif',name:'NEO4_CURCU_INH_8.4001.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/_NEO4_CURCU_INH_8.5001.tif.vips.tif',name:'_NEO4_CURCU_INH_8.5001.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/_NEO4_CURCU_INH_8.6001.tif.vips.tif',name:'_NEO4_CURCU_INH_8.6001.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/_NEO4_CURCU_INH_8.7001.tif.vips.tif',name:'_NEO4_CURCU_INH_8.7001.tif.vips.tif',slidename:'NEO_4_Curcu_INH_8',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.10__01.tif.vips.tif',name:'NEO4_HPg_INH_1.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.1__01.tif.vips.tif',name:'NEO4_HPg_INH_1.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.20__01.tif.vips.tif',name:'NEO4_HPg_INH_1.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.40__01.tif.vips.tif',name:'NEO4_HPg_INH_1.40__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.50__01.tif.vips.tif',name:'NEO4_HPg_INH_1.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.60__01.tif.vips.tif',name:'NEO4_HPg_INH_1.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_1.70__01.tif.vips.tif',name:'NEO4_HPg_INH_1.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_1',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.10__01.tif.vips.tif',name:'NEO4_HPg_INH_2.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.1__01.tif.vips.tif',name:'NEO4_HPg_INH_2.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.20__01.tif.vips.tif',name:'NEO4_HPg_INH_2.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.30__01.tif.vips.tif',name:'NEO4_HPg_INH_2.30__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.40__01.tif.vips.tif',name:'NEO4_HPg_INH_2.40__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.50__01.tif.vips.tif',name:'NEO4_HPg_INH_2.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.60__01.tif.vips.tif',name:'NEO4_HPg_INH_2.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_2.70__01.tif.vips.tif',name:'NEO4_HPg_INH_2.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_2',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.10__01.tif.vips.tif',name:'NEO4_HPg_INH_3.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.1__01.tif.vips.tif',name:'NEO4_HPg_INH_3.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.20__01.tif.vips.tif',name:'NEO4_HPg_INH_3.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.30__01.tif.vips.tif',name:'NEO4_HPg_INH_3.30__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.40__01.tif.vips.tif',name:'NEO4_HPg_INH_3.40__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.50__01.tif.vips.tif',name:'NEO4_HPg_INH_3.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.60__01.tif.vips.tif',name:'NEO4_HPg_INH_3.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_3.70__01.tif.vips.tif',name:'NEO4_HPg_INH_3.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_3',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.10__01.tif.vips.tif',name:'NEO4_HPg_INH_4.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.1__01.tif.vips.tif',name:'NEO4_HPg_INH_4.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.20__01.tif.vips.tif',name:'NEO4_HPg_INH_4.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.30__01.tif.vips.tif',name:'NEO4_HPg_INH_4.30__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.50__01.tif.vips.tif',name:'NEO4_HPg_INH_4.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.60__01.tif.vips.tif',name:'NEO4_HPg_INH_4.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_4.70__01.tif.vips.tif',name:'NEO4_HPg_INH_4.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_4',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.10__01.tif.vips.tif',name:'NEO4_HPg_INH_5.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.1__01.tif.vips.tif',name:'NEO4_HPg_INH_5.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.20__01.tif.vips.tif',name:'NEO4_HPg_INH_5.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.30__01.tif.vips.tif',name:'NEO4_HPg_INH_5.30__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.40__01.tif.vips.tif',name:'NEO4_HPg_INH_5.40__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.50__01.tif.vips.tif',name:'NEO4_HPg_INH_5.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.60__01.tif.vips.tif',name:'NEO4_HPg_INH_5.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_5.70__01.tif.vips.tif',name:'NEO4_HPg_INH_5.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_5',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.10__01.tif.vips.tif',name:'NEO4_HPg_INH_6.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.1__01.tif.vips.tif',name:'NEO4_HPg_INH_6.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.20__01.tif.vips.tif',name:'NEO4_HPg_INH_6.20__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.30__01.tif.vips.tif',name:'NEO4_HPg_INH_6.30__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.40__01.tif.vips.tif',name:'NEO4_HPg_INH_6.40__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.50__01.tif.vips.tif',name:'NEO4_HPg_INH_6.50__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.60__01.tif.vips.tif',name:'NEO4_HPg_INH_6.60__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_6.70__01.tif.vips.tif',name:'NEO4_HPg_INH_6.70__01.tif.vips.tif',slidename:'NEO4_HPg_INH_6',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_7.10__01.tif.vips.tif',name:'NEO4_HPg_INH_7.10__01.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/NEO4_HPg_INH_7.1__01.tif.vips.tif',name:'NEO4_HPg_INH_7.1__01.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:1,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.2001.tif.vips.tif',name:'_NEO4_HPg_INH_7.2001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.3001.tif.vips.tif',name:'_NEO4_HPg_INH_7.3001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.4001.tif.vips.tif',name:'_NEO4_HPg_INH_7.4001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.5001.tif.vips.tif',name:'_NEO4_HPg_INH_7.5001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.6001.tif.vips.tif',name:'_NEO4_HPg_INH_7.6001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_7.7001.tif.vips.tif',name:'_NEO4_HPg_INH_7.7001.tif.vips.tif',slidename:'NEO4_HPg_INH_7',order:70,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.1001.tif.vips.tif',name:'_NEO4_HPg_INH_8.1001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:10,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.101.tif.vips.tif',name:'_NEO4_HPg_INH_8.101.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:11,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.2001.tif.vips.tif',name:'_NEO4_HPg_INH_8.2001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:20,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.3001.tif.vips.tif',name:'_NEO4_HPg_INH_8.3001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:30,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.4001.tif.vips.tif',name:'_NEO4_HPg_INH_8.4001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:40,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.5001.tif.vips.tif',name:'_NEO4_HPg_INH_8.5001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:50,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.6001.tif.vips.tif',name:'_NEO4_HPg_INH_8.6001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:60,study:'LBTD NEO4', extension :'tif'],
            [filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/_NEO4_HPg_INH_8.7001.tif.vips.tif',name:'_NEO4_HPg_INH_8.7001.tif.vips.tif',slidename:'NEO4_HPg_INH_8',order:70,study:'LBTD NEO4', extension :'tif'],
            //[filename:'/home/stevben/Slides/LBTD/Slides/Olympus/study_test/grp/converti/NEO_4_Curcu_INH_1.10_3_2_01.tif.vips.tif',name:'NEO_4_Curcu_INH_1.10_3_2_01.tif.vips.tif',slidename:'NEO_4_Curcu_INH_1',order:10,study:'LBTD NEO4', extension :'tif']
    ]

}
class BootStrapData2 {
    static def CERVIXScans1 = [

            //[filename:'/home/stevben/Slides/CERVIX/09-032099.mrxs', name :'09-032099.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-082544.mrxs', name :'09-082544.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-083151.mrxs', name :'09-083151.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-083782.mrxs', name :'09-083782.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-083903.mrxs', name :'09-083903.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-086380.mrxs', name :'09-086380.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-086562.mrxs', name :'09-086562.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-086566.mrxs', name :'09-086566.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-087214.mrxs', name :'09-087214.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-087496.mrxs', name :'09-087496.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-088022.mrxs', name :'09-088022.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-088135.mrxs', name :'09-088135.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-088456.mrxs', name :'09-088456.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-089901.mrxs', name :'09-089901.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-090210.mrxs', name :'09-090210.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-090362.mrxs', name :'09-090362.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-091034.mrxs', name :'09-091034.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-091152.mrxs', name :'09-091152.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-091511.mrxs', name :'09-091511.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-091669.mrxs', name :'09-091669.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-092517.mrxs', name :'09-092517.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-092970.mrxs', name :'09-092970.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-092974.mrxs', name :'09-092974.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-093881.mrxs', name :'09-093881.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-093911.mrxs', name :'09-093911.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094116.mrxs', name :'09-094116.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094158.mrxs', name :'09-094158.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094406.mrxs', name :'09-094406.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094465.mrxs', name :'09-094465.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094475.mrxs', name :'09-094475.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094476.mrxs', name :'09-094476.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094494.mrxs', name :'09-094494.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094594.mrxs', name :'09-094594.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094626.mrxs', name :'09-094626.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094705.mrxs', name :'09-094705.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-094990.mrxs', name :'09-094990.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-095364.mrxs', name :'09-095364.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-095763.mrxs', name :'09-095763.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-095813.mrxs', name :'09-095813.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096330.mrxs', name :'09-096330.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096556.mrxs', name :'09-096556.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096615.mrxs', name :'09-096615.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096682.mrxs', name :'09-096682.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096696.mrxs', name :'09-096696.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096886.mrxs', name :'09-096886.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-096935.mrxs', name :'09-096935.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097003.mrxs', name :'09-097003.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097060.mrxs', name :'09-097060.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097065.mrxs', name :'09-097065.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097182.mrxs', name :'09-097182.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097217.mrxs', name :'09-097217.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097219.mrxs', name :'09-097219.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097222.mrxs', name :'09-097222.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097233.mrxs', name :'09-097233.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097237.mrxs', name :'09-097237.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097407.mrxs', name :'09-097407.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097650.mrxs', name :'09-097650.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097662.mrxs', name :'09-097662.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097712.mrxs', name :'09-097712.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097861.mrxs', name :'09-097861.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-097873.mrxs', name :'09-097873.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098226.mrxs', name :'09-098226.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098231.mrxs', name :'09-098231.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098383.mrxs', name :'09-098383.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098437.mrxs', name :'09-098437.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098438.mrxs', name :'09-098438.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098447.mrxs', name :'09-098447.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098449.mrxs', name :'09-098449.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098452.mrxs', name :'09-098452.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098455.mrxs', name :'09-098455.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098456.mrxs', name :'09-098456.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098457.mrxs', name :'09-098457.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098458.mrxs', name :'09-098458.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098459.mrxs', name :'09-098459.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098462.mrxs', name :'09-098462.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098463.mrxs', name :'09-098463.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098464.mrxs', name :'09-098464.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098466_21.05.2009_15.34.20.mrxs', name :'09-098466_21.05.2009_15.34.20.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098466.mrxs', name :'09-098466.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098467.mrxs', name :'09-098467.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098472.mrxs', name :'09-098472.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098474.mrxs', name :'09-098474.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098476.mrxs', name :'09-098476.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098478.mrxs', name :'09-098478.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098482.mrxs', name :'09-098482.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098483.mrxs', name :'09-098483.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098486.mrxs', name :'09-098486.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098488.mrxs', name :'09-098488.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098489.mrxs', name :'09-098489.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098491.mrxs', name :'09-098491.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098493.mrxs', name :'09-098493.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098494.mrxs', name :'09-098494.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098495.mrxs', name :'09-098495.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-098496.mrxs', name :'09-098496.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099259.mrxs', name :'09-099259.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099263.mrxs', name :'09-099263.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099290.mrxs', name :'09-099290.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099362.mrxs', name :'09-099362.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099382.mrxs', name :'09-099382.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099537.mrxs', name :'09-099537.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099550.mrxs', name :'09-099550.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099551.mrxs', name :'09-099551.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099553.mrxs', name :'09-099553.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099555.mrxs', name :'09-099555.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099556.mrxs', name :'09-099556.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099557.mrxs', name :'09-099557.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099558.mrxs', name :'09-099558.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099559.mrxs', name :'09-099559.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099560.mrxs', name :'09-099560.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099561.mrxs', name :'09-099561.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099562.mrxs', name :'09-099562.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099563.mrxs', name :'09-099563.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099565.mrxs', name :'09-099565.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099566.mrxs', name :'09-099566.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099568.mrxs', name :'09-099568.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099570.mrxs', name :'09-099570.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099571.mrxs', name :'09-099571.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099572.mrxs', name :'09-099572.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099573.mrxs', name :'09-099573.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099574.mrxs', name :'09-099574.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099575.mrxs', name :'09-099575.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099576.mrxs', name :'09-099576.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099577.mrxs', name :'09-099577.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099579.mrxs', name :'09-099579.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099580.mrxs', name :'09-099580.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099582.mrxs', name :'09-099582.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099583.mrxs', name :'09-099583.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099584.mrxs', name :'09-099584.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099585.mrxs', name :'09-099585.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099586.mrxs', name :'09-099586.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099587.mrxs', name :'09-099587.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099588.mrxs', name :'09-099588.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099589.mrxs', name :'09-099589.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099645.mrxs', name :'09-099645.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099818.mrxs', name :'09-099818.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099846.mrxs', name :'09-099846.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099898.mrxs', name :'09-099898.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-099950.mrxs', name :'09-099950.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-100149.mrxs', name :'09-100149.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-100923.mrxs', name :'09-100923.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101077.mrxs', name :'09-101077.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101078.mrxs', name :'09-101078.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101079.mrxs', name :'09-101079.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101080.mrxs', name :'09-101080.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101081.mrxs', name :'09-101081.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101082.mrxs', name :'09-101082.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101083.mrxs', name :'09-101083.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101084.mrxs', name :'09-101084.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101085.mrxs', name :'09-101085.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101086.mrxs', name :'09-101086.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101088.mrxs', name :'09-101088.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101089.mrxs', name :'09-101089.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101090.mrxs', name :'09-101090.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101091.mrxs', name :'09-101091.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101092.mrxs', name :'09-101092.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101093.mrxs', name :'09-101093.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101184.mrxs', name :'09-101184.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101195.mrxs', name :'09-101195.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101235.mrxs', name :'09-101235.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101247.mrxs', name :'09-101247.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101284.mrxs', name :'09-101284.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-101460.mrxs', name :'09-101460.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102486.mrxs', name :'09-102486.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102488.mrxs', name :'09-102488.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102490.mrxs', name :'09-102490.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102491.mrxs', name :'09-102491.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102493.mrxs', name :'09-102493.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102494.mrxs', name :'09-102494.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102495.mrxs', name :'09-102495.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102496.mrxs', name :'09-102496.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102498.mrxs', name :'09-102498.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102499.mrxs', name :'09-102499.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102500.mrxs', name :'09-102500.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102501.mrxs', name :'09-102501.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102502.mrxs', name :'09-102502.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102503.mrxs', name :'09-102503.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102508.mrxs', name :'09-102508.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102509.mrxs', name :'09-102509.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102510.mrxs', name :'09-102510.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102512.mrxs', name :'09-102512.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102513.mrxs', name :'09-102513.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102514.mrxs', name :'09-102514.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102517.mrxs', name :'09-102517.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102518.mrxs', name :'09-102518.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102519.mrxs', name :'09-102519.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102520.mrxs', name :'09-102520.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102522.mrxs', name :'09-102522.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102523.mrxs', name :'09-102523.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102524.mrxs', name :'09-102524.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102528.mrxs', name :'09-102528.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102529.mrxs', name :'09-102529.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102530.mrxs', name :'09-102530.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102531.mrxs', name :'09-102531.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102532.mrxs', name :'09-102532.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102534.mrxs', name :'09-102534.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102535.mrxs', name :'09-102535.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102536.mrxs', name :'09-102536.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102537.mrxs', name :'09-102537.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102538.mrxs', name :'09-102538.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102542.mrxs', name :'09-102542.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102543.mrxs', name :'09-102543.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102544.mrxs', name :'09-102544.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102545.mrxs', name :'09-102545.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102606.mrxs', name :'09-102606.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102607.mrxs', name :'09-102607.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102608.mrxs', name :'09-102608.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102610.mrxs', name :'09-102610.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102611.mrxs', name :'09-102611.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102612.mrxs', name :'09-102612.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102613.mrxs', name :'09-102613.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102614.mrxs', name :'09-102614.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102615.mrxs', name :'09-102615.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102616.mrxs', name :'09-102616.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102617.mrxs', name :'09-102617.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102618.mrxs', name :'09-102618.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102619.mrxs', name :'09-102619.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102621.mrxs', name :'09-102621.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102622.mrxs', name :'09-102622.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102625.mrxs', name :'09-102625.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102666.mrxs', name :'09-102666.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102667.mrxs', name :'09-102667.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102668.mrxs', name :'09-102668.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102669.mrxs', name :'09-102669.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102670.mrxs', name :'09-102670.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102671.mrxs', name :'09-102671.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102672.mrxs', name :'09-102672.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102673.mrxs', name :'09-102673.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102674.mrxs', name :'09-102674.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102675.mrxs', name :'09-102675.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102676.mrxs', name :'09-102676.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102677.mrxs', name :'09-102677.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102678.mrxs', name :'09-102678.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102679.mrxs', name :'09-102679.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102680.mrxs', name :'09-102680.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102682.mrxs', name :'09-102682.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102683.mrxs', name :'09-102683.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102685.mrxs', name :'09-102685.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102686.mrxs', name :'09-102686.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102687.mrxs', name :'09-102687.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102688.mrxs', name :'09-102688.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102689.mrxs', name :'09-102689.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102691.mrxs', name :'09-102691.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102693.mrxs', name :'09-102693.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102694.mrxs', name :'09-102694.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102695.mrxs', name :'09-102695.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102696.mrxs', name :'09-102696.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102697.mrxs', name :'09-102697.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102698.mrxs', name :'09-102698.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102699.mrxs', name :'09-102699.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102700.mrxs', name :'09-102700.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102701.mrxs', name :'09-102701.mrxs', extension :'mrxs', order : 0,study : 'CERVIX']
    ]
    static def CERVIXScans2 = [
            [filename:'/home/stevben/Slides/CERVIX/09-102703.mrxs', name :'09-102703.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102704.mrxs', name :'09-102704.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102706.mrxs', name :'09-102706.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102707.mrxs', name :'09-102707.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102709.mrxs', name :'09-102709.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102710.mrxs', name :'09-102710.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102711.mrxs', name :'09-102711.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102712.mrxs', name :'09-102712.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102713.mrxs', name :'09-102713.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102714.mrxs', name :'09-102714.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102715.mrxs', name :'09-102715.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102716.mrxs', name :'09-102716.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102717.mrxs', name :'09-102717.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102718.mrxs', name :'09-102718.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102719.mrxs', name :'09-102719.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102720.mrxs', name :'09-102720.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102723.mrxs', name :'09-102723.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102725.mrxs', name :'09-102725.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102726.mrxs', name :'09-102726.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102727.mrxs', name :'09-102727.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102728.mrxs', name :'09-102728.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102729.mrxs', name :'09-102729.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102730.mrxs', name :'09-102730.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102732.mrxs', name :'09-102732.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102735.mrxs', name :'09-102735.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102736.mrxs', name :'09-102736.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102737.mrxs', name :'09-102737.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102738.mrxs', name :'09-102738.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102739.mrxs', name :'09-102739.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102740.mrxs', name :'09-102740.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102741.mrxs', name :'09-102741.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102742.mrxs', name :'09-102742.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102744.mrxs', name :'09-102744.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102745.mrxs', name :'09-102745.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102883.mrxs', name :'09-102883.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102884.mrxs', name :'09-102884.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102887.mrxs', name :'09-102887.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-102888.mrxs', name :'09-102888.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-104192.mrxs', name :'09-104192.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-104468.mrxs', name :'09-104468.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105057.mrxs', name :'09-105057.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105059.mrxs', name :'09-105059.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105060.mrxs', name :'09-105060.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105061_15.05.2009_23.05.43.mrxs', name :'09-105061_15.05.2009_23.05.43.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105061.mrxs', name :'09-105061.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105158.mrxs', name :'09-105158.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-105159.mrxs', name :'09-105159.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-106417.mrxs', name :'09-106417.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-106464.mrxs', name :'09-106464.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107596.mrxs', name :'09-107596.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107619.mrxs', name :'09-107619.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107660.mrxs', name :'09-107660.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107840.mrxs', name :'09-107840.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107843.mrxs', name :'09-107843.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107857.mrxs', name :'09-107857.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-107872.mrxs', name :'09-107872.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108108.mrxs', name :'09-108108.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108145.mrxs', name :'09-108145.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108173.mrxs', name :'09-108173.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108265.mrxs', name :'09-108265.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108279.mrxs', name :'09-108279.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108288.mrxs', name :'09-108288.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108294.mrxs', name :'09-108294.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108421.mrxs', name :'09-108421.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108456.mrxs', name :'09-108456.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108460.mrxs', name :'09-108460.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108474.mrxs', name :'09-108474.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108573.mrxs', name :'09-108573.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108762.mrxs', name :'09-108762.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108769.mrxs', name :'09-108769.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108791.mrxs', name :'09-108791.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108849.mrxs', name :'09-108849.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-108950.mrxs', name :'09-108950.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109071.mrxs', name :'09-109071.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109115.mrxs', name :'09-109115.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109137.mrxs', name :'09-109137.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109293.mrxs', name :'09-109293.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109311.mrxs', name :'09-109311.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109371.mrxs', name :'09-109371.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109389.mrxs', name :'09-109389.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109429.mrxs', name :'09-109429.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109527.mrxs', name :'09-109527.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109605.mrxs', name :'09-109605.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109678.mrxs', name :'09-109678.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109687.mrxs', name :'09-109687.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109718.mrxs', name :'09-109718.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-109722.mrxs', name :'09-109722.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110149.mrxs', name :'09-110149.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110319.mrxs', name :'09-110319.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110466.mrxs', name :'09-110466.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110502_20.05.2009_09.59.15.mrxs', name :'09-110502_20.05.2009_09.59.15.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110502.mrxs', name :'09-110502.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110531.mrxs', name :'09-110531.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110756.mrxs', name :'09-110756.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110914.mrxs', name :'09-110914.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110919.mrxs', name :'09-110919.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-110958.mrxs', name :'09-110958.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111028.mrxs', name :'09-111028.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111158.mrxs', name :'09-111158.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111159.mrxs', name :'09-111159.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111299.mrxs', name :'09-111299.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111421.mrxs', name :'09-111421.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111484.mrxs', name :'09-111484.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111490.mrxs', name :'09-111490.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111515.mrxs', name :'09-111515.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111624.mrxs', name :'09-111624.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111706.mrxs', name :'09-111706.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111882.mrxs', name :'09-111882.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111897.mrxs', name :'09-111897.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111930.mrxs', name :'09-111930.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-111942.mrxs', name :'09-111942.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112054.mrxs', name :'09-112054.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112096.mrxs', name :'09-112096.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112119.mrxs', name :'09-112119.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112307.mrxs', name :'09-112307.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112362.mrxs', name :'09-112362.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112392.mrxs', name :'09-112392.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112438.mrxs', name :'09-112438.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112446.mrxs', name :'09-112446.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112576.mrxs', name :'09-112576.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112615.mrxs', name :'09-112615.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-112705.mrxs', name :'09-112705.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113277.mrxs', name :'09-113277.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113292.mrxs', name :'09-113292.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113354.mrxs', name :'09-113354.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113365.mrxs', name :'09-113365.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113549.mrxs', name :'09-113549.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113590.mrxs', name :'09-113590.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113682.mrxs', name :'09-113682.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-113683.mrxs', name :'09-113683.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114094.mrxs', name :'09-114094.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114157.mrxs', name :'09-114157.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114182.mrxs', name :'09-114182.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114358.mrxs', name :'09-114358.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114574.mrxs', name :'09-114574.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-114589.mrxs', name :'09-114589.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115072.mrxs', name :'09-115072.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115253.mrxs', name :'09-115253.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115331.mrxs', name :'09-115331.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115354.mrxs', name :'09-115354.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115524.mrxs', name :'09-115524.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115527.mrxs', name :'09-115527.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115560.mrxs', name :'09-115560.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115658.mrxs', name :'09-115658.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115706.mrxs', name :'09-115706.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115738.mrxs', name :'09-115738.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-115966.mrxs', name :'09-115966.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-116931.mrxs', name :'09-116931.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117078.mrxs', name :'09-117078.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117103.mrxs', name :'09-117103.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117106.mrxs', name :'09-117106.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117109.mrxs', name :'09-117109.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117219.mrxs', name :'09-117219.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117237.mrxs', name :'09-117237.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117299.mrxs', name :'09-117299.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117375.mrxs', name :'09-117375.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117381.mrxs', name :'09-117381.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-117859.mrxs', name :'09-117859.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-118088.mrxs', name :'09-118088.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-118112.mrxs', name :'09-118112.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-118533.mrxs', name :'09-118533.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-118854.mrxs', name :'09-118854.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-118855.mrxs', name :'09-118855.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-119486.mrxs', name :'09-119486.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-119497.mrxs', name :'09-119497.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-120159.mrxs', name :'09-120159.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-120166.mrxs', name :'09-120166.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-123134.mrxs', name :'09-123134.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-125354.mrxs', name :'09-125354.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/09-125358.mrxs', name :'09-125358.mrxs', extension :'mrxs', order : 0,study : 'CERVIX']
    ]

    static def CERVIXScans3 = [
            [filename:'/home/stevben/Slides/CERVIX/10HSIL101309.mrxs', name :'10HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/11HSIL101309.mrxs', name :'11HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1HSIL101309.mrxs', name :'1HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M03_20.05.2009_10.22.27.mrxs', name :'1M03_20.05.2009_10.22.27.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M03_20.05.2009_13.38.20.mrxs', name :'1M03_20.05.2009_13.38.20.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M03.mrxs', name :'1M03.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M05.mrxs', name :'1M05.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M11_21.05.2009_19.17.13.mrxs', name :'1M11_21.05.2009_19.17.13.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M11.mrxs', name :'1M11.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M13.mrxs', name :'1M13.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M15.mrxs', name :'1M15.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M16.mrxs', name :'1M16.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M21.mrxs', name :'1M21.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M24.mrxs', name :'1M24.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M25.mrxs', name :'1M25.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M38.mrxs', name :'1M38.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1M41.mrxs', name :'1M41.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1SCC101309.mrxs', name :'1SCC101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/1SMALL101309.mrxs', name :'1SMALL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/2HSIL101309.mrxs', name :'2HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/2M01.mrxs', name :'2M01.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/2SCC101309.mrxs', name :'2SCC101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/3HSIL101309.mrxs', name :'3HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/3SCC101309.mrxs', name :'3SCC101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-105.mrxs', name :'4-105.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-127.mrxs', name :'4-127.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
//[filename:'/home/stevben/Slides/CERVIX/4-152.mrxs', name :'4-152.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-1681.mrxs', name :'4-1681.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-1860.mrxs', name :'4-1860.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-1895.mrxs', name :'4-1895.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-3914.mrxs', name :'4-3914.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-3985.mrxs', name :'4-3985.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4-4478.mrxs', name :'4-4478.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4HSIL101309.mrxs', name :'4HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/4SCC101309.mrxs', name :'4SCC101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/5HSIL101309.mrxs', name :'5HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/6HSIL101309.mrxs', name :'6HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/7HSIL101309.mrxs', name :'7HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/8HSIL101309.mrxs', name :'8HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/9HSIL101309.mrxs', name :'9HSIL101309.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H10 AIS.mrxs', name :'H10 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H11 AIS.mrxs', name :'H11 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H12 AIS.mrxs', name :'H12 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H13.mrxs', name :'H13.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H14.mrxs', name :'H14.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H15 SCC.mrxs', name :'H15 SCC.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H16 Endcx Adeno.mrxs', name :'H16 Endcx Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H17 Adeno.mrxs', name :'H17 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H18.mrxs', name :'H18.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H19.mrxs', name :'H19.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H1 Enmtl Adeno.mrxs', name :'H1 Enmtl Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H20.mrxs', name :'H20.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H21.mrxs', name :'H21.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H22.mrxs', name :'H22.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H23.mrxs', name :'H23.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H24.mrxs', name :'H24.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H25 AIS.mrxs', name :'H25 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H26 SCC.mrxs', name :'H26 SCC.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H27 SCC.mrxs', name :'H27 SCC.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H28.mrxs', name :'H28.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H29.mrxs', name :'H29.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H2 Adeno.mrxs', name :'H2 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H30.mrxs', name :'H30.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H31 AdenoSq.mrxs', name :'H31 AdenoSq.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H32 AdenoSq.mrxs', name :'H32 AdenoSq.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H33 AdenoSq.mrxs', name :'H33 AdenoSq.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H34 Small Cell.mrxs', name :'H34 Small Cell.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H35 SCC.mrxs', name :'H35 SCC.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H36 CIS.mrxs', name :'H36 CIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H37.mrxs', name :'H37.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H38.mrxs', name :'H38.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H39.mrxs', name :'H39.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H3 Adeno.mrxs', name :'H3 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H40.mrxs', name :'H40.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H41.mrxs', name :'H41.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H42.mrxs', name :'H42.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H43.mrxs', name :'H43.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H44.mrxs', name :'H44.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H45.mrxs', name :'H45.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H46 SCC.mrxs', name :'H46 SCC.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H47 HSIL.mrxs', name :'H47 HSIL.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H48 Adeno_18.08.2009_15.42.04.mrxs', name :'H48 Adeno_18.08.2009_15.42.04.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
//[filename:'/home/stevben/Slides/CERVIX/H48 Adeno.mrxs', name :'H48 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H49.mrxs', name :'H49.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H4 Adeno.mrxs', name :'H4 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H50.mrxs', name :'H50.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H51 small cell.mrxs', name :'H51 small cell.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H52 Adeno.mrxs', name :'H52 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H53.mrxs', name :'H53.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H54.mrxs', name :'H54.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H55.mrxs', name :'H55.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H56 Adeno.mrxs', name :'H56 Adeno.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H57 Adenosq.mrxs', name :'H57 Adenosq.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H58 Adenosq.mrxs', name :'H58 Adenosq.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H5.mrxs', name :'H5.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H6 AIS.mrxs', name :'H6 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H7.mrxs', name :'H7.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H8 AIS.mrxs', name :'H8 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/H9 AIS.mrxs', name :'H9 AIS.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/Herpes 1.mrxs', name :'Herpes 1.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/Herpes 2.mrxs', name :'Herpes 2.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/Herpes 3.mrxs', name :'Herpes 3.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/HPVm.mrxs', name :'HPVm.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/HSILm.mrxs', name :'HSILm.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L01.mrxs', name :'L01.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L02.mrxs', name :'L02.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L03.mrxs', name :'L03.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L04.mrxs', name :'L04.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L05.mrxs', name :'L05.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L06.mrxs', name :'L06.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L07.mrxs', name :'L07.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L08.mrxs', name :'L08.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L09.mrxs', name :'L09.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L10.mrxs', name :'L10.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L11.mrxs', name :'L11.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L12.mrxs', name :'L12.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L13.mrxs', name :'L13.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L14.mrxs', name :'L14.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L15.mrxs', name :'L15.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L16.mrxs', name :'L16.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L17.mrxs', name :'L17.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L18.mrxs', name :'L18.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L19.mrxs', name :'L19.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L20.mrxs', name :'L20.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L21.mrxs', name :'L21.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L22.mrxs', name :'L22.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L23.mrxs', name :'L23.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L24.mrxs', name :'L24.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/L25.mrxs', name :'L25.mrxs', extension :'mrxs', order : 0,study : 'CERVIX']
    ]
    static def CERVIXScans4 = [
            [filename:'/home/stevben/Slides/CERVIX/M-001_25.06.2009_11.24.00.mrxs', name :'M-001_25.06.2009_11.24.00.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-001.mrxs', name :'M-001.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-002_25.06.2009_11.30.22.mrxs', name :'M-002_25.06.2009_11.30.22.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-002.mrxs', name :'M-002.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-003_25.06.2009_11.35.43.mrxs', name :'M-003_25.06.2009_11.35.43.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-003.mrxs', name :'M-003.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-004_25.06.2009_11.41.32.mrxs', name :'M-004_25.06.2009_11.41.32.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-004.mrxs', name :'M-004.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-005_25.06.2009_11.47.24.mrxs', name :'M-005_25.06.2009_11.47.24.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-005.mrxs', name :'M-005.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-006_25.06.2009_11.54.41.mrxs', name :'M-006_25.06.2009_11.54.41.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-006.mrxs', name :'M-006.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-007_25.06.2009_12.01.40.mrxs', name :'M-007_25.06.2009_12.01.40.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-007.mrxs', name :'M-007.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-008_25.06.2009_12.08.35.mrxs', name :'M-008_25.06.2009_12.08.35.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-008.mrxs', name :'M-008.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-009_25.06.2009_12.15.04.mrxs', name :'M-009_25.06.2009_12.15.04.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-009.mrxs', name :'M-009.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-010_25.06.2009_12.21.18.mrxs', name :'M-010_25.06.2009_12.21.18.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-010.mrxs', name :'M-010.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-011_25.06.2009_12.28.06.mrxs', name :'M-011_25.06.2009_12.28.06.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-011.mrxs', name :'M-011.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-012_25.06.2009_12.33.13.mrxs', name :'M-012_25.06.2009_12.33.13.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-012.mrxs', name :'M-012.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-013_25.06.2009_12.38.45.mrxs', name :'M-013_25.06.2009_12.38.45.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-013.mrxs', name :'M-013.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-014_25.06.2009_12.44.33.mrxs', name :'M-014_25.06.2009_12.44.33.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-014.mrxs', name :'M-014.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-015_25.06.2009_12.49.52.mrxs', name :'M-015_25.06.2009_12.49.52.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-015.mrxs', name :'M-015.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-016_25.06.2009_12.54.48.mrxs', name :'M-016_25.06.2009_12.54.48.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-016.mrxs', name :'M-016.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-017_25.06.2009_12.59.57.mrxs', name :'M-017_25.06.2009_12.59.57.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-017.mrxs', name :'M-017.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-018_25.06.2009_13.05.07.mrxs', name :'M-018_25.06.2009_13.05.07.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-018.mrxs', name :'M-018.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-019.mrxs', name :'M-019.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-020_25.06.2009_13.10.56.mrxs', name :'M-020_25.06.2009_13.10.56.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-020.mrxs', name :'M-020.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-021_25.06.2009_13.16.10.mrxs', name :'M-021_25.06.2009_13.16.10.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-021.mrxs', name :'M-021.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-022_25.06.2009_13.21.40.mrxs', name :'M-022_25.06.2009_13.21.40.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-022.mrxs', name :'M-022.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-023_25.06.2009_13.27.40.mrxs', name :'M-023_25.06.2009_13.27.40.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-023.mrxs', name :'M-023.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-024_25.06.2009_13.33.21.mrxs', name :'M-024_25.06.2009_13.33.21.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-024.mrxs', name :'M-024.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-025_25.06.2009_13.39.28.mrxs', name :'M-025_25.06.2009_13.39.28.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-025.mrxs', name :'M-025.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-026_25.06.2009_13.45.05.mrxs', name :'M-026_25.06.2009_13.45.05.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-026.mrxs', name :'M-026.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-027_25.06.2009_13.51.32.mrxs', name :'M-027_25.06.2009_13.51.32.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-027.mrxs', name :'M-027.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-028_25.06.2009_13.57.49.mrxs', name :'M-028_25.06.2009_13.57.49.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-028.mrxs', name :'M-028.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-029_25.06.2009_14.03.26.mrxs', name :'M-029_25.06.2009_14.03.26.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-029.mrxs', name :'M-029.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-030_25.06.2009_14.10.31.mrxs', name :'M-030_25.06.2009_14.10.31.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-030.mrxs', name :'M-030.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-031_25.06.2009_14.16.46.mrxs', name :'M-031_25.06.2009_14.16.46.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-031.mrxs', name :'M-031.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-032_25.06.2009_14.22.48.mrxs', name :'M-032_25.06.2009_14.22.48.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-032.mrxs', name :'M-032.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-033_25.06.2009_14.27.55.mrxs', name :'M-033_25.06.2009_14.27.55.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-033.mrxs', name :'M-033.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-034_25.06.2009_14.33.06.mrxs', name :'M-034_25.06.2009_14.33.06.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-034.mrxs', name :'M-034.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-035_25.06.2009_14.38.04.mrxs', name :'M-035_25.06.2009_14.38.04.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-035.mrxs', name :'M-035.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-036_25.06.2009_14.42.51.mrxs', name :'M-036_25.06.2009_14.42.51.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-036.mrxs', name :'M-036.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-037_25.06.2009_14.48.00.mrxs', name :'M-037_25.06.2009_14.48.00.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-037.mrxs', name :'M-037.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-038_25.06.2009_14.54.03.mrxs', name :'M-038_25.06.2009_14.54.03.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-038.mrxs', name :'M-038.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-039_25.06.2009_15.00.47.mrxs', name :'M-039_25.06.2009_15.00.47.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-039.mrxs', name :'M-039.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-040_25.06.2009_15.06.07.mrxs', name :'M-040_25.06.2009_15.06.07.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-040.mrxs', name :'M-040.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-041_25.06.2009_15.10.51.mrxs', name :'M-041_25.06.2009_15.10.51.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-041.mrxs', name :'M-041.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-042_25.06.2009_15.17.47.mrxs', name :'M-042_25.06.2009_15.17.47.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-042.mrxs', name :'M-042.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-043_25.06.2009_15.22.55.mrxs', name :'M-043_25.06.2009_15.22.55.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-043.mrxs', name :'M-043.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-044_25.06.2009_15.27.22.mrxs', name :'M-044_25.06.2009_15.27.22.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-044.mrxs', name :'M-044.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-045_25.06.2009_15.33.37.mrxs', name :'M-045_25.06.2009_15.33.37.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-045.mrxs', name :'M-045.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-046_25.06.2009_15.38.32.mrxs', name :'M-046_25.06.2009_15.38.32.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-046.mrxs', name :'M-046.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-047_25.06.2009_15.44.48.mrxs', name :'M-047_25.06.2009_15.44.48.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-047.mrxs', name :'M-047.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-048_25.06.2009_15.52.23.mrxs', name :'M-048_25.06.2009_15.52.23.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-048.mrxs', name :'M-048.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-049_25.06.2009_15.57.34.mrxs', name :'M-049_25.06.2009_15.57.34.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-049.mrxs', name :'M-049.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-050_25.06.2009_16.04.21.mrxs', name :'M-050_25.06.2009_16.04.21.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-050.mrxs', name :'M-050.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-051.mrxs', name :'M-051.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-052.mrxs', name :'M-052.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/M-053.mrxs', name :'M-053.mrxs', extension :'mrxs', order : 0,study : 'CERVIX']
    ]
    static def CERVIXScans5 = [
            [filename:'/home/stevben/Slides/CERVIX/R201.mrxs', name :'R201.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R212.mrxs', name :'R212.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R214.mrxs', name :'R214.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R215.mrxs', name :'R215.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R224.mrxs', name :'R224.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R235.mrxs', name :'R235.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R236.mrxs', name :'R236.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R237.mrxs', name :'R237.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R238.mrxs', name :'R238.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R240.mrxs', name :'R240.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R241.mrxs', name :'R241.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R243.mrxs', name :'R243.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R246.mrxs', name :'R246.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R247.mrxs', name :'R247.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R248.mrxs', name :'R248.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R249.mrxs', name :'R249.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R250.mrxs', name :'R250.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R253.mrxs', name :'R253.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R259.mrxs', name :'R259.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R260.mrxs', name :'R260.mrxs', extension :'mrxs', order : 0,study : 'CERVIX'],
            [filename:'/home/stevben/Slides/CERVIX/R261.mrxs', name :'R261.mrxs', extension :'mrxs', order : 0,study : 'CERVIX']
    ]


    static def PhillipsScans = [
            [filename:'/home/stevben/Slides/Philips/03258b99-4d38-4ca6-ba38-8dc4bf366482.isyntax.tiff.vips.tiff', name :'03258b99-4d38-4ca6-ba38-8dc4bf366482', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/03a7e50b-71a5-4998-9691-15bda142ee7f.isyntax.tiff.vips.tiff', name :'03a7e50b-71a5-4998-9691-15bda142ee7f', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/2c9958fe-a258-4cdf-a631-465840b275c7.isyntax.tiff.vips.tiff', name :'2c9958fe-a258-4cdf-a631-465840b275c7', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/2e5a4b75-65b3-4699-89dc-0a8756734507.isyntax.tiff.vips.tiff', name :'2e5a4b75-65b3-4699-89dc-0a8756734507', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/3edd7269-0e07-4151-b3f1-e5349dd27b30.isyntax.tiff.vips.tiff', name :'3edd7269-0e07-4151-b3f1-e5349dd27b30', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/431a1752-e139-4500-afa2-0823a86fbcb5.isyntax.tiff.vips.tiff', name :'431a1752-e139-4500-afa2-0823a86fbcb5', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/74bcee60-d7c2-43fe-a6a8-b53899babdbd.isyntax.tiff.vips.tiff', name :'74bcee60-d7c2-43fe-a6a8-b53899babdbd', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/75cc5e5a-966a-453f-93af-2a6b36091c06.isyntax.tiff.vips.tiff', name :'75cc5e5a-966a-453f-93af-2a6b36091c06', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/86909191-bf5e-4362-88b0-84d22a99f7a5.isyntax.tiff.vips.tiff', name :'86909191-bf5e-4362-88b0-84d22a99f7a5', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/92ad8cef-df28-41d1-9f25-aed464154153.isyntax.tiff.vips.tiff', name :'92ad8cef-df28-41d1-9f25-aed464154153', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/99969963-85b9-4f9a-beff-a4348e2bf704.isyntax.tiff.vips.tiff', name :'99969963-85b9-4f9a-beff-a4348e2bf704', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/a3e98e41-994d-4b15-90a6-2875289c25b6.isyntax.tiff.vips.tiff', name :'a3e98e41-994d-4b15-90a6-2875289c25b6', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/a4a71727-ed86-4350-aa06-80601843f334.isyntax.tiff.vips.tiff', name :'a4a71727-ed86-4350-aa06-80601843f334', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/a84d8dc8-d291-418d-a9f2-9c5ca99e30df.isyntax.tiff.vips.tiff', name :'a84d8dc8-d291-418d-a9f2-9c5ca99e30df', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/afa9be2c-26e8-4020-a8e4-6672894f7bfb.isyntax.tiff.vips.tiff', name :'afa9be2c-26e8-4020-a8e4-6672894f7bfb', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/ca60d7dd-bdcc-4ef2-a2b2-bcbbb5b2ff7c.isyntax.tiff.vips.tiff', name :'ca60d7dd-bdcc-4ef2-a2b2-bcbbb5b2ff7c', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/da92415a-9001-49d5-8d03-7832e4d2c8af.isyntax.tiff.vips.tiff', name :'da92415a-9001-49d5-8d03-7832e4d2c8af', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/ea7c15b1-c56c-4963-bfe1-b62f974ed050.isyntax.tiff.vips.tiff', name :'ea7c15b1-c56c-4963-bfe1-b62f974ed050', extension :'tiff', order : 0,study : 'PHILIPS'],
            [filename:'/home/stevben/Slides/Philips/face3d70-d2f7-453a-8ea9-8baab569d02a.isyntax.tiff.vips.tiff', name :'face3d70-d2f7-453a-8ea9-8baab569d02a', extension :'tiff', order : 0,study : 'PHILIPS']
    ]
}


