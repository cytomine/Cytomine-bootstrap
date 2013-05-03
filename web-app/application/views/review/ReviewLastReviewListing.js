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
        self.model = new AnnotationReviewedCollection({project: self.project, user: self.user, max: 5});
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

                    $(thumb.el).append("<p class='terms text-center'></p>")
                    var termNames = []
                    _.each(rev.get("term"), function (it) {
                        var term = window.app.status.currentTermsCollection.get(it);
                        var termName = term.get('name');
                        termNames.push('<span class="label label-warning" style="background-color:'+term.get('color')+';">'+termName+'</span>')
                    });

                    $(thumb.el).find(".terms").append(termNames.join(", "));


                    $(self.el).find("#lastReviewedAnnotation").append("<br>");
                    $(self.el).find("#lastReviewedAnnotation").append(thumb.el);
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