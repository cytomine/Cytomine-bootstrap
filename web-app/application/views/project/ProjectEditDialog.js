var EditProjectDialog = Backbone.View.extend({
    projectsPanel: null,
    editProjectDialog: null,
    projectMultiSelectAlreadyLoad: false,
    initialize: function (options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectEditDialog.tpl.html",
            "text!application/templates/project/UsersChoices.tpl.html"
        ],
            function (projectEditDialogTpl, usersChoicesTpl) {
                self.doLayout(projectEditDialogTpl, usersChoicesTpl);
            });
        return this;
    },
    doLayout: function (projectEditDialogTpl, usersChoicesTpl) {
        var self = this;
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        var dialog = _.template(projectEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#editProjectButton").click(function () {
            $("#login-form-edit-project").submit();
            return false;
        });
        $("#closeEditProjectDialog").click(function () {
            $("#editproject").modal('hide');
            $("#editproject").remove();
            return false;
        });

        self.initStepy();
        self.createProjectInfo();
        self.createUserList();
        $("#project-edit-name").val(self.model.get('name'));
        self.createRetrievalProject();
//        self.createUserList(usersChoicesTpl);

        //Build dialog
        self.editProjectDialog = $("#editproject").modal({
            keyboard: true,
            backdrop: false
        });
        self.open();
        self.fillForm();
        return this;

    },
    initStepy: function () {
        $('#login-form-edit-project').stepy({next: function (index) {
            //check validate name
            if (index == 2) {
                if ($("#project-edit-name").val().toUpperCase().trim() == "") {
                    window.app.view.message("User", "You must provide a valide project name!", "error");
                    return false;
                }
            }
            //show save button on last step
            if (index == $("#login-form-edit-project").find("fieldset").length) {
                $("#editProjectButton").show();
            }
        }, back: function (index) {
            //hide save button if not on last step
            if (index != $("#login-form-edit-project").find("fieldset").length) {
                $("#editProjectButton").hide();
            }
        }});
        $('#login-form-edit-project').find("fieldset").find("a.button-next").css("float", "right");
        $('#login-form-edit-project').find("fieldset").find("a.button-back").css("float", "left");
        $('#login-form-edit-project').find("fieldset").find("a").removeClass("button-next");
        $('#login-form-edit-project').find("fieldset").find("a").removeClass("button-back");
        $('#login-form-edit-project').find("fieldset").find("a").addClass("btn btn-primary");
    },
    createProjectInfo: function () {
        var self = this;
        $("#login-form-edit-project").submit(function () {
            self.editProject();
            return false;
        });
        $("#login-form-edit-project").find("input").keydown(function (e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-project").submit();
                return false;
            }
        });


        $("input#blindMode").attr('checked', self.model.get('blindMode'));
        $("input#privateLayer").attr('checked', self.model.get('privateLayer'));

    },
    createUserList: function () {
        var self = this;
        /* Create Users List */
        $("#projectedituser").empty();

        var allUser = null;
        var projectUser = null;
        var loadUser = function () {
            if (allUser == null || projectUser == null) {
                return
            }

            allUser.each(function (user) {
                if (projectUser.get(user.id) != undefined) {
                    $("#projectedituser").append('<option value="' + user.id + '" selected="selected">' + user.prettyName() + '</option>');
                } else {
                    $("#projectedituser").append('<option value="' + user.id + '">' + user.prettyName() + '</option>');
                }
            });

            $("#projectedituser").multiselectNext({
                deselected: function (event, ui) {
                    //lock current user (cannot be deselected
                    if ($(ui.option).val() == window.app.status.user.id) {
                        $("#projectedituser").multiselectNext('select', $(ui.option).text());
                        window.app.view.message("User", "You must be in user list of your project!", "error");
                    } else {
                    }
                },
                selected: function (event, ui) {
                }});

            $("div.ui-multiselect").find("ul.available").css("height", "150px");
            $("div.ui-multiselect").find("ul.selected").css("height", "150px");
            $("div.ui-multiselect").find("input.search").css("width", "75px");

            $("div.ui-multiselect").find("div.actions").css("background-color", "#DDDDDD");

            console.log("window.app.status.user.model.prettyName()=" + window.app.status.user.model.prettyName());
            $("#projectedituser").multiselectNext('select', window.app.status.user.model.prettyName());
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
    },
    createRetrievalProject: function () {
        var self = this;
        $('#login-form-edit-project').find("input#retrievalProjectSome,input#retrievalProjectAll,input#retrievalProjectNone").change(function () {
            console.log("change1");
            if ($('#login-form-edit-project').find("input#retrievalProjectSome").is(':checked')) {
                console.log("createRetrievalProject=" + self.projectMultiSelectAlreadyLoad);
                if (!self.projectMultiSelectAlreadyLoad) {
                    self.createRetrievalProjectSelect();
                    self.projectMultiSelectAlreadyLoad = true
                } else {
                    console.log("Show");
                    $('#login-form-edit-project').find("div#retrievalGroup").find(".ui-multiselect").show();
                }
            } else {
                console.log("Hide");
                $('#login-form-edit-project').find("div#retrievalGroup").find(".ui-multiselect").hide();
            }
        });

        $('#login-form-edit-project').find("input#project-name").change(function () {
            console.log("change");
            if (self.projectMultiSelectAlreadyLoad) {
                self.createRetrievalProjectSelect();
            }
        });

        if (self.model.get('retrievalDisable')) {
            $('#login-form-edit-project').find("input#retrievalProjectNone").attr("checked", "checked");
            $('#login-form-edit-project').find("input#retrievalProjectNone").change();
        } else if (self.model.get('retrievalAllOntology')) {
            $('#login-form-edit-project').find("input#retrievalProjectAll").attr("checked", "checked");
            $('#login-form-edit-project').find("input#retrievalProjectAll").change();
        } else {
            $('#login-form-edit-project').find("input#retrievalProjectSome").attr("checked", "checked");
            $('#login-form-edit-project').find("input#retrievalProjectSome").change();
            //retrievalProjectAll

        }
    },
    createRetrievalProjectSelect: function () {
        var self = this;
        /* Create Users List */
        $('#login-form-edit-project').find("#retrievalproject").empty();

        window.app.models.projects.each(function (project) {
            if (project.get('ontology') == self.model.get('ontology') && project.id != self.model.id) {
                if (_.indexOf(self.model.get('retrievalProjects'), project.id) == -1) {
                    $('#login-form-edit-project').find("#retrievalproject").append('<option value="' + project.id + '">' + project.get('name') + '</option>');
                }
                else {
                    $('#login-form-edit-project').find("#retrievalproject").append('<option value="' + project.id + '" selected="selected">' + project.get('name') + '</option>');
                }
            }
        });
        $('#login-form-edit-project').find("#retrievalproject").append('<option value="' + self.model.id + '" selected="selected">' + $('#login-form-edit-project').find("#project-edit-name").val() + '</option>');

        $('#login-form-edit-project').find("#retrievalproject").multiselectNext('destroy');
        $('#login-form-edit-project').find("#retrievalproject").multiselectNext({
            selected: function (event, ui) {
                //alert($(ui.option).val() + " has been selected");
            }});

        $("div.ui-multiselect").find("ul.available").css("height", "150px");
        $("div.ui-multiselect").find("ul.selected").css("height", "150px");
        $("div.ui-multiselect").find("input.search").css("width", "75px");

        $("div.ui-multiselect").find("div.actions").css("background-color", "#DDDDDD");
    },
    fillForm: function () {

        var self = this;
        //fill project Name

    },
    refresh: function () {
    },
    open: function () {
        var self = this;
        self.clearEditProjectPanel();
        $("#editproject").modal('show');
    },
    clearEditProjectPanel: function () {
        var self = this;

        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();
//        $("#project-edit-name").val("");

        //$(self.editProjectCheckedUsersCheckboxElem).attr("checked", false);
    },
    /**
     * Function which returns the result of the subtraction method applied to
     * sets (mathematical concept).
     *
     * @param a Array one
     * @param b Array two
     * @return An array containing the result
     */
    diffArray: function (a, b) {
        var seen = [], diff = [];
        for (var i = 0; i < b.length; i++) {
            seen[b[i]] = true;
        }
        for (var i = 0; i < a.length; i++) {
            if (!seen[a[i]]) {
                diff.push(a[i]);
            }
        }
        return diff;
    },
    editProject: function () {

        var self = this;

        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();

        var name = $("#project-edit-name").val().toUpperCase();
        var users = $('#login-form-edit-project').find("#projectedituser").multiselectNext('selectedValues');
        console.log("users=" + users);
        console.log("multiselect=" + $('#login-form-edit-project').length);
        var retrievalDisable = $('#login-form-edit-project').find("input#retrievalProjectNone").is(':checked');
        var retrievalProjectAll = $('#login-form-edit-project').find("input#retrievalProjectAll").is(':checked');
        var retrievalProjectSome = $('#login-form-edit-project').find("input#retrievalProjectSome").is(':checked');
        var projectRetrieval = [];
        if (retrievalProjectSome) {
            projectRetrieval = $('#login-form-edit-project').find("#retrievalproject").multiselectNext('selectedValues');
        }
        var blindMode = $("input#blindMode").attr('checked')=="checked";
        var privateLayer = $("input#privateLayer").attr('checked')=="checked";

        console.log("blindMode="+blindMode);
        console.log("privateLayer="+privateLayer);

        var projectOldUsers = []; //[a,b,c]
        var projectNewUsers = null;  //[a,b,x]
        var projectAddUser = null;
        var projectDeleteUser = null;
        var jsonuser = [];

        window.app.models.projectUser.each(function (user) {
            jsonuser.push(user.id);
        });

        _.each(jsonuser,
            function (user) {
                projectOldUsers.push(user)
            });
        projectOldUsers.sort();
        projectNewUsers = users;
        projectNewUsers.sort();
        //var diff = self.diffArray(projectOldUsers,projectNewUsers);
        projectAddUser = self.diffArray(projectNewUsers, projectOldUsers); //[x] must be added
        projectDeleteUser = self.diffArray(projectOldUsers, projectNewUsers); //[c] must be deleted
        console.log("projectOldUsers=" + projectOldUsers);
        console.log("projectNewUsers=" + projectNewUsers);
        console.log("projectAddUser=" + projectAddUser);
        console.log("projectDeleteUser=" + projectDeleteUser);



        self.initProgressBar();
        var totalOperation = projectAddUser.length + projectDeleteUser.length + 1; //N users +1 for project creation
        self.changeProgressBarStatus((1/totalOperation)*100);


        //edit project
        var project = self.model;
        project.set({name: name, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,blindMode:blindMode,privateLayer:privateLayer});

        project.save({name: name, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,blindMode:blindMode,privateLayer:privateLayer}, {
                success: function (model, response) {


                    window.app.view.message("Project", response.message, "success");

                    var id = response.project.id;

                    /*_.each(projectOldUsers,function(user){

                     _.each(projectNewUsers,function(user){

                     _.each(projectAddUser,function(user){

                     _.each(projectDeleteUser,function(user){*/
                    var total = projectAddUser.length + projectDeleteUser.length;
                    var counter = 0;
                    if (total == 0) {
                        self.addDeleteUserProjectCallback(0, 0);
                    }
                    _.each(projectAddUser, function (user) {

                        new ProjectUserModel({project: id, user: user}).save({}, {
                            success: function (model, response) {
                                self.addDeleteUserProjectCallback(total, ++counter);
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                            }, error: function (model, response) {
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                                var json = $.parseJSON(response.responseText);
                                window.app.view.message("User", json.errors[0], "error");

                            }});
                    });
                    _.each(projectDeleteUser, function (user) {
                        //use fake ID since backbone > 0.5 : we should destroy only object saved or fetched
                        new ProjectUserModel({id: 1, project: id, user: user}).destroy({
                            success: function (model, response) {
                                self.addDeleteUserProjectCallback(total, ++counter);
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                            }, error: function (model, response) {
                                self.changeProgressBarStatus(((counter+1)/totalOperation)*100);
                                var json = $.parseJSON(response.responseText);
                                window.app.view.message("User", json.errors[0], "error");
                            }});
                    });

                },
                error: function (model, response) {
                    console.log(response);
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Project", json.errors[0], "error");

                    //$("#projectediterrorlabel").show();


                }
            }
        );
    },
    initProgressBar : function() {
        console.log("initProgressBar");
        var divToFill = $("#login-form-edit-project");
        divToFill.empty();
        $("#login-form-edit-project-titles").empty();
        divToFill.append('' +
            '<br><br><div id="progressBarEditProject" class="progress progress-striped active">' +
            '   <div class="bar" style="width:0%;"></div>' +
            '</div><br><br>');
        $("#editproject").find(".modal-footer").empty();
    },
    changeProgressBarStatus: function(progress) {
        console.log("changeProgressBarStatus:"+progress);
        var progressBar = $("#progressBarEditProject").find(".bar");
        progressBar.css("width",progress+"%");
    },
    addDeleteUserProjectCallback: function (total, counter) {
        if (counter < total) {
            return;
        }
        var self = this;
        self.projectPanel.refresh();
        $("#editproject").modal('hide');
        $("#editproject").remove();
    }
});