package be.cytomine.api.stats

import be.cytomine.AnnotationDomain
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.sql.UserAnnotationListing
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader

class StatsController extends RestController {

    def termService
    def jobService
    def secUserService

    /**
     * Compute for each user, the number of annotation of each term
     */
    def statUserAnnotations = {

        Map<Long, Object> result = new HashMap<Long, Object>()

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        //Get project terms
        def terms = Term.findAllByOntology(project.getOntology())
        if(terms.isEmpty()) {
            responseSuccess([])
            return
        }

        //compute number of annotation for each user and each term
        def nbAnnotationsByUserAndTerms = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            join("userAnnotation")
            createAlias("userAnnotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.user.id")
                groupProperty("term.id")
                count("term")
            }
        }

        //build empty result table
        secUserService.listUsers(project).each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.terms = []
            terms.each { term ->
                def t = [:]
                t.id = term.id
                t.name = term.name
                t.color = term.color
                t.value = 0
                item.terms << t
            }
            result.put(user.id, item)
        }

        //complete stats for each user and term
        nbAnnotationsByUserAndTerms.each { stat ->
            def user = result.get(stat[0])
            if(user) {
                user.terms.each {
                    if (it.id == stat[1]) {
                        it.value = stat[2]
                    }
                }
            }
        }
        responseSuccess(result.values())
    }

    /**
     * Compute number of annotation for each user
     */
    def statUser = {

        Map<Long, Object> result = new HashMap<Long, Object>()

        //Get project
        Project project = Project.read(params.id)
        if (!project) {
            responseNotFound("Project", params.id)
            return
        }

        //compute number of annotation for each user
        def userAnnotations = UserAnnotation.createCriteria().list {
            eq("project", project)
            join("user")  //right join possible ? it will be sufficient
            projections {
                countDistinct('id')
                groupProperty("user.id")
            }
        }

        //build empty result table
        secUserService.listLayers(project).each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }

        //fill result table with number of annotation
        userAnnotations.each { item ->
            def user = result.get(item[1])
            if(user) user.value = item[0]
        }

        responseSuccess(result.values())
    }

    /**
     * Compute the number of annotation for each term
     */
    def statTerm = {

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        //Get leaf term (parent term cannot be map with annotation)
        def terms = project.ontology.leafTerms()

        //Get the number of annotation for each term
        def numberOfAnnotationForEachTerm = UserAnnotation.executeQuery('select t.term.id, count(t) from AnnotationTerm as t, UserAnnotation as b where b.id=t.userAnnotation.id and b.project = ? group by t.term.id', [project])

        def stats = [:]
        def color = [:]
        def ids = [:]
        def idsRevert = [:]
        def list = []

        //build empty result table
        terms.each { term ->
                stats[term.name] = 0
                color[term.name] = term.color
                ids[term.name] = term.id
                idsRevert[term.id] = term.name
        }

        //init result table with data
        numberOfAnnotationForEachTerm .each { result ->
            def name = idsRevert[result[0]]
            if(name) stats[name]=result[1]
        }

        //fill results stats tabble
        stats.each {
            list << ["id": ids.get(it.key), "key": it.key, "value": it.value, "color": color.get(it.key)]
        }
        responseSuccess(list)
    }

    /**
     * Compute the number of annotation for each sample and for each term
     */
    def statTermSlide = {

        Map<Long, Object> result = new HashMap<Long, Object>()

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        //Get project term
        def terms = Term.findAllByOntology(project.getOntology())

        //Check if there are user layers
        def userLayers = secUserService.listLayers(project)
        if(terms.isEmpty() || userLayers.isEmpty()) {
            responseSuccess([])
            return
        }

        def annotationsNumber = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            inList("user", userLayers)
            join("userAnnotation")
            createAlias("userAnnotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.image.id")
                groupProperty("term.id")
                count("term.id")
            }
        }

        //build empty result table
        terms.each { term ->
            def item = [:]
            item.id = term.id
            item.key = term.name
            item.value = 0
            result.put(item.id, item)
        }

        //Fill result table
        annotationsNumber.each { item ->
            def term = item[1]
            result.get(term).value++;
        }
        responseSuccess(result.values())
    }

    /**
     * For each user, compute the number of sample where he made annotation
     */
    def statUserSlide = {
        Project project = Project.read(params.id)
        if (!project) {
            responseNotFound("Project", params.id)
            return
        }
        def terms = Term.findAllByOntology(project.getOntology())
        if(terms.isEmpty()) {
            responseSuccess([])
            return
        }
        Map<Long, Object> result = new HashMap<Long, Object>()

        //numberOfAnnotationsByUserAndImage[0] = id image, numberOfAnnotationsByUserAndImage[1] = user, numberOfAnnotationsByUserAndImage[2] = number of annotation
        def numberOfAnnotationsByUserAndImage = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            join("userAnnotation")
            createAlias("userAnnotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.image.id")
                groupProperty("a.user")
                count("a.user")
            }
        }

        //build empty result table
        secUserService.listLayers(project).each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }

        //Fill result table
        numberOfAnnotationsByUserAndImage.each { item ->
            def user = result.get(item[1].id)
            if(user) user.value++;
        }

        responseSuccess(result.values())
    }

    /**
     * Compute user annotation number evolution over the time for a project (start = project creation, stop = today)
     * params.daysRange = number of days between each measure
     * param.term = (optional) filter on a specific term
     */
    def statAnnotationEvolution = {

        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        int daysRange = params.daysRange!=null ? params.getInt('daysRange') : 1
        Term term = Term.read(params.getLong('term'))

        def data = []
        int count = 0;

        def annotations = null;
        if(!term) {
            //find all annotation user for this project
            annotations = UserAnnotation.executeQuery("select a.created from UserAnnotation a where a.project = ? order by a.created desc", [project])
        }
        else {
            log.info "Search on term " + term.name
            //find all annotation user for this project and this term
            annotations = UserAnnotation.executeQuery("select b.created from UserAnnotation b where b.project = ? and b.id in (select x.userAnnotation.id from AnnotationTerm x where x.term = ?) order by b.created desc", [project,term])
        }

        //start a the project creation and stop today
        Date creation = project.created
        Date current = new Date()

        //for each day (step = daysRange), compute annotation number
        //start at the end date until the begining
        while(current.getTime()>=creation.getTime()) {

            def item = [:]
            while(count<annotations.size()) {
                //compute each annotation until the next step
                if(annotations.get(count).getTime()<current.getTime()) break;
                count++;
            }

            item.date = current.getTime()
            item.size = annotations.size()-count;
            data << item

            //add a new step
            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            cal.add(Calendar.DATE, -daysRange);
            current = cal.getTime();
        }
        responseSuccess(data)
    }

    def imageInstanceService
    def annotationListingService

    def statsPrediction = {

//        params.put("term1",83009160)
//        params.put("term2",83009161)
//        params.put("image",83009171)
//        params.put("user",18)


        params.put("term1",20202)
        params.put("term2",5735)
        params.put("image",7889358)
        params.put("user",8468429)

//        params.put("term1",83009160)
//        params.put("term2",83009161)
//        params.put("image",83009551)
//        params.put("user",14)
                //


        Term predicted = termService.read(params.long('term1'))
        Term content = termService.read(params.long('term2'))
        ImageInstance image = imageInstanceService.read(params.long('image'))
        SecUser predictor = secUserService.read(params.long('user'))
        SecUser reviewer = image.reviewUser

        //gérer/tester pour algo
        //RM: évlauer
        //Prédiction peuvent se toucher?



        //get all reviewed annotation for the predicted term
        def allReviewed = retrieveAllReviewed(image,predicted)

        //get all predicted user or algo annotation for the predicted term by the predicteor
        def allPredicted = retrieveAllPredicted(image,predicted,predictor)

        //get all annotation not predicted by reviewed
        def notPredictedReviewed = retrieveNotPredictedReviewed(allReviewed)

        //get all annotation predicted by not reviewed
        def predictedNotReviewed = retrievePredictedNotReviewed(allReviewed,allPredicted)

        //get all annotation reviewed and modifier / or not modified
        def splitReviewed =  splitReviewedModifiedAndNotModified(allReviewed,predictor)
        def reviewedNotModified = splitReviewed.reviewedNotModified
        def reviewedModified = splitReviewed.reviewedModified

        //get all annotation reviewed "content" (like a hung)
        def contentReviewed = retrieveAllReviewed(image,content)


//        assert contentReviewed.size()==1
//        assert allReviewed.size() == 6
//        assert allPredicted.size() == 6
//        assert predictedNotReviewed.size() == 2
//        assert notPredictedReviewed.size() == 2
//        assert reviewedNotModified.size() == 2
//        assert reviewedModified.size() == 2

        //convert annotation to geometry type + filter to keep only annotation inside content annotation (tumor inside hung)

        def contentReviewedGeometry = contentReviewed.collect{new WKTReader().read(it['location'])}
        def reviewedGeometries = allReviewed.collect{new WKTReader().read(it['location'])}
        reviewedGeometries =  reviewedGeometries.findAll { rev ->
            boolean isInContent = false
            contentReviewedGeometry.each { cont ->
                isInContent = isInContent ? isInContent : cont.difference(rev)
            }
            return isInContent
        }
        def predictedGeometries = allPredicted.collect{new WKTReader().read(it['location'])}
        predictedGeometries =  predictedGeometries.findAll { rev ->
            boolean isInContent = false
            contentReviewedGeometry.each { cont ->
                isInContent = isInContent ? isInContent : cont.difference(rev)
            }
            return isInContent
        }

        def reviewedGeometries2 = reviewedGeometries.collect{it.clone()}
        def predictedGeometries2 = predictedGeometries.collect{it.clone()}

//        assert reviewedGeometries.size() == 6
//        assert predictedGeometries.size() == 6

        //x = x c'est le pourcentage de tumeur de la couche review bien prédite comme tumeur dans la couche algo;
        def instersectGeometries = []
        reviewedGeometries.each { revgeom ->
            predictedGeometries.each { predictgeom ->
                instersectGeometries << revgeom.intersection(predictgeom)
            }
        }
        instersectGeometries = instersectGeometries.findAll {!it.isEmpty()}
//        assert instersectGeometries.size()==4

        def x = computeGeometriesArea(instersectGeometries)/computeGeometriesArea(reviewedGeometries)

        //y = c'est le pourcentage de tumeur de la couche review mal classée comme pastumeur dans la couche algo;
        def differenceGeometries = []

        reviewedGeometries.each{ revgeom  ->
            //for each reveiwed, get the intersection with a predicted (=good prediction) and make difference between the reviewed and good prediction (= bad prediction)
            boolean intersect = false
            def  badPrediction = revgeom.clone()
            predictedGeometries.each { predictgeom->

                def goodPrediction = revgeom.intersection(predictgeom)

                if(!goodPrediction.isEmpty()) {
                    intersect = true

                    try {badPrediction = badPrediction.difference(goodPrediction)} catch(Exception e) {println e}
//                    if(badPrediction && badPrediction.area>0) {
//                        revgeom = revgeom.difference(goodPrediction) //if predicted annotation intersect themselve
//
//                        if(badPrediction.toString().startsWith("MULTIPOLYGON (((24032 9317, 24200 9317,")) {
//                            println "found bad prediction"
//                        }
//                    }
                }
            }
            differenceGeometries << badPrediction
//            if(!intersect) {
//                //if no intersect with predicted, the reviewed geometry will ne be in collection
//                differenceGeometries << revgeom
//            }
        }
        differenceGeometries = differenceGeometries.findAll {!it.isEmpty()}
        differenceGeometries = differenceGeometries.unique()

        println "Y"
        differenceGeometries.each {
            println  it
        }
        differenceGeometries.each {
            println  it.area
        }
        def y = computeGeometriesArea(differenceGeometries)/computeGeometriesArea(reviewedGeometries)


       //w = c'est le pourcentage de pastumeur de la couche review mal classée comme tumeur dans la couche algo;

        def differenceGeometriesW = []

        predictedGeometries2.each{ predictgeom  ->
            boolean intersect = false
            def badPrediction = predictgeom.clone()
            println "predictgeom=$predictgeom"
            reviewedGeometries2.each {revgeom ->
                def goodPrediction = predictgeom.intersection(revgeom)
                println "goodPrediction=$goodPrediction"
                if(!goodPrediction.isEmpty()) {
                    intersect = true
                    println "badPrediction=$badPrediction"
                    badPrediction = badPrediction.difference(goodPrediction)
//                    try {predictgeom = revgeom.difference(goodPrediction)} catch(Exception e) {println e}


                }
            }
            println "badPrediction=$badPrediction"
            differenceGeometriesW << badPrediction
//            if(!intersect) {
//                //if no intersect with predicted, the reviewed geometry will ne be in collection
//                differenceGeometriesW << predictgeom
//            }
        }

        differenceGeometriesW = differenceGeometriesW.findAll {!it.isEmpty()}
        differenceGeometriesW = differenceGeometriesW.unique()

        println "W"
        differenceGeometriesW.each {
            println it
        }


        //compute the content annotation without the reviewed annotation (= not tumor)
        def contentWithoutReviewedPrediction = []
        contentReviewedGeometry.each { contentRev ->
            reviewedGeometries2.each { wellPredicted ->
                contentRev = contentRev.difference(wellPredicted)
            }
            contentWithoutReviewedPrediction << contentRev
        }

        def w = computeGeometriesArea(differenceGeometriesW)/computeGeometriesArea(contentWithoutReviewedPrediction)


      //z = c'est le pourcentage de pastumeur de la couche review bien classée come pastumeur dans la couche algo
        def contentWithoutReviewedPredictionAndPredicted  = []
        //compute the content without the reviewed and without the predicted (not tumor well predicted)
        contentWithoutReviewedPrediction.each { contentRev ->
              predictedGeometries2.each { predict ->
                  contentRev = contentRev.difference(predict)
              }
            contentWithoutReviewedPredictionAndPredicted << contentRev
        }

        def z = computeGeometriesArea(contentWithoutReviewedPredictionAndPredicted)/computeGeometriesArea(contentWithoutReviewedPrediction)



        println "x=$x"
        println "y=$y"
//        assert 1==x+y
        println "w=$w"
        println "z=$z"

        println "x+y="+(x+y)
        println "w+z="+(w+z)
//        assert 1==z+w


//       Pourcentage d'"overlap" (= surface de l'intersection des deux couches)
//       Pourcentage de "surplus" (= surface de ce qui était détecté par l'algo mais non retenu après review)
//       Pourcentage de "manquement" (= surface qui n'était pas détectée par l'algo mais ajoutée par la review)




    }

    static Geometry combineIntoOneGeometry( Collection<Geometry> geometryCollection ){
        Geometry all = null;
        for( Iterator<Geometry> i = geometryCollection.iterator(); i.hasNext(); ){
        Geometry geometry = i.next();
        if( geometry == null ) continue;
        if( all == null ){
            all = geometry;
        }
        else {
            all = all.union( geometry );
        }
    }
    return all;
    }

    private def computeGeometriesArea(def geometries) {
        def intersectArea = 0
        geometries.each {

            intersectArea = intersectArea + it.area
        }
        //intersectArea = combineIntoOneGeometry(geometries).area
//        GeometryCollection unionCollection = new GeometryCollection(geometries.toArray(new Geometry[geometries.size()]), new GeometryFactory());
//        intersectArea = unionCollection.area
        return intersectArea
    }

    private def splitReviewedModifiedAndNotModified(def allReviewed, def predictor) {
        //Nbre d'annotations acceptées et non modifiées
        def reviewedNotModified = []
        //Nbre d'annotations acceptées et modifiées
        def reviewedModified = []
        allReviewed.each { reviewed ->
            def parent
            try {
                parent = AnnotationDomain.getAnnotationDomain(reviewed.parentIdent)
            }catch(Exception e) {}
            log.info "${reviewed['id']} parent?.user=${parent?.user} vs ${predictor.id}"
            log.info "user ${parent?.user.id}"
            if( parent && parent.user.id == predictor.id && parent.location.equals(new WKTReader().read(reviewed['location']))) {
                reviewedNotModified << reviewed
            } else if (parent && parent.user.id == predictor.id) {
                reviewedModified << reviewed
            }
        }
        return  [reviewedNotModified:reviewedNotModified,reviewedModified:reviewedModified]
    }


    private def retrievePredictedNotReviewed(def allReviewed, def allPredicted) {
        def reviewedParentsIds = allReviewed.collect{it['parentIdent']}

//       Nbre d'annotations non reviewé
        def predictedNotReviewed = []
        allPredicted.each {
            if(!reviewedParentsIds.contains(it['id'])) {
                predictedNotReviewed <<  it
            }

        }
        predictedNotReviewed
    }

   //TODO: error, in database user/review_user are the same ... why?
    private def retrieveNotPredictedReviewed(def allReviewed) {
        def notPredictedReviewed = []
//       Nbre d'annotations ajoutées (non présentes dans la couche algo)
        allReviewed.each { reviewed ->
            log.info "${reviewed['user']} == ${reviewed['reviewUser']}"
            if(reviewed['user']==reviewed['reviewUser']) {
                notPredictedReviewed <<  reviewed
            }
        }
        notPredictedReviewed

    }


    private def retrieveAllReviewed(ImageInstance image,Term term) {
        AnnotationListing al = new ReviewedAnnotationListing(
                columnToPrint: ['basic','meta',"wkt",'term'],
                image : image.id,
                term : term.id
        )
        annotationListingService.executeRequest(al)

    }

    private def retrieveAllPredicted(ImageInstance image,Term term, SecUser user) {
        def allPredicted
        AnnotationListing al
        if(user.algo()) {
            al = new AlgoAnnotationListing(
                            columnToPrint: ['basic','meta','term',"wkt"],
                            image : image.id,
                            term : term.id,
                            user: user.id
             )

        } else {
            al = new UserAnnotationListing(
                            columnToPrint: ['basic','meta','term',"wkt"],
                            image : image.id,
                            term : term.id,
                            user: user.id
             )
        }
        allPredicted = annotationListingService.executeRequest(al)
        return allPredicted
    }
}
