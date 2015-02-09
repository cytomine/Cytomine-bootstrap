/**
 * Created by hoyoux on 09.02.15.
 */
var ProjectCommandsView = Backbone.View.extend({

    tagName: 'ul',
    initialize: function (options) {
        if(options != undefined) {
            // Since 1.1.0, Backbone Views no longer automatically attach options passed to the constructor as this.options
            this.options = options;
            this.idProject = options.idProject;
            this.collection = options.collection;
        }
    },
    render: function () {
        var self = this;
        require([
                "text!application/templates/command/CommandAnnotation.tpl.html",
                "text!application/templates/command/CommandGeneric.tpl.html",
                "text!application/templates/command/CommandImageInstance.tpl.html"],
            function (commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {

                self.collection.each(function(command){
                    var commandView = new ProjectCommandView({ idProject: self.idProject, command: command });
                    var result = commandView.render(commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl);
                    if(result != null){
                        self.$el.append(result.$el);
                    }
                });
            }
        );
        return this;
    }
});

/**
 * Created by hoyoux on 09.02.15.
 */
var ProjectCommandView = Backbone.View.extend({

    tagName: 'li',
    initialize: function (options) {
        if(options != undefined) {
            // Since 1.1.0, Backbone Views no longer automatically attach options passed to the constructor as this.options
            this.options = options;
            this.command= options.command;
            this.idProject= options.idProject;
        }
    },
    render: function (commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {
        var self = this;
        this.$el = this.decodeCommandAction(this.command, commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl);
        if(this.$el == "undefined") {
            return null;
        }
        return this; // returning this for chaining..
    },
    decodeCommandAction : function(commandHistory,commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {
        var self = this;
        var action = "undefined";
        var jsonCommand = $.parseJSON(commandHistory.get("data"));
        var dateCreated = new Date();
        dateCreated.setTime(commandHistory.get('created'));
        var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();


        if (commandHistory.get('serviceName') == "userAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: idProject,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'),
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (commandHistory.get('serviceName') == "userAnnotationService" && commandHistory.get('className') == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.newUserAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.newUserAnnotation.id, idImage: jsonCommand.newUserAnnotation.image, imageFilename: jsonCommand.newUserAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "userAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "reviewedAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.idProject,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'),
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (commandHistory.get('serviceName') == "reviewedAnnotationService" && commandHistory.get('className') == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.newReviewedAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.newReviewedAnnotation.id, idImage: jsonCommand.newReviewedAnnotation.image, imageFilename: jsonCommand.newReviewedAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "reviewedAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "algoAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.idProject,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'),
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (commandHistory.get('serviceName') == "algoAnnotationService" && commandHistory.get('className') == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.newAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.newAnnotation.id, idImage: jsonCommand.newAnnotation.image, imageFilename: jsonCommand.newAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "algoAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.idProject, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "annotationTermService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-plus", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, image: ""});

        }
        else if (commandHistory.get('serviceName') == "annotationTermService" && commandHistory.get('className') == "be.cytomine.command.EditCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-pencil", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, image: ""});

        }
        else if (commandHistory.get('serviceName') == "annotationTermService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-trash", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, image: ""});

        }
        else if (commandHistory.get('serviceName') == "imageInstanceService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.thumb;
            action = _.template(commandImageInstanceTpl, {idProject: self.idProject, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "add.png", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "imageInstanceService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.thumb;
            action = _.template(commandImageInstanceTpl, {idProject: self.idProject, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "jobService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-plus", text: commandHistory.get('action'), datestr: dateStr, image: ""});

        }
        return action
    }
});