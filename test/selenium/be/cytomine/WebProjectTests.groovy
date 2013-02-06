package be.cytomine

import be.cytomine.project.Project
import grails.plugins.selenium.SeleniumAware

@Mixin(SeleniumAware)
public class WebProjectTests extends AbstractWebProject{

//    void setUp() throws Exception{
//        logIn()
//        openProjectPageAndWait()
//    }
//
//   void tearDown() throws Exception {
//       logOut()
//   }
//
//    void testConsultProject() {
//        //check if the basic project is visible
//        checkProjectOnList(getBasicProject().id)
//    }
//
//    void testAddProject() {
//        //open dialog
//        openAddProjectDialogAndWait()
//        //fill form and save
//        fillAddProjectDialogAndSave("testaddnewproject",getBasicProject().discipline?.id,getBasicProject().ontology?.id)
//        //check if project is on the listing page
//        Project project = null
//        int max = 10
//        while(project==null && max>0) {
//            Thread.sleep(500)
//            project = Project.findByNameIlike("testaddnewproject")
//            max--
//        }
//
//        checkProjectOnList(project.id)
//    }
//
//    void testEditProject() {
//        Project project = createBasicProjectNotExist()
//        //open page
//        openProjectPageAndWait()
//        //open edit dialog
//        openEditProjectDialgoAndWait(project.id)
//        //fill with new name and new user
//        fillEditProjectDialogAndSave("BASICPROJECTUPDATED")
//        //check if new text is there
//        waitForTextPresent("BASICPROJECTUPDATED")
//    }
//
//    void testDeleteProject() {
//        Project project = createBasicProjectNotExist()
//        //open page
//        openProjectPageAndWait()
//        //open delete dialog
//        openDeleteProjectDialogAndWait(project.id)
//        //comfirm delete project
//        fillDeleteProjectDialogAndSave()
//        //check if project is deleted
//        checkProjectNotOnList(project.id)
//    }
//
//
//    void testFilterProjectName() {
//        Project project1 = createBasicProjectNotExist("ABC123")
//        Project project2 = createBasicProjectNotExist("XYZ789")
//        //open page
//        openProjectPageAndWait()
//
//        //check if project are in list
//        waitForTextPresent(project1.name)
//        waitForTextPresent(project2.name)
//
//        //wait filter view build
//        selenium.waitForElementPresent("id=projectsearchtextbox")
//
//        //put "WRONG" on seach project name input => no project visible
//        selenium.type("id=projectsearchtextbox", "WRONG");
//        selenium.typeKeys("id=projectsearchtextbox"," ")
//        checkProjectNotOnList(project1.id)
//        checkProjectNotOnList(project2.id)
//
//        //put "ABC" => just project 1 must be in list
//        selenium.type("id=projectsearchtextbox", "ABC");
//        selenium.typeKeys("id=projectsearchtextbox"," ")
//        checkProjectOnList(project1.id)
//        checkProjectNotOnList(project2.id)
//
//
//        //put "789" => just project 2 must be in list
//        selenium.type("id=projectsearchtextbox", "789");
//        selenium.typeKeys("id=projectsearchtextbox"," ")
//        waitForNotTextPresent(project1.name)
//        waitForTextPresent(project2.name)
//
//        //put "" => all project must be visible
//        selenium.type("id=projectsearchtextbox", "");
//        selenium.typeKeys("id=projectsearchtextbox"," ")
//        waitForTextPresent(project1.name)
//        waitForTextPresent(project2.name)
//    }
//
//    void testFilterProjectAutoComplete() {
//        Project project = createBasicProjectNotExist()
//        String firstLetter = project.name.substring(0,2)
//        //open page
//        openProjectPageAndWait()
//        //type first caracter from project on autocomplete textbox
//        selenium.waitForElementPresent("id=projectsearchtextbox")
//        selenium.typeKeys("id=projectsearchtextbox",firstLetter)
//        //check is elem is on the list
//        selenium.waitForElementPresent("//html/body/ul/li/a[. = \""+project.name.toUpperCase()+"\"]")
//    }
//
//    void testOpenDashboardProject() {
//        click("id=radioprojectchange"+getBasicProject().id)
//        waitForTextPresent("activity");
//    }


    void testJustForNoError() {

    }
}
