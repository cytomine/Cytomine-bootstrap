package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.*
import be.cytomine.utils.Description
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ProjectSecurityTests extends SecurityTestsAbstract {


  void testProjectSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstanceBuilder.getAdmin(USERNAMEADMIN,PASSWORDADMIN)


      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
       Infos.printRight(project)
      Infos.printUserRight(user1)
       Infos.printUserRight(admin)
      //check if admin user can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == ProjectAPI.delete(project.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testProjectSecurityForProjectCreator() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      println "PROJECT="+project.deleted

      //check if user 1 can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAME1,PASSWORD1).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME1,PASSWORD1).data)))
      assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == ProjectAPI.delete(project.id,USERNAME1,PASSWORD1).code)
  }

  void testProjectSecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add right to user2
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code
      //log.info "AFTER:"+user2.getAuthorities().toString()

      Infos.printRight(project)
      //check if user 2 can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code

      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (403 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert (false == ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)
  }

  void testProjectSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (403 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert(false==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      Infos.printRight(project)
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)

  }

    void testProjectSecurityForGhestUser() {

        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        //Get ghest
         User ghest = BasicInstanceBuilder.getGhest("GHESTONTOLOGY","PASSWORD")

        //Create new project (user1)
        def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Project project = result.data

        //Add right to user2
        def resAddUser = ProjectAPI.addUserProject(project.id,ghest.id,USERNAME1,PASSWORD1)
        assert 200 == resAddUser.code
        //log.info "AFTER:"+user2.getAuthorities().toString()

        Infos.printRight(project)
        //check if user 2 can access/update/delete
        assert (200 == ProjectAPI.show(project.id,"GHESTONTOLOGY","PASSWORD").code)
        assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list("GHESTONTOLOGY","PASSWORD").data)))
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),"GHESTONTOLOGY","PASSWORD").code)
        assert (403 == ProjectAPI.delete(project.id,"GHESTONTOLOGY","PASSWORD").code)
        assert (403 == ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),"GHESTONTOLOGY","PASSWORD").code)
    }




  void testProjectSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (401 == ProjectAPI.show(project.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.delete(project.id,USERNAMEBAD,PASSWORDBAD).code)
  }

  void testAddProjectGrantAdminUndoRedo() {
    //not implemented (no undo/redo for project)
  }


    void testReadOnlyProjectData() {

        /*
           Init dataset
         */

        def simpleUsername = "simpleUserRO"
        def adminUsername = "adminRO"
        def password = "password"

        //Create a project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        //Set project as Readonly
        project.isReadOnly = true
        BasicInstanceBuilder.saveDomain(project)

        //Add a simple project user
        User simpleUser = BasicInstanceBuilder.getUser(simpleUsername,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code

        //Create an annotation
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,image,true)

        //Create a description
        Description description = BasicInstanceBuilder.getDescriptionNotExist(annotation,true)

        //Create a property
        Property property = BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation,true)


        /*
           Now Test as simple user
         */


        //add property (simple user)
        assert 403 == PropertyAPI.create(annotation.id, "annotation" ,BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation,false).encodeAsJSON(),simpleUsername,password).code

        //update property (simple user)
        assert 403 == PropertyAPI.update(property.id, property.domainIdent, "annotation" ,property.encodeAsJSON(), simpleUsername,password).code

        //delete property (simple user)
        assert 403 == PropertyAPI.delete(property.id, property.domainIdent, "annotation", simpleUsername, password).code

        //add image instance (simple user)
        assert 403 == ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist(project,false).encodeAsJSON(),simpleUsername, password).code

        //delete image instance (simple user)
        ImageInstance image2 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        assert 403 == ImageInstanceAPI.delete(image2, simpleUsername, password).code

        //start reviewing image (simple user)
        println "###"+image.id
        assert 403 == ReviewedAnnotationAPI.markStartReview(image.id,simpleUsername, password).code

        //add annotation (simple user)
        assert 200 == UserAnnotationAPI.create(BasicInstanceBuilder.getUserAnnotationNotExist(project,image,false).encodeAsJSON(),simpleUsername, password).code

        //update project (simple user)
        assert 403 == ProjectAPI.update(project.id,project.encodeAsJSON(),simpleUsername, password).code

        //add description (simple user)
        assert 403 == DescriptionAPI.create(project.id,project.class.name,description.encodeAsJSON(),simpleUsername, password).code

        //update description (simple user)
        assert 403 == DescriptionAPI.update(description.domainIdent,description.domainClassName,description.encodeAsJSON(),simpleUsername, password).code

        //delete description  (simple user)
        assert 403 == DescriptionAPI.delete(description.domainIdent,description.domainClassName,simpleUsername, password).code


        /*
          Now run test as a project admin
         */

        //add property (admin user)
        assert 200 == PropertyAPI.create(annotation.id, "annotation" ,BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation,false).encodeAsJSON(),adminUsername,password).code

        //update property (admin user)
        assert 200 == PropertyAPI.update(property.id, property.domainIdent, "annotation" ,property.encodeAsJSON(), adminUsername,password).code

        //delete property (admin user)
        assert 200 == PropertyAPI.delete(property.id, property.domainIdent, "annotation", adminUsername, password).code

        //add image instance (admin user)
        assert 200 == ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist(project,false).encodeAsJSON(),adminUsername, password).code

        //delete image instance (admin user)
        assert 200 == ImageInstanceAPI.delete(image2, adminUsername, password).code

        //start reviewing image (admin user)
        println "###" + image.id
        assert 200 == ReviewedAnnotationAPI.markStartReview(image.id,adminUsername, password).code

        //add annotation (admin user)
        assert 200 == UserAnnotationAPI.create(BasicInstanceBuilder.getUserAnnotationNotExist(project,image,false).encodeAsJSON(),adminUsername, password).code

        //update project (admin user)
        assert 200 == ProjectAPI.update(project.id,project.encodeAsJSON(),adminUsername, password).code

        //add description (admin user)
        ImageInstance image3 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        Description description2 = BasicInstanceBuilder.getDescriptionNotExist(image3,false)
        println "-> " + project.id
        println "image3.project="+image3.project

        assert 200 == DescriptionAPI.create(image3.id,image3.class.name,description2.encodeAsJSON(),adminUsername, password).code

        //update description (admin user)
        assert 200 == DescriptionAPI.update(description.domainIdent,description.domainClassName,description.encodeAsJSON(),adminUsername, password).code

        //delete description  (admin user)
        assert 200 == DescriptionAPI.delete(description.domainIdent,description.domainClassName,adminUsername, password).code


    }

    void testNotReadOnlyProjectData() {
        /*
           Init dataset
         */

        def simpleUsername = "simpleUserRO"
        def adminUsername = "adminRO"
        def password = "password"

        //Create a project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        //Force project to Readonly
        project.isReadOnly = false
        BasicInstanceBuilder.saveDomain(project)

        //Add a simple project user
        User simpleUser = BasicInstanceBuilder.getUser(simpleUsername,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code

        //Create an annotation
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,image,true)

        //Create a description
        Description description = BasicInstanceBuilder.getDescriptionNotExist(annotation,true)

        //Create a property
        Property property = BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation,true)


        /*
           Now Test
         */


        //add property (simple user)
        assert 200 == PropertyAPI.create(annotation.id, "annotation" ,BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation,false).encodeAsJSON(),simpleUsername,password).code

        //update property (simple user)
        assert 200 == PropertyAPI.update(property.id, property.domainIdent, "annotation" ,property.encodeAsJSON(), simpleUsername,password).code

        //delete property (simple user)
        assert 200 == PropertyAPI.delete(property.id, property.domainIdent, "annotation", simpleUsername, password).code

        //add image instance (simple user)
        assert 200 == ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist(project,false).encodeAsJSON(),simpleUsername, password).code

        //delete image instance (simple user)
        ImageInstance image2 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        assert 403 == ImageInstanceAPI.delete(image2, simpleUsername, password).code

        //start reviewing image (simple user)
        println "###" + image.id
        assert 200 == ReviewedAnnotationAPI.markStartReview(image.id,simpleUsername, password).code

        //add annotation (simple user)
        assert 200 == UserAnnotationAPI.create(BasicInstanceBuilder.getUserAnnotationNotExist(project,image,false).encodeAsJSON(),simpleUsername, password).code

        //update project (simple user)
        assert 403 == ProjectAPI.update(project.id,project.encodeAsJSON(),simpleUsername, password).code

        //add description (simple user)
        ImageInstance image3 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        Description description2 = BasicInstanceBuilder.getDescriptionNotExist(image3,false)
        assert 200 == DescriptionAPI.create(image3.id,image3.class.name,description2.encodeAsJSON(),simpleUsername, password).code

        //update description (simple user)
        assert 200 == DescriptionAPI.update(description.domainIdent,description.domainClassName,description.encodeAsJSON(),simpleUsername, password).code

        //delete description  (simple user)
        assert 200 == DescriptionAPI.delete(description.domainIdent,description.domainClassName,simpleUsername, password).code


        /*
           Now run as an admin project
         */
        //Create an annotation
        ImageInstance image0 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        UserAnnotation annotation0 = BasicInstanceBuilder.getUserAnnotationNotExist(project,image0,true)

        //Create a description
        Description description0 = BasicInstanceBuilder.getDescriptionNotExist(annotation0,true)

        //Create a property
        Property property0 = BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation0,true)


        //add property (admin user)
        assert 200 == PropertyAPI.create(annotation0.id, "annotation" ,BasicInstanceBuilder.getAnnotationPropertyNotExist(annotation0,false).encodeAsJSON(),adminUsername,password).code

        //update property (admin user)
        assert 200 == PropertyAPI.update(property0.id, property0.domainIdent, "annotation" ,property0.encodeAsJSON(), adminUsername,password).code

        //delete property (admin user)
        assert 200 == PropertyAPI.delete(property0.id, property0.domainIdent, "annotation", adminUsername, password).code

        //add image instance (admin user)
        assert 200 == ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist(project,false).encodeAsJSON(),adminUsername, password).code

        //delete image instance (admin user)
        ImageInstance image4 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        assert 200 == ImageInstanceAPI.delete(image4, adminUsername, password).code

        //start reviewing image (admin user)
        println "###" + image.id
        assert 200 == ReviewedAnnotationAPI.markStartReview(image0.id,adminUsername, password).code

        //add annotation (admin user)
        assert 200 == UserAnnotationAPI.create(BasicInstanceBuilder.getUserAnnotationNotExist(project,image0,false).encodeAsJSON(),adminUsername, password).code

        //update project (admin user)
        assert 200 == ProjectAPI.update(project.id,project.encodeAsJSON(),adminUsername, password).code

        //add description (admin user)
        ImageInstance image5 = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        Description description3 = BasicInstanceBuilder.getDescriptionNotExist(image5,false)
        assert 200 == DescriptionAPI.create(image4.id,image4.class.name,description3.encodeAsJSON(),adminUsername, password).code

        //update description (admin user)
        assert 200 == DescriptionAPI.update(description0.domainIdent,description0.domainClassName,description0.encodeAsJSON(),adminUsername, password).code

        //delete description  (admin user)
        assert 200 == DescriptionAPI.delete(description0.domainIdent,description0.domainClassName,adminUsername, password).code
    }




}
