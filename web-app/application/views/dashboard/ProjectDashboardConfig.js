var ProjectDashboardConfig = Backbone.View.extend({
    initialize: function (options) {
        this.rendered = false;
    },
    render: function () {

        var self = this;
        require(["text!application/templates/dashboard/config/DefaultProjectLayersConfig.tpl.html", "text!application/templates/dashboard/config/CustomUIConfig.tpl.html",
            "text!application/templates/dashboard/config/MagicWandConfig.tpl.html", "text!application/templates/dashboard/config/ImageFiltersConfig.tpl.html",
                "text!application/templates/dashboard/config/SoftwareConfig.tpl.html", "text!application/templates/dashboard/config/GeneralConfig.tpl.html"],
            function (defaultLayersTemplate,customUIConfigTemplate, magicWandTemplate, imageFiltersTemplate,softwareConfigTemplate, generalConfigTemplate) {
            self.doLayout(defaultLayersTemplate,customUIConfigTemplate, magicWandTemplate,imageFiltersTemplate,softwareConfigTemplate, generalConfigTemplate);
            self.rendered = true;
        });
        return this;
    },
    doLayout: function (defaultLayersTemplate,customUIConfigTemplate, magicWandTemplate, imageFiltersTemplate,softwareTemplate, generalConfigTemplate) {

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
        var defaultLayers = new GeneralConfigPanel({
            el: _.template(generalConfigTemplate, {titre : titlePanel, id : idPanel}),
            model: this.model
        }).render();
        configList.append(defaultLayers.el);


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


        // Magic Wand
        idPanel = "magicWand";
        titlePanel = "Magic Wand Configuration";
        configs.push({id: idPanel, title : titlePanel});
        var magicWand = new MagicWandConfig({
            el: _.template(magicWandTemplate, {titre : titlePanel, id : idPanel})
        }).render();
        configList.append(magicWand.el);


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
    render: function () {
        var self = this;

        // initialization
        $(self.el).find("input#blindMode-checkbox-config").attr('checked', self.model.get('blindMode'));
        $(self.el).find("input#hideUsersLayers-checkbox-config").attr('checked', self.model.get('hideUsersLayers'));
        $(self.el).find("input#hideAdminsLayers-checkbox-config").attr('checked', self.model.get('hideAdminsLayers'));
        $(self.el).find("input#isReadOnly-checkbox-config").attr('checked', self.model.get('isReadOnly'));

        // update
        $(self.el).on('click', '.general-checkbox-config', function() {
            var project = self.model;

            var blindMode = $(self.el).find("input#blindMode-checkbox-config").is(':checked');
            var isReadOnly = $(self.el).find("input#isReadOnly-checkbox-config").is(':checked');
            var hideUsersLayers = $(self.el).find("input#hideUsersLayers-checkbox-config").is(':checked');
            var hideAdminsLayers = $(self.el).find("input#hideAdminsLayers-checkbox-config").is(':checked');

            project.set({/*retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,*/
                blindMode:blindMode,isReadOnly:isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers});
            project.save({/*retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,*/
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
        });

        return this;
    }

});

var MagicWandConfig = Backbone.View.extend({
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
    },

    fillForm: function () {
        var self = this;
        $(self.el).find("#input_tolerance").val(window.localStorage.getItem(this.toleranceKey));
        $(self.el).find("#input_threshold").val(window.localStorage.getItem(this.thresholdKey));
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

        // load all user and admin of the project
        new UserCollection({project: self.model.id}).fetch({
            success: function (projectUserCollection, response) {
                projectUserCollection.each(function(user) {
                    $(self.el).find('#availableprojectdefaultlayers').append('<option value="'+ user.id +'">' + user.prettyName() + '</option>');
                });
            }
        });




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

        // load existing default layers
        new ProjectDefaultLayerCollection({project: self.model.id}).fetch({
            success: function (collection) {
                var defaultLayersArray=[]
                collection.each(function(layer) {
                    defaultLayersArray.push({id: layer.id, userId: layer.attributes.user, hideByDefault: layer.attributes.hideByDefault});
                });


                for(var i = 0; i<defaultLayersArray.length; i++){
                    $(self.el).find("#selectedDefaultLayers").show();
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

        return this;
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
