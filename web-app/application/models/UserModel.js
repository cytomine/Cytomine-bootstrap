var UserModel = Backbone.Model.extend({
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
		var base = 'api/user';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


// define our collection
var UserCollection = Backbone.Collection.extend({
    model: UserModel,

    url: 'api/user.json',
    initialize: function () {
        // something
    },

	parse: function(response) {
		console.log("response : " + response);
	    return response.user;
	}
});

