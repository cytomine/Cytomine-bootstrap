/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var JobDataModel = Backbone.Model.extend({
   url : function() {
       var base = 'api/jobdata';
       var format = '.json';
       if (this.isNew()) return base + format;
       return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
   }
});

// define our collection
var JobDataCollection = Backbone.Collection.extend({
   model: JobModel,

   url: function() {
      if (this.job != undefined) {
         return "api/job/" + this.job + "/jobdata.json";
      }else {
         return "api/jobdata.json";
      }
   },
   initialize: function (options) {
      this.job = options.job;
   },
   comparator : function (jobdata) {
      return -jobdata.get("created");
   }
});
