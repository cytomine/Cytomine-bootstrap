/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var ProjectModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/project';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


// define our collection
var ProjectCollection = Backbone.Collection.extend({
    model: ProjectModel,

    url: function() {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/project.json";
        } else {
            return "api/project.json";
        }
    },
    initialize: function (options) {
        this.user = options.user;
    }
});