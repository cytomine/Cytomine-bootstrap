package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import grails.converters.JSON
import org.apache.log4j.Logger

class SoftwareProject extends CytomineDomain implements Serializable{

    Software software
    Project project

    static mapping = {
        id(generator: 'assigned', unique: true)
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
        } else {
            Logger.getLogger(this).info("no link between " + software + " " + project)
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
   static SoftwareProject createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static SoftwareProject createFromData(def json) {
        def softwareProject = new SoftwareProject()
        insertDataIntoDomain(softwareProject, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static SoftwareProject insertDataIntoDomain(def domain, def json) {
        try {
            domain.software = Software.get(json.software.id)
            domain.project = Project.get(json.project.id)
        }
        catch (Exception e) {
            domain.software = Software.get(json.software)
            domain.project = Project.get(json.project)
        }
        if (!domain.software) throw new WrongArgumentException("Software ${json.software.toString()} doesn't exist!")
        if (!domain.project) throw new WrongArgumentException("Project ${json.project.toString()} doesn't exist!")
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + SoftwareProject.class)
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
