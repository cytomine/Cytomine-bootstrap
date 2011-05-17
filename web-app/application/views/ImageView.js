var ImageView = Backbone.View.extend({
    tagName : "div",
    images : null, //array of images that are printed
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;
        var tpl = ich.imageviewtpl({page : (Math.abs(self.page)+1)}, true);
        $(this.el).html(tpl);
        self.appendThumbs(self.page);

        $(window).scroll(function(){
            if  (($(window).scrollTop() + 100) >= $(document).height() - $(window).height()){
                self.appendThumbs(++self.page);
            }
        });
        return this;
    },
    appendThumbs : function(page) {
        var self = this;
        var cpt = 0;
        var nb_thumb_by_page = 5000;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

        self.images = new Array();

        self.model.each(function(image) {
            if ((cpt >= inf) && (cpt < sup)) {
                var thumb = new ImageThumbView({
                    model : image,
                    className : "thumb-wrap",
                    id : "thumb"+image.get('id')
                }).render();
                $(self.el).append(thumb.el);
            }
            cpt++;
            self.images.push(image.id);
        });
    },
    /**
     * Add the thumb image
     * @param image Image model
     */
    add : function(image) {
        console.log("ImageView: add");
        var self = this;
        var thumb = new ImageThumbView({
            model : image,
            className : "thumb-wrap",
            id : "thumb"+image.get('id')
        }).render();
        $(self.el).append(thumb.el);

    },
    /**
     * Remove thumb image with id
     * @param idImage  Image id
     */
    remove : function (idImage) {
        console.log("ImageView: remove");
        $("#thumb"+idImage).remove();
    },
    /**
     * Refresh thumb with newImages collection:
     * -Add images thumb from newImages which are not already in the thumb set
     * -Remove images which are not in newImages but well in the thumb set
     * @param newImages newImages collection
     */
    refresh : function(newImages) {
        console.log("ImageView: refresh");

        var self = this;
        var arrayDeletedImages = self.images;
        newImages.each(function(image) {
            //if image is not in table, add it
            if(_.indexOf(self.images, image.id)==-1){
                self.add(image);
                self.images.push(image.id);
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
            self.images = _.without(self.images,removeImage);
        }
                );

    }
});
