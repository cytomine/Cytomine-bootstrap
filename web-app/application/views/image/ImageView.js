var ImageView = Backbone.View.extend({
   tagName : "div",
   initialize: function(options) {
      this.images = null; //array of images that are printed
      this.container = options.container;
      this.page = options.page;
      this.nb_thumb_by_page = 30;
      this.appendingThumbs = false;
      if (this.page == undefined) this.page = 0;
   },
   render: function() {
      var self = this;
      $(self.el).empty();
      self.appendThumbs(self.page);

      $(window).scroll(function(){
         //1. Look if the tabs is active. don't append thumbs if not
         var currentUrl = "" + window.location;
         if (currentUrl.search("#tabs-images-") == -1){
            return;
         }
         //2. Look if we are already appending thumbs. If yes, return
         if (self.appendingThumbs) return;

         if  (($(window).scrollTop() + 50) >= $(document).height() - $(window).height()){
            console.log("$(window).scrollTop() : " + $(window).scrollTop());
            console.log("$(document).height()- $(window).height() " + ($(document).height() - $(window).height()));


            self.appendingThumbs = true;
            self.appendThumbs(++self.page);
            self.appendingThumbs = false;
         }
      });
      return this;
   },
   showLoading : function() {
      var opts = {
         pnotify_title: "Loading...",
         pnotify_text: "",
         pnotify_notice_icon: "ui-icon ui-icon-info",
         pnotify_hide : false,
         pnotify_closer: false,
         pnotify_history: false
      };
      var loadingMessage = $.pnotify(opts);
      setTimeout(function(){loadingMessage.remove();}, 2000);
   },
   appendThumbs : function(page) {

      var self = this;

      var inf = Math.abs(page) * this.nb_thumb_by_page;
      var sup = (Math.abs(page) + 1) * this.nb_thumb_by_page;

      if (Math.abs(page) * this.nb_thumb_by_page < self.model.size() ) {
         this.showLoading();
      }
      self.tabsContent = new Array();
      var cpt = inf;

      while (cpt < sup && cpt < this.model.size()) {
         var image  = this.model.at(cpt);
         var thumb = new ImageThumbView({
            model : image
         }).render();
         $(self.el).append(thumb.el);
         cpt++;
         self.tabsContent.push(image.id);
      }

   },
   /**
    * Add the thumb image
    * @param image Image model
    */
   add : function(image) {
      var self = this;
      var thumb = new ImageThumbView({
         model : image,
         className : "row",
         id : "thumb"+image.get('id')
      }).render();
      $(self.el).append(thumb.el);

   },
   /**
    * Remove thumb image with id
    * @param idImage  Image id
    */
   remove : function (idImage) {
      $("#thumb"+idImage).remove();
   },
   /**
    * Refresh thumb with newImages collection:
    * -Add images thumb from newImages which are not already in the thumb set
    * -Remove images which are not in newImages but well in the thumb set
    * @param newImages newImages collection
    */
   refresh : function(newImages) {
      return; //DESACTIVED, does not take into account the actual page & nb_thums_by_page
      var self = this;
      var arrayDeletedImages = self.tabsContent;
      newImages.each(function(image) {
         //if image is not in table, add it
         if(_.indexOf(self.tabsContent, image.id)==-1){
            self.add(image);
            self.tabsContent.push(image.id);
         }
         /*
          * We remove each "new" image from  arrayDeletedImage
          * At the end of the loop, element from arrayDeletedImages must be deleted because they aren't
          * in the set of new images
          */
         arrayDeletedImages = _.without(arrayDeletedImages,image.id);
      });

      arrayDeletedImages.forEach(function(removeImage) {
             self.remove(removeImage);
             self.tabsContent = _.without(self.tabsContent,removeImage);
          }
      );
   }
});
