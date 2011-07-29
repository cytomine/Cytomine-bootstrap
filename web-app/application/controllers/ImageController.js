
var ImageController = Backbone.Controller.extend({

   routes: {
      "image"            :   "image",
      "image/p:page"     :   "image"
   },

   image : function(page) {
      if (!this.view) {
         this.view = new ImageView({
            page : page,
            model : window.app.models.images,
            el:$("#warehouse > .image"),
            container : window.app.view.components.warehouse
         }).render();

         this.view.container.views.image = this.view;
      }

      this.view.container.show(this.view, "#warehouse > .sidebar", "image");
      window.app.view.showComponent(window.app.view.components.warehouse);
   }

});