var ProjectDashboardAlgos = Backbone.View.extend({
   rendered : false,
   initialize : function (options) {
      this.el = "#tabs-algos-" + this.model.id;
   },
   render : function() {
      this.initImageFilters();
      this.initBatchProcessing();
      this.rendered = true;
   },
   refresh : function() {
      if (!this.rendered) this.render();
   },
   initImageFilters : function() {
      console.log("initImageFilters");
      $(this.el).find(".image-filters").append(":-)");
   },
   initBatchProcessing : function () {
      console.log("initBatchProcessing");
      $(this.el).find(".batch-processing").append(":-) !");
   }
});