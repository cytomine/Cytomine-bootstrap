package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import com.imon.apidocs.annotations.Api
import com.imon.apidocs.annotations.ApiOperation
/**
 * Controller for term request (word in ontology)
 */

//@Produces({"application/json"})
@Api(module="companyModule", description="Deals with company resource", href = "https://sites.google.com/home")
class RestTermController extends RestController {

    def termService

    /**
     * List all term available
     */
//    @GET
//    @ApiOperation(value = "List all term available")
    def list = {
        responseSuccess(termService.list())
    }

    /**
     * Get a single term
     */
//    @GET
//    @Path("/{id}")
//    @ApiOperation(value = "Retrieve a specific term")
//    def show = {
//        Term term = termService.read(params.long('id'))
//        if (term) {
//            responseSuccess(term)
//        } else {
//            responseNotFound("Term", params.id)
//        }
//    }

//    @GET
//    @Path("/{id}")
//    @ApiOperation(value = "Logs out current logged in user session")
//    public def retrieve(@ApiParam(value = "List of user object", required = true) String id) {
//        println "show:" + params
//        return null
//    }
//    @GET
//    @Path("/{id}")
//    @ApiOperation(value = "Logs out current logged in user session")
//    public def retrieve() {
//        println "show:" + params
//        return null
//    }

//    @POST
//    @ApiOperation(value = "Create user", notes = "This can only be done by the logged in user.")
//    public Response createUser(
//            @ApiParam(value = "Created user object", required = true) User user) {
//
//        return Response.ok().entity("").build();
//    }
      def show() {
        Term term = termService.read(params.long('id'))
        if (term) {
            responseSuccess(term)
        } else {
            responseNotFound("Term", params.id)
        }
      }

//    @POST
//    @Path("/createWithList")
//    @ApiOperation(value = "Creates list of users with given input array")
//    public Response createUsersWithListInput(@ApiParam(value = "List of user object", required = true) java.util.List<User> users) {
//        for (User user : users) {
//            userData.addUser(user);
//        }
//        return Response.ok().entity("").build();
//    }

//
//    @GET
//    @Path("/logout")
//    @ApiOperation(value = "Logs out current logged in user session")
//    public Response logoutUser() {
//        return Response.ok().entity("").build();
//    }

    /**
     * Get all term in the ontology
     */
    def listByOntology = {
        Ontology ontology = Ontology.read(params.idontology)
        if (ontology) {
            responseSuccess(termService.list(ontology))
        } else {
            responseNotFound("Term", "Ontology", params.idontology)
        }
    }

    /**
     * Get all term for the project ontology
     */
    def listAllByProject = {
        Project project = Project.read(params.idProject)
        if (project && project.ontology) {
            responseSuccess(termService.list(project))
        }
        else {
            responseNotFound("Term", "Project", params.idProject)
        }
    }

    /**
     * Get the stats info for a term
     */
    def statProject = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(termService.statProject(term))
        else responseNotFound("Project", params.id)
    }

    /**
     * Add a new term
     * Use next add relation-term to add relation with another term
     */
    def add = {
        add(termService, request.JSON)
    }

    /**
     * Update a term
     */
    def update = {
        update(termService, request.JSON)
    }

    /**
     * Delete a term
     */
    def delete = {
        delete(termService, JSON.parse("{id : $params.id}"),null)
    }

}
