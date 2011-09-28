var AnnotationThumbView = Backbone.View.extend({

       events: {

       },

       initialize: function(options) {
          this.id = "annotationthumb"+this.model.get('id');
          _.bindAll(this, 'render');
       },

       render: function() {
          var json = this.model.toJSON();
          var self = this;
          require(["text!application/templates/dashboard/AnnotationThumb.tpl.html"], function(tpl) {
             $(self.el).html(_.template(tpl, json));
          });
          return this;
       }
    });
