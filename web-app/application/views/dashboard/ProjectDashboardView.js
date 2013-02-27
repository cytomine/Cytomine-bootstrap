/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectDashboardView = Backbone.View.extend({
    tagName: "div",
    projectElem: "#projectdashboardinfo", //div with project info
    maxCommandsView: 20,
    maxSuggestView: 30,
    projectDashboardAnnotations: null,
    projectDashboardImages: null,
    rendered: false,
    initialize: function (options) {
        _.bindAll(this, 'render');
    },
    events: {
    },
    /**
     * Print all information for this project
     */
    render: function () {
        var self = this;
        require(["text!application/templates/dashboard/Dashboard.tpl.html"], function (tpl) {
            self.doLayout(tpl);
            self.rendered = true;
        });
        return this;

    },
    doLayout: function (tpl) {
        var self = this;
        $(self.el).append(_.template(tpl, self.model.toJSON()));
        window.app.controllers.browse.tabs.addDashboard(self);
        self.showImagesThumbs();

        //Refresh dashboard
        setInterval(function () {
            if ($("#tabs-dashboard-" + self.model.id).hasClass('active')) {
                self.refreshDashboard();
            }
        }, 60000);
    },
    refreshImagesThumbs: function () {
        if (this.projectDashboardImages == null) {
            this.projectDashboardImages = new ProjectDashboardImages({ model: this.model});
        }
        this.projectDashboardImages.refreshImagesThumbs();

    },
    refreshAlgos: function (idSoftware, idJob) {
        if (this.projectDashboardAlgos == null || this.projectDashboardAlgos == undefined) {
            this.projectDashboardAlgos = new ProjectDashboardAlgos({
                model: this.model,
                idSoftware: idSoftware,
                idJob: idJob
            }).render();
        }
        else {
            this.projectDashboardAlgos.refresh(idSoftware, idJob);
        }

    },
    refreshConfig: function () {
        if (this.ProjectDashboardConfig == null) {
            this.ProjectDashboardConfig = new ProjectDashboardConfig({
                model: this.model
            });
        }

        this.ProjectDashboardConfig.refresh();

    },

    refreshImagesTable: function () {
        if (this.projectDashboardImages == null) {
            this.projectDashboardImages = new ProjectDashboardImages({ model: this.model});
        }
        this.projectDashboardImages.refreshImagesTable();
    },
    refreshAnnotations: function (terms, users) {
        console.log("ProjectDashboardView.refreshAnnotations=" + users);
        var self = this;

        if (this.projectDashboardAnnotations == null) {
            this.projectDashboardAnnotations = new ProjectDashboardAnnotations({ model: this.model, el: this.el});
            this.projectDashboardAnnotations.render(function () {
                self.projectDashboardAnnotations.checkTermsAndUsers(terms, users);
            });
        } else {
            this.projectDashboardAnnotations.checkTermsAndUsers(terms, users);
        }

    },
    refreshAnnotationsProperties: function (annotation) {
        console.log("ProjectDashboardView");
        var self = this;

        if (this.projectDashboardAnnotationsProperties == null) {
            this.projectDashboardAnnotationsProperties = new ProjectDashboardAnnotationsProperties({ model: this.model, el: this.el, idAnnotation: annotation});
            this.projectDashboardAnnotationsProperties.render();
        } else {
            this.projectDashboardAnnotationsProperties.refresh(annotation)
        }
    },
    /**
     * Refresh all information for this project
     */
    refreshDashboard: function () {
        var self = this;
        if (!self.rendered) {
            return;
        }
        self.model.fetch({
            success: function (model, response) {
                window.app.status.currentProjectModel = model;
                self.model = model;
                self.fetchProjectInfo();
                self.fetchCommands();
                self.fetchUsersOnline();

                if (self.projectStats == null) {
                    self.projectStats = new ProjectDashboardStats({model: self.model});
                }

            }
        });
    },
    fetchProjectInfo: function () {
        var self = this;
        require(["text!application/templates/dashboard/ProjectInfoContent.tpl.html"], function (tpl) {
            $("#projectInfoPanel").html(_.template(tpl, self.model.toJSON()));
            //Get users list
            $("#projectInfoUserList").empty();
            var users = []
            window.app.models.projectUser.each(function (user) {
                users.push(user.prettyName());
            });
        });

        $("#seeUsersProjectList-" + self.model.id).on("click", function () {
            new ProjectUsersDialog({model: self.model, el: $("#explorer")}).render();
        });
    },
    fetchUsersOnline: function () {
        var self = this;
        var refreshData = function () {
            require(["text!application/templates/dashboard/OnlineUser.tpl.html"],
                function (userOnlineTpl) {
                    new UserOnlineCollection({project: self.model.id}).fetch({
                        success: function (collection, response) {
                            $("#userOnlineItem").empty();
                            collection.each(function (user) {
                                //if undefined => user is cytomine admin but not in project!
                                if (window.app.models.projectUser.get(user.id) == undefined) {
                                    return;
                                }

                                var positions = "";
                                _.each(user.get('position'), function (position) {
                                    var position = _.template(userOnlineTpl, {project: self.model.id, filename: window.app.minString(position.filename, 15, 10), image: position.image});
                                    positions += position;
                                });
                                var onlineUser = _.template("<div id='onlineUser-<%= id %>'><%= user %><ul><%= positions %></ul></div>", {
                                    id: user.id,
                                    user: window.app.models.projectUser.get(user.id).prettyName(),
                                    positions: positions
                                });
                                $("#userOnlineItem").append(onlineUser);
                            });
                        }
                    });
                }
            )
        };
        refreshData();
        var interval = setInterval(refreshData, 5000);
        $(window).bind('hashchange', function () {
            clearInterval(interval);
        });
    },
    fetchCommands: function () {
        var self = this;
        require([
            "text!application/templates/dashboard/CommandAnnotation.tpl.html",
            "text!application/templates/dashboard/CommandGeneric.tpl.html",
            "text!application/templates/dashboard/CommandImageInstance.tpl.html"],
            function (commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {
                var commandCollection = new CommandCollection({project: self.model.get('id'), max: self.maxCommandsView});
                var commandCallback = function (collection, response) {
                    $("#lastactionitem").empty();
                    if (collection.size() == 0) {
                        var noDataAlert = _.template("<br /><br /><div class='alert alert-block'>No data to display</div>", {});
                        $("#lastactionitem").append(noDataAlert);
                    }
                    $("#lastactionitem").append("<ul></ul>");
                    var ulContainer = $("#lastactionitem").find("ul");
                    collection.each(function (commandHistory) {

                        var action = self.decodeCommandAction(commandHistory,commandAnnotationTpl,commandGenericTpl,commandImageInstanceTpl);
                        if (action != "undefined") {
                            ulContainer.append(action);
                        }
                    });
                }
                commandCollection.fetch({
                    success: function (collection, response) {
                        commandCallback(collection, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
            });
    },
    decodeCommandAction : function(commandHistory,commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {
        var self = this;
        var command = commandHistory.get("command");
        var action = "undefined"
        var jsonCommand = $.parseJSON(command.data);
        var dateCreated = new Date();
        dateCreated.setTime(command.created);
        var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
        if (command.serviceName == "userAnnotationService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + command.action,
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (command.serviceName == "userAnnotationService" && command.CLASSNAME == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.newUserAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newUserAnnotation.id, idImage: jsonCommand.newUserAnnotation.image, imageFilename: jsonCommand.newUserAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (command.serviceName == "userAnnotationService" && command.CLASSNAME == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (command.serviceName == "reviewedAnnotationService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + command.action,
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (command.serviceName == "reviewedAnnotationService" && command.CLASSNAME == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            console.log(jsonCommand);
            var cropURL = jsonCommand.newReviewedAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newReviewedAnnotation.id, idImage: jsonCommand.newReviewedAnnotation.image, imageFilename: jsonCommand.newReviewedAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (command.serviceName == "reviewedAnnotationService" && command.CLASSNAME == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (command.serviceName == "algoAnnotationService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
                    idAnnotation: jsonCommand.id,
                    idImage: jsonCommand.image,
                    imageFilename: jsonCommand.imageFilename,
                    icon: "add.png",
                    text: commandHistory.get("prefixAction") + " " + command.action,
                    datestr: dateStr,
                    cropURL: cropURL,
                    cropStyle: cropStyle
                });
        }
        else if (command.serviceName == "algoAnnotationService" && command.CLASSNAME == "be.cytomine.command.EditCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.newAnnotation.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newAnnotation.id, idImage: jsonCommand.newAnnotation.image, imageFilename: jsonCommand.newAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (command.serviceName == "algoAnnotationService" && command.CLASSNAME == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (command.serviceName == "annotationTermService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-plus", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, image: ""});

        }
        else if (command.serviceName == "annotationTermService" && command.CLASSNAME == "be.cytomine.command.EditCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-pencil", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, image: ""});

        }
        else if (command.serviceName == "annotationTermService" && command.CLASSNAME == "be.cytomine.command.DeleteCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-trash", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, image: ""});

        }
        else if (command.serviceName == "imageInstanceService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.thumb;
            action = _.template(commandImageInstanceTpl, {idProject: self.model.id, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "add.png", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (command.serviceName == "imageInstanceService" && command.CLASSNAME == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.thumb;
            action = _.template(commandImageInstanceTpl, {idProject: self.model.id, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + command.action, datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (command.serviceName == "jobService" && command.CLASSNAME == "be.cytomine.command.AddCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-plus", text: command.action, datestr: dateStr, image: ""});

        }
        return action
    },
    showImagesThumbs: function () {
        $("#tabs-projectImageThumb" + this.model.id).show();
        $("#tabs-projectImageListing" + this.model.id).hide();
        $('#imageThumbs' + this.model.id).attr("disabled", "disabled");
        $('#imageArray' + this.model.id).removeAttr("disabled");
    },
    showImagesTable: function () {
        $("#tabs-projectImageThumb" + this.model.id).hide();
        $("#tabs-projectImageListing" + this.model.id).show();
        $('#imageThumbs' + this.model.id).removeAttr("disabled");
        $('#imageArray' + this.model.id).attr("disabled", "disabled");
    }

});
