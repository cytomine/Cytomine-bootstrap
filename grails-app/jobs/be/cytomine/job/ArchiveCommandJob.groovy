package be.cytomine.job

class ArchiveCommandJob {

    def retrievalService
    def archiveCommandService
    def grailsApplication

    static triggers = {
        String cronexpr = "0 0 2 * * ?"
        cron name: 'myArchiveCommandJobTrigger', cronExpression: cronexpr //"s m h D M W Y"
      }


}
