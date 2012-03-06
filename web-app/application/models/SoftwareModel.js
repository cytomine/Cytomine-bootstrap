var SoftwareModel = Backbone.Model.extend({
   url : function() {
      var base = 'api/software';
      var format = '.json';
      if (this.isNew()) return base + format;
      return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
   }
});

// define our collection
var SoftwareCollection = Backbone.Collection.extend({
    model: SoftwareModel,
    CLASS_NAME: "be.cytomine.processing.Software",
	url : function() {
        if (this.project != null)
		    return 'api/project/'+ this.project +'/software.json';
        else
            return 'api/software.json';

        //Request URL:http://localhost:8080/cytomine-web/api/currentuser/image.json?_search=false&nd=1310463777413&rows=10&page=1&sidx=filename&sord=asc
	},
    initialize: function (options) {
        this.project = options.project;
    },comparator : function(software) {
        return software.get("name");
    }
});