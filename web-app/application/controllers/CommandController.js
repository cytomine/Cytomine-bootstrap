var CommandController = Backbone.Router.extend({
    initialize:function () {
        this.commandInProgess = false;
    },
    undo:function () {
        var self = this;
        console.log("undo");
        if (self.commandInProgess) {
            window.app.view.message("Oh wait :-)", "Operation already in progress", "error");
            return
        }
        self.commandInProgess = true;
        console.log("post undo");
        $.post('command/undo.json', {}, function (data) {
            _.each(data, function (undoElem) {
                console.log("undoElem");
                console.log(undoElem);
                self.dispatch(undoElem.callback, undoElem.message, "Undo");
                if (undoElem.printMessage) {
                    window.app.view.message("Undo", undoElem.message, "info");
                }
                self.commandInProgess = false;
            });

        }, "json");

    },

    redo:function () {
        var self = this;
        if (self.commandInProgess) {
            window.app.view.message("Oh wait :-)", "Operation already in progress", "error");
            return
        }
        self.commandInProgess = true;
        $.post('command/redo.json', {}, function (data) {
            _.each(data, function (redoElem) {
                self.dispatch(redoElem.callback, redoElem.message, "Redo");
                if (redoElem.printMessage) window.app.view.message("Redo", redoElem.message, "info");
            });
            self.commandInProgess = false;
        }, "json");

    },

    dispatch:function (callback, message, operation) {
        console.log("callback");
        console.log(callback);
        console.log("operation");
        console.log(operation);
        if (!callback) return; //nothing to do

        /**
         * ANNOTATION
         */
        if (callback.method == "be.cytomine.AddAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {

                return object.idImage == callback.imageID;
            });

            if (tab == undefined) return; //tab is closed

            var image = tab.view;
            image.getUserLayer().annotationAdded(callback.annotationID);
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refreshAnnotations();
        } else if (callback.method == "be.cytomine.EditAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) return; //tab is closed

            var image = tab.view;
            image.getUserLayer().annotationUpdated(callback.annotationID);
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refreshAnnotations();
        } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {
            console.log("delete annotation");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) return; //tab is closed

            var image = tab.view;

            image.getUserLayer().annotationRemoved(callback.annotationID);
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refreshAnnotations();
            /**
             * ANNOTATION TERM
             */
        } else if (callback.method == "be.cytomine.AddAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) return; //tab is closed

            var image = tab.view;
            image.getUserLayer().termAdded(callback.annotationID, callback.termID);
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refreshAnnotations();
        } else if (callback.method == "be.cytomine.DeleteAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) return; //tab is closed

            var image = tab.view;
            image.getUserLayer().termRemoved(callback.annotationID, callback.termID);
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refreshAnnotations();
        }

        /**
         * ONTOLOGY
         */
        else if (callback.method == "be.cytomine.AddOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.DeleteOntologyCommand") {

            window.app.controllers.ontology.view.refresh();
        } else if (callback.method == "be.cytomine.EditOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }
        /**
         * PROJECT
         */
        else if (callback.method == "be.cytomine.AddProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "be.cytomine.EditProjectCommand") {

            window.app.controllers.project.view.refresh();
        }
        /**
         * TERM
         */
        else if (callback.method == "be.cytomine.AddTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.DeleteTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.EditTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }

        else if (callback.method == "be.cytomine.AddImageInstanceCommand") {
            if (window.app.controllers.project.view != null)
                window.app.controllers.project.view.refresh();
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteImageInstanceCommand") {

            if (window.app.controllers.project.view != null)
                window.app.controllers.project.view.refresh();
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.EditImageInstanceCommand") {
            if (window.app.controllers.project.view != null)
                window.app.controllers.project.view.refresh();
            if (window.app.controllers.dashboard.view != null)
                window.app.controllers.dashboard.view.refresh();
        }

    }
});