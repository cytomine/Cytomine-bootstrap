package be.cytomine.job

import jsondoc.APIUtils


class ArchiveCommandJob {

    def retrievalService
    def archiveCommandService

    static triggers = {
        String cronexpr = "0 0 2 * * ?"
        cron name: 'myArchiveCommandJobTrigger', cronExpression: cronexpr //"s m h D M W Y"
      }

     def execute(){
         //ask indexed annotation
         //retrievalService.indexMissingAnnotation()
         APIUtils.buildApiRegistry(ctx, application)
     }



}
