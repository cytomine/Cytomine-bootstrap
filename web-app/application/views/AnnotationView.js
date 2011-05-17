var AnnotationView = Backbone.View.extend({
    tagName : "div",
    annotations : null, //array of annotations that are printed
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

        self.annotations = new Array();

        self.model.each(function(annotation) {
            if ((cpt >= inf) && (cpt < sup)) {
                var thumb = new AnnotationThumbView({
                    model : annotation,
                    className : "thumb-wrap",
                    id : "annotationthumb"+annotation.get('id')
                }).render();
                $(self.el).append(thumb.el);
            }
            cpt++;
            self.annotations.push(annotation.id);
        });
    },
    /**
     * Add the thumb annotation
     * @param annotation Annotation model
     */
    add : function(annotation) {
        console.log("AnnotationView: add " + annotation.id);
        var self = this;
        var thumb = new AnnotationThumbView({
            model : annotation,
            className : "thumb-wrap",
            id : "thumb"+annotation.get('id')
        }).render();
        $(self.el).append(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove : function (idAnnotation) {
        console.log("AnnotationView: remove "+idAnnotation);
        $("#thumb"+idAnnotation).remove();
    },
    /**
     * Refresh thumb with newAnnotations collection:
     * -Add annotations thumb from newAnnotations which are not already in the thumb set
     * -Remove annotations which are not in newAnnotations but well in the thumb set
     * @param newAnnotations newAnnotations collection
     */
    refresh : function(newAnnotations) {

        var self = this;
        console.log("AnnotationView: refresh");
        console.log(self.annotations);
        console.log(newAnnotations.models);


        var arrayDeletedAnnotations = self.annotations;
        newAnnotations.each(function(annotation) {
            //if annotation is not in table, add it
            if(_.indexOf(self.annotations, annotation.id)==-1){
                self.add(annotation);
                self.annotations.push(annotation.id);
            }
            /*
             * We remove each "new" annotation from  arrayDeletedAnnotations
             * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
             * in the set of new annotations
             */
            console.log("arrayDeletedAnnotations:"+arrayDeletedAnnotations);
            arrayDeletedAnnotations = _.without(arrayDeletedAnnotations,annotation.id);

        });

        arrayDeletedAnnotations.forEach(function(removeAnnotation) {
            self.remove(removeAnnotation);
            self.annotations = _.without(self.annotations,removeAnnotation);
        }
                );

    }


});
