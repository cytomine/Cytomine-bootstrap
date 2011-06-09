var CommandController = Backbone.Controller.extend({
    undo : function() {
        var self = this;
        $.post('command/undo.json', {}, function(data) {
            console.log("data:");
            console.log(data);
             _.each(data, function(undoElem){
                  console.log("undoElem" + undoElem);
                  self.dispatch(undoElem.callback,undoElem.message,"Undo");
             });

        }, "json");

    },

    redo : function () {
        var self = this;
        $.post('command/redo.json', {}, function(data) {
                console.log("data:");
                console.log(data);
                 _.each(data, function(redoElem){
                      console.log("redoElem" + redoElem);
                      self.dispatch(redoElem.callback,redoElem.message, "Redo");
                 });
        }, "json");

    },

    dispatch : function(callback,message,operation) {
        console.log("callback method ? " + callback.method);
        if (!callback) return; //nothing to do

        /**
         * ANNOTATION
         */
        if (callback.method == "be.cytomine.AddAnnotationCommand") {
             window.app.view.message(operation, message, "");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                console.log("object.idImage="+object.idImage + " callback.imageID=" + callback.imageID);
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
             console.log(tab);
            if (image == undefined) return; //tab is closed
            console.log("callback.annotationID="+callback.annotationID);
            image.getUserLayer().annotationAdded(callback.annotationID);
        } else if (callback.method == "be.cytomine.EditAnnotationCommand") {
            window.app.view.message(operation, message, "");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationUpdated(callback.annotationID);
        } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {
            window.app.view.message(operation, message, "");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            console.log(tab);
            console.log("tab.view="+tab.view);
            if (image == undefined) return; //tab is closed
            console.log("callback.annotationID="+callback.annotationID);
            image.getUserLayer().annotationRemoved(callback.annotationID);

            /**
             * ANNOTATION TERM
             */
        } else if (callback.method == "be.cytomine.AddAnnotationTermCommand") {
            window.app.view.message(operation, message, "");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termAdded(callback.annotationID,callback.termID);
        } else if (callback.method == "be.cytomine.DeleteAnnotationTermCommand") {
            window.app.view.message(operation, message, "");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termRemoved(callback.annotationID,callback.termID);
        }

        /**
         * ONTOLOGY
         */
        else if (callback.method == "be.cytomine.AddOntologyCommand") {
            window.app.view.message(operation, message, "");
            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.DeleteOntologyCommand") {
            window.app.view.message(operation, message, "");
            window.app.controllers.ontology.view.refresh();
        } else if (callback.method == "be.cytomine.EditOntologyCommand") {
            window.app.view.message(operation, message, "");
            window.app.controllers.ontology.view.refreshAndSelect(callback.ontologyID);
        }
    }
});