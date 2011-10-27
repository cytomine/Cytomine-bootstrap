package be.cytomine.ontology

import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.security.User

class AnnotationTerm extends SequenceDomain implements Serializable{

    Annotation annotation
    Term term
    User user

    static mapping = {
        id (generator:'assigned', unique : true)
    }
    String toString()
    {
        "[" + this.id + " <" + annotation + "," + term + ","+user+">]"
    }

    def getIdTerm() {
        if(this.termId) return this.termId
        else return this.term?.id
    }

    static AnnotationTerm link(Annotation annotation,Term term, User user) {
        if(!annotation)  throw new IllegalArgumentException("Annotation cannot be null")
        if(!term)  throw new IllegalArgumentException("Term cannot be null")
        if(!user)  throw new IllegalArgumentException("User cannot be null")
        def annotationTerm = AnnotationTerm.findWhere('annotation':annotation, 'term':term,'user':user)
        if(annotationTerm) throw new IllegalArgumentException("Annotation - term already exist")
        //Annotation.withTransaction {
        if (!annotationTerm) {
            annotationTerm = new AnnotationTerm(user:user)
            annotation?.addToAnnotationTerm(annotationTerm)
            term?.addToAnnotationTerm(annotationTerm)
            println "save annotationTerm"
            annotation.refresh()
            term.refresh()
            annotationTerm.save(flush:true)
        } else throw new IllegalArgumentException("Annotation " + annotation.id + " and term " + term.id + " are already mapped with user " + user.id)
        //}
        return annotationTerm
    }


    static AnnotationTerm link(long id,Annotation annotation,Term term, User user) {

        if(!annotation)  throw new IllegalArgumentException("Annotation cannot be null")
        if(!term)  throw new IllegalArgumentException("Term cannot be null")
        if(!user)  throw new IllegalArgumentException("User cannot be null")
        def annotationTerm = AnnotationTerm.findWhere('annotation':annotation, 'term':term,'user':user)
        if(annotationTerm) throw new IllegalArgumentException("Annotation - term already exist")

        if (!annotationTerm) {
            annotationTerm = new AnnotationTerm(user:user)
            annotationTerm.id = id
            annotation?.addToAnnotationTerm(annotationTerm)
            term?.addToAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            annotationTerm.save(flush:true)
        } else throw new IllegalArgumentException("Annotation " + annotation.id + " and term " + term.id + " are already mapped with user " + user.id)
        return annotationTerm
    }

    static void unlink(Annotation annotation, Term term,User user) {

        if(!annotation)  throw new IllegalArgumentException("Annotation cannot be null")
        if(!term)  throw new IllegalArgumentException("Term cannot be null")
        if(!user)  throw new IllegalArgumentException("User cannot be null")
        def annotationTerm = AnnotationTerm.findWhere('annotation':annotation, 'term':term,'user':user)
        if(!annotationTerm) throw new IllegalArgumentException("Annotation - term - user not exist")

        if (annotationTerm) {
            annotation?.removeFromAnnotationTerm(annotationTerm)
            term?.removeFromAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            println "delete annotationTerm="+annotationTerm
            annotationTerm.delete(flush : true)

        }
    }

    static AnnotationTerm createAnnotationTermFromData(jsonAnnotationTerm) {
        def annotationTerm = new AnnotationTerm()
        getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm)
    }

    static AnnotationTerm getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm) {
        println "jsonAnnotationTerm from getAnnotationTermFromData = " + jsonAnnotationTerm
        annotationTerm.annotation = Annotation.get(jsonAnnotationTerm.annotation.toString())
        annotationTerm.term = Term.get(jsonAnnotationTerm.term.toString())
        annotationTerm.user = User.get(jsonAnnotationTerm.user.toString())
        return annotationTerm;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + AnnotationTerm.class
        JSON.registerObjectMarshaller(AnnotationTerm) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['annotation'] = it.annotation?.id
            returnArray['term'] = it.term?.id
            returnArray['user'] = it.user?.id
            return returnArray
        }
    }
}
