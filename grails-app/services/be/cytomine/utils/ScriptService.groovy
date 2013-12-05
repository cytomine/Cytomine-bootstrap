package be.cytomine.utils

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */

class ScriptService extends ModelService {

    def statsProject() {
        println "statsProject"
        User lrollus = User.findByUsername("lrollus")
        User stevben = User.findByUsername("stevben")
        Ontology ontology = saveAndReturnDomain(new Ontology(name: "tumeuroupas2", user:lrollus))
        Term tumeur = saveAndReturnDomain(new Term(name:"tumeur",ontology:ontology, color:"#ff0000"))
        Term poumon = saveAndReturnDomain(new Term(name:"poumon",ontology:ontology,color:"#00ff00"))


        println "lrollus=$lrollus"
        println "stevben=$stevben"

        Project project = saveAndReturnDomain(new Project(name:"statstestfull2",ontology:ontology))
        Infos.addUserRight("lrollus",project)
        Infos.addUserRight("stevben",project)

        def based = AbstractImage.read(71l)
        println "image=${based}"

        ImageInstance image = saveAndReturnDomain(new ImageInstance(baseImage: based, project:project, user: lrollus))

        //algo (=stevben) annotation
        UserAnnotation annotationBase1 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((8720 35120, 8720 38032, 13584 38032, 13584 35120, 8720 35120))")))
        AnnotationTerm at1 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase1, term: tumeur,user:stevben))

        UserAnnotation annotationBase2 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((11656 33280, 13288 32512, 14216 30432, 13512 28032, 11336 26912, 9416 27424, 8392 28800, 8264 31488, 9608 32864, 11656 33280))")))
        AnnotationTerm at2 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase2, term: tumeur,user:stevben))

        UserAnnotation annotationBase3 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((16168 35136, 16168 38048, 22056 38048, 22056 35136, 16168 35136))")))
        AnnotationTerm at3 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase3, term: tumeur,user:stevben))

        UserAnnotation annotationBase4 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((16120 30672, 17112 32560, 19072 33664, 20904 33104, 22472 31824, 22424 29104, 21336 27088, 18744 26608, 16392 28240, 15864 29360, 16120 30672))")))
        AnnotationTerm at4 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase4, term: tumeur,user:stevben))

        UserAnnotation annotationBase7 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((34024 34928, 34024 38064, 37432 37968, 37592 34960, 34024 34928))")))
        AnnotationTerm at7 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase7, term: tumeur,user:stevben))

        UserAnnotation annotationBase8 = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((33960 31152, 35304 32688, 37928 33200, 40504 33008, 41528 30800, 40872 29040, 39448 27024, 37032 26672, 35496 27376, 33768 28784, 33768 29744, 33960 31152))")))
        AnnotationTerm at8 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationBase8, term: tumeur,user:stevben))

        UserAnnotation annotationNew5 = saveAndReturnDomain(new UserAnnotation(user:lrollus,project:project,image:image,location:new WKTReader().read("POLYGON ((24896 34624, 24896 37696, 31360 37696, 31360 34624, 24896 34624))")))
        AnnotationTerm at5 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationNew5, term: tumeur,user:lrollus))

        UserAnnotation annotationNew6 = saveAndReturnDomain(new UserAnnotation(user:lrollus,project:project,image:image,location:new WKTReader().read("POLYGON ((25128 31024, 26920 32688, 28712 32752, 30120 32176, 31464 30512, 31272 28208, 30056 27184, 27560 26608, 26024 27568, 25064 28912, 24936 29808, 25128 31024))")))
        AnnotationTerm at6 = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationNew6, term: tumeur,user:lrollus))

        UserAnnotation annotationPoumon = saveAndReturnDomain(new UserAnnotation(user:stevben,project:project,image:image,location:new WKTReader().read("POLYGON ((6167.999999999996 26256, 6167.999999999996 41488, 43736 41488, 43736.00000000001 26256, 6167.999999999996 26256))")))
        AnnotationTerm atp = saveAndReturnDomain(new AnnotationTerm(userAnnotation: annotationPoumon, term: poumon,user:stevben))

        ReviewedAnnotation reviewedAnnotation1 = new ReviewedAnnotation(status:0,reviewUser:lrollus,  project:project,image:image,user: stevben,location:new WKTReader().read("POLYGON ((8720 35120, 8720 38032, 13584 38032, 13584 35120, 8720 35120))") )
        reviewedAnnotation1.addToTerms(tumeur)
        reviewedAnnotation1.putParentAnnotation(annotationBase1)
        saveAndReturnDomain(reviewedAnnotation1)

        ReviewedAnnotation reviewedAnnotation2 = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: stevben,location:new WKTReader().read("POLYGON ((11656 33280, 13288 32512, 14216 30432, 13512 28032, 11336 26912, 9416 27424, 8392 28800, 8264 31488, 9608 32864, 11656 33280))") )
        reviewedAnnotation2.addToTerms(tumeur)
        reviewedAnnotation2.putParentAnnotation(annotationBase2)
        saveAndReturnDomain(reviewedAnnotation2)

        ReviewedAnnotation reviewedAnnotation3 = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: stevben,location:new WKTReader().read("POLYGON ((16168 35136, 16064 40448, 22016 40512, 22056 35136, 16168 35136))") )
        reviewedAnnotation3.addToTerms(tumeur)
        reviewedAnnotation3.putParentAnnotation(annotationBase3)
        saveAndReturnDomain(reviewedAnnotation3)

        ReviewedAnnotation reviewedAnnotation4 = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: stevben,location:new WKTReader().read("POLYGON ((16120 30672, 17112 32560, 19072 31808, 20928 32448, 22472 31824, 22424 29104, 21336 27088, 18744 26608, 16392 28240, 15864 29360, 16120 30672))") )
        reviewedAnnotation4.addToTerms(tumeur)
        reviewedAnnotation4.putParentAnnotation(annotationBase4)
        saveAndReturnDomain(reviewedAnnotation4)


        ReviewedAnnotation reviewedAnnotation5 = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: lrollus,location:new WKTReader().read("POLYGON ((24896 34624, 24896 37696, 31360 37696, 31360 34624, 24896 34624))") )
        reviewedAnnotation5.addToTerms(tumeur)
        reviewedAnnotation5.putParentAnnotation(annotationNew5)
        saveAndReturnDomain(reviewedAnnotation5)

        ReviewedAnnotation reviewedAnnotation6 = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: lrollus,location:new WKTReader().read("POLYGON ((25128 31024, 26920 32688, 28712 32752, 30120 32176, 31464 30512, 31272 28208, 30056 27184, 27560 26608, 26024 27568, 25064 28912, 24936 29808, 25128 31024))") )
        reviewedAnnotation6.addToTerms(tumeur)
        reviewedAnnotation6.putParentAnnotation(annotationNew6)
        saveAndReturnDomain(reviewedAnnotation6)

        ReviewedAnnotation reviewedAnnotationPoumon = new ReviewedAnnotation(status:0,reviewUser:lrollus,project:project,image:image,user: stevben,location:new WKTReader().read("POLYGON ((6167.999999999996 26256, 6167.999999999996 41488, 43736 41488, 43736.00000000001 26256, 6167.999999999996 26256))"))
        reviewedAnnotationPoumon.addToTerms(poumon)
        reviewedAnnotationPoumon.putParentAnnotation(annotationPoumon)
        saveAndReturnDomain(reviewedAnnotationPoumon)
        //start reviewed image


    }
}
