package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.processing.ImageFilterProject
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageFilterProjectAPI
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ImageFilterProjectSecurityTests extends SecurityTestsAbstract {

    void testimageFilterProjectSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create project with user 1
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user,project)


        //Add imageFilterProject 1 with cytomine admin
        ImageFilterProject imageFilterProject1 = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject1.project = project
        def result = ImageFilterProjectAPI.create(imageFilterProject1.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        imageFilterProject1 = result.data

        //Add imageFilterProject 2 with user 1
        ImageFilterProject imageFilterProject2 = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject2.project = project
        result = ImageFilterProjectAPI.create(imageFilterProject2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        imageFilterProject2 = result.data

        //Get/List imageFilterProject with cytomine admin
        result = ImageFilterProjectAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        log.info "JSON.parse(result.data)="+JSON.parse(result.data)
        assert (true ==ImageFilterProjectAPI.containsInJSONList(imageFilterProject2.id, JSON.parse(result.data)))

        //Delete imageFilterProject 2 with cytomine admin
        assert (200 == ImageFilterProjectAPI.delete(imageFilterProject2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
    }

    void testimageFilterProjectSecurityForProjectUserAndimageFilterProjectAdmin() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user,project)

        //Add imageFilterProject 1 with user1
        ImageFilterProject imageFilterProject2 = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject2.project = project
        def result = ImageFilterProjectAPI.create(imageFilterProject2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        imageFilterProject2 = result.data

        //Get/List imageFilterProject 2 with user 1
        result = ImageFilterProjectAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        assert (true ==ImageFilterProjectAPI.containsInJSONList(imageFilterProject2.id, JSON.parse(result.data)))

        //Delete imageFilterProject 2 with user 1
        assert (200 == ImageFilterProjectAPI.delete(imageFilterProject2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testimageFilterProjectSecurityForProjectUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user1,project)
        Infos.printRight(project)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, user2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        Infos.printRight(project)
        assert 200 == resAddUser.code

        //Add imageFilterProject 1 with user 1
        ImageFilterProject imageFilterProject1 = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject1.project = project
        def result = ImageFilterProjectAPI.create(imageFilterProject1.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        imageFilterProject1 = result.data

        //Get/List imageFilterProject 1 with user 2
        result = ImageFilterProjectAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert 200 == result.code
        assert (true ==ImageFilterProjectAPI.containsInJSONList(imageFilterProject1.id, JSON.parse(result.data)))

        //Delete imageFilterProject 1 with user 2
        assert (200 == ImageFilterProjectAPI.delete(imageFilterProject1.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }


    void testimageFilterProjectSecurityForUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user1,project)

        //Add imageFilterProject 1 with user 1
        ImageFilterProject imageFilterProject1 = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject1.project = project
        def result = ImageFilterProjectAPI.create(imageFilterProject1.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        imageFilterProject1 = result.data

        //Get/List imageFilterProject 1 with user 2
        result = ImageFilterProjectAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert (403 == result.code)
        //assert (true ==ImageFilterProjectAPI.containsInJSONList(imageFilterProject1.id, JSON.parse(result.data)))

        //Delete imageFilterProject 1 with user 2
        assert (403 == ImageFilterProjectAPI.delete(imageFilterProject1.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }



    void testimageFilterProjectSecurityForAnonymous() {
        //Get User 1
        User user1 = getUser1()

        //Create project with user 1
        ImageInstance image = ImageInstanceAPI.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add imageFilterProject 1 with user 1
        ImageFilterProject imageFilterProject = BasicInstanceBuilder.getImageFilterProjectNotExist()
        imageFilterProject.project = project
        imageFilterProject.project = image.project
        def result = ImageFilterProjectAPI.create(imageFilterProject.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        imageFilterProject = result.data

        //Get/List imageFilterProject 1 with user 2
        assert (401 == ImageFilterProjectAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == ImageFilterProjectAPI.delete(imageFilterProject.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }

}
