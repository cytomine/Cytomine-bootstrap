var ReviewAnnotationListing = Backbone.View.extend({
    tagName: "div",
    pagination_window: 3,
    nbAnnotation: -1,
    initialize: function (options) {
        this.page = options.page;
        this.term = options.term;
        this.annotations = null; //array of annotations that are printed
        if (this.page == undefined) {
            this.page = 0;
        }
    },
    render: function (project,user,term) {
        var self = this;
        console.log("render: project="+project + " user="+user + " term="+term);
        self.project = project;
        self.user = user;
        self.term = term;
        self.refresh();
        return this;
    },
    refresh: function () {
        var self = this;

        self.model = new AnnotationCollection({project: self.project, term: self.term, users: (self.user? [self.user] : null),reviewed:false, max: 20});

        self.model.goTo(this.page,{
            success: function (collection, response) {
                $(self.el).empty();
                self.model = collection;
                self.nbAnnotation = collection.fullSize;
                self.initPagination();
                self.appendThumbs(self.page);

        }});
    },
    nextOne : function() {

    },
    initPagination: function () {
        var self = this;
         var nbPages = self.model.getNumberOfPages();

         if(nbPages<2) {
             return;
         } else {
            require(["text!application/templates/dashboard/Pagination.tpl.html"], function (paginationTpl) {

                var pagination = _.template(paginationTpl, { term: "all"});
                $(self.el).append(pagination);
                var $pagination = $(self.el).find("#pagination-term-" + "all").find("ul");
                var className = (self.page == 0) ? "prev disabled" : "";

                var pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>&larr; Previous</a></li>", { className: className, page: self.page - 1});
                $pagination.append(pageLink);
                var shiftUp = (self.page - self.pagination_window < 0) ? Math.abs(self.page - self.pagination_window) : 0;
                var shiftDown = (self.page + self.pagination_window >= nbPages) ? Math.abs(self.pagination_window + self.page - nbPages + 1) : 0;

                for (var i = Math.max(0, self.page - self.pagination_window - shiftDown); i < Math.min(nbPages, self.page + self.pagination_window + shiftUp + 1); i++) {
                    var linkID = "term-" + "all" + "-page-" + i;
                    className = (i == self.page) ? "active" : "";
                    pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'><%= page %></a></li>", {
                        className: className,
                        linkID: linkID,
                        page: i
                    });
                    $pagination.append(pageLink);
                }
                var className = (self.page == nbPages - 1) ? "next disabled" : "";
                pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>Next &rarr;</a></li>", { className: className, page: self.page + 1});
                $pagination.append(pageLink);
                $pagination.find("a").click(function (event) {
                    event.preventDefault();
                    var page = parseInt($(this).attr("data-page"));
                    if (page >= 0 && page < nbPages) {
                        self.switchToPage(page);
                    }
                    return false;
                });

            });
         }
    },
    switchToPage: function (page) {
        var self = this;
        self.page = page;
        $(self.el).empty();
        self.refresh();
    },
    appendThumbs: function (page) {
        var self = this;
        self.annotations = [];
        self.model.each(function (annotation) {
            var thumb = new AnnotationThumbView({
                model: annotation,
                className: "thumb-wrap",
                term: "all",
                reviewMode : true
            }).render();
            $(self.el).append(thumb.el);
            self.annotations.push(annotation.id);
        });
    }
});