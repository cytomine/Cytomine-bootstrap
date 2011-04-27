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


// define our collection
var AnnotationCollection = Backbone.Collection.extend({
    model: AnnotationModel,
    url: function() {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/image/" + this.image + "/annotation.json";
        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/annotation.json";
        } else {
            return "api/image/" + this.image + "/annotation.json";
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
        this.project = options.project;
    }
});

