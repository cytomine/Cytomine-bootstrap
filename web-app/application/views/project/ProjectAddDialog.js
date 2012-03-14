var AddProjectDialog = Backbone.View.extend({
    projectsPanel : null,
    addProjectDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        this.ontologies = options.ontologies;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddDialog.tpl.html",
            "text!application/templates/project/OntologiesChoicesRadio.tpl.html",
            "text!application/templates/project/DisciplinesChoicesRadio.tpl.html",
            "text!application/templates/project/UsersChoices.tpl.html"
        ],
                function(projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl, usersChoicesTpl) {
                    self.doLayout(projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl,usersChoicesTpl);
                });
        return this;
    },
    doLayout : function(projectAddDialogTpl, ontologiesChoicesRadioTpl, disciplinesChoicesRadioTpl, usersChoicesTpl) {

        var self = this;
        var dialog = _.template(projectAddDialogTpl, {});
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        $(self.el).append(dialog);

        $("#login-form-add-project").submit(function () {self.createProject(); return false;});
        $("#login-form-add-project").find("input").keydown(function(e){
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-project").submit();
                return false;
            }
        });

        $("#projectdiscipline").empty();
        var choice = _.template(disciplinesChoicesRadioTpl, {id:-1,name:"*** Undefined ***"});
        $("#projectdiscipline").append(choice);
        window.app.models.disciplines.each(function(discipline){
            var choice = _.template(disciplinesChoicesRadioTpl, {id:discipline.id,name:discipline.get("name")});
            $("#projectdiscipline").append(choice);
        });
        $("#projectontology").empty();
        self.ontologies.fetch({
            success : function (collection, response) {
                collection.each(function(ontology){
                    var choice = _.template(ontologiesChoicesRadioTpl, {id:ontology.id,name:ontology.get("name")});
                    $("#projectontology").append(choice);
                });
            }
        });



        self.createUserList(usersChoicesTpl);


        //Build dialog

        self.addProjectDialog = $("#addproject").modal({
            keyboard : true,
            backdrop : true
        });

        $("#saveProjectButton").click(function(){$("#login-form-add-project").submit();return false;});
        $("#closeAddProjectDialog").click(function(){$("#addproject").modal('hide');$("#addproject").remove();return false;});

        self.open();

        return this;

    },
    /* REDONDANT WITH ProjectEditDialog.createUserList !!! */
    createUserList : function (usersChoicesTpl) {
        /* Create Users List */
        var listIdentifierName =  "projectuser";
        $("#"+listIdentifierName).empty();
        var numberOfColumns = 4;
        var numberOfUsersByColumn = Math.floor(_.size(window.app.models.users) / numberOfColumns);
        for (var k = 0; k < numberOfColumns; k++) {
            $("#"+listIdentifierName).append(_.template("<div class='span2'><ul id='<%= identifier %><%= column %>'></ul></div>", { identifier : listIdentifierName, column : k}));
        }
        var  i = 0;
        window.app.models.users.each(function(user) {
            var column = Math.floor(i / numberOfUsersByColumn);
            var choice = _.template(usersChoicesTpl, {id:user.id,username:user.prettyName()});
            $("#"+listIdentifierName+column).append(choice);
            i++;
        });
        $("#"+listIdentifierName).find('#users'+window.app.status.user.id).attr('checked','checked');
        $("#"+listIdentifierName).find('#users'+window.app.status.user.id).click(function() {
            $("#"+listIdentifierName).find('#users'+window.app.status.user.id).attr('checked','checked');
        });
        $("#"+listIdentifierName).find('[for="users'+window.app.status.user.id+'"]').css("font-weight","bold");
    },
    refresh : function() {
    },
    open: function() {
        var self = this;
        self.clearAddProjectPanel();
        $("#addproject").modal('show');
    },
    clearAddProjectPanel : function() {
        var self = this;
        $("#errormessage").empty();
        $("#projecterrorlabel").hide();
        $("#project-name").val("");

        $(self.addProjectCheckedOntologiesRadioElem).attr("checked", false);
        $(self.addProjectCheckedDisciplinesRadioElem).attr("checked", false);
        $(self.addProjectCheckedUsersCheckboxElem).attr("checked", false);
    },
    createProject : function() {

        var self = this;

        $("#errormessage").empty();
        $("#projecterrorlabel").hide();

        var name =  $("#project-name").val().toUpperCase();
        var discipline = $("#projectdiscipline").attr('value');
        if(discipline==-1) discipline=null;
        var ontology = $("#projectontology").attr('value');
        var users = new Array();

        $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
            users.push($(item).attr("value"))
        });
        //create project
        new ProjectModel({name : name, ontology : ontology, discipline:discipline }).save({name : name, ontology : ontology,discipline:discipline},{
                    success: function (model, response) {

                        window.app.view.message("Project", response.message, "success");
                        var id = response.project.id;

                        //create user-project "link"
                        var total = users.length;
                        var counter = 0;
                        if(total==0) self.addDeleteUserProjectCallback(0,0);
                        _.each(users,function(user){

                            new ProjectUserModel({project: id,user:user}).save({}, {
                                success: function (model, response) {
                                    self.addUserProjectCallback(total,++counter);
                                },error: function (model, response) {

                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("User", json.errors, "error");
                                }});
                        });

                    },
                    error: function (model, response) {

                        var json = $.parseJSON(response.responseText);

                        window.app.view.message("Project", json.errors[0], "error");
                        //$("#projecterrorlabel").show();


                    }
                }
        );
    },
    addUserProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.projectsPanel.refresh();
        $("#addproject").modal("hide");
        $("#addproject").remove();
    }
});