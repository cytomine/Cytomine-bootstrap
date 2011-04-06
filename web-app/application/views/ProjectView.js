var ProjectView = Backbone.View.extend({
    tagName : "div",
    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container
    },
    render: function() {
        $(this.el).html(ich.projectviewtpl({}, true));
        //$(this.el).html(this.template());
        var self = this;
        /*this.model.fetch({
            success: function(){
                self.model.each(function(image) {
                    new ImageThumbView({
                        el : self.el,
                        model : image
                    }).render();
                });

                $(self.el).imagesLoaded( function(){
                    console.log("imageLoaded");
                    $(this).isotope({
                        itemSelector: '.thumb-wrap'
                    });
                });
            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });*/




        return this;
    }
});
