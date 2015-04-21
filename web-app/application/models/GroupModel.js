var GroupModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/group';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

// define our collection
var GroupCollection = PaginatedCollection.extend({
    model: GroupModel,
    url: function () {
        return "api/group.json";
    }
});
var GroupWithUserCollection = PaginatedCollection.extend({
    //model: GroupModel,
    url: function () {
        return "api/group.json?withUser=true";
    }
});
