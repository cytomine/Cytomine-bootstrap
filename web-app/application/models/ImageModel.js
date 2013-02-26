var ImageModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/image';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


var ImageReviewModel = Backbone.Model.extend({
    url: function () {
        if (this.cancel != undefined) {
            return "/api/imageinstance/" + this.id + "/review?cancel=" + this.cancel;
        } else {
            return "/api/imageinstance/" + this.id + "/review";
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.cancel = options.cancel;
    }
});


var ImageMetadataModel = Backbone.Model.extend({
    initialize: function (options) {
        this.image = options.image;
    },
    url: function () {
        return 'api/image/' + this.image + "/metadata.json?extract=true";
    }
});

var ImagePropertyCollection = PaginatedCollection.extend({
    initialize: function (options) {
        this.image = options.image;
    },
    url: function () {
        return 'api/image/' + this.image + "/property.json";
    },
    comparator: function (model) {
        return model.get("key");
    }
});

// define our collection
var ImageCollection = PaginatedCollection.extend({
    model: ImageModel,
    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/image.json";
        } else {
            return "api/image.json?datatable=true";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var ImageServerUrlsModel = Backbone.Model.extend({
    url: function () {
        return 'api/image/' + this.id + "/imageservers.json";
    }
});

var ImageInstanceModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/imageinstance';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        var nextImage = "";
        if(this.next) {
            nextImage = "/next"
        }
        var url = base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + nextImage + format;
        console.log(url);
        return url;
    },
    initialize: function (options) {
        this.id = options.id;
        this.next = options.next;
    }
});

// define our collection
var ImageInstanceCollection = PaginatedCollection.extend({
    model: ImageModel,
    url: function () {
        if (this.inf != undefined && this.sup != undefined) {
            return "api/project/" + this.project + "/imageinstance.json?inf=" + this.inf + "&sup=" + this.sup;
        } else if (this.tree) {
            return "api/project/" + this.project + "/imageinstance.json?tree=true";
        } else {
            return "api/project/" + this.project + "/imageinstance.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.tree = options.tree != undefined && options.tree == true;
        this.inf = options.inf;
        this.sup = options.sup;
    }
});


var UserPositionModel = Backbone.Model.extend({
    url: function () {
        if (this.user == undefined) {
            return 'api/imageinstance/' + this.image + '/position';
        } else {
            return 'api/imageinstance/' + this.image + '/position/' + this.user;
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
    }
});

var UserOnlineModel = Backbone.Model.extend({
    url: function () {
        return 'api/imageinstance/' + this.image + '/online';
    },
    initialize: function (options) {
        this.image = options.image;
    }
});


