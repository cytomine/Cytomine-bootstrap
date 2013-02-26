var SecRoleModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/role';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }

});

var SecRoleCollection = PaginatedCollection.extend({
    model: SecRoleModel,

    url: function () {
        return "api/role.json";
    }
});
