var ProjectPropertyModel = Backbone.Model.extend({
    initialize: function (options) {
        this.idProject = options.idProject;
    },
    url: function () {
        var base = 'api/project/' + this.idProject + '/property';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    parse : function (response, options) {
        console.log("parse " + response.property);
        if (response.property) return response.property;
        else return response;
    }
});

var ProjectPropertyCollection = PaginatedCollection.extend({
    model: ProjectPropertyModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.idProject = options.idProject;
    },
    url: function () {
        if (this.idProject != undefined) {
            return "api/project/" + this.idProject + "/property.json";
        }
    }
});