package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.*
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import grails.converters.JSON


class SearchSecurityTests extends SecurityTestsAbstract {

    void testSearchSecurityForCytomineAdmin() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

        //Get admin user
        User admin = BasicInstanceBuilder.getAdmin(USERNAMEADMIN,PASSWORDADMIN)

        //Create project with user 1
        ImageInstance image = ImageInstanceAPI.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation with cytomine admin
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.image = image
        annotation.project = project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code

        //Annotation
        Property annotationPropertyToAdd1 = BasicInstanceBuilder.getAnnotationPropertyNotExist(result.data)
        Property annotationPropertyToAdd2 = BasicInstanceBuilder.getAnnotationPropertyNotExist(result.data)
        annotationPropertyToAdd1.value = "Poney"
        annotationPropertyToAdd2.value = "Cheval"

        result = PropertyAPI.create(annotationPropertyToAdd1.domainIdent, "annotation" ,annotationPropertyToAdd1.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty1 = result.data

        result = PropertyAPI.create(annotationPropertyToAdd2.domainIdent, "annotation" ,annotationPropertyToAdd2.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty2 = result.data

        //Image
        Property imagePropertyToAdd = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        imagePropertyToAdd.value = "Cheval"
        imagePropertyToAdd.domain = image
        result = PropertyAPI.create(imagePropertyToAdd.domainIdent, "imageinstance" ,imagePropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property imageProperty = result.data

        //Project
        Property projectPropertyToAdd = BasicInstanceBuilder.getProjectPropertyNotExist()
        projectPropertyToAdd.value = "Poney"
        projectPropertyToAdd.domain = project
        result = PropertyAPI.create(projectPropertyToAdd.domainIdent, "project" ,projectPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property projectProperty = result.data

        result = SearchAPI.listDomain("Cheval,Poney", SearchOperator.OR, SearchFilter.ALL, USERNAMEADMIN, PASSWORDADMIN)
        assert (200 == result.code)
        assert (true == SearchAPI.containsInJSONList(annotationProperty1.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(annotationProperty2.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(imageProperty.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(projectProperty.domainIdent, JSON.parse(result.data)))

        result = SearchAPI.listDomain("Cheval,Poney", SearchOperator.AND, SearchFilter.ALL, USERNAMEADMIN, PASSWORDADMIN)
        assert (200 == result.code)
        assert (true == SearchAPI.containsInJSONList(annotationProperty1.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(annotationProperty2.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(imageProperty.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(projectProperty.domainIdent, JSON.parse(result.data)))

    }

    void testSearchSecurityForSimpleUser() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        //Get user2
        User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

        //Create project with user 1
        ImageInstance image = ImageInstanceAPI.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation with cytomine admin
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.image = image
        annotation.project = project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code

        //Annotation
        Property annotationPropertyToAdd1 = BasicInstanceBuilder.getAnnotationPropertyNotExist(result.data)
        Property annotationPropertyToAdd2 = BasicInstanceBuilder.getAnnotationPropertyNotExist(result.data)
        annotationPropertyToAdd1.value = "Poney"
        annotationPropertyToAdd2.value = "Cheval"

        result = PropertyAPI.create(annotationPropertyToAdd1.domainIdent, "annotation" ,annotationPropertyToAdd1.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty1 = result.data

        result = PropertyAPI.create(annotationPropertyToAdd2.domainIdent, "annotation" ,annotationPropertyToAdd2.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty2 = result.data

        //Image
        Property imagePropertyToAdd = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        imagePropertyToAdd.value = "Cheval"
        imagePropertyToAdd.domain = image
        result = PropertyAPI.create(imagePropertyToAdd.domainIdent, "imageinstance" ,imagePropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property imageProperty = result.data

        //Project
        Property projectPropertyToAdd = BasicInstanceBuilder.getProjectPropertyNotExist()
        projectPropertyToAdd.value = "Poney"
        projectPropertyToAdd.domain = project
        result = PropertyAPI.create(projectPropertyToAdd.domainIdent, "project" ,projectPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property projectProperty = result.data


        result = SearchAPI.listDomain("Cheval,Poney", SearchOperator.OR, SearchFilter.ALL, USERNAME1, PASSWORD1)
        assert (200 == result.code)
        assert (true == SearchAPI.containsInJSONList(annotationProperty1.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(annotationProperty2.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(imageProperty.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(projectProperty.domainIdent, JSON.parse(result.data)))

        result = SearchAPI.listDomain("Cheval,Poney", SearchOperator.AND, SearchFilter.ALL, USERNAME1, PASSWORD1)
        assert (200 == result.code)
        assert (true == SearchAPI.containsInJSONList(annotationProperty1.domainIdent, JSON.parse(result.data)))
        assert (true == SearchAPI.containsInJSONList(annotationProperty2.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(imageProperty.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(projectProperty.domainIdent, JSON.parse(result.data)))

        result = SearchAPI.listDomain("Cheval,Poney", SearchOperator.OR, SearchFilter.ALL, USERNAME2, PASSWORD2)
        assert (200 == result.code)
        assert (false == SearchAPI.containsInJSONList(annotationProperty1.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(annotationProperty2.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(imageProperty.domainIdent, JSON.parse(result.data)))
        assert (false == SearchAPI.containsInJSONList(projectProperty.domainIdent, JSON.parse(result.data)))

    }

    void testSearchSecurityForAnonymous() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

        def project = BasicInstanceBuilder.getProjectNotExist()
        Infos.addUserRight(USERNAME1,project)

        def result = ProjectAPI.create(project.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code

        def projectPropertyToAdd = BasicInstanceBuilder.getProjectPropertyNotExist()
        projectPropertyToAdd.value = "Poney"
        projectPropertyToAdd.domain = result.data
        result = PropertyAPI.create(projectPropertyToAdd.domainIdent, "project", projectPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property projectProperty = result.data

        //check if user 2 cannot access/update/delete
        assert (401 == SearchAPI.listDomain(projectProperty.value, SearchOperator.OR, SearchFilter.PROJECT ,USERNAMEBAD, PASSWORDBAD).code)
    }
}
