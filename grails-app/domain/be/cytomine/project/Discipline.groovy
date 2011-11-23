package be.cytomine.project

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SequenceDomain
import grails.converters.JSON

class Discipline extends SequenceDomain {

    String name

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Discipline.class
        JSON.registerObjectMarshaller(Discipline) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

      static Discipline createFromDataWithId(json)  {
        def domain = createFromData(json)
        domain.id = json.id
        return domain
    }

    static Discipline createFromData(jsonDiscipline) {
        def discipline = new Discipline()
        getFromData(discipline, jsonDiscipline)
    }

    static Discipline getFromData(discipline, jsonDiscipline) {
        String name = jsonDiscipline.name.toString()
        if (!name.equals("null"))
            discipline.name = jsonDiscipline.name.toUpperCase()
        else throw new WrongArgumentException("Discipline name cannot be null")
        return discipline;
    }
}
