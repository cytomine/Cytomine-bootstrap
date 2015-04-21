var ProjectDashboardConfig = Backbone.View.extend({
    initialize: function (options) {
        this.rendered = false;
    },
    render: function () {

        var self = this;
        require(["text!application/templates/dashboard/config/DefaultProjectLayersConfig.tpl.html", "text!application/templates/dashboard/config/CustomUIConfig.tpl.html",
            "text!application/templates/dashboard/config/AnnotationToolsConfig.tpl.html", "text!application/templates/dashboard/config/ImageFiltersConfig.tpl.html",
                "text!application/templates/dashboard/config/SoftwareConfig.tpl.html", "text!application/templates/dashboard/config/GeneralConfig.tpl.html",
                "text!application/templates/dashboard/config/UsersConfig.tpl.html"
            ],
            function (defaultLayersTemplate,customUIConfigTemplate, magicWandTemplate, imageFiltersTemplate,softwareConfigTemplate, generalConfigTemplate, usersConfigTemplate) {
            self.doLayout(defaultLayersTemplate,customUIConfigTemplate, magicWandTemplate,imageFiltersTemplate,softwareConfigTemplate, generalConfigTemplate, usersConfigTemplate);
            self.rendered = true;
        });
        return this;
    },
    doLayout: function (defaultLayersTemplate,customUIConfigTemplate, AnnotToolsTemplate, imageFiltersTemplate,softwareTemplate, generalConfigTemplate, usersConfigTemplate) {

        // generate the menu skeleton

        var html = '';
        html = html + '<div class="col-md-2">';
        html = html + '    <div class="panel panel-default">';
        html = html + '        <div class="panel-heading">';
        html = html + '            <h4>Configurations</h4>';
        html = html + '        </div>';

        html = html + '    </div>';
        html = html + '</div>';

        var menu = $(html)

        var configMenu = $('<div class="panel-body"></div>');


        // generate the config tabs

        var configList = $('<div class="col-md-9"></div>');
        var idPanel;
        var titlePanel;
        var configs=[];

        // General Configs
        idPanel = "general";
        titlePanel = "General Configuration";
        configs.push({id: idPanel, title : titlePanel});
        var general = new GeneralConfigPanel({
            el: _.template(generalConfigTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(general.el);


        // Users Management
        idPanel = "users";
        titlePanel = "Users Management";
        configs.push({id: idPanel, title : titlePanel});
        var users = new UsersConfigPanel({
            el: _.template(usersConfigTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(users.el);


        // Default Layers
        idPanel = "defaultLayers";
        titlePanel = "Default Layers Configuration";
        configs.push({id: idPanel, title : titlePanel});
        var defaultLayers = new DefaultLayerPanel({
            el: _.template(defaultLayersTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(defaultLayers.el);


        // CustomUI
        idPanel = "customUi";
        titlePanel = "Custom UI Configuration";
        configs.push({id: idPanel, title : titlePanel});
        var uiPanel = new CutomUIPanel({
            el: _.template(customUIConfigTemplate, {titre : titlePanel, id : idPanel})
        }).render();
        configList.append(uiPanel.el);


        // Image Filters
        idPanel = "imageFilters";
        titlePanel = "Image filters";
        configs.push({id: idPanel, title : titlePanel});
        var filters = new ImageFiltersProjectPanel({
            el: _.template(imageFiltersTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(filters.el);


        // Softwares Project
        idPanel = "softwaresProject";
        titlePanel = "Softwares";
        configs.push({id: idPanel, title : titlePanel});
        var softwares = new SoftwareProjectPanel({
            el: _.template(softwareTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(softwares.el);

        // Annotation tools Config
        idPanel = "annotTools";
        titlePanel = "Private Annotation Tools Configuration";
        configs.push({id: idPanel, title : titlePanel});
        var magicWand = new AnnotationToolsConfig({
            el: _.template(AnnotToolsTemplate, {titre : titlePanel, id : idPanel})
        }).render();
        configList.append(magicWand.el);


        var callBack = function(users) {
            new ProjectDefaultLayerCollection({project: this.model.id}).fetch({
                success: function (collection) {
                    var layersToDelete = 0;
                    var layersDeleted = 0;
                    collection.each(function(layer) {
                        if(users.indexOf(layer.attributes.user) == -1) {
                            layersToDelete++;
                            console.log("deletion de ");
                            console.log(layer.id);
                            layer.destroy({
                                success: function (model, response) {
                                    layersDeleted++;
                                    if(layersToDelete == layersDeleted) {
                                        defaultLayers.refresh();
                                    }
                                }
                            });
                        }
                        if(layersToDelete == 0) {
                            defaultLayers.refresh();
                        }
                    });
                }
            });
        }
        users.setCallback(callBack);


        // Generation of the left menu
        $.each(configs, function (index, value) {
            configMenu.append('<div><input id="'+value.id+'-config-checkbox" type="checkbox" checked> '+value.title+'</div>');
            configMenu.find("input#"+value.id+"-config-checkbox").change(function () {
                if ($(this).is(':checked')) {
                    $("#config-panel-"+value.id).show()
                } else {
                    $("#config-panel-"+value.id).hide()
                }
            });

        });

        menu.find(".panel-default").append(configMenu);

        $(this.el).append(menu);
        $(this.el).append(configList);

    },
    refresh: function () {
        if (!this.rendered) {
            this.render();
        }
    }

});

var GeneralConfigPanel = Backbone.View.extend({
    projectMultiSelectAlreadyLoad: false,
    projects: [],
    projectRetrieval: [],
    render: function () {
        var self = this;

        // initialization
        $(self.el).find("input#blindMode-checkbox-config").attr('checked', self.model.get('blindMode'));
        $(self.el).find("input#hideUsersLayers-checkbox-config").attr('checked', self.model.get('hideUsersLayers'));
        $(self.el).find("input#hideAdminsLayers-checkbox-config").attr('checked', self.model.get('hideAdminsLayers'));
        $(self.el).find("input#isReadOnly-checkbox-config").attr('checked', self.model.get('isReadOnly'));


        new ProjectCollection().fetch({
            success: function (collection, response) {
                self.projects = collection;

                // change handler
                $(self.el).find("input#retrievalProjectSome-radio-config,input#retrievalProjectAll-radio-config,input#retrievalProjectNone-radio-config").change(function (test) {
                    self.refreshRetrievalProjectSelect();
                    self.update();
                });

                if (self.model.get('retrievalDisable')) {
                    $(self.el).find("input#retrievalProjectNone-radio-config").attr("checked", "checked");
                } else if (self.model.get('retrievalAllOntology')) {
                    $(self.el).find("input#retrievalProjectAll-radio-config").attr("checked", "checked");
                } else {
                    $(self.el).find("input#retrievalProjectSome-radio-config").attr("checked", "checked");
                }
                self.refreshRetrievalProjectSelect();
            }
        });

        // if the project name change !
        /*$(self.el).find("input#project-name").change(function () {
            console.log("change");
            if (self.projectMultiSelectAlreadyLoad) {
                self.createRetrievalProjectSelect();
            }
        });*/

        $(self.el).on('click', '.general-checkbox-config', function() {
            self.update();
        });

        return this;
    },
    refreshRetrievalProjectSelect: function () {
        var self = this;
        if ($(self.el).find("input#retrievalProjectSome-radio-config").is(':checked')) {
            if (!self.projectMultiSelectAlreadyLoad) {
                self.createRetrievalProjectSelect(self.projects);
                self.projectMultiSelectAlreadyLoad = true
            } else {
                $(self.el).find("div#retrievalGroup").find(".uix-multiselect").show();
            }
        } else {
            $(self.el).find("div#retrievalGroup").find(".uix-multiselect").hide();
        }
    },
    createRetrievalProjectSelect: function (projects) {
        var self = this;
        /* Create Projects List */
        $(self.el).find("#retrievalproject").empty();

        projects.each(function (project) {
            if (project.get('ontology') == self.model.get('ontology') && project.id != self.model.id) {
                if (_.indexOf(self.model.get('retrievalProjects'), project.id) == -1) {
                    $(self.el).find("#retrievalproject").append('<option value="' + project.id + '">' + project.get('name') + '</option>');
                }
                else {
                    $(self.el).find("#retrievalproject").append('<option value="' + project.id + '" selected="selected">' + project.get('name') + '</option>');
                }
            }
        });

        // try to change dynamically the name
        //$(self.el).find("#retrievalproject").append('<option value="' + self.model.id + '" selected="selected">' + $('#login-form-edit-project').find("#project-edit-name").val() + '</option>');
        $(self.el).find("#retrievalproject").append('<option value="' + self.model.id + '" selected="selected">' + self.model.get('name') + '</option>');

        $(self.el).find("#retrievalproject").multiselectNext().bind("multiselectChange", function(evt, ui) {
            self.projectRetrieval = [];
            //var values = $.map(ui.optionElements, function(opt) { return $(opt).attr('value'); });
            //console.log("Multiselect change event! " + ui.optionElements.length + ' value ' + (ui.selected ? 'selected' : 'deselected') + ' (' + values + ')');
            $(this).find("option:selected").each(function(i, o) {
                self.projectRetrieval.push(o.value);
            });
            self.update();
        });

        $(self.el).find(".ui-button-icon-only .ui-icon").css("margin-top", "-8px");
        $(self.el).find("div.uix-multiselect").css("background-color", "#DDDDDD");
    },
    update: function() {
        var self = this;

        var project = self.model;

        var blindMode = $(self.el).find("input#blindMode-checkbox-config").is(':checked');
        var isReadOnly = $(self.el).find("input#isReadOnly-checkbox-config").is(':checked');
        var hideUsersLayers = $(self.el).find("input#hideUsersLayers-checkbox-config").is(':checked');
        var hideAdminsLayers = $(self.el).find("input#hideAdminsLayers-checkbox-config").is(':checked');

        var retrievalDisable = $(self.el).find("input#retrievalProjectNone-radio-config").is(':checked');
        var retrievalProjectAll = $(self.el).find("input#retrievalProjectAll-radio-config").is(':checked');
        var retrievalProjectSome = $(self.el).find("input#retrievalProjectSome-radio-config").is(':checked');
        if (!retrievalProjectSome) {
            self.projectRetrieval = [];
        }


        project.set({retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: self.projectRetrieval,
            blindMode:blindMode,isReadOnly:isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers});
        project.save({retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: self.projectRetrieval,
            blindMode:blindMode,isReadOnly:isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers}, {
            success: function (model, response) {
                console.log("1. Project edited!");
                window.app.view.message("Project", response.message, "success");
                var id = response.project.id;
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Project", json.errors, "error");
            }
        });
    }

});

var UsersConfigPanel = Backbone.View.extend({
    userMaggicSuggest : null,
    adminMaggicSuggest : null,
    projectUsers: [],
    projectAdmins: [],
    groups: null,
    callback: null,
    render: function() {
        var self = this;
        self.createUserList();
        self.createMultiSelectUser();

        $(self.el).find("input#addUsersByName-radio-config,input#addUsersByGroup-radio-config,input#addUsersByMail-radio-config").change(function (test) {
            if ($(self.el).find("input#addUsersByName-radio-config").is(':checked')) {
                $(self.el).find("div#projectedituser").show();
                $(self.el).find(".uix-multiselect").hide();
                $(self.el).find("#invite_new_user").hide();
            } else if ($(self.el).find("input#addUsersByMail-radio-config").is(':checked')){
                $(self.el).find("div#projectedituser").hide();
                $(self.el).find("#invite_new_user").show();
                $(self.el).find(".uix-multiselect").hide();
            } else {
                $(self.el).find("div#projectedituser").hide();
                $(self.el).find("#invite_new_user").hide();
                $(self.el).find(".uix-multiselect").show();
            }
        });
        $(self.el).find("input#addUsersByName-radio-config,input#addUsersByGroup-radio-config").trigger('change');

        $(self.el).find("#invitenewuserbutton").click(function (event) {
            var username = $(self.el).find("#new_username").val()
            var mail = $(self.el).find("#new_mail").val()

            $.ajax({
                type: "POST",
                url: "api/project/"+self.model.id+"/invitation.json",
                data: " {name : "+username+", mail:"+mail+"}",
                contentType:"application/json; charset=utf-8",
                dataType:"json",
                success: function() {
                    window.app.view.message("Project", username+" invited!", "success");
                    self.refreshUserList(true);
                    self.loadMultiSelectUser()
                    $(self.el).find("#new_username").val("")
                    $(self.el).find("#new_mail").val("")
                },
                error: function(x) {
                    window.app.view.message("Project", x.responseJSON.errors, "error");
                }
            });


        });

        return this;
    },
    createMultiSelectUser: function() {

        var self = this;

        $(self.el).find("#usersByGroup").multiselectNext().bind("multiselectChange", function(evt, ui) {

            self.projectUsers = [];
            //var values = $.map(ui.optionElements, function(opt) { return $(opt).attr('value'); });
            //console.log("Multiselect change event! " + ui.optionElements.length + ' value ' + (ui.selected ? 'selected' : 'deselected') + ' (' + values + ')');
            $(this).find("option:selected").each(function(i, o) {
                self.projectUsers.push(o.value);
            });
            self.update(function() {
                self.refreshUserList();
            });

            $(self.el).find("#usersByGroup").multiselectNext('refresh', function() {
                $(self.el).find(".ui-button-icon-only .ui-icon").css("margin-top", "-8px");
            });
        });

        $(self.el).find(".ui-button-icon-only .ui-icon").css("margin-top", "-8px");
        $(self.el).find("div.uix-multiselect").css("background-color", "#DDDDDD");

        self.loadMultiSelectUser();
    },
    loadMultiSelectUser: function() {

        var self = this;
        var currentUsers;

        $(self.el).find("#usersByGroup").empty();
        $(self.el).find("#usersByGroup").multiselectNext('refresh');

        // I need to restart multiselect to include to options append to the select
        var reload = function(currentUsers, groupUsers) {
            if(currentUsers==null || groupUsers==null || currentUsers==undefined || groupUsers==undefined) {
                return;
            }

            currentUsers.each(function(user) {
                if($.inArray( user.id, self.projectAdmins ) == -1){
                    $(self.el).find("#usersByGroup").append('<option value="' + user.id + '" selected>' + user.prettyName() + '</option>');
                } else {
                    $(self.el).find("#usersByGroup").append('<option value="' + user.id + '" selected disabled>' + user.prettyName() + '</option>');
                }
            });

            var ids = $.map( currentUsers.models, function( a ) {
                return a.id;
            });

            groupUsers.each(function(group) {

                $(self.el).find("#usersByGroup").append('<optgroup label="'+group.attributes.name+'">');
                var optGroup = $(self.el).find("#usersByGroup optgroup").last()
                for(var i=0; i<group.attributes.users.length ; i++) {
                    var currentUser = group.attributes.users[i];
                    if($.inArray( currentUser.id, ids ) == -1){
                        optGroup.append('<option value="' + currentUser.id + '">' + currentUser.lastname + ' ' + currentUser.firstname + '(' + currentUser.username + ')' + '</option>');
                    } else {
                        optGroup.append('<option value="' + currentUser.id + '" disabled>' + currentUser.lastname + ' ' + currentUser.firstname + '(' + currentUser.username + ')' + '</option>');
                    }
                }
                $(self.el).find("#usersByGroup").append('</optgroup>');


            });
            $(self.el).find("#usersByGroup").multiselectNext('refresh', function() {
                $(self.el).find(".ui-button-icon-only .ui-icon").css("margin-top", "-8px");
            });
        };


        // load current users
        new UserCollection({project:self.model.id}).fetch({
            success: function (currentUsersCollection, response) {
                currentUsers = currentUsersCollection;
                reload(currentUsers, self.groups);
            }
        });

        // TODO

        // do a request to have all users of this group
        new GroupWithUserCollection().fetch({
            success: function (groupUsersCollection, response) {
                self.groups = groupUsersCollection;
                reload(currentUsers, self.groups);
            }
        });
    },
    createUserList: function () {
        var self = this;
        var allUser = null;
        var projectUser = null;
        var projectAdmin = null;


        var loadUser = function() {
            if(allUser == null || projectUser == null || projectAdmin == null) {
                return;
            }
            var allUserArray = [];

            allUser.each(function(user) {
                allUserArray.push({id:user.id,label:user.prettyName()});
            });

            var projectUserArray=[]
            projectUser.each(function(user) {
                projectUserArray.push(user.id);
            });

            projectAdmin.each(function(user) {
                self.projectAdmins.push(user.id);
            });

            self.userMaggicSuggest = $(self.el).find('#projectedituser').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: projectUserArray,
                width: 590,
                maxSelection:null
            });
            $(self.userMaggicSuggest).on('selectionchange', function(e,m){
                self.projectUsers = this.getValue()
                self.update(function() {
                    self.loadMultiSelectUser()
                })
            });

            self.adminMaggicSuggest = $(self.el).find('#projecteditadmin').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: self.projectAdmins,
                width: 590,
                maxSelection:null
            });
            $(self.adminMaggicSuggest).on('selectionchange', function(e,m){
                self.update(function() {
                    self.loadMultiSelectUser()
                })
            });
        }

        new UserCollection({}).fetch({
            success: function (allUserCollection, response) {
                allUser = allUserCollection;
                loadUser();
            }});

        new UserCollection({project: self.model.id}).fetch({
            success: function (projectUserCollection, response) {
                projectUser = projectUserCollection;
                window.app.models.projectUser = projectUserCollection;
                loadUser();
            }});

        new UserCollection({project: self.model.id, admin:true}).fetch({
            success: function (projectUserCollection, response) {
                projectAdmin = projectUserCollection;
                window.app.models.projectAddmin = projectUserCollection;
                loadUser();
            }});

    },
    refreshUserList: function (reloadAllUsers) {
        var self = this;
        var projectUsers = null;
        var allUsers = null;
        var reloadDone = false;

        var loadUser = function() {

            if(!reloadDone || projectUsers == null) {
                return
            }

            var projectUserArray=[]
            projectUsers.each(function(user) {
                projectUserArray.push(user.id);
            });

            // Avoid an infinite loop between the 2 listeners.
            $(self.userMaggicSuggest).off('selectionchange');
            self.userMaggicSuggest.clear();
            self.userMaggicSuggest.setValue(projectUserArray);

            $(self.userMaggicSuggest).on('selectionchange', function(e,m){
                self.projectUsers = this.getValue()
                self.update(function() {
                    self.loadMultiSelectUser()
                })
            });
        }

        new UserCollection({project: self.model.id}).fetch({
            success: function (projectUserCollection, response) {
                projectUsers = projectUserCollection;
                window.app.models.projectUser = projectUserCollection;
                loadUser();
            }});


        if(reloadAllUsers) {
            new UserCollection({}).fetch({
                success: function (allUserCollection, response) {
                    var allUserArray = [];
                    allUserCollection.each(function(user) {
                        allUserArray.push({id:user.id,label:user.prettyName()});
                    });

                    self.userMaggicSuggest.setData(allUserArray)
                    self.adminMaggicSuggest.setData(allUserArray)
                    reloadDone = true
                    loadUser();
                }});

        }





    },
    update: function(callbackSuccess) {
        var self = this;




        var project = self.model;

        var users = self.projectUsers;

        var admins = self.adminMaggicSuggest.getValue();
        self.projectAdmins = admins

        project.set({users: users, admins:admins});
        project.save({users:users, admins:admins}, {
            success: function (model, response) {
                console.log("1. Project edited!");
                window.app.view.message("Project", response.message, "success");
                var id = response.project.id;
                if(callbackSuccess != null && callbackSuccess != undefined) {
                    callbackSuccess()
                }
                // here, we need a refresh of the DefaultLayerPanel as the users have changed !!!
                self.callback(users.concat(admins))
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Project", json.errors, "error");
            }
        });
    },
    setCallback : function(callback) {
        this.callback = callback;
    }
});

var AnnotationToolsConfig = Backbone.View.extend({
    defaultRadiusValue: 8,
    thresholdKey: null,
    toleranceKey: null,
    initialize: function () {
        this.toleranceKey = "mw_tolerance" + window.app.status.currentProject;
        if (window.localStorage.getItem(this.toleranceKey) == null) {
            window.localStorage.setItem(this.toleranceKey, Processing.MagicWand.defaultTolerance);
        }
        this.thresholdKey = "th_threshold" + window.app.status.currentProject;
        if (window.localStorage.getItem(this.thresholdKey) == null) {
            window.localStorage.setItem(this.thresholdKey, Processing.Threshold.defaultTheshold);
        }
        this.radiusKey = "point_radius" + window.app.status.currentProject;
        if (window.localStorage.getItem(this.radiusKey) == null) {
            window.localStorage.setItem(this.radiusKey, this.defaultRadiusValue);
        }
        return this;
    },

    render: function () {
        this.fillForm();
        this.initEvents();
        return this;
    },

    initEvents: function () {
        var self = this;
        var form = $(self.el).find("#mwToleranceForm")
        var max_euclidian_distance = Math.ceil(Math.sqrt(255 * 255 + 255 * 255 + 255 * 255)) //between pixels
        // Magic Wand Form
        form.on("submit", function (e) {
            e.preventDefault();
            //tolerance
            var toleranceValue = parseInt($(self.el).find("#input_tolerance").val());
            if (_.isNumber(toleranceValue) && toleranceValue >= 0 && toleranceValue < max_euclidian_distance) {
                window.localStorage.setItem(self.toleranceKey, Math.round(toleranceValue));
                var successMessage = _.template("Tolerance value for project <%= name %> is now <%= tolerance %>", {
                    name: window.app.status.currentProjectModel.get('name'),
                    tolerance: toleranceValue
                });
                window.app.view.message("Success", successMessage, "success");
            } else {
                window.app.view.message("Error", "Tolerance must be an integer between 0 and " + max_euclidian_distance, "error");
            }

            var thresholdValue = parseInt($(self.el).find("#input_threshold").val());
            if (_.isNumber(thresholdValue) && thresholdValue >= 0 && thresholdValue < 255) {
                window.localStorage.setItem(self.thresholdKey, Math.round(thresholdValue));
                successMessage = _.template("Threshold value for project <%= name %> is now <%= threshold %>", {
                    name: window.app.status.currentProjectModel.get('name'),
                    threshold: thresholdValue
                });
                window.app.view.message("Success", successMessage, "success");
            } else {
                window.app.view.message("Error", "Threshold must be an integer between 0 and 255", "error");
            }
        });

        // Point Form
        var form = $(self.el).find("#pointConfigForm")
        form.on("submit", function (e) {
            e.preventDefault();
            var radiusValue = parseInt($(self.el).find("#input_radius").val());
            if (_.isNumber(radiusValue) && radiusValue >= 0) {
                window.localStorage.setItem(self.radiusKey, Math.round(radiusValue));
                var successMessage = _.template("Radius value for project <%= name %> is now <%= radius %>", {
                    name: window.app.status.currentProjectModel.get('name'),
                    radius: radiusValue
                });
                window.app.view.message("Success", successMessage, "success");
            } else {
                window.app.view.message("Error", "Radius must be an integer greater than 0 ", "error");
            }
        });
    },

    fillForm: function () {
        var self = this;
        $(self.el).find("#input_tolerance").val(window.localStorage.getItem(this.toleranceKey));
        $(self.el).find("#input_threshold").val(window.localStorage.getItem(this.thresholdKey));
        $(self.el).find("#input_radius").val(window.localStorage.getItem(this.radiusKey));
    }
});

var SoftwareProjectPanel = Backbone.View.extend({

    removeSoftware: function (idSoftwareProject) {
        var self = this;
        new SoftwareProjectModel({ id: idSoftwareProject }).destroy({
            success: function (model, response) {
                $(self.el).find("li.software" + idSoftwareProject).remove();
                window.app.view.message("", response.message, "success");
            },
            error: function (model, response) {

            }
        });
        return false;
    },

    renderSoftwares: function () {
        var self = this;
        var el = $(this.el).find(".softwares");
        new SoftwareProjectCollection({ project: self.model.id}).fetch({
            success: function (softwareProjectCollection, response) {
                softwareProjectCollection.each(function (softwareProject) {
                    self.renderSoftware(softwareProject, el);
                });
            }
        });

    },
    renderSoftware: function (softwareProject, el) {
        var tpl = _.template("<li class='software<%= id %>' style='padding-bottom : 3px;'><a class='btn btn-default  btn-sm btn-danger removeSoftware' data-id='<%= id %>' href='#'><i class='icon-trash icon-white' /> Delete</a> <%= name %></li>", softwareProject.toJSON());
        $(el).append(tpl);
    },

    render: function () {
        var self = this;
        new SoftwareCollection().fetch({
            success: function (softwareCollection, response) {
                softwareCollection.each(function (software) {
                    var option = _.template("<option value='<%= id %>'><%= name %></option>", software.toJSON());
                    $(self.el).find("#addSoftware").append(option);
                });
                $(self.el).find("#addSoftwareButton").click(function (event) {
                    event.preventDefault();
                    new SoftwareProjectModel({ project: self.model.id, software: $(self.el).find("#addSoftware").val()}).save({}, {
                        success: function (softwareProject, response) {
                            self.renderSoftware(new SoftwareProjectModel(softwareProject.toJSON().softwareproject), el);
                            window.app.view.message("", response.message, "success");
                        },
                        error: function (model, response) {
                            window.app.view.message("", $.parseJSON(response.responseText).errors, "error");
                        }
                    });
                    return false;
                });
            }
        });

        self.renderSoftwares();

        $(document).on('click', "a.removeSoftware", function () {
            var idSoftwareProject = $(this).attr('data-id');
            self.removeSoftware(idSoftwareProject);
            return false;
        });

        return this;
    }

});

var DefaultLayerPanel = Backbone.View.extend({

    render: function () {
        var self = this;

        $(self.el).find("#selectedDefaultLayers").hide();

        self.refresh();


        $(self.el).find('#projectadddefaultlayersbutton').click(function() {

            var container = $(self.el).find('#availableprojectdefaultlayers')[0];
            var selected = container.options[container.options.selectedIndex];
            if(selected.value != null && selected.value != undefined && selected.value != '') {
                $(self.el).find("#selectedDefaultLayers").show();
                // check if not already taken
                if ($(self.el).find('#selectedDefaultLayers #defaultlayer' + selected.value).length == 0) {
                    $(self.el).find('#selectedDefaultLayers').append('<div class="col-md-3 col-md-offset-1"><input type="checkbox" id="hideByDefault' + selected.value + '" class="hideByDefault"> Hide layers by default</div>');
                    $(self.el).find('#selectedDefaultLayers').append('<div class="col-md-5"><p>' + selected.text + '</p></div>');
                    $(self.el).find('#selectedDefaultLayers').append('<div class="col-md-2"><a id="defaultlayer' + selected.value + '" class="projectremovedefaultlayersbutton btn btn-info" href="javascript:void(0);">Remove</a></div>');
                    save(selected.value, false);
                }
            }
        });
        $(self.el).find('#selectedDefaultLayers').on('click', '.projectremovedefaultlayersbutton', function() {
            var id = $(this).attr("id").replace("defaultlayer","");
            $(this).parent().prev().prev().remove();
            $(this).parent().prev().remove();
            $(this).parent().remove();
            if($(self.el).find("#selectedDefaultLayers").children().length ==0){
                $(self.el).find("#selectedDefaultLayers").hide();
            }
            destroy(id);
        });

        $(self.el).find('#selectedDefaultLayers').on('click', '.hideByDefault', function() {
            var id = $(this).attr("id").replace("hideByDefault","");
            var chkb = $(this);

            var layer = new ProjectDefaultLayerModel({id:id, project: self.model.id}).fetch({
                success: function (lModel) {
                    lModel.set('hideByDefault', chkb.is(":checked"));
                    lModel.save(null, {
                        success: function (model) {
                            console.log("updated");
                        }
                    });
                }
             });

        });


        var save = function(userId, hide) {
            var layer = new ProjectDefaultLayerModel({user: userId, project: self.model.id, hideByDefault: hide});
            layer.save(null, {
                success: function (model, response) {
                    console.log("save success");
                    // with the project_default_layer id, we will be able to delete them more efficiently
                    var id = $(self.el).find('#selectedDefaultLayers #defaultlayer' + userId).attr("id").replace(userId,response.projectdefaultlayer.id);
                    $(self.el).find('#selectedDefaultLayers #defaultlayer' + userId).attr("id", id);
                    id = $(self.el).find('#selectedDefaultLayers #hideByDefault' + userId).attr("id").replace(userId,response.projectdefaultlayer.id);
                    $(self.el).find('#selectedDefaultLayers #hideByDefault' + userId).attr("id", id);
                },
                error: function (x, y) {
                    console.log("save error");
                    console.log(x);
                    console.log(y.responseText);
                }
            });
        };
        var destroy = function(id) {
            var layer = new ProjectDefaultLayerModel({id:id, project: self.model.id}).fetch({
                success: function (lModel) {
                    lModel.destroy({
                        success: function (model) {
                            console.log("destroyed");
                        }
                    });
                }
            });
        };

        return this;
    },
    refresh : function(){
        var self = this;
        $(self.el).find('#availableprojectdefaultlayers').empty();
        $(self.el).find('#selectedDefaultLayers').empty();


        // load all user and admin of the project
        new UserCollection({project: self.model.id}).fetch({
            success: function (projectUserCollection, response) {
                projectUserCollection.each(function(user) {
                    $(self.el).find('#availableprojectdefaultlayers').append('<option value="'+ user.id +'">' + user.prettyName() + '</option>');
                });
            }
        });

        // load existing default layers
        new ProjectDefaultLayerCollection({project: self.model.id}).fetch({
            success: function (collection) {
                var defaultLayersArray=[]
                collection.each(function(layer) {
                    defaultLayersArray.push({id: layer.id, userId: layer.attributes.user, hideByDefault: layer.attributes.hideByDefault});
                });


                $(self.el).find("#selectedDefaultLayers").show();
                for(var i = 0; i<defaultLayersArray.length; i++){
                    // check if not already taken
                    $(self.el).find('#selectedDefaultLayers').append('<div class="col-md-3 col-md-offset-1"><input type="checkbox" id="hideByDefault' + defaultLayersArray[i].id + '" class="hideByDefault"> Hide layers by default</div>');
                    $(self.el).find('#hideByDefault' + defaultLayersArray[i].id)[0].checked = defaultLayersArray[i].hideByDefault;
                    $(self.el).find('#selectedDefaultLayers').append('<div id = "tmp'+ defaultLayersArray[i].userId +'" class="col-md-5"><p></p></div>');
                    $(self.el).find('#selectedDefaultLayers').append('<div class="col-md-2"><a id="defaultlayer' + defaultLayersArray[i].id + '" class="projectremovedefaultlayersbutton btn btn-info" href="javascript:void(0);">Remove</a></div>');
                    new UserModel({id: defaultLayersArray[i].userId}).fetch({
                        success: function (model) {
                            $(self.el).find('#tmp'+model.id).find("p").text(model.prettyName());
                            $(self.el).find('#tmp'+model.id).removeAttr('id');
                        }
                    });
                }
            }
        });
    }

});

var ImageFiltersProjectPanel = Backbone.View.extend({
    removeImageFilter: function (idImageFilter) {
        var self = this;
        new ProjectImageFilterModel({ id: idImageFilter}).destroy({
            success: function (model, response) {
                $(self.el).find("li.imageFilter" + idImageFilter).remove();
                window.app.view.message("", response.message, "success");
            }
        });
        return false;
    },
    renderFilters: function () {
        var self = this;
        var el = $(this.el).find(".image-filters");
        new ProjectImageFilterCollection({ project: self.model.id}).fetch({
            success: function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    self.renderImageFilter(imageFilter, el);
                });
            }
        });
    },
    renderImageFilter: function (imageFilter, el) {
        var tpl = _.template("<li class='imageFilter<%= id %>' style='padding-bottom : 3px;'> <a class='btn btn-default  btn-sm btn-danger removeImageFilter' data-id='<%= id %>' href='#'><i class=' icon-trash icon-white' /> Delete</a> <%= name %></li>", imageFilter.toJSON());
        $(el).append(tpl);
    },
    render: function () {
        var self = this;
        var el = $(this.el).find(".image-filters");
        new ImageFilterCollection().fetch({
            success: function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    var option = _.template("<option value='<%=  id %>'><%=   name %></option>", imageFilter.toJSON());
                    $(self.el).find("#addImageFilter").append(option);

                });
                $(self.el).find("#addImageFilterButton").click(function (event) {
                    event.preventDefault();
                    new ProjectImageFilterModel({ project: self.model.id, imageFilter: $(self.el).find("#addImageFilter").val()}).save({}, {
                        success: function (imageFilter, response) {
                            self.renderImageFilter(new ImageFilterModel(imageFilter.toJSON().imagefilterproject), el);
                            window.app.view.message("", response.message, "success");
                        },
                        error: function (response) {
                            window.app.view.message("", $.parseJSON(response.responseText).errors, "error");
                        }
                    });
                    return false;
                });
            }
        });

        self.renderFilters();

        $(document).on('click', "a.removeImageFilter", function () {
            var idImageFilter = $(this).attr("data-id");
            self.removeImageFilter(idImageFilter);
            return false;
        });
        return this;

    }
});


var CutomUIPanel = Backbone.View.extend({
    obj : null,

    refresh : function() {
        var self = this;
        var elTabs = $(self.el).find("#custom-ui-table-tabs");
        var elPanels = $(self.el).find("#custom-ui-table-panels");
        var elTools = $(self.el).find("#custom-ui-table-tools");

        var fn = function() {
            require(["text!application/templates/dashboard/config/CustomUIItem.tpl.html"], function (customUIItemTpl) {
                elTabs.empty();
                elPanels.empty();
                elTools.empty();

                _.each(CustomUI.components,function(component) {
                    self.createComponentConfig(component,customUIItemTpl,elTabs);
                });
                _.each(CustomUI.componentsPanels,function(component) {
                    self.createComponentConfig(component,customUIItemTpl,elPanels);
                });
                _.each(CustomUI.componentsTools,function(component) {
                    self.createComponentConfig(component,customUIItemTpl,elTools);
                });

                $(self.el).find("#btn-project-configuration-tab-ADMIN_PROJECT").attr("disabled", "disabled");

                $(self.el).find("#custom-ui-table").find("button").click(function(eventData,ui) {

                    console.log(eventData.target.id);
                    var currentButton = $(self.el).find("#"+eventData.target.id);
                    var isActiveNow = self.obj[currentButton.data("component")][currentButton.data("role")]==true;
                    currentButton.removeClass(isActiveNow? "btn-success" : "btn-danger");
                    currentButton.addClass(isActiveNow? "btn-danger" : "btn-success");
                    self.obj[currentButton.data("component")][currentButton.data("role")]=!self.obj[currentButton.data("component")][currentButton.data("role")];
                    self.addConfig();
                })

            });
        }
        self.retrieveConfig(fn);
    },
    createComponentConfig : function(component, template,mainElement) {
        var self = this;
        var customUI = _.template(template,component);
        $(mainElement).append(customUI);
        var tr = $(mainElement).find("#customUI-"+component.componentId+"-roles");
        tr.append("<td>"+component.componentName+"</td>");
        if(!self.obj[component.componentId]) {
            //component is not define in the project config, active by default
            self.obj[component.componentId] = {};
            _.each(CustomUI.roles,function(role) {
                var active = true;
                self.obj[component.componentId][role.authority] = active;
                tr.append(self.createButton(role,component,active));
            });
        } else {
            _.each(CustomUI.roles,function(role) {
                var active = true;
                if( !self.obj[component.componentId][role.authority]) {
                    active = false;
                }
                tr.append(self.createButton(role,component,active));
            });

        }
    },
    render: function () {
        var self = this;

        self.refresh();
        return this;
    },
    addConfig : function() {
        var self = this;
        $.ajax({
            type: "POST",
            url: "custom-ui/project/"+window.app.status.currentProject+".json",
            data: JSON.stringify(self.obj),
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function() {
                self.refresh();
                window.app.view.message("Project", "Configuration save!", "success");
                CustomUI.customizeUI(function() {CustomUI.hideOrShowComponents();});
            }
        });
    },
    retrieveConfig : function(callback) {
        var self = this;
        $.get( "custom-ui/project/"+window.app.status.currentProject+".json", function( data ) {
            self.obj = data;
            callback();
        });
    },
    createButton : function(role,component, active) {
        var classBtn = active? "btn-success" : "btn-danger";
        return '<td><button type="radio" data-component="'+component.componentId+'" data-role="'+role.authority+'" id="btn-' + component.componentId +'-'+role.authority+'" class="btn  btn-large btn-block '+classBtn+'">'+role.name+'</button></td>';
    }

});
