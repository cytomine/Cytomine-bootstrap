var ProjectDashboardImages = Backbone.View.extend({
   imagesView : null, //image view
   images : null,
   imagesTabsView : null,
   imagesThumbOrTab : null, //0=thumb, 1=tab
   projectImages : null,
   /**
    * Get and Print ALL images (use for the first time)
    */
   fetchImages : function() {
      $('#imageThumbs'+this.model.id).button({
         text: false,
         icons: {
            primary: "ui-icon-image"

         }
      });
      $('#imageArray'+this.model.id).button({
         text: false,
         icons: {
            primary: "ui-icon-calculator"

         }
      });
      var self = this;

      new ImageInstanceCollection({project:self.model.get('id')}).fetch({
         success : function (collection, response) {

            self.projectImages = collection;
            self.imagesView = new ImageView({
               page : 0,
               model : collection,
               el:$("#tabs-projectImageThumb"+self.model.get('id')),
               container : window.app.view.components.warehouse
            }).render();


            self.imagesTabsView = new ImageTabsView({
               model : collection,
               el:$("#tabs-projectImageListing"+self.model.get('id')),
               container : window.app.view.components.warehouse,
               idProject : self.model.id
            }).render();



            /*$("#tabs-images-listing-"+ self.model.get('id')).tabs();
             self.selectTab(1);                                      */
            //$("#tabs-images-listing-"+ self.model.get('id')).show();
         }});

   },
   changeImagePage : function(page) {
      var self = this;
      //$("#tabs-projectImageThumb"+self.model.get('id')).empty();
      self.imagesView = new ImageView({
         page : page,
         model : self.projectImages,
         el:$("#tabs-projectImageThumb"+self.model.get('id')),
         container : window.app.view.components.warehouse
      }).render();
   },

   /**
    * Get and Print only new images and remove delted images
    */
   refreshImagesThumbs : function() {
      var self = this;
      if (self.imagesView == null) {
         self.fetchImages();
         return;
      }
      new ImageInstanceCollection({project:self.model.get('id')}).fetch({
         success : function (collection, response) {
            self.imagesView.refresh(collection);
         }});
   },
   refreshImagesTable : function() {

      var self = this;
      if(self.imagesTabsView==null) return; //imageView is not yet build
      new ImageInstanceCollection({project:self.model.get('id')}).fetch({
         success : function (collection, response) {
            self.imagesTabsView.refresh(collection);
         }});
   }

});