var AnnotationFilterModel = Backbone.Model.extend({

    url:function () {
        var base = 'api/annotationfilter';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


var AnnotationFilterCollection = Backbone.Collection.extend({
    model:AnnotationFilterModel,
    initialize:function (options) {
        this.project = options.project
    },
    url:function () {
        return "api/annotationfilter.json?project=" + this.project;
    }
});