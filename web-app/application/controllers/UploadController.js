var UploadController = Backbone.Controller.extend({
       initialized  : true,
       routes: {
       },
       upload : function() {
          if (this.initialized) return;
          /* init upload */

          this.initialized = true;
       }
    });

