var ImageView = Backbone.View.extend({
    tagName : "div",
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
                self.appendThumbs(self.page);

                $(window).scroll(function(){
                    if  (($(window).scrollTop() + 100) >= $(document).height() - $(window).height()){
                        self.appendThumbs(++self.page);
                    }
                });
            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
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
        });
    }
});
