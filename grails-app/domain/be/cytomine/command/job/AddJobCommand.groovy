package be.cytomine.command.job

import be.cytomine.command.SimpleCommand
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import grails.converters.JSON
import be.cytomine.processing.Job

class AddJobCommand extends AddCommand implements SimpleCommand {

    def execute() {
       log.info("Execute")
       Job job=null
       try {
         def json = JSON.parse(postData)
         job = Job.createFromData(json)
         return super.validateAndSave(job,["#ID#",job] as Object[])
         //errors:
       }catch(ConstraintException  ex){
         return [data : [job:job,errors:job.retrieveErrors()], status : 400]
       }catch(IllegalArgumentException ex){
         return [data : [user:null,errors:["Cannot save job:"+ex.toString()]], status : 400]
       }

     }

}
