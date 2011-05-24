/**
* Created by IntelliJ IDEA.
* User: lrollus
* Date: 7/04/11
* Time: 10:05
* To change this template use File | Settings | File Templates.
*/

var TermController = Backbone.Controller.extend({

	routes: {
		"term"            :   "term" ,
		"term/o:ontology"            :   "term"
	},

	term : function(ontology) {
		if (!this.view) {
			this.view = new TermView({
				model : window.app.models.terms,
				ontology : ontology,
				el:$("#explorer > .term"),
				container : window.app.view.components.explorer
			}).render();

			this.view.container.views.term = this.view;
		}

		this.view.container.show(this.view);
	}


});