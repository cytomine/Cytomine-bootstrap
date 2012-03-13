package be.cytomine.processing

import be.cytomine.project.Project
import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON

class SoftwareProject extends CytomineDomain implements Serializable{

    Software software
    Project project

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    def getIdProject() {
        if (this.projectId) return this.projectId
        else return this.project?.id
    }

    static SoftwareProject link(Software software, Project project) {
        link(null, software, project)
    }

    static SoftwareProject link(def id,Software software, Project project) {
        def softwareProjects = SoftwareProject.findBySoftwareAndProject(software, project)
        if (!softwareProjects) {
            softwareProjects = new SoftwareProject()
            softwareProjects.id = id
            softwareProjects.software = software
            softwareProjects.project = project
            software?.addToSoftwareProjects(softwareProjects)
            project?.addToSoftwareProjects(softwareProjects)
            project.refresh()
            software.refresh()
            softwareProjects.save(flush: true)
        }
        softwareProjects
    }

    static void unlink(Software software, Project project) {
        def softwareProjects = SoftwareProject.findBySoftwareAndProject(software, project)
        if (softwareProjects) {
            software?.removeFromSoftwareProjects(softwareProjects)
            project?.removeFromSoftwareProjects(softwareProjects)
            softwareProjects.delete(flush: true)
        } else {println "no link between " + software + " " + project}
    }
    
   static SoftwareProject createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static SoftwareProject createFromData(jsonSoftwareParameter) {
        def softwareProject = new SoftwareProject()
        getFromData(softwareProject, jsonSoftwareParameter)
    }

    static SoftwareProject getFromData(SoftwareProject softwareProject, jsonSoftwareParameter) {
        println "jsonSoftwareParameter=" + jsonSoftwareParameter.toString()
        try {
            println "jsonSoftwareParameter.xxx.id"
            softwareProject.software = Software.get(jsonSoftwareParameter.software.id)
            softwareProject.project = Project.get(jsonSoftwareParameter.project.id)
        }
        catch (Exception e) {
            println "jsonSoftwareParameter.idXXX"
            softwareProject.software = Software.get(jsonSoftwareParameter.software)
            softwareProject.project = Project.get(jsonSoftwareParameter.project)
        }
        if (!softwareProject.software) throw new WrongArgumentException("Software ${jsonSoftwareParameter.software.toString()} doesn't exist!")
        if (!softwareProject.project) throw new WrongArgumentException("Project ${jsonSoftwareParameter.project.toString()} doesn't exist!")
        return softwareProject;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + SoftwareProject.class
        JSON.registerObjectMarshaller(SoftwareProject) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['software'] = it.software?.id
            returnArray['name'] = it.software?.name
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }    
}
