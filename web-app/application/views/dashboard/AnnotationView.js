var AnnotationView = Backbone.View.extend({
    tagName:"div",
    nb_thumb_by_page:50,
    pagination_window:3,
    nbAnnotation:-1,
    initialize:function (options) {
        this.page = options.page;
        this.term = options.term;
        this.annotations = null; //array of annotations that are printed
        if (this.page == undefined) this.page = 0;
    },
    render:function () {
        var self = this;

        self.model.offset = (self.page) * self.nb_thumb_by_page;
        self.model.maxResult = self.nb_thumb_by_page;

        self.model.fetch({
            success:function (collection, response) {

                $(self.el).empty();
                collection.build();
                self.model = collection;
                self.nbAnnotation = collection.fullSize;
                self.initPagination();
                self.appendThumbs(self.page);

            }});
        return this;
    },
    initPagination:function () {
        var self = this;
        var nb_pages = Math.ceil(self.nbAnnotation / this.nb_thumb_by_page);
        if (nb_pages < 2) return; //paginator not useful
        require(["text!application/templates/dashboard/Pagination.tpl.html"], function (paginationTpl) {
            var pagination = _.template(paginationTpl, { term:self.term});
            $(self.el).append(pagination);
            var $pagination = $(self.el).find("#pagination-term-" + self.term).find("ul");
            var className = (self.page == 0) ? "prev disabled" : "";
            var pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>&larr; Previous</a></li>", { className:className, page:self.page - 1});
            $pagination.append(pageLink);
            var shiftUp = (self.page - self.pagination_window < 0) ? Math.abs(self.page - self.pagination_window) : 0;
            var shiftDown = (self.page + self.pagination_window >= nb_pages) ? Math.abs(self.pagination_window + self.page - nb_pages + 1) : 0;
            for (var i = Math.max(0, self.page - self.pagination_window - shiftDown); i < Math.min(nb_pages, self.page + self.pagination_window + shiftUp + 1); i++) {
                var linkID = "term-" + self.term + "-page-" + i;
                className = (i == self.page) ? "active" : "";
                pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'><%= page %></a></li>", {
                    className:className,
                    linkID:linkID,
                    page:i
                });
                $pagination.append(pageLink);
            }
            var className = (self.page == nb_pages - 1) ? "next disabled" : "";
            pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>Next &rarr;</a></li>", { className:className, page:self.page + 1});
            $pagination.append(pageLink);
            $pagination.find("a").click(function () {
                var page = parseInt($(this).attr("data-page"));
                if (page >= 0 && page < nb_pages) self.switchToPage(page);
                return false;
            });

        });
    },
    switchToPage:function (page) {
        var self = this;
        self.page = page;
        $(self.el).empty();
        self.render();
    },
    appendThumbs:function (page) {

        var self = this;
        var cpt = 0;
        var inf = Math.abs(page) * self.nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * self.nb_thumb_by_page;

        self.annotations = [];

        //check if it exist annotations
        /*if (_.size(self.model) == 0) {
         (self.el).html("No annotation with this term");
         } */

        self.model.each(function (annotation) {
            //if ((cpt >= inf) && (cpt < sup)) {
            var thumb = new AnnotationThumbView({
                model:annotation,
                className:"thumb-wrap",
                term:self.term
                //id : "annotationthumb"+annotation.get('id')
            }).render();
            $(self.el).append(thumb.el);
            //}
            //cpt++;
            self.annotations.push(annotation.id);
        });
    },
    /**
     * Add the thumb annotation
     * @param annotation Annotation model
     */
    add:function (annotation) {

        var self = this;
        var thumb = new AnnotationThumbView({
            model:annotation,
            className:"thumb-wrap",
            id:"thumb" + annotation.get('id')
        }).render();
        $(self.el).prepend(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove:function (idAnnotation) {
        $("#thumb" + idAnnotation).remove();
    }
    /**
     * Refresh thumb with newAnnotations collection:
     * -Add annotations thumb from newAnnotations which are not already in the thumb set
     * -Remove annotations which are not in newAnnotations but well in the thumb set
     * @param newAnnotations newAnnotations collection
     */
//    refresh : function(newAnnotations) {
//        var self = this;
//
//
//        newAnnotations.fetch({
//            success : function (collection, response) {
//                collection.build();
//
//                var arrayDeletedAnnotations = self.annotations;
//                collection.each(function(annotation) {
//                    //if annotation is not in table, add it
//                    if(_.indexOf(self.annotations, annotation.id)==-1){
//                        self.add(annotation);
//                        self.annotations.push(annotation.id);
//                    }
//                    /*
//                     * We remove each "new" annotation from  arrayDeletedAnnotations
//                     * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
//                     * in the set of new annotations
//                     */
//                    //
//                    arrayDeletedAnnotations = _.without(arrayDeletedAnnotations,annotation.id);
//
//                });
//
//                arrayDeletedAnnotations.forEach(function(removeAnnotation) {
//                    self.remove(removeAnnotation);
//                    self.annotations = _.without(self.annotations,removeAnnotation);
//                });
//
//
//
//
//            }
//
//
//
//
//        });
//
//
//
//    }


});
