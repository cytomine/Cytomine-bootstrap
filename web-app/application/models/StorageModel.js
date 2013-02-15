
var StorageModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/storage';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var StorageCollection = Backbone.Collection.extend({
    model: StorageModel,
    url: function () {
        return 'api/storage.json';
    },
    initialize: function (options) {        
    },
    comparator: function (storage) {
        return storage.get("name");
    }
});