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
        $('#activityTab a').click(function (e) {
          e.preventDefault();
          $(this).tab('show');
        })
        //Refresh dashboard
        setInterval(function () {
            if ($("#tabs-dashboard-" + self.model.id).hasClass('active')) {
                self.refreshDashboard();
            }
        }, 60000);

        setInterval(function () {
            if ($("#tabs-dashboard-" + self.model.id).hasClass('active')) {
                self.fetchTasks();
                self.fetchCommands();
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
        console.log("this.projectDashboardAlgos="+this.projectDashboardAlgos);
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
    refreshReview: function (image,user,term) {
        console.log("DashBoardView.refreshReview user"+user);
        console.log("New.Image="+image);
        if (this.projectDashboardReview == null || this.projectDashboardReview.image!=image) {

            this.projectDashboardReview = new DashboardReviewPanel({ model: this.model});
            this.projectDashboardReview.render(image,user,term);
        } else {
            this.projectDashboardReview.refresh(image,user,term);
        }
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
    refreshProperties: function (idDomain, nameDomain) {
        if (this.projectDashboardProperties == null) {
            this.projectDashboardProperties = new ProjectDashboardProperties({ model: this.model, el: this.el, idDomain: idDomain, nameDomain: nameDomain});
            this.projectDashboardProperties.render();
        } else {
            this.projectDashboardProperties.refresh(idDomain, nameDomain);
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
                self.fetchTasks();
                self.fetchUsersOnline();

                if (self.projectStats == null) {
                    self.projectStats = new ProjectDashboardStats({model: self.model});
                }

            }
        });
    },
    fetchProjectInfo: function () {
        var self = this;
        console.log("a");
        require(["text!application/templates/dashboard/ProjectInfoContent.tpl.html"], function (tpl) {
            console.log("b");
            //Description
//            var maxLengthDescription = 30;
//            var shortDescription;
//            if (self.model.get('description') == undefined || self.model.get('description').length == 0) {
//                shortDescription = "Click here to add description "
//            } else {
//                shortDescription = (self.model.get('description').length > maxLengthDescription) ? self.model.get('description').substring(0,maxLengthDescription) : self.model.get('description');
//            }
//            self.model.set({ "shortDescription" : shortDescription});

            $("#projectInfoPanel").html(_.template(tpl, self.model.toJSON()));


            var updateProjectClosed = function(close) {
                self.model.set({isClosed: close});

                self.model.save({}, {
                        success: function (model, response) {
                            self.model = model;
                            self.refreshDashboard();
                        },
                        error: function (model, response) {
                            console.log(response);
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Project", json.errors[0], "error");
                        }
                    }
                );

            }

            $("#projectInfoPanel").find("#closeProject").click(function() {
                updateProjectClosed(true);
            });
            $("#projectInfoPanel").find("#uncloseProject").click(function() {
                updateProjectClosed(false);
            });

            self.initClearDataModal();


            $("#projectInfoPanel").find(".description");
            console.log("d");
            //initDescriptionView(domainIdent, domainClassName, container, maxPreviewCharNumber);
            console.log("test");
            console.log(DescriptionModal);
            DescriptionModal.initDescriptionView(self.model.id, self.model.get('class'), $("#projectInfoPanel").find(".description"), 150,
                    function() {
                        var text = $("#projectInfoPanel").find(".description").html();
                        $("#projectInfoPanel").find(".description").empty().append(text.replace(new RegExp("<h.>", "g"),'<br>').replace(new RegExp("</h.>", "g"),'<br>'));
                    },
                    function() {
                        self.fetchProjectInfo();
                    }
            );




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

        $("#seeDescriptionProject-" + self.model.id).on("click", function () {
            new ProjectDescriptionDialog({model: self.model, el: $("#explorer")}).render();
        });
    },
    initClearDataModal : function() {
         var self = this;
        var dropAllDataView = function() {
            new TaskModel({project: self.model.id, printInActivity: true}).save({}, {
                    success: function (task, response) {
                        console.log(response.task);
                        $("#jobDataClear"+self.model.id).empty();
                        $("#jobDataClear"+self.model.id).append('<div class="alert alert-info"><i class="icon-refresh"/> Loading...Cytomine is deleting all unused data... You can close this windows and check the progress in project dashboard "Last tasks" view  </div>');
                        $("#jobDataClear"+self.model.id).append('<div id="task-' + response.task.id + '"></div>');
                        var timer = window.app.view.printTaskEvolution(response.task, $("#jobDataClear"+self.model.id).find("#task-" + response.task.id), 1000);

                        //load all job data
                        new JobDataClearModel({id: self.model.id, task: response.task.id}).fetch({
                                success: function (model, response) {
                                    console.log("data loaded:" + model.toJSON());
                                    clearInterval(timer);

                                    $("#jobDataClear"+self.model.id).empty();
                                    $("#jobDataClear"+self.model.id).append("Clear is done!");

                                },
                                error: function (collection, response) {
                                    clearInterval(timer);
                                    console.log("error getting job data stat");
                                }}
                        );

                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Task", json.errors, "error");
                    }
                }
            );
        }



        var modal = new CustomModal({
                      idModal : "clearjobdataModal",
                      button : $("#clearjobdata"),
                      header :"Clear unused data",
                      body :'<div class="span3" id="jobDataClear'+self.model.id+'" style="min-width: 600px;">This will drop all data from job that are not reviewed. Are you sure? </div>',
                      width : 700,
                      height : 200
          });
         modal.addButtons("dropAllData","Drop all data",true,false,dropAllDataView);
         modal.addButtons("closeHotKeys","Close",false,true);

         $("#dropAllData").click(function(event) {
             event.preventDefault();

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
                                    console.log(position);
                                    var image = new ImageModel(position);
                                    var position = _.template(userOnlineTpl, {project: self.model.id, filename: window.app.minString(image.getVisibleName(window.app.status.currentProjectModel.get('blindMode')), 15, 10), image: position.image});
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
                var commandCollection = new CommandHistoryCollection({project: self.model.get('id'), max: self.maxCommandsView, fullData: true});
                var commandCallback = function (collection, response) {
                    $("#lastcommandsitem").empty();
                    if (collection.size() == 0) {
                        var noDataAlert = _.template("<br /><br /><div class='alert alert-block'>No data to display</div>", {});
                        $("#lastcommandsitem").append(noDataAlert);
                    }
                    $("#lastcommandsitem").append("<ul></ul>");
                    var ulContainer = $("#lastcommandsitem").find("ul");
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
    fetchTasks: function () {
        var self = this;

        var commandCollection = new TaskCommentsCollection({project: self.model.get('id'), max: 10});
        var commandCallback = function (collection, response) {
            $("#lasttasksitem").empty();
            if (collection.size() == 0) {
                var noDataAlert = _.template("<br /><br /><div class='alert alert-block'>No data to display</div>", {});
                $("#lasttasksitem").append(noDataAlert);
            }
            $("#lasttasksitem").append("<ul></ul>");
            var ulContainer = $("#lasttasksitem").find("ul");
            collection.each(function (task) {
                var dateCreated = new Date();
                dateCreated.setTime(task.get('timestamp'));
                var action = "<i>- <b>"+dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString()+"</b> : "+task.get('comment')+"<br></i>";
                ulContainer.append(action);
            });
        }
        commandCollection.fetch({
            success: function (collection, response) {
                commandCallback(collection, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });

    },
    decodeCommandAction : function(commandHistory,commandAnnotationTpl, commandGenericTpl, commandImageInstanceTpl) {
        var self = this;
        var action = "undefined"
        var jsonCommand = $.parseJSON(commandHistory.get("data"));
        var dateCreated = new Date();
        dateCreated.setTime(commandHistory.get('created'));
        var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
        if (commandHistory.get('serviceName') == "userAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
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
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newUserAnnotation.id, idImage: jsonCommand.newUserAnnotation.image, imageFilename: jsonCommand.newUserAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "userAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "reviewedAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
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
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newReviewedAnnotation.id, idImage: jsonCommand.newReviewedAnnotation.image, imageFilename: jsonCommand.newReviewedAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "reviewedAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "algoAnnotationService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl,
                {   idProject: self.model.id,
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
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.newAnnotation.id, idImage: jsonCommand.newAnnotation.image, imageFilename: jsonCommand.newAnnotation.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
        }
        else if (commandHistory.get('serviceName') == "algoAnnotationService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "";
            var cropURL = jsonCommand.cropURL;
            action = _.template(commandAnnotationTpl, {idProject: self.model.id, idAnnotation: jsonCommand.id, idImage: jsonCommand.image, imageFilename: jsonCommand.imageFilename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});
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
            action = _.template(commandImageInstanceTpl, {idProject: self.model.id, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "add.png", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "imageInstanceService" && commandHistory.get('className') == "be.cytomine.command.DeleteCommand") {
            var cropStyle = "block";
            var cropURL = jsonCommand.thumb;
            action = _.template(commandImageInstanceTpl, {idProject: self.model.id, idImage: jsonCommand.id, imageFilename: jsonCommand.filename, icon: "delete.gif", text: commandHistory.get("prefixAction") + " " + commandHistory.get('action'), datestr: dateStr, cropURL: cropURL, cropStyle: cropStyle});

        }
        else if (commandHistory.get('serviceName') == "jobService" && commandHistory.get('className') == "be.cytomine.command.AddCommand") {
            action = _.template(commandGenericTpl, {icon: "ui-icon-plus", text: commandHistory.get('action'), datestr: dateStr, image: ""});

        }
        return action
    },
    showImagesThumbs: function () {
        console.log("#showImagesThumbs");
        $("#tabs-projectImageThumb" + this.model.id).show();
        $("#tabs-projectImageListing" + this.model.id).hide();
        $('#imageThumbs' + this.model.id).attr("disabled", "disabled");
        $('#imageArray' + this.model.id).removeAttr("disabled");
    },
    showImagesTable: function () {
        console.log("#showImagesTable");
        $("#tabs-projectImageThumb" + this.model.id).hide();
        $("#tabs-projectImageListing" + this.model.id).show();
        $('#imageThumbs' + this.model.id).removeAttr("disabled");
        $('#imageArray' + this.model.id).attr("disabled", "disabled");
    }

});
