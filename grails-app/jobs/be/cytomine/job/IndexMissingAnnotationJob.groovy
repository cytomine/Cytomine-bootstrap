package be.cytomine.job


class IndexMissingAnnotationJob {

    def imageRetrievalService

    static triggers = {
        String cronexpr = "0 0 1 * * ?"
        cron name: 'myIndexMissingAnnotationJobTrigger', cronExpression: cronexpr //"s m h D M W Y"
      }

     def execute(){
         //ask indexed annotation
         imageRetrievalService.indexMissingAnnotation()
     }



}
