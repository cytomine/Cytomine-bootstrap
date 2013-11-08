package be.cytomine.job


class ArchiveCommandJob {

    def retrievalService
    def archiveCommandService

    static triggers = {
        String cronexpr = "0 0 2 * * ?"
        cron name: 'myArciveCommandJobTrigger', cronExpression: cronexpr //"s m h D M W Y"
      }

     def execute(){
         //ask indexed annotation
         //retrievalService.indexMissingAnnotation()
         archiveCommandService.archiveOldCommand()
     }



}
