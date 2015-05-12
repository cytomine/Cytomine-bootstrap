package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Property
import be.cytomine.ontology.Term
import be.cytomine.processing.*
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.JobParameterAPI
import be.cytomine.test.http.JobTemplateAPI
import be.cytomine.test.http.JobTemplateAnnotationAPI
import be.cytomine.utils.UpdateData
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class JobTemplateTests {

    void testGetJobTemplateWithCredential() {
        def template = BasicInstanceBuilder.getJobTemplate()
        def result = JobTemplateAPI.show(template.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.id.toString()== template.id.toString()

        result = JobAPI.show(template.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.id.toString()== template.id.toString()
    }

    void testListTemplateByProjectAndSoftware() {
        def template = BasicInstanceBuilder.getJobTemplate()
        def result = JobTemplateAPI.list(template.project.id, template.software.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert JobTemplateAPI.containsInJSONList(template.id,json)

        result = JobTemplateAPI.list(null, template.software.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(template.project.id, null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        assert JobTemplateAPI.containsInJSONList(template.id,json)

        result = JobTemplateAPI.list(null, null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(-99,null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(null,-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code        
    }



    void testAddJobTemplateCorrect() {
        def template = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.create(template.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        template = result.data
        Long idTemplate = template.id

        result = JobTemplateAPI.show(idTemplate, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.undo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.redo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

    }


    void testAddJobTemplateAlreadyExist() {
        def jobTemplate = BasicInstanceBuilder.getJobTemplateNotExist(true)
        log.info jobTemplate.encodeAsJSON()
        def result = JobTemplateAPI.create(jobTemplate.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testEditJobTemplate() {

        def template = BasicInstanceBuilder.getJobTemplate()
        def data = UpdateData.createUpdateSet(template,[project: [BasicInstanceBuilder.getProject(),BasicInstanceBuilder.getProjectNotExist(true)]])

        def result = JobTemplateAPI.update(template.id,data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idJobTemplate = json.jobtemplate.id
        def showResult = JobTemplateAPI.show(idJobTemplate,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testDeleteJobTemplate() {
        def jobTemplateToDelete = BasicInstanceBuilder.getJobTemplateNotExist()
        assert jobTemplateToDelete.save(flush: true) != null
        def idTemplate = jobTemplateToDelete.id

        def result = JobTemplateAPI.delete(jobTemplateToDelete.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = JobTemplateAPI.show(jobTemplateToDelete.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code

        result = JobTemplateAPI.undo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.redo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testDeleteJobTemplateNoExist() {
        def jobTemplateToDelete = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.delete(jobTemplateToDelete.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }


    void testAddJobTemplateCorrectWorkflow() {
        def template = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.create(template.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        template = result.data
        Long idTemplate = template.id

        result = JobTemplateAPI.show(idTemplate, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def parameter = BasicInstanceBuilder.getJobParameterNotExist()
        def json = parameter.encodeAsJSON()
        def jsonUpdate = JSON.parse(json)
        jsonUpdate.job = idTemplate
        json = jsonUpdate.toString()
        log.info json
        result = JobParameterAPI.create(json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.jobParameters.size()==1
    }


    void testAddJobTemplateConcreteCase() {
        //Algo compute area/number of annotation of a specific term inside a ROI annotation (for area: only the annotation part INSIDE the roi)
        //The job template is a shortcut to launch this algo for term "adeno"

        //create ontology and term
        Ontology ontology = BasicInstanceBuilder.getOntologyNotExist(true)
        Term term = BasicInstanceBuilder.getTermNotExist(ontology,true)
        term.name = "Adeno"
        BasicInstanceBuilder.saveDomain(term)

        //create a project + image
        Project project = BasicInstanceBuilder.getProjectNotExist(ontology,true)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        //add software + jobtemplate
        Software software = new Software(
                name: "computeAnnotationStats",
                serviceName: 'launchLocalScriptService',
                resultName:'DownloadFiles',
                description: 'Compute term stats area for an annotation',
                executeCommand: "groovy -cp algo/computeAnnotationStats/Cytomine-Java-Client.jar:algo/computeAnnotationStats/jts-1.13.jar algo/computeAnnotationStats/computeAnnotationStats.groovy"
        )
        software.save(failOnError: true,flush: true)

        SoftwareProject softwareProject = new SoftwareProject(software:software, project: project)
        softwareProject.save(failOnError: true,flush: true)

        SoftwareParameter param0 = new SoftwareParameter(software:software, name:"host",type:"String",required: true, index:10,setByServer:true)
        param0.save(failOnError: true,flush:true)

        SoftwareParameter param1 = new SoftwareParameter(software:software, name:"publicKey",type:"String",required: true, index:100,setByServer:true)
        param1.save(failOnError: true,flush:true)
        SoftwareParameter param2 = new SoftwareParameter(software:software, name:"privateKey",type:"String",required: true, index:200,setByServer:true)
        param2.save(failOnError: true,flush:true)

        SoftwareParameter param3 = new SoftwareParameter(software:software, name:"annotation",type:"Domain",required: true, index:400)
        param3.save(failOnError: true,flush:true)
        SoftwareParameter param4 = new SoftwareParameter(software:software, name:"term",type:"Domain",required: true, index:500)
        param4.save(failOnError: true,flush:true)

        JobTemplate jobTemplate = new JobTemplate(name:"ComputeAdenocarcinomesStat", software: software, project: project)
        jobTemplate.save(failOnError: true,flush:true)

        JobParameter paramTmpl1 = new JobParameter(job: jobTemplate,softwareParameter: param4, value: term.id+"")
        paramTmpl1.save(failOnError: true, flush:true)

        //add 3 annotation + 1 roi
        def polygons = [
                "POLYGON ((11504 17216, 12208 19104, 14640 19264, 15952 17952, 15216 16928, 12944 16704, 12784 16672, 11504 17216))",
                "POLYGON ((12432 22400, 14736 24576, 16592 22464, 16688 21056, 15152 20160, 13648 20928, 12432 22400))",
                "POLYGON ((6864 19040, 8112 21248, 10192 20256, 10320 18464, 8656 17312, 7664 17760, 6864 19040))"
        ]

        polygons.each {
            BasicInstanceBuilder.getUserAnnotationNotExist(image,it,BasicInstanceBuilder.getUser(),term)
        }


        RoiAnnotation roi = BasicInstanceBuilder.getRoiAnnotationNotExist(image,BasicInstanceBuilder.getUser())
        roi.location = new WKTReader().read("POLYGON ((9616 16480, 8048 21120, 10480 24640, 16240 25120, 16240.000000000002 16256, 9616 16480))")
        BasicInstanceBuilder.saveDomain(roi)

        //add job template annotation link
        def JobTemplateAnnotationToAdd = new JobTemplateAnnotation(jobTemplate: jobTemplate, annotationIdent: roi.id, annotationClassName: roi.class.name)
        def result = JobTemplateAnnotationAPI.create(JobTemplateAnnotationToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idJob = result.job.id

        result = JobAPI.execute(idJob, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //wait


        //check annotation property ROI
        println Property.findAllByDomainIdent(roi.id)
        def areaProp
        def numberProp

        def start = System.currentTimeMillis()

        while(System.currentTimeMillis()-start<10000 && !areaProp) {
            areaProp = Property.findByDomainIdentAndKey(roi.id,"AREA_OF_"+term.name)
            numberProp = Property.findByDomainIdentAndKey(roi.id,"NUMBER_OF_"+term.name)
        }

        assert numberProp
        assert numberProp.value.equals("3")


        assert areaProp
        assert areaProp.value.equals("23108698pixels²")
    }


}
