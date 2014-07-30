package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Property
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.SearchAPI
import be.cytomine.utils.Description
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray


class SearchEngineTests {

  void testProjectSearchComplex() {
      //word 1 = hello
      //word 2 = world
      //word 3 = cytomine

      //projectA: create a project with name "hello" with description with data "blabla cytomine world"
      Project projectA = createProject("heLlO")
      Description descriptionA = createDescriptionForDomain(projectA,"blabla Cytomine world")

      //projectB: create a project with name "cytomine" with property value "hello world"
      Project projectB = createProject("cytomine")
      Property propertyB = createPropertyForDomain(projectB,"hello WORLD hello world")
      Property propertyB2 = createPropertyForDomain(projectB,"xxxx eeeee")

      //projectC: create a project with name "test"
      Project projectC = createProject("test")


      //test search with "hello" AND "world"
      def results = SearchAPI.search(["hello","world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      results = SearchAPI.searchResults([projectA.id,projectB.id],["hello","world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      def json = JSON.parse(results.data)
      def item1 = json.collection[0]
      assert item1["id"]==projectB.id
      assert item1["matching"][0].type== "property"
      def item2 = json.collection[1]
      assert item2["id"]==projectA.id
      assert item2["matching"][0].type== "description" || item2["matching"][1].type== "description"
      assert item2["matching"][0].value.contains("blabla Cytomine world") || item2["matching"][1].value.contains("blabla Cytomine world")

      //test search with "hello world" expression, only in property from B
      results = SearchAPI.search(["hello world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit on project domain
      //test search with "hello world" expression, only in property from B
      results = SearchAPI.search(["hello","world"],"project",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit on another domain
      results = SearchAPI.search(["hello","world"],"annotation",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit on property
      results = SearchAPI.search(["world"],null,["property"],null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit on description
      results = SearchAPI.search(["world"],null,["description"],null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit on domain
      results = SearchAPI.search(["world"],null,["domain"],null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      //test limit per project id
      results = SearchAPI.search(["hello","world"],null,null,[projectB.id,projectC.id],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))
      results = SearchAPI.search(["hello","world"],null,null,[projectC.id,],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

  }


    void testAnnotationSearch() {
        Project projectA = createProject("annotation")
        projectA.ontology = BasicInstanceBuilder.getOntologyNotExist(true)
        BasicInstanceBuilder.saveDomain(projectA)
        Term term = BasicInstanceBuilder.getTermNotExist(projectA.ontology,true)
        term.name = "hello"
        BasicInstanceBuilder.saveDomain(term)
        Term term2 = BasicInstanceBuilder.getTermNotExist(projectA.ontology,true)
        term2.name = "world"
        BasicInstanceBuilder.saveDomain(term2)

        //add term "hello" and "world" for annotation1
        UserAnnotation annotation1 = createUserAnnotation(projectA)
        AnnotationTerm at1 = BasicInstanceBuilder.getAnnotationTermNotExist(annotation1,term,true)
        AnnotationTerm at2 = BasicInstanceBuilder.getAnnotationTermNotExist(annotation1,term2,true)

        //add term "hello"for annotation2
        AlgoAnnotation annotation2 = createAlgoAnnotation(projectA)
        AlgoAnnotationTerm aat2 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist(annotation2,term,true)

        //add property "hello" and descr "world" for annotation 3
        ReviewedAnnotation annotation3 = createReviewedAnnotation(projectA)
        createDescriptionForDomain(annotation3,"world")
        createPropertyForDomain(annotation3,"hello")

        //add annotation for another project
        UserAnnotation annotationNotInProject = createUserAnnotation(BasicInstanceBuilder.getProjectNotExist(true))
        createPropertyForDomain(annotationNotInProject,"world")

        //test search with "hello"
        def results = SearchAPI.search(["hello"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        //get result for only 3 and 2
        results = SearchAPI.searchResults([annotation3.id,annotation2.id],["hello"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        def json = JSON.parse(results.data)
        assert !SearchAPI.containsInJSONList(annotation1.id,json)
        assert SearchAPI.containsInJSONList(annotation2.id,json)
        assert SearchAPI.containsInJSONList(annotation3.id,json)
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,json)

        def item1 = json.collection[0]
        assert item1["id"]==annotation3.id
        assert item1["matching"][0].type== "property"
        assert item1["matching"][0].value.contains("hello")

        def item2 = json.collection[1]
        assert item2["id"]==annotation2.id
        assert item2["matching"][0].type== "domain"


        //test search with "hello"
        results = SearchAPI.search(["hello","world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["hello","world"],"annotation",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["hello","world"],"project",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["hello"],null,["property"],null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["hello"],null,null,[projectA.id],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["world"],null,null,[projectA.id],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

        results = SearchAPI.search(["world"],null,null,[annotationNotInProject.project.id],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotationNotInProject.id,JSON.parse(results.data))

    }

    void testImageSearch() {
        Project projectA = createProject("image")

        //image1 has prop world and desc hello
        ImageInstance image1 = createImageInstance(projectA)
        createDescriptionForDomain(image1,"wORld")
        createPropertyForDomain(image1,"HELLO")

        //image2 has name world
        ImageInstance image2 = createImageInstance(projectA)
        image2.baseImage = createAbstractImage()
        image2.baseImage.originalFilename = "zzzzworldfffff"
        BasicInstanceBuilder.saveDomain(image2.baseImage)
        BasicInstanceBuilder.saveDomain(image2)

        //abstracimage1 has propertyname hello/world
        AbstractImage abstractImage1 = createAbstractImage()
        abstractImage1.originalFilename = "blablabla_hello_World.jpg"
        BasicInstanceBuilder.saveDomain(abstractImage1)

        //test search with "hello"
        def results = SearchAPI.search(["hello"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))

        results = SearchAPI.search(["world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))


        //get result for only 3 and 2
        results = SearchAPI.searchResults([image2.id,abstractImage1.id],["world"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        def json = JSON.parse(results.data)
        assert !SearchAPI.containsInJSONList(image1.id,json)
        assert SearchAPI.containsInJSONList(image2.id,json)
        assert SearchAPI.containsInJSONList(abstractImage1.id,json)

        def item1 = json.collection[0]
        assert item1["id"]==abstractImage1.id
        assert item1["matching"][0].type== "domain"
        assert item1["matching"][0].value.contains("World")

        def item2 = json.collection[1]
        assert item2["id"]==image2.id
        assert item1["matching"][0].type== "domain"
        assert item1["matching"][0].value.contains("World")


        results = SearchAPI.search(["world"],"image",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))

        results = SearchAPI.search(["world"],"project",null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))

        results = SearchAPI.search(["hello"],null,["property"],null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))

        results = SearchAPI.search(["world"],null,null,[projectA.id],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image2.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data)) //abstracimage is not in a project
    }

    void testMixSearch() {
        //PROJECT
        Project projectA = createProject("mix")
        createDescriptionForDomain(projectA,"blabla mix world")

        //ABSTRRACIMAGE
        AbstractImage abstractImage1 = createAbstractImage()
        abstractImage1.originalFilename = "blablablmixmix_World.jpg"
        BasicInstanceBuilder.saveDomain(abstractImage1)

        //IMAGEINSTANCE
        ImageInstance image1 = createImageInstance(projectA)
        createDescriptionForDomain(image1,"mix mix")

        //REVIEWEDANNOTATION
        ReviewedAnnotation annotation3 = createReviewedAnnotation(projectA)
        createDescriptionForDomain(annotation3,"mix")

        //USERANNOTATION
        UserAnnotation annotation1 = createUserAnnotation(projectA)
        createDescriptionForDomain(annotation1,"mix")

        //REVIEWEDANNOTATION
        AlgoAnnotation annotation2 = createAlgoAnnotation(projectA)
        createPropertyForDomain(annotation2,"mix")

        def results = SearchAPI.search(["mix"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))

        results = SearchAPI.search(["thiswordnotexist"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))


        results = SearchAPI.searchResults([projectA.id,abstractImage1.id,image1.id,annotation1.id,annotation2.id,annotation3.id],["mix"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        def json = JSON.parse(results.data)
        assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
    }


    void testMaxWordsLimit() {
        def results = SearchAPI.search(["not","too","much"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        results = SearchAPI.search(["there","is","one","word","too","much"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.searchResults([1,2,3],["there","is","one","word","too","much"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code

    }

    void testMinWordsLimit() {
        def results = SearchAPI.search(["siz","e is","ok!"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        results = SearchAPI.search(["aa","is too ","short"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search([""],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.searchResults([1,2,3],["aa","is too ","short"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    void testMinResuls() {
        def results = SearchAPI.searchResults([],["hello"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    void testAvoidSpecialChar() {
        def results = SearchAPI.search(["*****"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search(["%%%%%%%%"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search(["______"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    private Project createProject(String name) {
        Project projectA = BasicInstanceBuilder.getProjectNotExist(true)
        projectA.name = "$name ${new Date().getTime()}"
        BasicInstanceBuilder.saveDomain(projectA)
    }

    private UserAnnotation createAnnotation(Project project) {
        return createUserAnnotation(project)
    }

    private UserAnnotation createUserAnnotation(Project project) {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,true)
        return annotation
    }

    private AlgoAnnotation createAlgoAnnotation(Project project) {
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist(project,true)
        return annotation
    }

    private ReviewedAnnotation createReviewedAnnotation(Project project) {
        ReviewedAnnotation annotation = BasicInstanceBuilder.getReviewedAnnotationNotExist(project,true)
        return annotation
    }

    private ImageInstance createImageInstance(Project project) {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        return image
    }

    private AbstractImage createAbstractImage() {
        AbstractImage image = BasicInstanceBuilder.getAbstractImageNotExist(true)
        return image
    }

    private Description createDescriptionForDomain(CytomineDomain domain, String data) {
        Description description = BasicInstanceBuilder.getDescriptionNotExist(domain,true)
        description.data = data
        BasicInstanceBuilder.saveDomain(description)
    }

    private Property createPropertyForDomain(CytomineDomain domain, String data) {
        Property property = new Property(domain: domain, key: 'key '+new Date().getTime(), value:data)
        BasicInstanceBuilder.saveDomain(property)
    }


}
