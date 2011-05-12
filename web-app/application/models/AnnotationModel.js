var AnnotationModel = Backbone.Model.extend({
    /*initialize: function(spec) {
        if (!spec || !spec.name || !spec.username) {
            throw "InvalidConstructArgs";
        }
    },

    validate: function(attrs) {
        if (attrs.name) {
            if (!_.isString(attrs.name) || attrs.name.length === 0) {
                return "Name must be a string with a length";
            }
        }
    },*/

	url : function() {
		var base = 'api/annotation';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


var AnnotationModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/annotation';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


var AnnotationCropModel = Backbone.Model.extend({

	url : null,
    initialize: function (options) {
        this.url = options.url;
    }
});

// define our collection
var AnnotationCollection = Backbone.Collection.extend({
    model: AnnotationModel,
    url: function() {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/image/" + this.image + "/annotation.json";
        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/annotation.json";
        } else if (this.term != undefined){
            return "api/term/" + this.term + "/annotation.json";
        } else {
            return "api/image/" + this.image + "/annotation.json";
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
        this.project = options.project;
        this.term = options.term;
    }
});

AnnotationCollection.comparator = function(annotation) {
  return annotation.get("created");
};

