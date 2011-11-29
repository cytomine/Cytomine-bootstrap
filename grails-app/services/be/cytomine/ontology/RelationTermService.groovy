package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class RelationTermService extends ModelService {

    static transactional = true

    def springSecurityService
    def commandService
    def cytomineService
    def domainService

    boolean saveOnUndoRedoStack = true

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
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return deleteRelationTerm(json.relation ? json.relation : -1, json.idterm1, json.idterm2, currentUser)
    }

    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser) {
        return deleteRelationTerm(idRelation, idTerm1, idTerm2, currentUser, true)
    }

    def deleteRelationTerm(def idRelation, def idTerm1, def idTerm2, User currentUser, boolean printMessage) {
        def json = JSON.parse("{relation: $idRelation, term1: $idTerm1, term2: $idTerm2}")
        log.info "json=" + json
        return executeCommand(new DeleteCommand(user: currentUser,printMessage:printMessage), json)
    }

    def deleteRelationTermFromTerm(Term term, User currentUser) {
        def relationTerm = RelationTerm.findAllByTerm1OrTerm2(term, term)
        log.info "relationTerm= " + relationTerm.size()

        relationTerm.each { relterm ->
            log.info "unlink relterm:" + relationTerm.id
            deleteRelationTerm(relterm.relation.id, relterm.term1.id, relterm.term2.id, currentUser, false)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, boolean printMessage) {
        restore(RelationTerm.createFromDataWithId(json),printMessage)
    }
    def restore(RelationTerm domain, boolean printMessage) {
        //Build response message
        log.debug "domain="+domain + " responseService="+responseService
        def response = responseService.createResponseMessage(domain,[domain.id, domain.relation.name, domain.term1.name, domain.term2.name],printMessage,"Add",domain.getCallBack())
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
         destroy(RelationTerm.createFromData(json),printMessage)
    }
    def destroy(RelationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.relation.name, domain.term1.name, domain.term2.name],printMessage,"Delete",domain.getCallBack())
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
