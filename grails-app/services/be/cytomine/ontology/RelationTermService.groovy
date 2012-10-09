package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class RelationTermService extends ModelService {

    static transactional = true

    def springSecurityService
    def commandService
    def cytomineService
    def domainService

    final boolean saveOnUndoRedoStack = true

    def list() {
        RelationTerm.list()
    }

    def list(Relation relation) {
        RelationTerm.findAllByRelation(relation)
    }

    def list(Term term, def position) {
        position == "1" ? RelationTerm.findAllByTerm1(term) : RelationTerm.findAllByTerm2(term)
    }

    def list(Term term) {
        def relation1 = RelationTerm.findAllByTerm1(term);
        def relation2 = RelationTerm.findAllByTerm2(term);
        def all = (relation1 << relation2).flatten();
        return all
    }

    def get(Relation relation, Term term1, Term term2) {
        RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
    }



    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    def delete(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return deleteRelationTerm(json.relation ? json.relation : -1, json.term1, json.term2, currentUser, null)
    }

    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser,Transaction transaction) {
        return deleteRelationTerm(idRelation, idTerm1, idTerm2, currentUser, true,transaction)
    }

    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser, boolean printMessage,Transaction transaction) {
        def json = JSON.parse("{relation: $idRelation, term1: $idTerm1, term2: $idTerm2}")
        log.info "json=" + json
        return executeCommand(new DeleteCommand(user: currentUser, printMessage: printMessage,transaction:transaction), json)
    }

    def deleteRelationTermFromTerm(Term term, User currentUser, Transaction transaction) {
        def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term, term)
        log.info "relationTerm= " + relationTerm.size()

        relationTerm.each { relterm ->
            log.info "unlink relterm:" + relationTerm.id
            deleteRelationTerm(relterm.relation.id, relterm.term1.id, relterm.term2.id, currentUser, false,transaction)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(RelationTerm.createFromDataWithId(json), printMessage)
    }

    def create(RelationTerm domain, boolean printMessage) {
        //Build response message
        log.debug "domain=" + domain + " responseService=" + responseService
        def response = responseService.createResponseMessage(domain, [domain.id, domain.relation.name, domain.term1.name, domain.term2.name], printMessage, "Add", domain.getCallBack())
        //Save new object
        RelationTerm.link(domain.relation, domain.term1, domain.term2)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, boolean printMessage) {
        destroy(RelationTerm.createFromData(json), printMessage)
    }

    def destroy(RelationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.relation.name, domain.term1.name, domain.term2.name], printMessage, "Delete", domain.getCallBack())
        //Delete new object
        RelationTerm.unlink(domain.relation, domain.term1, domain.term2)
        return response
    }

    RelationTerm createFromJSON(def json) {
        return RelationTerm.createFromData(json)
    }

    def retrieve(def json) {
        Relation relation = Relation.get(json.relation)
        Term term1 = Term.get(json.term1)
        Term term2 = Term.get(json.term2)
        RelationTerm relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) throw new ObjectNotFoundException("Relation-term not found ($relation,$term1,$term2)")
        return relationTerm
    }
}
