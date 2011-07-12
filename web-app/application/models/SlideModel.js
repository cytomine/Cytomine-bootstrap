/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var SlideModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/slide';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});

var ProjectSlideModel = Backbone.Model.extend({
	url : function() {
        if (this.slide == null)
		    return 'api/project/' + this.project +'/slide.json';
        else
            return 'api/project/' + this.project +'/slide/'+this.slide+'.json';
	},
    initialize: function (options) {
        this.project = options.project;
        this.slide = options.slide;
    }
});

var ProjectSlideCollection = Backbone.Collection.extend({
    model : SlideModel,
	url : function() {
		return 'api/project/' + this.idProject +'/slide.json';
	},
    initialize: function (options) {
        this.idProject = options.idProject;

    }
});



// define our collection
var SlideCollection = Backbone.Collection.extend({
    model: SlideModel,
    CLASS_NAME: "be.cytomine.image.Slide",
	url : function() {
        if (this.page == null)
		    return 'api/currentuser/slide.json';
        else
            return 'api/currentuser/slide.json?page='+this.page+'&limit=';

        //Request URL:http://localhost:8080/cytomine-web/api/currentuser/image.json?_search=false&nd=1310463777413&rows=10&page=1&sidx=filename&sord=asc
	},
    initialize: function (options) {
        this.page = options.page;
        this.limit = options.limit;
        this.sidx = options.sidx;
        this.sord = options.sord;
    }
});
