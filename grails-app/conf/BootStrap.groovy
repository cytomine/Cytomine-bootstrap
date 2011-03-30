import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.image.Image
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.security.Group
import be.cytomine.security.UserGroup
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.project.Slide
import be.cytomine.project.ProjectSlide
import be.cytomine.ontology.Annotation
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Polygon
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Term
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
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

class BootStrap {
  def springSecurityService
  def sequenceService
  def marshallersService
  def grailsApplication
  def messageSource

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

    List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
    for(int i =0;i<inputArgs.size();i++)
    {
      println inputArgs.get(i)
    }



    log.info "add data"
    println """add Data"""
    /* Groups */
    def groupsSamples = [
            [name : "GIGA"],
            [name : "LBTD"] ,
            [name : "ANAPATH"]
    ]
    createGroups(groupsSamples)


    /* Users */
    def usersSamples = [
            [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group : [[ name :"GIGA"]], password : 'password'],
            [username : 'lrollus', firstname : 'Loic', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[ name :"GIGA"]], password : 'password'],
            [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[ name :"GIGA"], [name : "ANAPATH"]], password : 'password'] ,
            [username : 'demo', firstname : 'Jean', lastname : 'Dupont', email : 'mymail@ulg.ac.be', group : [[ name :"GIGA"], [name : "ANAPATH"]], password : 'demodemo']
    ]
    createUsers(usersSamples)


    /* Scanners */
    def scannersSamples = [
            [brand : "gigascan", model : "MODEL1"]
    ]
    createScanners(scannersSamples)


    /* MIME Types */
    def mimeSamples = [
            [extension : "jp2", mimeType : "image/jp2"],
            [extension : "tif", mimeType : "image/tiff"],
            [extension : "gdal", mimeType : "gdalType"],
    ]
    createMimes(mimeSamples)


    /* Image Server */
    def imageServerSamples =  [
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is1.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is2.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is3.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is4.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is5.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is6.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is7.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is8.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is9.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is10.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver',
                    'extension' : 'jp2'
            ],
            [
                    'name' : 'GDAL',
                    'url' : 'http://localhost/~stevben',
                    'service' : '/gdal',
                    'className' : 'GDALResolver',
                    'extension' : 'gdal'
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
            [name: "Ontology1"],
            [name: "Ontology2"],
            [name: "Ontology3"]
    ]
    createOntology(ontologySamples)

    /* Projects */
    def projectSamples = [
            [name : "GIGA-DEV",  groups : [[ name :"GIGA"]],ontology: "Ontology1"],
            [name : "GIGA-DEV2",  groups : [[ name :"GIGA"]],ontology: "Ontology2"]
            // [name : "NEO13", groups : [[ name :"GIGA"]]],
            // [name : "NEO4",  groups : [[ name :"GIGA"]]]

    ]

    createProjects(projectSamples)

    /* Slides */
    def slideSamples = [
            [name : "testslide", order : 8, projects : [[name : "GIGA-DEV"]]],
            [name : "testslide2", order : 8, projects : [[name : "GIGA-DEV2"]]]
    ]
    def slides = createSlides(slideSamples)


    /* Scans */
    def scanSamples = [
            [filename: 'ImageNEO13_CNS_5.10_5_4_01',path:'ImageNEO13_CNS_5.10_5_4_01', extension : 'gdal', slide : 0],
            [filename: 'Boyden - essai _10x_02',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2',slide : 0],
            [filename: 'Aperio - 003',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2',slide : 0 ],
            [filename: 'Aperio - 2005900969-2', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2',slide : 0 ],
            [filename: 'bottom-nocompression', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/bottom-nocompression-crop-8levels-256.jp2',slide : 0 ],
            [filename: '70pc_cropnew', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/PhDelvenne/2_02_JPEG_70pc_cropnew.jp2',slide : 0 ],
            [filename: 'Agar seul 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-1.jp2',slide : 0 ],
            [filename: 'Agar seul 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-2.jp2',slide : 0 ],
            [filename: 'Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-1.jp2',slide : 0 ],
            [filename: 'Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-2.jp2',slide : 0 ],
            [filename: 'Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-3.jp2',slide : 0 ],
            [filename: 'Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-4.jp2',slide : 0 ],
            [filename: 'Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-5.jp2',slide : 0 ],
            [filename: 'Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-6.jp2',slide : 0 ],
            [filename: 'Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-7.jp2',slide : 0 ],
            [filename: 'Curcu non soluble 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-1.jp2',slide : 0 ],
            [filename: 'Curcu non soluble 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-2.jp2',slide : 0 ],
            [filename: 'Curcu non soluble 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-3.jp2',slide : 0 ],
            [filename: 'Curcu non soluble 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-4.jp2',slide : 0 ],
            [filename: 'Curcu non soluble 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-5.jp2',slide : 0 ],
            [filename: 'Gemzar 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-1.jp2',slide : 0 ],
            [filename: 'Gemzar 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-2.jp2',slide : 0 ],
            [filename: 'Gemzar 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-3.jp2',slide : 0 ],
            [filename: 'Gemzar 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-4.jp2',slide : 0 ],
            [filename: 'Gemzar 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-5.jp2',slide : 0 ],
            [filename: 'Gemzar 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-6.jp2',slide : 0 ],
            [filename: 'Gemzar 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-7.jp2',slide : 0 ],
            [filename: 'Gemzar 8', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-8.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-1.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-2.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-3.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-4.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-5.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-6.jp2',slide : 0 ],
            [filename: 'Gemzar + Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-7.jp2',slide : 0 ],
            [filename: 'HPg 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-1.jp2',slide :1 ],
            [filename: 'HPg 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-3.jp2',slide :1 ],
            [filename: 'HPg 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-4.jp2',slide :1 ],
            [filename: 'HPg 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-5.jp2',slide :1 ],
            [filename: 'HPg 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-6.jp2',slide :1 ],
            [filename: 'HPg 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-7.jp2',slide :1 ]
    ]
    /*//reduce data set for test
    switch(GrailsUtil.environment) {
        case "test":
          for(int i=scanSamples.size()-1;i>4;i--)
            scanSamples.remove(i)
        break
    }*/
    createScans(scanSamples, slides)






    def LBTDScans = [
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/ImageNEO13_CNS_5.10_5_4_01.tif.jp2',name:'ImageNEO13_CNS_5.10_5_4_01.tif.jp2',slidename:'ImageNEO13_CNS',order:10,study:'NEO13'],
            /*          [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/ImageNEO13_CNS_5.1_5_3_01.tif.jp2',name:'ImageNEO13_CNS_5.1_5_3_01.tif.jp2',slidename:'ImageNEO13_CNS',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/ImageNEO13_CNS_5.20_5_5_01.tif.jp2',name:'ImageNEO13_CNS_5.20_5_5_01.tif.jp2',slidename:'ImageNEO13_CNS',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.10_5_1_01.tif.jp2',name:'NEO13_CNS_1.10_5_1_01.tif.jp2',slidename:'NEO13_CNS_1',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.1_4_10_01.tif.jp2',name:'NEO13_CNS_1.1_4_10_01.tif.jp2',slidename:'NEO13_CNS_1',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.20_5_2_01.tif.jp2',name:'NEO13_CNS_1.20_5_2_01.tif.jp2',slidename:'NEO13_CNS_1',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.30_5_3_01.tif.jp2',name:'NEO13_CNS_1.30_5_3_01.tif.jp2',slidename:'NEO13_CNS_1',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.40_5_4_01.tif.jp2',name:'NEO13_CNS_1.40_5_4_01.tif.jp2',slidename:'NEO13_CNS_1',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.50_5_5_01.tif.jp2',name:'NEO13_CNS_1.50_5_5_01.tif.jp2',slidename:'NEO13_CNS_1',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.60_5_6_01.tif.jp2',name:'NEO13_CNS_1.60_5_6_01.tif.jp2',slidename:'NEO13_CNS_1',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_1.70_5_7_01.tif.jp2',name:'NEO13_CNS_1.70_5_7_01.tif.jp2',slidename:'NEO13_CNS_1',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.10_4_3_01.tif.jp2',name:'NEO13_CNS_2.10_4_3_01.tif.jp2',slidename:'NEO13_CNS_2',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.1_4_2_01.tif.jp2',name:'NEO13_CNS_2.1_4_2_01.tif.jp2',slidename:'NEO13_CNS_2',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.20_4_4_01.tif.jp2',name:'NEO13_CNS_2.20_4_4_01.tif.jp2',slidename:'NEO13_CNS_2',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.30_4_5_01.tif.jp2',name:'NEO13_CNS_2.30_4_5_01.tif.jp2',slidename:'NEO13_CNS_2',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.40_4_6_01.tif.jp2',name:'NEO13_CNS_2.40_4_6_01.tif.jp2',slidename:'NEO13_CNS_2',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.50_4_7_01.tif.jp2',name:'NEO13_CNS_2.50_4_7_01.tif.jp2',slidename:'NEO13_CNS_2',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.60_4_8_01.tif.jp2',name:'NEO13_CNS_2.60_4_8_01.tif.jp2',slidename:'NEO13_CNS_2',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_2.70_4_9_01.tif.jp2',name:'NEO13_CNS_2.70_4_9_01.tif.jp2',slidename:'NEO13_CNS_2',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.10_3_5_01.tif.jp2',name:'NEO13_CNS_3.10_3_5_01.tif.jp2',slidename:'NEO13_CNS_3',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.1_3_4_01.tif.jp2',name:'NEO13_CNS_3.1_3_4_01.tif.jp2',slidename:'NEO13_CNS_3',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.20_3_6_01.tif.jp2',name:'NEO13_CNS_3.20_3_6_01.tif.jp2',slidename:'NEO13_CNS_3',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.30_3_7_01.tif.jp2',name:'NEO13_CNS_3.30_3_7_01.tif.jp2',slidename:'NEO13_CNS_3',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.40_3_8_01.tif.jp2',name:'NEO13_CNS_3.40_3_8_01.tif.jp2',slidename:'NEO13_CNS_3',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.50_3_9_01.tif.jp2',name:'NEO13_CNS_3.50_3_9_01.tif.jp2',slidename:'NEO13_CNS_3',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.60_3_10_01.tif.jp2',name:'NEO13_CNS_3.60_3_10_01.tif.jp2',slidename:'NEO13_CNS_3',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_3.70_4_1_01.tif.jp2',name:'NEO13_CNS_3.70_4_1_01.tif.jp2',slidename:'NEO13_CNS_3',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.10_2_7_01.tif.jp2',name:'NEO13_CNS_4.10_2_7_01.tif.jp2',slidename:'NEO13_CNS_4',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.1_2_6_01.tif.jp2',name:'NEO13_CNS_4.1_2_6_01.tif.jp2',slidename:'NEO13_CNS_4',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.20_2_8_01.tif.jp2',name:'NEO13_CNS_4.20_2_8_01.tif.jp2',slidename:'NEO13_CNS_4',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.30_2_9_01.tif.jp2',name:'NEO13_CNS_4.30_2_9_01.tif.jp2',slidename:'NEO13_CNS_4',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.40_2_10_01.tif.jp2',name:'NEO13_CNS_4.40_2_10_01.tif.jp2',slidename:'NEO13_CNS_4',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.50_3_1_01.tif.jp2',name:'NEO13_CNS_4.50_3_1_01.tif.jp2',slidename:'NEO13_CNS_4',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.60_3_2_01.tif.jp2',name:'NEO13_CNS_4.60_3_2_01.tif.jp2',slidename:'NEO13_CNS_4',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_4.70_3_3_01.tif.jp2',name:'NEO13_CNS_4.70_3_3_01.tif.jp2',slidename:'NEO13_CNS_4',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_5.30_1_3_01.tif.jp2',name:'NEO13_CNS_5.30_1_3_01.tif.jp2',slidename:'NEO13_CNS_5',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_5.40_1_4_01.tif.jp2',name:'NEO13_CNS_5.40_1_4_01.tif.jp2',slidename:'NEO13_CNS_5',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_5.50_1_5_01.tif.jp2',name:'NEO13_CNS_5.50_1_5_01.tif.jp2',slidename:'NEO13_CNS_5',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_5.60_1_6_01.tif.jp2',name:'NEO13_CNS_5.60_1_6_01.tif.jp2',slidename:'NEO13_CNS_5',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_5.70_1_7_01.tif.jp2',name:'NEO13_CNS_5.70_1_7_01.tif.jp2',slidename:'NEO13_CNS_5',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.10_1_9_01.tif.jp2',name:'NEO13_CNS_6.10_1_9_01.tif.jp2',slidename:'NEO13_CNS_6',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.1_1_8_01.tif.jp2',name:'NEO13_CNS_6.1_1_8_01.tif.jp2',slidename:'NEO13_CNS_6',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.20_1_10_01.tif.jp2',name:'NEO13_CNS_6.20_1_10_01.tif.jp2',slidename:'NEO13_CNS_6',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.30_2_1_01.tif.jp2',name:'NEO13_CNS_6.30_2_1_01.tif.jp2',slidename:'NEO13_CNS_6',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.40_2_2_01.tif.jp2',name:'NEO13_CNS_6.40_2_2_01.tif.jp2',slidename:'NEO13_CNS_6',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.50_2_3_01.tif.jp2',name:'NEO13_CNS_6.50_2_3_01.tif.jp2',slidename:'NEO13_CNS_6',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.60_2_4_01.tif.jp2',name:'NEO13_CNS_6.60_2_4_01.tif.jp2',slidename:'NEO13_CNS_6',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_CNS/converti/jpg/NEO13_CNS_6.70_2_5_01.tif.jp2',name:'NEO13_CNS_6.70_2_5_01.tif.jp2',slidename:'NEO13_CNS_6',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_1.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_1.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_1',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_2.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_2.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_2',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_3.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_3.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_3',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_4.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_4.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_4',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_5.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_5.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_5',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.10_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.10_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.1_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.1_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.20_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.20_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.30_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.30_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.40_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.40_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.50_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.50_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.60_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.60_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_90/converti/jpg/NEO_13_Curcu_90pc_6.70_01.tif.jp2',name:'NEO_13_Curcu_90pc_6.70_01.tif.jp2',slidename:'NEO_13_Curcu_90pc_6',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.10_2_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.10_2_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.1_1_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.1_1_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.20_3_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.20_3_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.30_4_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.30_4_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.40_5_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.40_5_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.50_6_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.50_6_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.60_7_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.60_7_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_1.70_8_01.tif.jp2',name:'NEO_13_Curcu_93pc_1.70_8_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_1',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.10_10_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.10_10_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.1_9_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.1_9_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.20_1_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.20_1_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.30_2_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.30_2_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.40_3_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.40_3_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.50_4_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.50_4_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.60_5_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.60_5_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_2.70_6_01.tif.jp2',name:'NEO_13_Curcu_93pc_2.70_6_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_2',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.10_8_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.10_8_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.1_7_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.1_7_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.20_9_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.20_9_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.30_10_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.30_10_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.40_1_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.40_1_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO13_CURCU_93pc_3.50_1_1_01.tif.jp2',name:'NEO13_CURCU_93pc_3.50_1_1_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_3.60_3_01.tif.jp2',name:'NEO_13_Curcu_93pc_3.60_3_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO13_CURCU_93pc_3.70_1_2_01.tif.jp2',name:'NEO13_CURCU_93pc_3.70_1_2_01.tif.jp2',slidename:'NEO_13_Curcu_93pc_3',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.10.tif.jp2',name:'NEO_13_Curcu_93pc_4.10.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.1.tif.jp2',name:'NEO_13_Curcu_93pc_4.1.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.20.tif.jp2',name:'NEO_13_Curcu_93pc_4.20.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.30.tif.jp2',name:'NEO_13_Curcu_93pc_4.30.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.40.tif.jp2',name:'NEO_13_Curcu_93pc_4.40.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.50.tif.jp2',name:'NEO_13_Curcu_93pc_4.50.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.60.tif.jp2',name:'NEO_13_Curcu_93pc_4.60.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_4.70.tif.jp2',name:'NEO_13_Curcu_93pc_4.70.tif.jp2',slidename:'NEO_13_Curcu_93pc_4',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.10.tif.jp2',name:'NEO_13_Curcu_93pc_5.10.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.1.tif.jp2',name:'NEO_13_Curcu_93pc_5.1.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.20.tif.jp2',name:'NEO_13_Curcu_93pc_5.20.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.30.tif.jp2',name:'NEO_13_Curcu_93pc_5.30.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.40.tif.jp2',name:'NEO_13_Curcu_93pc_5.40.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.50.tif.jp2',name:'NEO_13_Curcu_93pc_5.50.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.60.tif.jp2',name:'NEO_13_Curcu_93pc_5.60.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_5.70.tif.jp2',name:'NEO_13_Curcu_93pc_5.70.tif.jp2',slidename:'NEO_13_Curcu_93pc_5',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.10.tif.jp2',name:'NEO_13_Curcu_93pc_6.10.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.1.tif.jp2',name:'NEO_13_Curcu_93pc_6.1.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.20.tif.jp2',name:'NEO_13_Curcu_93pc_6.20.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.30.tif.jp2',name:'NEO_13_Curcu_93pc_6.30.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.40.tif.jp2',name:'NEO_13_Curcu_93pc_6.40.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.50.tif.jp2',name:'NEO_13_Curcu_93pc_6.50.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.60.tif.jp2',name:'NEO_13_Curcu_93pc_6.60.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_93/converti/jpg/NEO_13_Curcu_93pc_6.70.tif.jp2',name:'NEO_13_Curcu_93pc_6.70.tif.jp2',slidename:'NEO_13_Curcu_93pc_6',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.20_1_1_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.20_1_1_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:20,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.30_1_2_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.30_1_2_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:30,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.40_1_3_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.40_1_3_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:40,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.50_1_4_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.50_1_4_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:50,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.60_1_5_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.60_1_5_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:60,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_2.70_1_6_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_2.70_1_6_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_2',order:70,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.10_1_8_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.10_1_8_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:10,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.1_1_7_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.1_1_7_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:1,study:'NEO13'		],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.20_1_9_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.20_1_9_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:20,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.30_1_10_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.30_1_10_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.40_2_1_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.40_2_1_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:40,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.50_2_2_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.50_2_2_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:50,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.60_2_3_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.60_2_3_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:60,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_3.70_2_4_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_3.70_2_4_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_3',order:70,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.10_2_6_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.10_2_6_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:10,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.1_2_5_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.1_2_5_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4'  ,order:1,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.20_2_7_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.20_2_7_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:20,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.30_2_8_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.30_2_8_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:30,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.40_2_9_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.40_2_9_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:40,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.50_2_10_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.50_2_10_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.60_3_1_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.60_3_1_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:60,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_4.70_3_2_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_4.70_3_2_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_4',order:70,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.10_3_4_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.10_3_4_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:10,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.1_3_3_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.1_3_3_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5'  ,order:1,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.20_3_5_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.20_3_5_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:20,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.30_3_6_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.30_3_6_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:30,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.40_3_7_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.40_3_7_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:40,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.50_3_8_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.50_3_8_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:50,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_5.70_3_10_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_5.70_3_10_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_5',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.10_4_2_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.10_4_2_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6,',order:10,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.1_4_1_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.1_4_1_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:1,study:'NEO13'		],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.20_4_3_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.20_4_3_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:20,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.30_4_4_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.30_4_4_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:30,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.40_4_5_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.40_4_5_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:40,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.50_4_6_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.50_4_6_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:50,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.60_4_7_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.60_4_7_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:60,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/ImageNEO_13_Curcu_99pc_6.70_4_8_01.tif.jp2',name:'ImageNEO_13_Curcu_99pc_6.70_4_8_01.tif.jp2',slidename:'ImageNEO_13_Curcu_99pc_6',order:70,study:'NEO13'	],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/NEO_13_Curcu_99pc_2.10.tif.jp2',name:'NEO_13_Curcu_99pc_2.10.tif.jp2',slidename:'NEO_13_Curcu_99pc_2',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_curcu_99/converti/jpg/NEO_13_Curcu_99pc_2.1.tif.jp2',name:'NEO_13_Curcu_99pc_2.1.tif.jp2',slidename:'NEO_13_Curcu_99pc_2',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO13_HPg_1.10_5_9_01.tif.jp2',name:'NEO13_HPg_1.10_5_9_01.tif.jp2',slidename:'NEO13_HPg_1',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO13_HPg_1.1_5_8_01.tif.jp2',name:'NEO13_HPg_1.1_5_8_01.tif.jp2',slidename:'NEO13_HPg_1',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO13_HPg_1.20_5_10_01.tif.jp2',name:'NEO13_HPg_1.20_5_10_01.tif.jp2',slidename:'NEO13_HPg_1',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_1.30__HE01.tif.jp2',name:'NEO_13_HPg_Gav_1.30__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_1',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_1.40__HE01.tif.jp2',name:'NEO_13_HPg_Gav_1.40__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_1',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_1.50__HE01.tif.jp2',name:'NEO_13_HPg_Gav_1.50__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_1',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_1.60__HE01.tif.jp2',name:'NEO_13_HPg_Gav_1.60__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_1',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_1.70__HE01.tif.jp2',name:'NEO_13_HPg_Gav_1.70__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_1',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.10__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.10__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.1__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.1__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.20__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.20__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.30__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.30__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.40__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.40__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.50__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.50__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.60__HE01.tif.jp2',name:'NEO_13_HPg_Gav_2.60__HE01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_2.70__01.tif.jp2',name:'NEO_13_HPg_Gav_2.70__01.tif.jp2',slidename:'NEO_13_HPg_Gav_2',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.10__01.tif.jp2',name:'NEO_13_HPg_Gav_3.10__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.1__01.tif.jp2',name:'NEO_13_HPg_Gav_3.1__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.20__01.tif.jp2',name:'NEO_13_HPg_Gav_3.20__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.30__01.tif.jp2',name:'NEO_13_HPg_Gav_3.30__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.40__01.tif.jp2',name:'NEO_13_HPg_Gav_3.40__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.50__01.tif.jp2',name:'NEO_13_HPg_Gav_3.50__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.60__01.tif.jp2',name:'NEO_13_HPg_Gav_3.60__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_3.70__01.tif.jp2',name:'NEO_13_HPg_Gav_3.70__01.tif.jp2',slidename:'NEO_13_HPg_Gav_3',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.10__01.tif.jp2',name:'NEO_13_HPg_Gav_4.10__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.1__01.tif.jp2',name:'NEO_13_HPg_Gav_4.1__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.20__01.tif.jp2',name:'NEO_13_HPg_Gav_4.20__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.30__01.tif.jp2',name:'NEO_13_HPg_Gav_4.30__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.40__01.tif.jp2',name:'NEO_13_HPg_Gav_4.40__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.50__01.tif.jp2',name:'NEO_13_HPg_Gav_4.50__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.60__01.tif.jp2',name:'NEO_13_HPg_Gav_4.60__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_4.70__01.tif.jp2',name:'NEO_13_HPg_Gav_4.70__01.tif.jp2',slidename:'NEO_13_HPg_Gav_4',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.10__01.tif.jp2',name:'NEO_13_HPg_Gav_5.10__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:10,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.1__01.tif.jp2',name:'NEO_13_HPg_Gav_5.1__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:1,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.20__01.tif.jp2',name:'NEO_13_HPg_Gav_5.20__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:20,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.30__01.tif.jp2',name:'NEO_13_HPg_Gav_5.30__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:30,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.40__01.tif.jp2',name:'NEO_13_HPg_Gav_5.40__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:40,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.50__01.tif.jp2',name:'NEO_13_HPg_Gav_5.50__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:50,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.60__01.tif.jp2',name:'NEO_13_HPg_Gav_5.60__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:60,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO13/grp_HPg/converti/jpg/NEO_13_HPg_Gav_5.70__01.tif.jp2',name:'NEO_13_HPg_Gav_5.70__01.tif.jp2',slidename:'NEO_13_HPg_Gav_5',order:70,study:'NEO13'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.10_3_2_01.tif.jp2',name:'NEO_4_Curcu_INH_1.10_3_2_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.1_3_1_01.tif.jp2',name:'NEO_4_Curcu_INH_1.1_3_1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.20_3_3_01.tif.jp2',name:'NEO_4_Curcu_INH_1.20_3_3_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.30_3_4_01.tif.jp2',name:'NEO_4_Curcu_INH_1.30_3_4_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.40_3_5_01.tif.jp2',name:'NEO_4_Curcu_INH_1.40_3_5_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.50_3_6_01.tif.jp2',name:'NEO_4_Curcu_INH_1.50_3_6_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.60_3_7_01.tif.jp2',name:'NEO_4_Curcu_INH_1.60_3_7_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_1.70_3_8_01.tif.jp2',name:'NEO_4_Curcu_INH_1.70_3_8_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_2.10_3_10_01.tif.jp2',name:'NEO_4_Curcu_INH_2.10_3_10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_2.1_3_9_01.tif.jp2',name:'NEO_4_Curcu_INH_2.1_3_9_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_2.20_4_1_01.tif.jp2',name:'NEO_4_Curcu_INH_2.20_4_1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_2.30_4_2_01.tif.jp2',name:'NEO_4_Curcu_INH_2.30_4_2_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO_4_Curcu_INH_2.40_1_1_01HE.tif.jp2',name:'NEO_4_Curcu_INH_2.40_1_1_01HE.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_2.50_01.tif.jp2',name:'NEO4_CURCU_INH_2.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_2.60_01.tif.jp2',name:'NEO4_CURCU_INH_2.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_2.70_01.tif.jp2',name:'NEO4_CURCU_INH_2.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_2',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.10_01.tif.jp2',name:'NEO4_CURCU_INH_3.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.1_01.tif.jp2',name:'NEO4_CURCU_INH_3.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.20_01.tif.jp2',name:'NEO4_CURCU_INH_3.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.30_01.tif.jp2',name:'NEO4_CURCU_INH_3.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.40_01.tif.jp2',name:'NEO4_CURCU_INH_3.40_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.50_01.tif.jp2',name:'NEO4_CURCU_INH_3.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.60_01.tif.jp2',name:'NEO4_CURCU_INH_3.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_3.70_01.tif.jp2',name:'NEO4_CURCU_INH_3.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_3',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.10_01.tif.jp2',name:'NEO4_CURCU_INH_4.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.1_01.tif.jp2',name:'NEO4_CURCU_INH_4.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.20_01.tif.jp2',name:'NEO4_CURCU_INH_4.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.30_01.tif.jp2',name:'NEO4_CURCU_INH_4.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.40_01.tif.jp2',name:'NEO4_CURCU_INH_4.40_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.50_01.tif.jp2',name:'NEO4_CURCU_INH_4.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.60_01.tif.jp2',name:'NEO4_CURCU_INH_4.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_4.70_01.tif.jp2',name:'NEO4_CURCU_INH_4.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_4',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.10_01.tif.jp2',name:'NEO4_CURCU_INH_5.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.1_01.tif.jp2',name:'NEO4_CURCU_INH_5.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.20_01.tif.jp2',name:'NEO4_CURCU_INH_5.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.30_01.tif.jp2',name:'NEO4_CURCU_INH_5.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.40_01.tif.jp2',name:'NEO4_CURCU_INH_5.40_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.50_01.tif.jp2',name:'NEO4_CURCU_INH_5.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.60_01.tif.jp2',name:'NEO4_CURCU_INH_5.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_5.70_01.tif.jp2',name:'NEO4_CURCU_INH_5.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_5',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.10_01.tif.jp2',name:'NEO4_CURCU_INH_6.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.1_01.tif.jp2',name:'NEO4_CURCU_INH_6.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.20_01.tif.jp2',name:'NEO4_CURCU_INH_6.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.30_01.tif.jp2',name:'NEO4_CURCU_INH_6.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.40_01.tif.jp2',name:'NEO4_CURCU_INH_6.40_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.50_01.tif.jp2',name:'NEO4_CURCU_INH_6.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.60_01.tif.jp2',name:'NEO4_CURCU_INH_6.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_6.70_01.tif.jp2',name:'NEO4_CURCU_INH_6.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_6',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.10_01.tif.jp2',name:'NEO4_CURCU_INH_7.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.1_01.tif.jp2',name:'NEO4_CURCU_INH_7.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.20_01.tif.jp2',name:'NEO4_CURCU_INH_7.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.30_01.tif.jp2',name:'NEO4_CURCU_INH_7.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.40_01.tif.jp2',name:'NEO4_CURCU_INH_7.40_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.50_01.tif.jp2',name:'NEO4_CURCU_INH_7.50_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.60_01.tif.jp2',name:'NEO4_CURCU_INH_7.60_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_7.70_01.tif.jp2',name:'NEO4_CURCU_INH_7.70_01.tif.jp2',slidename:'NEO_4_Curcu_INH_7',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_8.10_01.tif.jp2',name:'NEO4_CURCU_INH_8.10_01.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_8.1_01.tif.jp2',name:'NEO4_CURCU_INH_8.1_01.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_8.20_01.tif.jp2',name:'NEO4_CURCU_INH_8.20_01.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_8.30_01.tif.jp2',name:'NEO4_CURCU_INH_8.30_01.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/NEO4_CURCU_INH_8.4001.tif.jp2',name:'NEO4_CURCU_INH_8.4001.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/_NEO4_CURCU_INH_8.5001.tif.jp2',name:'_NEO4_CURCU_INH_8.5001.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/_NEO4_CURCU_INH_8.6001.tif.jp2',name:'_NEO4_CURCU_INH_8.6001.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_Curcu_INH/converti/jpg/_NEO4_CURCU_INH_8.7001.tif.jp2',name:'_NEO4_CURCU_INH_8.7001.tif.jp2',slidename:'NEO_4_Curcu_INH_8',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.10__01.tif.jp2',name:'NEO4_HPg_INH_1.10__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.1__01.tif.jp2',name:'NEO4_HPg_INH_1.1__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.20__01.tif.jp2',name:'NEO4_HPg_INH_1.20__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.40__01.tif.jp2',name:'NEO4_HPg_INH_1.40__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.50__01.tif.jp2',name:'NEO4_HPg_INH_1.50__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.60__01.tif.jp2',name:'NEO4_HPg_INH_1.60__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_1.70__01.tif.jp2',name:'NEO4_HPg_INH_1.70__01.tif.jp2',slidename:'NEO4_HPg_INH_1',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.10__01.tif.jp2',name:'NEO4_HPg_INH_2.10__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.1__01.tif.jp2',name:'NEO4_HPg_INH_2.1__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.20__01.tif.jp2',name:'NEO4_HPg_INH_2.20__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.30__01.tif.jp2',name:'NEO4_HPg_INH_2.30__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.40__01.tif.jp2',name:'NEO4_HPg_INH_2.40__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.50__01.tif.jp2',name:'NEO4_HPg_INH_2.50__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.60__01.tif.jp2',name:'NEO4_HPg_INH_2.60__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_2.70__01.tif.jp2',name:'NEO4_HPg_INH_2.70__01.tif.jp2',slidename:'NEO4_HPg_INH_2',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.10__01.tif.jp2',name:'NEO4_HPg_INH_3.10__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.1__01.tif.jp2',name:'NEO4_HPg_INH_3.1__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.20__01.tif.jp2',name:'NEO4_HPg_INH_3.20__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.30__01.tif.jp2',name:'NEO4_HPg_INH_3.30__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.40__01.tif.jp2',name:'NEO4_HPg_INH_3.40__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.50__01.tif.jp2',name:'NEO4_HPg_INH_3.50__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.60__01.tif.jp2',name:'NEO4_HPg_INH_3.60__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_3.70__01.tif.jp2',name:'NEO4_HPg_INH_3.70__01.tif.jp2',slidename:'NEO4_HPg_INH_3',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.10__01.tif.jp2',name:'NEO4_HPg_INH_4.10__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.1__01.tif.jp2',name:'NEO4_HPg_INH_4.1__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.20__01.tif.jp2',name:'NEO4_HPg_INH_4.20__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.30__01.tif.jp2',name:'NEO4_HPg_INH_4.30__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.50__01.tif.jp2',name:'NEO4_HPg_INH_4.50__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.60__01.tif.jp2',name:'NEO4_HPg_INH_4.60__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_4.70__01.tif.jp2',name:'NEO4_HPg_INH_4.70__01.tif.jp2',slidename:'NEO4_HPg_INH_4',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.10__01.tif.jp2',name:'NEO4_HPg_INH_5.10__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.1__01.tif.jp2',name:'NEO4_HPg_INH_5.1__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.20__01.tif.jp2',name:'NEO4_HPg_INH_5.20__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.30__01.tif.jp2',name:'NEO4_HPg_INH_5.30__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.40__01.tif.jp2',name:'NEO4_HPg_INH_5.40__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.50__01.tif.jp2',name:'NEO4_HPg_INH_5.50__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.60__01.tif.jp2',name:'NEO4_HPg_INH_5.60__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_5.70__01.tif.jp2',name:'NEO4_HPg_INH_5.70__01.tif.jp2',slidename:'NEO4_HPg_INH_5',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.10__01.tif.jp2',name:'NEO4_HPg_INH_6.10__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.1__01.tif.jp2',name:'NEO4_HPg_INH_6.1__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.20__01.tif.jp2',name:'NEO4_HPg_INH_6.20__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.30__01.tif.jp2',name:'NEO4_HPg_INH_6.30__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.40__01.tif.jp2',name:'NEO4_HPg_INH_6.40__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.50__01.tif.jp2',name:'NEO4_HPg_INH_6.50__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.60__01.tif.jp2',name:'NEO4_HPg_INH_6.60__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_6.70__01.tif.jp2',name:'NEO4_HPg_INH_6.70__01.tif.jp2',slidename:'NEO4_HPg_INH_6',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_7.10__01.tif.jp2',name:'NEO4_HPg_INH_7.10__01.tif.jp2',slidename:'NEO4_HPg_INH_7',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/NEO4_HPg_INH_7.1__01.tif.jp2',name:'NEO4_HPg_INH_7.1__01.tif.jp2',slidename:'NEO4_HPg_INH_7',order:1,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.2001.tif.jp2',name:'_NEO4_HPg_INH_7.2001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.3001.tif.jp2',name:'_NEO4_HPg_INH_7.3001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:30,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.4001.tif.jp2',name:'_NEO4_HPg_INH_7.4001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:40,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.5001.tif.jp2',name:'_NEO4_HPg_INH_7.5001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:50,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.6001.tif.jp2',name:'_NEO4_HPg_INH_7.6001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:60,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_7.7001.tif.jp2',name:'_NEO4_HPg_INH_7.7001.tif.jp2',slidename:'NEO4_HPg_INH_7',order:70,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.1001.tif.jp2',name:'_NEO4_HPg_INH_8.1001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:10,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.101.tif.jp2',name:'_NEO4_HPg_INH_8.101.tif.jp2',slidename:'NEO4_HPg_INH_8',order:11,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.2001.tif.jp2',name:'_NEO4_HPg_INH_8.2001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:20,study:'NEO4'],
            [filename:'file:////home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.3001.tif.jp2',name:'_NEO4_HPg_INH_8.3001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:30,study:'NEO4'],*/
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.4001.tif.jp2',name:'_NEO4_HPg_INH_8.4001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:40,study:'NEO4'],
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.5001.tif.jp2',name:'_NEO4_HPg_INH_8.5001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:50,study:'NEO4'],
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.6001.tif.jp2',name:'_NEO4_HPg_INH_8.6001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:60,study:'NEO4'],
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_NEO4/grp_HPg_INH/converti/jpg/_NEO4_HPg_INH_8.7001.tif.jp2',name:'_NEO4_HPg_INH_8.7001.tif.jp2',slidename:'NEO4_HPg_INH_8',order:70,study:'NEO4'],
            [filename:'file:///home/maree/data/CYTOMINE/LBTD/Slides/Olympus/study_test/grp/converti/jpg/NEO_4_Curcu_INH_1.10_3_2_01.tif.jp2',name:'NEO_4_Curcu_INH_1.10_3_2_01.tif.jp2',slidename:'NEO_4_Curcu_INH_1',order:10,study:'NEO4']
    ]
    // createLBTDScans(LBTDScans)



    def termSamples = [
            [name: "Cell in vivo",comment:"",ontology:[name:"Ontology1"],color:"FF0000"],
            [name: "Cell ex vivo",comment:"",ontology:[name:"Ontology1"],color:"00FF00"],
            [name: "Cell",comment:"A comment for cell",ontology:[name:"Ontology1"],color:"FF00FF"],
            [name: "Cell within a living organism",comment:"",ontology:[name:"Ontology1"],color:"0000FF"]
    ]
    createTerms(termSamples)

    def relationSamples = [
            [name: "Synonym"],
            [name: "Parent"],
    ]
    createRelation(relationSamples)


    def relationTermSamples = [
            [relation: "Parent",term1:"Cell", term2: "Cell ex vivo"],
            [relation: "Parent",term1:"Cell", term2: "Cell in vivo"],
            [relation: "Synonym", term1:"Cell within a living organism", term2: "Cell in vivo"]
    ]
    createRelationTerm(relationTermSamples)



    /* Annotations */
    def annotationSamples = [
            //[name : "annot3", location : ["POLYGON((2000 1000, 30 0, 40 10, 30 20, 2000 1000))","POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))"], image: [filename: "Boyden - essai _10x_02"]],
            //[name : "annot2", location : ["POLYGON((20 10, 30 50, 40 10, 30 20, 20 10))"],image: [filename: "Boyden - essai _10x_02"]]
            [name : "annot3", location : ["POINT(10000 10000)"], scan: [filename: "Aperio - 003"],term:["Cell","Cell in vivo"], user:"lrollus"],
            [name : "", location : ["POINT(5000 5000)"],scan: [filename: "Aperio - 003"],user:"lrollus"],
            [name : "annot4", location : ["POLYGON((5000 20000, 20000 17000, 20000 10000, 10000 7500, 5000 20000))","POLYGON((10000 15000, 15000 12000, 12000 12000, 10000 15000))"],scan: [filename: "Aperio - 003"],term:["Cell ex vivo"],user:"lrollus"]
    ]
    createAnnotations(annotationSamples)



    def destroy = {
    }
    //end of init
  }

  /* Methods */

  def createLBTDScans(LBTDScans) {
    LBTDScans.each { item ->
      def slide = Slide.findByName(item.slidename)
      if (!slide) {

        slide = new Slide(name : item.slidename, order : item.order)

        if (slide.validate()) {
          println "Creating slide  ${item.name}..."

          slide.save(flush : true)

          /* Link to projects */
          println "item.study=" + item.study

          Project project = Project.findByName(item.study)
          println "project=" + project
          ProjectSlide.link(project, slide)
        }
      }

      def extension = item.extension ?: "jp2"
      def mime = Mime.findByExtension(extension)

      def scanner = Scanner.findByBrand("gigascan")



      def image = new Image(
              filename: item.name,
              scanner : scanner,
              slide : slide,
              path : item.filename,
              mime : mime
      )

      if (image.validate()) {
        println "Creating image : ${image.filename}..."

        image.save(flush : true)
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
                className : item.className)

        if (imageServer.validate()) {
          println "Creating image server ${imageServer.name}... : ${imageServer.url}"

          imageServer.save(flush : true)

          imageServers << imageServer

          /* Link with MIME JP2 */
          Mime mime = Mime.findByExtension(item.extension)
          MimeImageServer.link(imageServer, mime)

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
          item.projects.each { elem ->
            Project project = Project.findByName(elem.name)
            ProjectSlide.link(project, slide)
          }

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
    def images = Image.list() ?: []
    if (!images) {
      scanSamples.each { item ->
        def extension = item.extension ?: "jp2"
        def mime = Mime.findByExtension(extension)

        def scanner = Scanner.findByBrand("gigascan")
        def user = User.findByUsername("lrollus")
        //  String path
        //Mime mime
        def image = new Image(
                filename: item.filename,
                path : item.path,
                mime : mime,
                scanner : scanner,
                slide : slides[item.slide],
                user:user
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
        def scanParent = Image.findByFilename(item.scan.filename)
        def user = User.findByUsername(item.user)
        println "user " + item.user +"=" + user.username
        annotation = new Annotation(name: item.name, location:geom, image:scanParent,user:user)


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
        ontology = new Ontology(name:item.name)
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

        def relation = Relation.findByName(item.relation)
        def term1 = Term.findByName(item.term1)
        def term2 = Term.findByName(item.term2)

        println "Creating term/relation  ${relation.name}:${item.term1}/${item.term2}..."
        RelationTerm.link(relation, term1, term2)

      }
    }
  }

}
