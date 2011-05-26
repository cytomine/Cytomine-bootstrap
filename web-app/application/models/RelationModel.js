var RelationTermModel = Backbone.Model.extend({
	url : function() {
        if (this.relation == undefined && this.term1==undefined && this.term2==undefined)
		    return 'api/relation/parent/term.json';
        else if(this.term1==undefined && this.term2==undefined)
            return 'api/relation/' + this.relation +'/term.json';
        else
            return 'api/relation/parent/term1/' + this.term1 + "/term2/" + this.term2 + ".json";
	},
    initialize: function (options) {
        this.relation = options.relation;
        this.term1 = options.term1;
        this.term2 = options.term2;
    }
});

var RelationTermCollection = Backbone.Collection.extend({
    model : RelationTermModel,
	url : function() {
		return 'api/relation/' + this.relation +'/term.json';
	},
    initialize: function (options) {
        this.relation = options.relation;

    }
});