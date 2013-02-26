var AnnotationView = Backbone.View.extend({
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
    render: function () {
        var self = this;

        self.model.goTo(this.page,{
            success: function (collection, response) {
                $(self.el).empty();
                self.model = collection;
                self.nbAnnotation = collection.fullSize;
                console.log("*******************************");
                console.log(self.nbAnnotation);
                self.initPagination();
                self.appendThumbs(self.page);

            }});

        return this;
    },
    initPagination: function () {
        var self = this;

        //self.model.

         var nbPages = self.model.getNumberOfPages();
        console.log("nbPages="+nbPages);
         if(nbPages<2) {
             return;
         } else {

            require(["text!application/templates/dashboard/Pagination.tpl.html"], function (paginationTpl) {

                var pagination = _.template(paginationTpl, { term: self.term});
                $(self.el).append(pagination);
                var $pagination = $(self.el).find("#pagination-term-" + self.term).find("ul");
                var className = (self.page == 0) ? "prev disabled" : "";

                var pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>&larr; Previous</a></li>", { className: className, page: self.page - 1});
                $pagination.append(pageLink);
                var shiftUp = (self.page - self.pagination_window < 0) ? Math.abs(self.page - self.pagination_window) : 0;
                var shiftDown = (self.page + self.pagination_window >= nbPages) ? Math.abs(self.pagination_window + self.page - nbPages + 1) : 0;

                for (var i = Math.max(0, self.page - self.pagination_window - shiftDown); i < Math.min(nbPages, self.page + self.pagination_window + shiftUp + 1); i++) {
                    var linkID = "term-" + self.term + "-page-" + i;
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
                $pagination.find("a").click(function () {
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
        self.render();
    },
    appendThumbs: function (page) {
        var self = this;
        self.annotations = [];
        self.model.each(function (annotation) {
            var thumb = new AnnotationThumbView({
                model: annotation,
                className: "thumb-wrap",
                term: self.term
            }).render();
            $(self.el).append(thumb.el);
            self.annotations.push(annotation.id);
        });
    },
    /**
     * Add the thumb annotation
     * @param annotation Annotation model
     */
    add: function (annotation) {

        var self = this;
        var thumb = new AnnotationThumbView({
            model: annotation,
            className: "thumb-wrap",
            id: "thumb" + annotation.get('id')
        }).render();
        $(self.el).prepend(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove: function (idAnnotation) {
        $("#thumb" + idAnnotation).remove();
    }

});
