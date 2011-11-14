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
   removeImageFilter : function (imageFilter) {
      var self = this;
      //use fake ID since backbone > 0.5 : we should destroy only object saved or fetched
      new ProjectImageFilterModel({ id : 1, project : self.model.id, imageFilter : imageFilter.get("id")}).destroy({
         success : function (model, response) {
            $(self.el).find("li.imageFilter"+imageFilter.get("id")).remove();
         }
      });
      return false;
   },
   renderFilters : function() {
      var self = this;
      var el = $(this.el).find(".image-filters");
      el.empty();
      new ProjectImageFilterCollection({ project : self.model.id}).fetch({
         success : function (imageFilters, response) {
            imageFilters.each(function (imageFilter) {
               self.renderImageFilter(imageFilter, el);
            });
         }
      });
   },
   renderImageFilter : function (imageFilter, el) {
      var self = this;
      var tpl = _.template("<li class='imageFilter<%=   id %>'><%=   name %> <a class='removeImageFilter<%=   id %>' href='#'><span class='label important'>Remove</span></a></li>", imageFilter.toJSON());
      $(el).append(tpl);
      $(this.el).find("a.removeImageFilter"+imageFilter.get("id")).click(function() {
         self.removeImageFilter(imageFilter);
         return false;
      });
   },
   initImageFilters : function() {
      var self = this;
      var el = $(this.el).find(".image-filters");

      new ImageFilterCollection().fetch({
         success : function (imageFilters, response) {
            imageFilters.each(function (imageFilter) {
               var option = _.template("<option value='<%=   id %>'><%=   name %></option>", imageFilter.toJSON());
               $(self.el).find("#addImageFilter").append(option);

            });
            $(self.el).find("#addImageFilterButton").click(function(){
               new ProjectImageFilterModel({ project : self.model.id, imageFilter : $(self.el).find("#addImageFilter").val()}).save({},{
                  success : function (imageFilter, response) {
                     self.renderImageFilter(imageFilter, el);
                  },
                  error : function (response) {

                  }
               });
               return false;
            });
         }
      });

      self.renderFilters();

   },
   initBatchProcessing : function () {
      console.log("initBatchProcessing");
      $(this.el).find(".batch-processing").append(":-) !");
   }
});