var AnnotationModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        var params = "";
        if (this.fill) {
            params = "?fill=true";
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format + params;
    },
    initialize: function (options) {
        this.id = options.id;
        this.fill = options.fill;
    }
});


var AnnotationCorrectionModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/annotationcorrection';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize: function (options) {
        this.id = options.id;
    }
});

var AnnotationReviewedModel = Backbone.Model.extend({
    url: function () {
        if (this.fill) {
            return 'api/annotation/' + this.id + "/review/fill.json";
        }
        else {
            return 'api/annotation/' + this.id + "/review.json";
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.fill = options.fill;
    }
});

var AnnotationCollection = PaginatedCollection.extend({
    model: AnnotationModel,
     initialize: function (options) {
         this.initPaginator(options);
         this.filters = options;
     },
    comparator: function (annotation) {
        if(annotation.get("rate")) {
            return -annotation.get("rate")
        } else {
            return -annotation.get("id");
        }

    },
    url : function () {
        var text = "";
        for(var key in this.filters) {
             if(this.filters[key]!=undefined && key!="max" && key!="offset") {
                 text = text + "&" + key + "=" + this.filters[key];
             }
         }
        return 'api/annotation.json?'+text
    }
});

// define our collection
var AnnotationImageReviewedModel = Backbone.Model.extend({
    url: function () {
        var task = ""
        if (this.task) {
            task = "&task=" + this.task;
        }


        return "api/imageinstance/" + this.id + "/annotation/review.json?users=" + this.layers.join(",") + task;
    },
    initialize: function (options) {
        this.id = options.image;//one image
        this.layers = options.layers;
        this.task = options.task;
    }
});


// define our collection
var AnnotationRetrievalModel = Backbone.Model.extend({
    url: function () {
        return "api/annotation/" + this.annotation + "/retrieval.json";
    },
    initialize: function (options) {
        this.annotation = options.annotation;
    }
});

var AnnotationRetrievalCollection = PaginatedCollection.extend({
    model: AnnotationModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.annotation = options.annotation;
    },
    comparator: function (annotation) {
        return -annotation.get("similarity");
    }
});


var AnnotationCommentModel = Backbone.Model.extend({
    initialize: function (options) {
        this.annotation = options.annotation;
    },
    url: function () {
        var base = 'api/annotation/' + this.annotation + '/comment';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var AnnotationCommentCollection = PaginatedCollection.extend({
    model: AnnotationCommentModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.annotation = options.annotation;
    },
    url: function () {
        return'api/annotation/' + this.annotation + '/comment.json';
    }
});
