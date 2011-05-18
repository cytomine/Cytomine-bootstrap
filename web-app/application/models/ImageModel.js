var ImageModel = Backbone.Model.extend({
	url : function() {
		var base = 'api/image';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});

// define our collection
var ImageCollection = Backbone.Collection.extend({
    model: ImageModel,
    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/image.json";
        } else {
            return "api/image.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var ImageInstanceModel = Backbone.Model.extend({
	url : function() {
        if(this.project == undefined && this.baseImage == undefined) {
            var base = 'api/imageinstance';
            var format = '.json';
            if (this.isNew()) return base + format;
            return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
        }
        else
        {
            return 'api/project/' + this.project +'/image/'+this.baseImage+'/imageinstance.json';
        }
	},
    initialize: function (options) {
        this.project = options.project;
        this.baseImage = options.baseImage;
    }
});

// define our collection
var ImageInstanceCollection = Backbone.Collection.extend({
    model: ImageModel,
    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/imageinstance.json";
        } else {
            return "api/imageinstance.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

