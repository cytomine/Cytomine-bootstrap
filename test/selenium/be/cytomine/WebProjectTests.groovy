package be.cytomine

import grails.plugins.selenium.*
import be.cytomine.test.Infos
import be.cytomine.test.BasicInstance
import be.cytomine.*
import be.cytomine.project.Project

@Mixin(SeleniumAware)
public class WebProjectTests extends AbstractWebProject{

    void setUp() throws Exception{
        logIn()
    }

   void tearDown() throws Exception {
       logOut()
   }

    void testConsultProject() {
        openProjectPageAndWait()
        //check if the basic project is visible
        waitForTextPresent(getBasicProject().name);
    }

    void testAddProject() {
        //open dialog
        openAddProjectDialogAndWait()
        //fill form and save
        fillAddProjectDialogAndSave("testaddnewproject",getBasicProject().discipline?.id,getBasicProject().ontology?.id)
        //check if project is on the listing page
        waitForTextPresent("testaddnewproject")
    }

    void testEditProject() {
        Project project = BasicInstance.getBasicProjectNotExist()
        project.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project)
        
        selenium.open("/")

        selenium.waitForElementPresent("id=editProjectButton"+project.id);
        
//        for (int second = 0;; second++) {
//      			if (second >= 60) fail("timeout");
//                if(second==1 || second==5 || second==20 || second==40) selenium.refresh()
//      			try { if (selenium.isElementPresent("id=editProjectButton"+project.id)) break; } catch (Exception e) {}
//      			Thread.sleep(1000);
//        }
        
       // println selenium.htmlSource
        selenium.click("id=editProjectButton"+project.id)

        selenium.type("id=project-edit-name", "BASICPROJECTUPDATED");

        selenium.click("id=users49");

      	selenium.click("id=editProjectButton");

      	selenium.waitForTextPresent("BASICPROJECTUPDATED")
    }

    void testDeleteProject() {
        Project project = BasicInstance.getBasicProjectNotExist()
        project.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project)

        selenium.open("/")

        selenium.waitForTextPresent(project.name.toUpperCase());

        selenium.click("id=deleteProjectButton"+project.id)

        selenium.waitForElementPresent("id=closeProjectDeleteConfirmDialog")

        selenium.click("id=closeProjectDeleteConfirmDialog")

        selenium.waitForNotTextPresent(project.name.toUpperCase());
    }

    void testFilterProjectName() {
        Project project1 = BasicInstance.getBasicProjectNotExist()
        project1.name = "ABC123"
        project1.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project1)
        Project project2 = BasicInstance.getBasicProjectNotExist()
        project2.name = "XYZ789"
        project2.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project2)

        selenium.open("/")



        selenium.waitForTextPresent(project1.name.toUpperCase());
        selenium.waitForTextPresent(project2.name.toUpperCase());

        selenium.waitForElementPresent("id=projectsearchtextbox")

        selenium.type("id=projectsearchtextbox", "WRONG");
        selenium.typeKeys("id=projectsearchtextbox"," ")
        //selenium.keyPress("id=projectsearchtextbox", "\\71");

        selenium.waitForNotTextPresent(project1.name.toUpperCase());
        selenium.waitForNotTextPresent(project2.name.toUpperCase());

        selenium.type("id=projectsearchtextbox", "ABC");
        selenium.typeKeys("id=projectsearchtextbox"," ")
        //selenium.keyPress("id=projectsearchtextbox", "\\67");

        selenium.waitForTextPresent(project1.name.toUpperCase());
        selenium.waitForNotTextPresent(project2.name.toUpperCase());

        selenium.type("id=projectsearchtextbox", "789");
        selenium.typeKeys("id=projectsearchtextbox"," ")
        //selenium.keyPress("id=projectsearchtextbox", "\\57");

        selenium.waitForNotTextPresent(project1.name.toUpperCase());
        selenium.waitForTextPresent(project2.name.toUpperCase());

        selenium.type("id=projectsearchtextbox", "");
        selenium.typeKeys("id=projectsearchtextbox"," ")
        //selenium.keyPress("id=projectsearchtextbox", "\\13");

        selenium.waitForTextPresent(project1.name.toUpperCase());
        selenium.waitForTextPresent(project2.name.toUpperCase());
    }

    void testFilterProjectAutoComplete() {
        Project project = BasicInstance.createOrGetBasicProject()
        String firstLetter = project.name.substring(0,2)

        selenium.open("/")

        selenium.waitForElementPresent("id=projectsearchtextbox")
        selenium.typeKeys("id=projectsearchtextbox",firstLetter)

        selenium.waitForElementPresent("//html/body/ul/li/a[. = \""+project.name.toUpperCase()+"\"]")
    }

    void testOpenDashboardProject() {
        Project project = BasicInstance.createOrGetBasicProject()

        selenium.open("/")
        selenium.waitForElementPresent("id=radioprojectchange"+project.id);
        selenium.click("id=radioprojectchange"+project.id);
        selenium.waitForTextPresent("regexpi:ACTIVITY");

    }


}
