package be.cytomine

import be.cytomine.project.Project
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import grails.plugins.selenium.SeleniumAware

@Mixin(SeleniumAware)
public class AbstractWebProject extends AbstractWebCytomine{

    void openProjectPageAndWait() {
        Project project = getBasicProject()
        Infos.addUserRight(Infos.GOODLOGIN,project)
        selenium.open("/")
        selenium.waitForTextPresent(project.name.toUpperCase());
        selenium.waitForElementPresent("id=project-button");
    }

    void openAddProjectDialogAndWait() {
        click("id=projectaddbutton")
        selenium.waitForElementPresent("css=#projectdiscipline > option[value=\""+ getBasicProject().discipline.id + "\"]")
        selenium.waitForElementPresent("css=#projectontology > option[value=\""+getBasicProject().ontology.id+"\"]")
        selenium.waitForTextPresent("Loïc Rollus")
    }

    void fillAddProjectDialogAndSave(String projectName, long idDiscipline, long idOntology) {
        selenium.type("id=project-name", projectName);
        selenium.select("id=projectdiscipline", "value="+idDiscipline);
        selenium.select("id=projectontology", "value="+idOntology);
        selenium.click("id=saveProjectButton");
    }

    void openEditProjectDialgoAndWait(Long idProject) {
        click("id=editProjectButton"+idProject)
    }

    void fillEditProjectDialogAndSave(String projectNewName) {
        selenium.type("id=project-edit-name", projectNewName);
        selenium.click("id=users49");
      	selenium.click("id=editProjectButton");
    }

    void openDeleteProjectDialogAndWait(Long idProject) {
        click("id=deleteProjectButton"+idProject)
    }

    void fillDeleteProjectDialogAndSave() {
        click("id=closeProjectDeleteConfirmDialog")
    }


    public void checkProjectOnList(Long id) {
        selenium.waitForElementPresent("//div[@id='projectlist"+id+"']")
        selenium.waitForVisible("id=projectlist"+id)
    }

    public void checkProjectNotOnList(Long id) {
        Thread.sleep(1000)
        if(selenium.isElementPresent("id=projectlist"+id)) {
            selenium.waitForNotVisible("id=projectlist"+id)
        }
    }

    Project getBasicProject() {
        return BasicInstance.createOrGetBasicProject()
    }

    Project createBasicProjectNotExist() {
        Project project = BasicInstance.getBasicProjectNotExist()
        project.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project)
        return project
    }

    Project createBasicProjectNotExist(String name) {
        Project project = BasicInstance.getBasicProjectNotExist()
        project.name = name
        project.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,project)
        return project
    }




}
