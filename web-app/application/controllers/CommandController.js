var CommandController = Backbone.Controller.extend({
    undo : function() {
        var self = this;
        $.post('command/undo.json', {}, function(data) {
            window.app.view.message("Redo", data.message, "");
            self.dispatch(data.callback);
        }, "json");

    },

    redo : function () {
        var self = this;
        $.post('command/redo.json', {}, function(data) {
            window.app.view.message("Undo", data.message, "");
            self.dispatch(data.callback);
        }, "json");

    },

    dispatch : function(callback) {
        if (!callback) return; //nothing to do

        //Annotations
        if (callback.method == "be.cytomine.AddAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationAdded(callback.annotationID);
        } else if (callback.method == "be.cytomine.EditAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationUpdated(callback.annotationID);
        } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationRemoved(callback.annotationID);
        }
    }
});