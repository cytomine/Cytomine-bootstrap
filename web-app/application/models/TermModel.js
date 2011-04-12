/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var TermModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/term';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


// define our collection
var TermCollection = Backbone.Collection.extend({
    model: TermModel,
    class: "be.cytomine.ontology.Term",
    url: 'api/term.json',
    initialize: function () {
        // something
    },

	parse: function(response) {
		console.log("response : " + response);
	    return response.term;
	}
});
