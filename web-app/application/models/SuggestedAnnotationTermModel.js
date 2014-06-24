var SuggestedAnnotationTermModel = Backbone.Model.extend({

    url: function () {
        return 'api/annotation/term/suggest.json';
    }
});

var SuggestedAnnotationTermCollection = PaginatedCollection.extend({
    model: SuggestedAnnotationTermModel,
    url: function () {
        return "api/project/" + this.project + "/annotation/term/suggest.json?max=" + this.max;
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.max = options.max;
    },
    comparator: function (annotation) {
        return -Number(annotation.get("rate")); //id or created (chronology?)
    }
});

var SuggestedTermCollection = PaginatedCollection.extend({
    model: SuggestedAnnotationTermModel,
    url: function () {
        return "api/project/" + this.project + "/term/suggest.json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
    },
    comparator: function (annotation) {
        return -Number(annotation.get("rate")); //id or created (chronology?)
    }
});