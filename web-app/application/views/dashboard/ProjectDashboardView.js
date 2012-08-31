/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectDashboardView = Backbone.View.extend({
    tagName : "div",
    projectElem : "#projectdashboardinfo",  //div with project info
    maxCommandsView : 20,
    maxSuggestView : 30,
    projectDashboardAnnotations : null,
    projectDashboardImages : null,
    rendered : false,
    initialize: function(options) {
        _.bindAll(this, 'render');
    },
    events: {
    },
    /**
     * Print all information for this project
     */
    render: function() {
        var self = this;
        require(["text!application/templates/dashboard/Dashboard.tpl.html"], function(tpl) {
            self.doLayout(tpl);
            self.rendered = true;
        });
        return this;

    },
    doLayout : function(tpl) {
        var self = this;
        var width = Math.round($(window).width()/2 - 95);
        var width2 = Math.round($(window).width() - 95);
        self.model.set({"width" : width+"px"});
        self.model.set({"width2" : width2+"px"});
        var html = _.template(tpl, self.model.toJSON());
        $(self.el).append(html);
        window.app.controllers.browse.tabs.addDashboard(self);
        self.showImagesThumbs();

        //Refresh dashboard
        setInterval(function(){
            if ($("#tabs-dashboard-"+self.model.id).hasClass('active')) self.refreshDashboard();
        }, 60000);
    },
    refreshImagesThumbs : function() {
        if (this.projectDashboardImages == null) {
            this.projectDashboardImages = new ProjectDashboardImages({ model : this.model});
        }
        this.projectDashboardImages.refreshImagesThumbs();

    },
    refreshAlgos : function(idSoftware,idJob) {
        if (this.projectDashboardAlgos == null || this.projectDashboardAlgos==undefined) {
            this.projectDashboardAlgos = new ProjectDashboardAlgos({
                model : this.model,
                idSoftware: idSoftware,
                idJob : idJob
            }).render();
        }
        else this.projectDashboardAlgos.refresh(idSoftware,idJob);

    },
    refreshConfig : function() {
        if (this.ProjectDashboardConfig == null) {
            this.ProjectDashboardConfig = new ProjectDashboardConfig({
                model : this.model
            });
        }

        this.ProjectDashboardConfig.refresh();

    },

    refreshImagesTable : function() {
        if (this.projectDashboardImages == null) {
            this.projectDashboardImages = new ProjectDashboardImages({ model : this.model});
        }
        this.projectDashboardImages.refreshImagesTable();
    },
    refreshAnnotations : function(terms, users) {
        var self = this;

        if (this.projectDashboardAnnotations == null) {
            this.projectDashboardAnnotations = new ProjectDashboardAnnotations({ model : this.model, el : this.el});
            this.projectDashboardAnnotations.render(function(){
                self.projectDashboardAnnotations.checkTermsAndUsers(terms, users);
            });
        } else {
            this.projectDashboardAnnotations.checkTermsAndUsers(terms, users);
        }

    },
    /**
     * Refresh all information for this project
     */
    refreshDashboard : function() {
        var self = this;
        if (!self.rendered) return;

        var refreshDashboard = function(model, response) {
            self.model = model;

            self.fetchProjectInfo();
            self.fetchCommands();
            new TermCollection({idProject:self.model.id}).fetch({
                success : function (terms, response) {
                    window.app.status.currentTermsCollection = terms;
                    //self.fetchWorstAnnotations(collection,terms);
                    if(self.projectStats==null)
                        self.projectStats = new ProjectDashboardStats({model : self.model});

                    self.projectStats.fetchStats(terms);

                }
            });

            //new ProjectDashboardStats({model : self.model}).fetchStats();
        }
        var fetchInformations = function () {
            self.model.fetch({
                success : function(model, response) {
                    window.app.status.currentProjectModel = model;
                    refreshDashboard(model, response); //to do : optimiser pour ne pas tout recharger
                }
            });
        }

        fetchInformations();

    },

    fetchProjectInfo : function () {
        var self = this;
        require(["text!application/templates/dashboard/ProjectInfoContent.tpl.html"], function(tpl) {
            $("#projectInfoPanel").html(_.template(tpl, self.model.toJSON()));
            //Get users list
            $("#projectInfoUserList").empty();
            var users = []
            _.each(self.model.get('users'), function (idUser) {
                users.push(window.app.models.users.get(idUser).prettyName());
            });
            $("#projectInfoUserList").html(users.join(", "));
        });
        return;
        var self = this;
        var json = self.model.toJSON();

        //Get created/updated date
        /*var dateCreated = new Date();
         dateCreated.setTime(json.created);
         json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
         var dateUpdated = new Date();
         dateUpdated.setTime(json.updated);
         json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();*/

        var resetElem = function(elem,txt) {
            $(this.el).find(elem).empty();
            $(this.el).find(elem).append(txt);
        };

        resetElem("#projectInfoName",json.name);
        resetElem("#projectInfoOntology",json.ontologyName);
        resetElem("#projectInfoNumberOfSlides",json.numberOfSlides);
        resetElem("#projectInfoNumberOfImages",json.numberOfImages);
        resetElem("#projectInfoNumberOfAnnotations",json.numberOfAnnotations);
        resetElem("#projectInfoCreated",json.created);
        resetElem("#projectInfoUpdated",json.updated);



    },
    fetchCommands : function () {
        var self = this;
        require([
            "text!application/templates/dashboard/CommandAnnotation.tpl.html",
            "text!application/templates/dashboard/CommandAnnotationTerm.tpl.html",
            "text!application/templates/dashboard/CommandImageInstance.tpl.html"],
            function(commandAnnotationTpl, commandAnnotationTermTpl,commandImageInstanceTpl) {
                var commandCollection = new CommandCollection({project:self.model.get('id'),max:self.maxCommandsView});
                var commandCallback = function(collection, response) {
                    $("#lastactionitem").empty();
                    if (collection.size() == 0) {
                        var noDataAlert = _.template("<br /><br /><div class='alert alert-block'>No data to display</div>",{});
                        $("#lastactionitem").append(noDataAlert);
                    }
                    $("#lastactionitem").append("<ul></ul>");
                    var ulContainer = $("#lastactionitem").find("ul");
                    collection.each(function(commandHistory) {
                        var command = commandHistory.get("command");
                        var dateCreated = new Date();
                        dateCreated.setTime(command.created);
                        var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
                        var jsonCommand = $.parseJSON(command.data);
                        var action = "undefined";
                        if(command.serviceName=="annotationService" && command.CLASSNAME=="be.cytomine.command.AddCommand") {
                            var cropStyle = "block";
                            var cropURL = jsonCommand.cropURL;
//                                if (annotations.get(jsonCommand.id) == undefined) {
//                                    cropStyle = "none";
//                                    cropURL = "";
//                                }
                            action = _.template(commandAnnotationTpl,
                                {   idProject : self.model.id,
                                    idAnnotation : jsonCommand.id,
                                    idImage : jsonCommand.image,
                                    imageFilename : jsonCommand.imageFilename,
                                    icon:"add.png",
                                    text:commandHistory.get("prefixAction")+ " " + command.action,
                                    datestr:dateStr,
                                    cropURL:cropURL,
                                    cropStyle:cropStyle
                                });
                        }
                        else if(command.serviceName=="annotationService" && command.CLASSNAME=="be.cytomine.command.EditCommand") {
                            var cropStyle = "";
                            var cropURL = jsonCommand.newAnnotation.cropURL;
//                                if (annotations.get(jsonCommand.newAnnotation.id) == undefined) {
//                                    cropStyle = "display : none;";
//                                    cropURL = "";
//                                }
                            action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.newAnnotation.id, idImage : jsonCommand.newAnnotation.image,imageFilename : jsonCommand.newAnnotation.imageFilename,icon:"delete.gif",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                        }
                        else if(command.serviceName=="annotationService" && command.CLASSNAME=="be.cytomine.command.DeleteCommand") {
                            var cropStyle = "";
                            var cropURL = jsonCommand.cropURL;
//                                if (annotations.get(jsonCommand.id) == undefined) {
//                                    cropStyle = "display : none;";
//                                    cropURL = "";
//                                }
                            action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename,icon:"delete.gif",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});

                        }
                        else if(command.serviceName=="annotationTermService" && command.CLASSNAME=="be.cytomine.command.AddCommand") {
                            action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-plus",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,image:""});

                        }
                        else if(command.serviceName=="annotationTermService" && command.CLASSNAME=="be.cytomine.command.EditCommand") {
                            action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-pencil",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,image:""});

                        }
                        else if(command.serviceName=="annotationTermService" && command.CLASSNAME=="be.cytomine.command.DeleteCommand") {
                            action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-trash",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,image:""});

                        }
                        else if(command.serviceName=="imageInstanceService" && command.CLASSNAME=="be.cytomine.command.AddCommand") {
                            var cropStyle = "block";
                            var cropURL = jsonCommand.thumb;
                            action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename, icon:"add.png",text:commandHistory.get("prefixAction")+ " " + command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});

                        }
                        else if(command.serviceName=="imageInstanceService" && command.CLASSNAME=="be.cytomine.command.DeleteCommand") {
                            var cropStyle = "block";
                            var cropURL = jsonCommand.thumb;
                            action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename,icon:"delete.gif",text:commandHistory.get("prefixAction")+ " " +command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});

                        }
                        ulContainer.append(action);
                    });
                }
                commandCollection.fetch({
                    success : function(collection, response) {
                        commandCallback(collection, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
            });
    },
    showImagesThumbs : function() {
        $("#tabs-projectImageThumb"+this.model.id).show();
        $("#tabs-projectImageListing"+this.model.id).hide();
        $('#imageThumbs'+this.model.id).attr("disabled", "disabled");
        $('#imageArray'+this.model.id).removeAttr("disabled");
    },
    showImagesTable : function() {
        $("#tabs-projectImageThumb"+this.model.id).hide();
        $("#tabs-projectImageListing"+this.model.id).show();
        $('#imageThumbs'+this.model.id).removeAttr("disabled");
        $('#imageArray'+this.model.id).attr("disabled", "disabled");
    }

});