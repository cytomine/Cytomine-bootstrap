var AnnotationView = Backbone.View.extend({
    tagName: "div",
    nb_thumb_by_page: 50,
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

        self.model.offset = (self.page) * self.nb_thumb_by_page;
        self.model.maxResult = self.nb_thumb_by_page;

        self.model.fetch({
            success: function (collection, response) {
                $(self.el).empty();
                collection.build();
                self.model = collection;
                self.nbAnnotation = collection.fullSize;
                self.initPagination();
                self.appendThumbs(self.page);

            }});
        return this;
    },
    initPagination: function () {
        var self = this;
        var nb_pages = Math.ceil(self.nbAnnotation / this.nb_thumb_by_page);
        if (nb_pages < 2) {
            return;
        } //paginator not useful
        require(["text!application/templates/dashboard/Pagination.tpl.html"], function (paginationTpl) {
            var pagination = _.template(paginationTpl, { term: self.term});
            $(self.el).append(pagination);
            var $pagination = $(self.el).find("#pagination-term-" + self.term).find("ul");
            var className = (self.page == 0) ? "prev disabled" : "";
            var pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>&larr; Previous</a></li>", { className: className, page: self.page - 1});
            $pagination.append(pageLink);
            var shiftUp = (self.page - self.pagination_window < 0) ? Math.abs(self.page - self.pagination_window) : 0;
            var shiftDown = (self.page + self.pagination_window >= nb_pages) ? Math.abs(self.pagination_window + self.page - nb_pages + 1) : 0;
            for (var i = Math.max(0, self.page - self.pagination_window - shiftDown); i < Math.min(nb_pages, self.page + self.pagination_window + shiftUp + 1); i++) {
                var linkID = "term-" + self.term + "-page-" + i;
                className = (i == self.page) ? "active" : "";
                pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'><%= page %></a></li>", {
                    className: className,
                    linkID: linkID,
                    page: i
                });
                $pagination.append(pageLink);
            }
            var className = (self.page == nb_pages - 1) ? "next disabled" : "";
            pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>Next &rarr;</a></li>", { className: className, page: self.page + 1});
            $pagination.append(pageLink);
            $pagination.find("a").click(function () {
                var page = parseInt($(this).attr("data-page"));
                if (page >= 0 && page < nb_pages) {
                    self.switchToPage(page);
                }
                return false;
            });

        });
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
