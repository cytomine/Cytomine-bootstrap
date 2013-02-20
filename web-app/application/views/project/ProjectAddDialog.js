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
            "text!application/templates/project/DisciplinesChoicesRadio.tpl.html",
            "text!application/templates/project/UsersChoices.tpl.html"
        ],
            function (projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl, usersChoicesTpl) {
                self.doLayout(projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl, usersChoicesTpl);
            });
        return this;
    },
    doLayout: function (projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl, usersChoicesTpl) {
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
            backdrop: false
        });
        $("#saveProjectButton").click(function () {
            $("#login-form-add-project").submit();
            return false;
        });
        $("#closeAddProjectDialog").click(function () {
            $("#addproject").modal('hide');
            $("#addproject").remove();
            return false;
        });
        self.open();
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
        $("fieldset").find("a").addClass("btn btn-primary");
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
            if(projectName!="") {
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
            }  else {
                window.app.view.message("Project", "You must first write a valid project name!", "error");
            }



        })
    },
    createUserList: function () {
        /* Create Users List */
        $("#projectuser").empty();

        new UserCollection({}).fetch({
            success: function (collection, response) {

                collection.each(function (user) {
                    if (user.id == window.app.status.user.id) {
                        $("#projectuser").append('<option value="' + user.id + '" selected="selected">' + user.prettyName() + '</option>');
                    } else {
                        $("#projectuser").append('<option value="' + user.id + '">' + user.prettyName() + '</option>');
                    }
                });
                $("#projectuser").multiselectNext({
                    deselected: function (event, ui) {
                        //lock current user (cannot be deselected
                        if ($(ui.option).val() == window.app.status.user.id) {
                            $("#projectuser").multiselectNext('select', $(ui.option).text());
                            window.app.view.message("User", "You must be in user list of your project!", "error");
                        }
                    },
                    selected: function (event, ui) {
                        //alert($(ui.option).val() + " has been selected");
                    }});

                $("div.ui-multiselect").find("ul.available").css("height", "150px");
                $("div.ui-multiselect").find("ul.selected").css("height", "150px");
                $("div.ui-multiselect").find("input.search").css("width", "75px");

                $("div.ui-multiselect").find("div.actions").css("background-color", "#DDDDDD");

                console.log("window.app.status.user.model.prettyName()=" + window.app.status.user.model.prettyName());
                $("#projectuser").multiselectNext('select', window.app.status.user.model.prettyName());
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
        $("#retrievalproject").empty();

        var projectName = $("input#project-name").val();
        var idOntology = $("select#projectontology").val();


        window.app.models.projects.each(function (project) {
            if (project.get('ontology') == idOntology) {
                $("#retrievalproject").append('<option value="' + project.id + '">' + project.get('name') + '</option>');
            }
        });
        $("#retrievalproject").append('<option value="-1" selected="selected">' + projectName + '</option>');

        $("#retrievalproject").multiselectNext('destroy');
        $("#retrievalproject").multiselectNext({
            selected: function (event, ui) {
                //alert($(ui.option).val() + " has been selected");
            }});

        $("div.ui-multiselect").find("ul.available").css("height", "150px");
        $("div.ui-multiselect").find("ul.selected").css("height", "150px");
        $("div.ui-multiselect").find("input.search").css("width", "75px");

        $("div.ui-multiselect").find("div.actions").css("background-color", "#DDDDDD");
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
    initProgressBar : function() {
        console.log("initProgressBar");
        var divToFill = $("#login-form-add-project");
        divToFill.empty();
        $("#login-form-add-project-titles").empty();
        divToFill.append('' +
            '<br><br><div id="progressBarCreateProject" class="progress progress-striped active">' +
            '   <div class="bar" style="width:0%;"></div>' +
            '</div><br><br>');
        $("#addproject").find(".modal-footer").empty();
    },
    changeProgressBarStatus: function(progress) {
        console.log("changeProgressBarStatus:"+progress);
        var progressBar = $("#progressBarCreateProject").find(".bar");
        progressBar.css("width",progress+"%");
    },

    createProject: function () {

        var self = this;

        $("#errormessage").empty();
        $("#projecterrorlabel").hide();

        var name = $("#project-name").val().toUpperCase();
        var discipline = $("#projectdiscipline").attr('value');
        if (discipline == -1) {
            discipline = null;
        }
        var ontology = $("#projectontology").attr('value');
        var users = $("#projectuser").multiselectNext('selectedValues');


        var retrievalDisable = $("input#retrievalProjectNone").is(':checked');
        var retrievalProjectAll = $("input#retrievalProjectAll").is(':checked');
        var retrievalProjectSome = $("input#retrievalProjectSome").is(':checked');
        var projectRetrieval = [];
        if (retrievalProjectSome) {
            projectRetrieval = $("#retrievalproject").multiselectNext('selectedValues');
        }

        self.initProgressBar();
        var totalOperation = users.length + 1; //N users +1 for project creation
        self.changeProgressBarStatus((1/totalOperation)*100);

        //create project
        new ProjectModel({name: name, ontology: ontology, discipline: discipline, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval}).save({name: name, ontology: ontology, discipline: discipline, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval}, {
                success: function (model, response) {
                    console.log("1. Project added!");
                    window.app.view.message("Project", response.message, "success");
                    var id = response.project.id;

                    //create user-project "link"
                    var total = users.length;
                    var counter = 0;
                    if (total == 0) {
                        self.addDeleteUserProjectCallback(0, 0);
                    }
                    _.each(users, function (user) {
                        console.log("Add user "+ user);
                        new ProjectUserModel({project: id, user: user}).save({}, {
                            success: function (model, response) {
                                console.log("2. User added");
                                self.addUserProjectCallback(total, ++counter);
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                            }, error: function (model, response) {
                                self.addUserProjectCallback(total, ++counter);
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                                var json = $.parseJSON(response.responseText);
                                window.app.view.message("User", json.errors, "error");
                            }});

                    });

                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Project", json.errors, "error");
                    $('#login-form-add-project').stepy('step', 1);
                    //$("#projecterrorlabel").show();
                }
            }
        );
    },
    addUserProjectCallback: function (total, counter) {
        console.log("3. addUserProjectCallback=" + counter +"/"+total);
        if (counter < total) {
            return;
        }
        var self = this;
        console.log("4. refresh");
        self.projectsPanel.refresh();
        $("#addproject").modal("hide");
        $("#addproject").remove();
    }
});