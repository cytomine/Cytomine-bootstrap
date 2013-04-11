var ImageInstancePropertyModel = Backbone.Model.extend({
    initialize: function (options) {
        this.idImageInstance = options.idImageInstance;
    },
    url: function () {
        var base = 'api/imageinstance/' + this.idImageInstance + '/property';
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

var ImageInstancePropertyCollection = PaginatedCollection.extend({
    model: ImageInstancePropertyModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.idImageInstance = options.idImageInstance;
    },
    url: function () {
        if (this.idImageInstance != undefined) {
            return "api/imageinstance/" + this.idImageInstance + "/property.json";
        }
    }
});