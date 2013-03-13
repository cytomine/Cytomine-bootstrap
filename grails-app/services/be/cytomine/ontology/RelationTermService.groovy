package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.*

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
    def get(Relation relation, Term term1, Term term2) {
        SecurityACL.check(term1.container(),READ)
        SecurityACL.check(term2.container(),READ)
        RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @param position Term position in relation (term x PARENT term y => term x position 1, term y position 2)
     * @return Relation term list
     */
    def list(Term term, def position) {
        SecurityACL.check(term.container(),READ)
        position == "1" ? RelationTerm.findAllByTerm1(term) : RelationTerm.findAllByTerm2(term)
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @return Relation term list
     */
    def list(Term term) {
        SecurityACL.check(term.container(),READ)
        def relation1 = RelationTerm.findAllByTerm1(term);
        def relation2 = RelationTerm.findAllByTerm2(term);
        def all = (relation1 << relation2).flatten();
        return all
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @return Response structure (new domain data, old domain data..)
     */
    def add(def json) {
        println "json=$json"
        SecurityACL.check(json.term1,Term,"getOntology",WRITE)
        SecurityACL.check(json.term2,Term,"getOntology",WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(RelationTerm domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
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
