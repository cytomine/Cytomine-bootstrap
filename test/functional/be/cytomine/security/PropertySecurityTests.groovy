package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.PropertyAPI
import be.cytomine.test.http.UserAnnotationAPI
import grails.converters.JSON

class PropertySecurityTests extends SecurityTestsAbstract {

    void testAnnotationPropertySecurityForCytomineAdmin() {
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

        Property annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationPropertyNotExist(result.data)
        result = PropertyAPI.create(annotationPropertyToAdd.domainIdent, "annotation" ,annotationPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code

        Property annotationProperty = result.data
        println "annotationProperty="+annotationProperty
        println "annotationProperty.id="+annotationProperty.id

        //check if admin user can access/update/delete
        assert (200 == PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation" , USERNAMEADMIN, PASSWORDADMIN).code)
        println JSON.parse(PropertyAPI.listByDomain(annotationProperty.domainIdent, "annotation", USERNAMEADMIN, PASSWORDADMIN).data)
        assert (true == PropertyAPI.containsInJSONList(annotationProperty.id, JSON.parse(PropertyAPI.listByDomain(annotationProperty.domainIdent, "annotation", USERNAMEADMIN, PASSWORDADMIN).data)))
        assert (200 == PropertyAPI.update(annotationProperty.id, annotationProperty.domainIdent, "annotation", annotationProperty.encodeAsJSON(), USERNAMEADMIN, PASSWORDADMIN).code)
        assert (200 == PropertyAPI.delete(annotationProperty.id, annotationProperty.domainIdent, "annotation", USERNAMEADMIN, PASSWORDADMIN).code)
    }

    void testAnnotationPropertySecurityForProjectUser() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        //Get user2
        User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist()
        Infos.addUserRight(USERNAME1,annotationToAdd.project)

        def result =  AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        def annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotationPropertyToAdd.domain = result.data
        result = PropertyAPI.create(annotationPropertyToAdd.domainIdent, "annotation" ,annotationPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty = result.data

        //Create image, project, annotation
        ImageInstance image = ImageInstanceAPI.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation = result.data

        annotationProperty.domain = annotation;

        BasicInstanceBuilder.saveDomain(project)
        BasicInstanceBuilder.saveDomain(annotationProperty)

        //TODO: try with USERNAME1 & PASSWORD1
        def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == resAddUser.code
        resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == resAddUser.code
        //check if user 2 can access/update/delete
        assert (200 == PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation" , USERNAME2, PASSWORD2).code)
        assert (true == PropertyAPI.containsInJSONList(annotationProperty.id, JSON.parse(PropertyAPI.listByDomain(annotationProperty.domainIdent, "annotation" , USERNAME2, PASSWORD2).data)))
        assert (200 == PropertyAPI.update(annotationProperty.id, annotationProperty.domainIdent, "annotation" ,annotationProperty.encodeAsJSON(), USERNAME2, PASSWORD2).code)


        //remove right to user2
        resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
        assert 403 == resAddUser.code
        //check if user 2 cannot access/update/delete
        assert (200 == PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation" ,USERNAME2,PASSWORD2).code)
        assert (200 == PropertyAPI.listByDomain(annotationProperty.domainIdent, "annotation" , USERNAME2,PASSWORD2).code)
        assert (200 == PropertyAPI.update(annotationProperty.id, annotationProperty.domainIdent, "annotation", annotationProperty.encodeAsJSON(),USERNAME2,PASSWORD2).code)

        //delete project because we will try to delete term
        def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert (200 == resDelProj.code)

        assert (404 == PropertyAPI.delete(annotationProperty.id, annotationProperty.domainIdent, "annotation", USERNAME2,PASSWORD2).code)
    }

    void testAnnotationPropertySecurityForSimpleUser() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        //Get user2
        User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist()
        Infos.addUserRight(USERNAME1,annotationToAdd.project)

        //Create new Term (user1)
        def result =  AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        def annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotationPropertyToAdd.domain = result.data
        result = PropertyAPI.create(annotationPropertyToAdd.domainIdent, "annotation" ,annotationPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty = result.data

        //check if user 2 cannot access/update/delete
        assert (403 == PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation" , USERNAME2, PASSWORD2).code)
        assert (403 == PropertyAPI.update(annotationProperty.id, annotationProperty.domainIdent, "annotation", annotationProperty.encodeAsJSON(), USERNAME2, PASSWORD2).code)
        assert (403 == PropertyAPI.delete(annotationProperty.id, annotationProperty.domainIdent, "annotation", USERNAME2, PASSWORD2).code)
    }

    void testAnnotationPropertySecurityForAnonymous() {
        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist()
        Infos.addUserRight(USERNAME1,annotationToAdd.project)

        //Create new Term (user1)
        def result =  AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        def annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotationPropertyToAdd.domain = result.data
        result = PropertyAPI.create(annotationPropertyToAdd.domainIdent, "annotation" ,annotationPropertyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Property annotationProperty = result.data

        //check if user 2 cannot access/update/delete
        assert (401 == PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation" ,USERNAMEBAD, PASSWORDBAD).code)
        assert (401 == PropertyAPI.listByDomain(annotationProperty.domainIdent, "annotation", USERNAMEBAD, PASSWORDBAD).code)
        assert (401 == PropertyAPI.update(annotationProperty.id, annotationProperty.domainIdent, "annotation", annotationProperty.encodeAsJSON(), USERNAMEBAD, PASSWORDBAD).code)
        assert (401 == PropertyAPI.delete(annotationProperty.id, annotationProperty.domainIdent, "annotation", USERNAMEBAD, PASSWORDBAD).code)
    }
}
