package be.cytomine

import grails.plugins.selenium.*
import be.cytomine.test.Infos
import be.cytomine.project.Project
import be.cytomine.test.BasicInstance

@Mixin(SeleniumAware)
public class AbstractWebProject extends AbstractWebCytomine{

    void openProjectPageAndWait() {
            Project project = getBasicProject()

            Infos.addUserRight(Infos.GOODLOGIN,project)

            selenium.open("/")

            selenium.waitForTextPresent(project.name.toUpperCase());
            selenium.waitForElementPresent("id=project-button");
        }

        Project getBasicProject() {
            return BasicInstance.createOrGetBasicProject()
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


}
