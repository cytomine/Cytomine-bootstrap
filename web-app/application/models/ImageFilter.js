var ImageFilterModel = Backbone.Model.extend({
    url:function () {
        var base = 'api/imagefilter';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var ImageFilterCollection = Backbone.Collection.extend({
    model:ImageFilterModel,
    url:function () {
        return 'api/imagefilter.json';
    }
});

var ProjectImageFilterModel = Backbone.Model.extend({
    url:function () {
        var base = 'api/imagefilterproject';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize:function (options) {
        this.project = options.project;
    }
});

var ProjectImageFilterCollection = Backbone.Collection.extend({
    model:ImageFilterModel,
    url:function () {
        return "api/project/" + this.project + "/imagefilterproject.json";
    },
    initialize:function (options) {
        this.project = options.project;
    }
});