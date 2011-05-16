var AnnotationView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;
        var tpl = ich.annotationviewtpl({page : (Math.abs(self.page)+1)}, true);
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
        var nb_thumb_by_page = 25;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;
        self.model.each(function(annotation) {
            if ((cpt >= inf) && (cpt < sup)) {
                console.log("*** CREATE THUMB ANNOTATION") ;
                var thumb = new AnnotationThumbView({
                    model : annotation,
                    className : "thumb-wrap",
                    id : "annotationthumb"+annotation.get('id')
                }).render();
                $(self.el).append(thumb.el);
            }
            cpt++;
        });
    }
});
