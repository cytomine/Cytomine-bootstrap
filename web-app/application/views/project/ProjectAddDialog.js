var AddProjectDialog = Backbone.View.extend({
    projectsPanel: null,
    addProjectDialog: null,
    userMultiSelect: null,
    projectMultiSelectAlreadyLoad: false,
    initialize: function (options) {
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        this.ontologies = options.ontologies;
        this.disciplines = options.disciplines;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddDialog.tpl.html",
            "text!application/templates/project/OntologiesChoicesRadio.tpl.html",
            "text!application/templates/project/DisciplinesChoicesRadio.tpl.html"
        ],
                function (projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl) {
                    self.doLayout(projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl);
                });
        return this;
    },
    doLayout: function (projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl) {
        var self = this;
        var dialog = _.template(projectAddDialogTpl, {});
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        $(self.el).append(dialog);

        self.initStepy();
        self.createProjectInfo(ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl);
        self.createUserList();
        self.createRetrievalProject();

        //Build dialog
        self.addProjectDialog = $("#addproject").modal({
            keyboard: true,
            backdrop: true
        });
        $("#saveProjectButton").click(function (event) {
            event.preventDefault();
            $("#login-form-add-project").submit();
            return false;
        });

        self.open();

        $("input#isReadOnly").attr('checked', window.app.params.readOnlyProjectsByDefault);

        return this;
    },
    initStepy: function () {
        $('#login-form-add-project').stepy({next: function (index) {
            //check validate name
            var error = false;
            if (index == 2) {
                if ($("#project-name").val().toUpperCase().trim() == "") {
                    window.app.view.message("User", "You must provide a valide project name!", "error");
                    error = true;
                }
                console.log($("#projectontology").val());
                if ($("#projectontology").val() == undefined) {
                    window.app.view.message("Ontology", "You must provide a ontology name!", "error");
                    error = true;
                }
                if ($("#projectdiscipline").val() == undefined) {
                    window.app.view.message("Discipline", "You must provide a discipline name!", "error");
                    error = true;
                }
                return !error;
            }
            //show save button on last step
            if (index == $("#login-form-add-project").find("fieldset").length) {
                $("#saveProjectButton").show();
            }
        }, back: function (index) {
            //hide save button if not on last step
            if (index != $("#login-form-add-project").find("fieldset").length) {
                $("#saveProjectButton").hide();
            }
        }});
        $("fieldset").find("a.button-next").css("float", "right");
        $("fieldset").find("a.button-back").css("float", "left");
        $("fieldset").find("a").removeClass("button-next");
        $("fieldset").find("a").removeClass("button-back");
        $("fieldset").find("a").addClass("btn btn-default btn-primary");
    },
    createProjectInfo: function (ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl) {
        var self = this;
        $("#login-form-add-project").submit(function () {
            self.createProject();
            return false;
        });
        $("#login-form-add-project").find("input").keydown(function (e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-project").submit();
                return false;
            }
        });

        $("#projectdiscipline").empty();
        $("#choiceListDiscipline").empty();
        $("#choiceListDiscipline").append('<select class="input-xlarge focused" id="projectdiscipline" />');
        var choice = _.template(disciplinesChoicesRadioTpl, {id: -1, name: "*** Undefined ***"});
        $("#projectdiscipline").append(choice);
        window.app.models.disciplines.fetch({
            success: function (collection, response) {

                collection.each(function (discipline) {
                    var choice = _.template(ontologiesChoicesRadioTpl, {id: discipline.id, name: discipline.get("name")});
                    $("#projectdiscipline").append(choice);
                });
                $("#projectdiscipline").find("option:selected").removeAttr("selected");
            }
        });
        $("#projectontology").empty();
        window.app.models.ontologiesLigth.fetch({
            success: function (collection, response) {
                $("#choiceListOntology").empty();
                $("#choiceListOntology").append('<select class="input-xlarge focused" id="projectontology" />');

                collection.each(function (ontology) {
                    var choice = _.template(ontologiesChoicesRadioTpl, {id: ontology.id, name: ontology.get("name")});
                    $("#projectontology").append(choice);
                });
                $("#projectontology").find("option:selected").removeAttr("selected");
            }
        });

        $("#createOntologyWithProjectName").click(function (evt) {

            //create ontology
            var projectName = $("#project-name").val().toUpperCase().trim();
            if (projectName != "") {
                var ontology = new OntologyModel({name: projectName}).save({name: projectName}, {
                            success: function (model, response) {
                                window.app.view.message("Ontology", response.message, "success");
                                var id = response.ontology.id;
                                window.app.models.ontologies.add(model);

                                var choice = _.template(ontologiesChoicesRadioTpl, {id: id, name: model.get("name")});
                                $("#projectontology").prepend(choice);
                                $("#projectontology").val(id);


                            },
                            error: function (model, response) {
                                var json = $.parseJSON(response.responseText);
                                window.app.view.message("Ontology", json.errors, "error");
                            }
                        }
                );
            } else {
                window.app.view.message("Project", "You must first write a valid project name!", "error");
            }


        })
    },

    createUserList: function () {
        var self = this;
        var allUser = null;

        var loadUser = function () {
            var allUserArray = [];

            allUser.each(function (user) {
                allUserArray.push({id: user.id, label: user.prettyName()});
            });

            self.userMaggicSuggest = $('#projectuser').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: [window.app.status.user.id],
                width: 590,
                maxSelection: null
            });

            self.adminMaggicSuggest = $('#projectadmin').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: [window.app.status.user.id],
                width: 590,
                maxSelection: null
            });

            self.defaultLayersMaggicSuggest = $('#projectadddefaultlayers').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                //value: defaultLayersArray,
                width: 590,
                maxSelection:null
            });
        }

        new UserCollection({}).fetch({
            success: function (allUserCollection, response) {
                allUser = allUserCollection;
                loadUser();
            }});
    },
    createRetrievalProject: function () {
        var self = this;
        $("input#retrievalProjectSome,input#retrievalProjectAll,input#retrievalProjectNone").change(function () {
            if ($("input#retrievalProjectSome").is(':checked')) {
                if (!self.projectMultiSelectAlreadyLoad) {
                    self.createRetrievalProjectSelect();
                    self.projectMultiSelectAlreadyLoad = true
                } else {
                    console.log("Show");
                    $("div#retrievalGroup").find(".ui-multiselect").show();
                }
            } else {
                console.log("Hide");
                $("div#retrievalGroup").find(".ui-multiselect").hide();
            }
        });

        $("input#project-name").change(function () {
            console.log("change");
            if (self.projectMultiSelectAlreadyLoad) {
                self.createRetrievalProjectSelect();
            }
        });

        $("#projectontology").change(function () {
            console.log("change");
            if (self.projectMultiSelectAlreadyLoad) {
                self.createRetrievalProjectSelect();
            }
        });
    },
    createRetrievalProjectSelect: function () {
        /* Create Users List */
        var retrievalProjectEl = $("#retrievalproject");
        retrievalProjectEl.empty();

        var projectName = $("input#project-name").val();
        var idOntology = $("select#projectontology").val();


        window.app.models.projects.each(function (project) {
            if (project.get('ontology') == idOntology) {
                $("#retrievalproject").append('<option value="' + project.id + '">' + project.get('name') + '</option>');
            }
        });
        retrievalProjectEl.append('<option value="-1" selected="selected">' + projectName + '</option>');

        retrievalProjectEl.multiselectNext('destroy');
        retrievalProjectEl.multiselectNext({
            selected: function (event, ui) {
                //alert($(ui.option).val() + " has been selected");
            }});

        /*var multiSelectEl = $("div.ui-multiselect");
        multiSelectEl.find("ul.available").css("height", "150px")
        multiSelectEl.find("ul.selected").css("height", "150px")
        multiSelectEl.find("input.search").css("width", "75px")
        multiSelectEl.find("div.actions").css("background-color", "#DDDDDD"); */
    },
    refresh: function () {
    },
    open: function () {
        var self = this;
        self.clearAddProjectPanel();
        $("#addproject").modal('show');
    },
    clearAddProjectPanel: function () {
        var self = this;
        $("#errormessage").empty();
        $("#projecterrorlabel").hide();
        $("#project-name").val("");

        $(self.addProjectCheckedOntologiesRadioElem).attr("checked", false);
        $(self.addProjectCheckedDisciplinesRadioElem).attr("checked", false);
        $(self.addProjectCheckedUsersCheckboxElem).attr("checked", false);
    },
    changeProgressBarStatus: function (progress) {
        console.log("changeProgressBarStatus:" + progress);
        var progressBar = $("#progressBarCreateProject").find(".bar");
        progressBar.css("width", progress + "%");
    },

    createProject: function () {

        var self = this;

        $("#errormessage").empty();
        $("#projecterrorlabel").hide();

        var name = $("#project-name").val().toUpperCase();
        var discipline = $("#projectdiscipline").val();
        if (discipline == -1) {
            discipline = null;
        }
        var ontology = $("#projectontology").val();
        var users = self.userMaggicSuggest.getValue();
        var admins = self.adminMaggicSuggest.getValue();
        var blindMode = $("input#blindMode").is(':checked');
        var isReadOnly = $("input#isReadOnly").is(':checked');
        var hideUsersLayers = $("input#hideUsersLayers").is(':checked');
        var hideAdminsLayers = $("input#hideAdminsLayers").is(':checked');

        console.log("blindMode=" + blindMode);
        console.log("isReadOnly=" + isReadOnly);
        console.log("hideUsersLayers=" + hideUsersLayers);
        console.log("hideAdminsLayers=" + hideAdminsLayers);

        var retrievalDisable = $("input#retrievalProjectNone").is(':checked');
        var retrievalProjectAll = $("input#retrievalProjectAll").is(':checked');
        var retrievalProjectSome = $("input#retrievalProjectSome").is(':checked');
        var projectRetrieval = [];
        if (retrievalProjectSome) {
            projectRetrieval = $("#retrievalproject").multiselectNext('selectedValues');
        }

        console.log("initProgressBar");
        var divToFill = $("#login-form-add-project");
        divToFill.hide();
//        $("#login-form-add-project-titles").empty();
        $("#addproject").find(".modal-footer").hide();



        new TaskModel({project: null}).save({}, {
            success: function (task, response) {
                $("#progressBarAddProjectContainer").append('<br><br><div id="task-' + response.task.id + '"></div><br><br>');
                console.log(response.task);
                var timer = window.app.view.printTaskEvolution(response.task, $("#progressBarAddProjectContainer").find("#task-" + response.task.id), 1000);

                //create project
                new ProjectModel({task:response.task.id,users: users, admins: admins,name: name, ontology: ontology, discipline: discipline, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval}).save({name: name, ontology: ontology, discipline: discipline, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval, blindMode: blindMode, isReadOnly: isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers}, {
                            success: function (model, response) {
                                console.log("1. Project added!");
                                clearInterval(timer);
                                window.app.view.message("Project", response.message, "success");
                                var id = response.project.id;
                                self.projectsPanel.refresh();
                                $("#addproject").modal("hide");
                                /*$("#addproject").remove();*/
                            },
                            error: function (model, response) {
                                var json = $.parseJSON(response.responseText);
                                clearInterval(timer);
                                window.app.view.message("Project", json.errors, "error");
                                divToFill.show();
                                $("#progressBarAddProjectContainer").empty();
                                $("#addproject").find(".modal-footer").show();
                                $('#login-form-add-project').stepy('step', 1);
                            }
                        });
            }
        });
    }
});