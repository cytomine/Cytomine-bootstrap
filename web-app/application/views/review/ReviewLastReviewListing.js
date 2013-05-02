var ReviewLastReviewListing = Backbone.View.extend({
    project: null,
    user : null,
    initialize: function (options) {
       this.project = options.project;
       this.user = options.user;
    },
    render: function () {
        var self = this;
        console.log("*********************"+self.project);
        self.model = new AnnotationReviewedCollection({project: self.project, user: self.user, max: 5});
        self.refresh();
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
                    $(self.el).find("#lastReviewedAnnotation").append(thumb.el);
                });
            }
        });
    }
});