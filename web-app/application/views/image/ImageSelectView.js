
var ImageSelectView = Backbone.View.extend({

       events: {

       },

       initialize: function(options) {
          this.id = "thumb"+this.model.get('id');
          _.bindAll(this, 'render');
       },

       render: function() {
          var self = this;
          this.model.set({ project : window.app.status.currentProject });
          var self = this;
          require(["text!application/templates/image/ImageChoice.tpl.html"], function(tpl) {
             $(self.el).html(_.template(tpl, self.model.toJSON()));
          });
          return this;
       }
    });
