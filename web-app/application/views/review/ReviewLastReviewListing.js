var ReviewLastReviewListing = Backbone.View.extend({
    project: null,
    user : null,
    initialize: function (options) {
        this.container = options.container;
       this.project = options.project;
       this.user = options.user;
    },
    render: function () {
        var self = this;
        console.log("*********************"+self.project);
        self.model = new AnnotationCollection({project: self.project, user: self.user, max: 5,reviewed:true});
        self.refresh();
        return this;
    },
    refresh :function() {
        var self = this;

        $(self.el).find("#lastReviewedAnnotation").empty();
        $(self.el).find("#lastReviewedAnnotation").append('<div class="alert alert-info"><i class="icon-refresh"/> Loading...</div>');


        self.model.fetch({
            success: function (collection, response) {
                self.model = collection;
                $(self.el).find("#lastReviewedAnnotation").empty();

                if(collection.length==0) {
                    $(self.el).append("<div class='alert alert-block'>No data to display</div> ");
                }

                self.model.each(function(rev) {
                    var thumb = new AnnotationThumbView({
                        model: rev,
                        className: "thumb-wrap",
                        term: "all",
                        reviewMode : true
                    }).render();

                    $(thumb.el).draggable({
                        scroll: true,
                        //scrollSpeed : 00,
                        revert: true,
                        delay: 500,
                        opacity: 0.35,
                        cursorAt: { top: 85, left: 90}
                    });

                    $(thumb.el).append("<p class='terms text-center'></p>")
                    var termNames = []
                    _.each(rev.get("term"), function (it) {
                        var term = window.app.status.currentTermsCollection.get(it);

                        termNames.push(_.template(self.container.termSpanTemplate,term.toJSON()));
                    });

                    $(thumb.el).find(".terms").append(termNames.join(", "));
                    $(thumb.el).find('div').css("margin","0px auto");
                    $(thumb.el).find('div').css("float","none");
                    $(thumb.el).append("<p class='text-center'>"+window.app.convertLongToDate(rev.get('created'))+ "</p>");
                    $(self.el).find("#lastReviewedAnnotation").append("<hr/>");
                    $(self.el).find("#lastReviewedAnnotation").append(thumb.el);
//                    $(thumb.el).find(".thumb-wrap").data("reviewed",'true');
//                    $(thumb.el).data("reviewed",'true');
                    $(self.el).attr("data-reviewed", "true");
                });
            }
        });

        $("#lastReviewListing").find(".thumb-wrap").draggable( {
              containment: self.el,
              stack: "#lastReviewListing .thumb-wrap",
              cursor: 'move',
              revert: true
         } );
    }
});