var EditProjectDialog = Backbone.View.extend({
    projectsPanel: null,
    editProjectDialog: null,
    projectMultiSelectAlreadyLoad: false,
    userMaggicSuggest : null,
    initialize: function (options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
                "text!application/templates/project/ProjectEditDialog.tpl.html"
            ],
            function (projectEditDialogTpl) {
                self.doLayout(projectEditDialogTpl);
            });
        return this;
    },
    doLayout: function (projectEditDialogTpl) {
        var self = this;
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        var dialog = _.template(projectEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#editProjectButton").click(function (event) {
            event.preventDefault();
            $("#login-form-edit-project").submit();
            return false;
        });
        $("#closeEditProjectDialog").click(function (event) {
            event.preventDefault();
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
            backdrop: true
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
        $('#login-form-edit-project').find("fieldset").find("a").addClass("btn btn-default btn-primary");
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
        $("input#hideUsersLayers").attr('checked', self.model.get('hideUsersLayers'));
        $("input#hideAdminsLayers").attr('checked', self.model.get('hideAdminsLayers'));
        $("input#isReadOnly").attr('checked', self.model.get('isReadOnly'));

    },
    createUserList: function () {
        var self = this;
        var allUser = null;
        var projectUser = null;
        var projectAdmin = null;


        var loadUser = function() {
            if(allUser == null || projectUser == null/* || defaultLayers == null*/) {
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

            var projectAdminArray=[]
            projectAdmin.each(function(user) {
                projectAdminArray.push(user.id);
            });

            self.userMaggicSuggest = $('#projectedituser').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: projectUserArray,
                width: 590,
                maxSelection:null
            });

            self.adminMaggicSuggest = $('#projecteditadmin').magicSuggest({
                data: allUserArray,
                displayField: 'label',
                value: projectAdminArray,
                width: 590,
                maxSelection:null
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
        var users = self.userMaggicSuggest.getValue();
        var admins = self.adminMaggicSuggest.getValue();
        var retrievalDisable = $('#login-form-edit-project').find("input#retrievalProjectNone").is(':checked');
        var retrievalProjectAll = $('#login-form-edit-project').find("input#retrievalProjectAll").is(':checked');
        var retrievalProjectSome = $('#login-form-edit-project').find("input#retrievalProjectSome").is(':checked');
        var projectRetrieval = [];
        if (retrievalProjectSome) {
            projectRetrieval = $('#login-form-edit-project').find("#retrievalproject").multiselectNext('selectedValues');
        }
        var blindMode = $("input#blindMode").is(':checked');
        var isReadOnly = $("input#isReadOnly").is(':checked');
        var hideUsersLayers = $("input#hideUsersLayers").is(':checked');
        var hideAdminsLayers = $("input#hideAdminsLayers").is(':checked');

        console.log("blindMode=" + blindMode);
        console.log("isReadOnly=" + isReadOnly);
        console.log("hideUsersLayers=" + hideUsersLayers);
        console.log("hideAdminsLayers=" + hideAdminsLayers);

        var divToFill = $("#login-form-edit-project");
        divToFill.hide();
//        $("#login-form-add-project-titles").empty();
        $("#editproject").find(".modal-footer").hide();


        var project = self.model;
        new TaskModel({project: null}).save({}, {
            success: function (task, response) {
                $("#progressBarEditProjectContainer").append('<br><br><div id="task-' + response.task.id + '"></div><br><br>');
                console.log(response.task);
                var timer = window.app.view.printTaskEvolution(response.task, $("#progressBarEditProjectContainer").find("#task-" + response.task.id), 1000);
                console.log(response);
                console.log(response.task);
                console.log(response.task.id);
                var taskId = response.task.id;
                //create project
                project.task = taskId
                project.set({users: users, admins:admins,name: name, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,blindMode:blindMode,isReadOnly:isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers});
                project.save({users:users, admins:admins, name: name, retrievalDisable: retrievalDisable, retrievalAllOntology: retrievalProjectAll, retrievalProjects: projectRetrieval,blindMode:blindMode,isReadOnly:isReadOnly,hideUsersLayers:hideUsersLayers,hideAdminsLayers:hideAdminsLayers}, {
                    success: function (model, response) {
                        console.log("1. Project edited!");
                        clearInterval(timer);
                        window.app.view.message("Project", response.message, "success");
                        var id = response.project.id;
                        $("#editproject").modal("hide");
                        window.app.controllers.dashboard.destroyView()
                        window.app.controllers.browse.closeAll();
                        window.app.status.currentProject = undefined;
                        window.app.view.clearIntervals();
                        /*$("#editproject").remove();*/
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        clearInterval(timer);
                        window.app.view.message("Project", json.errors, "error");
                        divToFill.show();
                        $("#progressBarEditProjectContainer").empty();
                        $("#editproject").find(".modal-footer").show();
                        $('#login-form-edit-project').stepy('step', 1);
                    }
                });
            }
        });
    }
});