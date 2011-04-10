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
                console.log("Model size=" + self.model.length);
                self.model.each(function(image) {
                    if ((cpt > inf) && (cpt <= sup)) {
                        var thumb = new ImageThumbView({
                            model : image
                        }).render();
                        $(self.el).append(thumb.el);
                    }
                    cpt++;
                });

                $(self.el).imagesLoaded( function(){
                    $(self.el).isotope({
                        itemSelector: '.thumb-wrap'
                    });

                });

                /*$(self.el).infinitescroll({
                    navSelector  : '#page_nav',    // selector for the paged navigation
                    nextSelector : '#page_nav a',  // selector for the NEXT link (to page 2)
                    itemSelector : '.thumb-wrap',     // selector for all items you'll retrieve
                    donetext  : 'No more pages to load.',
                    debug : true,
                    loadingImg : 'http://i.imgur.com/qkKy8.gif',
                    debug: false,
                    errorCallback: function() {
                        // fade out the error message after 2 seconds
                        console.log("ERROR INF");
                        //$('#infscr-loading').animate({opacity: .8},2000).fadeOut('normal');
                    }
                },
                    // call Isotope as a callback
                                         function( newElements ) {
                                             console.log("NEW ELEM");
                                             $(self.el).isotope( 'appended', $( newElements ) );
                                         });  */
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
