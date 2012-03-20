var UploadedFileModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/uploadedfile';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


var UploadedFileCollection = Backbone.Collection.extend({
    model : UploadedFileModel,
	url : function() {
		return 'api/uploadedfile.json';
	}
});