package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class RelationTermService extends ModelService {

    static transactional = true

    def springSecurityService
    def commandService
    def cytomineService
    def transactionService

    def currentDomain() {
        return RelationTerm
    }

    /**
     * Get a relation term
     */
    @PreAuthorize("(#term1.ontology.hasPermission('READ') and #term2.ontology.hasPermission('READ')) or hasRole('ROLE_ADMIN')")
    def get(Relation relation, Term term1, Term term2) {
        RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
    }

    /**
     * List all relation term (admin only)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        RelationTerm.list()
    }

    /**
     * List all relation term  for a relation (admin only)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list(Relation relation) {
        RelationTerm.findAllByRelation(relation)
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @param position Term position in relation (term x PARENT term y => term x position 1, term y position 2)
     * @return Relation term list
     */
    @PreAuthorize("#term.ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Term term, def position) {
        position == "1" ? RelationTerm.findAllByTerm1(term) : RelationTerm.findAllByTerm2(term)
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @return Relation term list
     */
    @PreAuthorize("#term.ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Term term) {
        def relation1 = RelationTerm.findAllByTerm1(term);
        def relation2 = RelationTerm.findAllByTerm2(term);
        def all = (relation1 << relation2).flatten();
        return all
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("(#security.checkTermAccess(#json['term1']) and #security.checkTermAccess(#json['term2'])) or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkOntologyAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json), transactionService.start())
    }

    def delete(RelationTerm rt, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{relation: ${rt.relation.id}, term1:${rt.term1.id}, term2:${rt.term2.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.relation.name, domain.term1.name, domain.term2.name]
    }

    /**
      * Retrieve domain thanks to a JSON object
      * @param json JSON with new domain info
      * @return domain retrieve thanks to json
      */
    def retrieve(Map json) {
        Relation relation = Relation.get(json.relation)
        Term term1 = Term.get(json.term1)
        Term term2 = Term.get(json.term2)
        RelationTerm relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) {
            throw new ObjectNotFoundException("Relation-term not found ($relation,$term1,$term2)")
        }
        return relationTerm
    }
}
