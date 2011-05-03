var ImageView = Backbone.View.extend({
    tagName : "div",
    // template : _.template($('#image-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;
        var tpl = ich.imageviewtpl({page : (Math.abs(self.page)+1)}, true);
        $(this.el).html(tpl);
        this.model.fetch({
            success: function(){
                var cpt = 0;
                var nb_thumb_by_page = 21;
                var inf = Math.abs(self.page) * nb_thumb_by_page;
                var sup = (Math.abs(self.page) + 1) * nb_thumb_by_page;
                $(self.el).hide();
                self.model.each(function(image) {
                    if ((cpt >= inf) && (cpt < sup)) {
                        var thumb = new ImageThumbView({
                            model : image
                        }).render();
                        $(self.el).append(thumb.el);
                    }
                    cpt++;
                });

                $(self.el).imagesLoaded( function(){
                    $(self.el).show();
                    $(self.el).isotope({
                        itemSelector: '.thumb-wrap'
                    });

                });

            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });




        return this;
    }
});
