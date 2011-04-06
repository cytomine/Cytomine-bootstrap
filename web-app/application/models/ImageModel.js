var ImageModel = Backbone.Model.extend({
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
		var base = 'api/image';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


// define our collection
var ImageCollection = Backbone.Collection.extend({
    model: ImageModel,

    url: 'api/image.json',
    initialize: function () {
        // something
    },

	parse: function(response) {
		console.log("response : " + response);
	    return response.image;
	}
});

