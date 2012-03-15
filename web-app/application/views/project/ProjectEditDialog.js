var EditProjectDialog = Backbone.View.extend({
    projectsPanel : null,
    editProjectDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectEditDialog.tpl.html",
            "text!application/templates/project/UsersChoices.tpl.html"
        ],
                function(projectEditDialogTpl, usersChoicesTpl) {
                    self.doLayout(projectEditDialogTpl, usersChoicesTpl);
                });
        return this;
    },
    doLayout : function(projectEditDialogTpl, usersChoicesTpl) {

        var self = this;
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        var dialog = _.template(projectEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#login-form-edit-project").submit(function () {
            self.editProject();
            return false;
        });
        $("#login-form-edit-project").find("input").keydown(function(e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-project").submit();
                return false;
            }
        });

        $("#editProjectButton").click(function(){$("#login-form-edit-project").submit();return false;});
        $("#closeEditProjectDialog").click(function(){$("#editproject").modal('hide');$("#editproject").remove();return false;});

        self.createUserList(usersChoicesTpl);

        //Build dialog
        self.editProjectDialog = $("#editproject").modal({
            keyboard : true,
            backdrop : false
        });


        self.open();
        self.fillForm();
        return this;

    },
    /* REDONDANT WITH ProjectAddDialog.createUserList !!! */
    createUserList : function (usersChoicesTpl) {
        /* Create Users List */
        var listIdentifierName =  "projectedituser";
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
    fillForm : function() {

        var self = this;
        $("#project-edit-name").val(self.model.get('name'));
        var jsonuser = self.model.get('users');
        _.each(jsonuser,
                function(user){

                    $('#users'+user).attr('checked', true);
                    //TODO: if user.id == currentuser, lock the checkbox (a user cannot delete himself from a project)
                    if(window.app.status.user.id==user.id) {

                    }
                });

    },
    refresh : function() {
    },
    open: function() {
        var self = this;
        self.clearEditProjectPanel();
        $("#editproject").modal('show');
    },
    clearEditProjectPanel : function() {
        var self = this;

        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();
        $("#project-edit-name").val("");

        $(self.editProjectCheckedUsersCheckboxElem).attr("checked", false);
    },
    /**
     * Function which returns the result of the subtraction method applied to
     * sets (mathematical concept).
     *
     * @param a Array one
     * @param b Array two
     * @return An array containing the result
     */
    diffArray: function(a, b) {
        var seen = [], diff = [];
        for ( var i = 0; i < b.length; i++)
            seen[b[i]] = true;
        for ( var i = 0; i < a.length; i++)
            if (!seen[a[i]])
                diff.push(a[i]);
        return diff;
    },


    editProject : function() {

        var self = this;

        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();

        var name = $("#project-edit-name").val().toUpperCase();;
        var users = new Array();

        $('input[type=checkbox][name=usercheckbox]:checked').each(function(i, item) {
            users.push($(item).attr("value"))
        });

        //edit project
        var project = self.model;
        project.set({name:name});

        project.save({name : name}, {
                    success: function (model, response) {


                        window.app.view.message("Project", response.message, "success");

                        var id = response.project.id;

                        //create user-project "link"


                        var projectOldUsers = new Array(); //[a,b,c]
                        var projectNewUsers = null;  //[a,b,x]
                        var projectAddUser = null;
                        var projectDeleteUser = null;

                        var jsonuser = self.model.get('users');

                        _.each(jsonuser,
                                function(user){
                                    projectOldUsers.push(user)
                                });
                        projectOldUsers.sort();
                        projectNewUsers = users;
                        projectNewUsers.sort();
                        //var diff = self.diffArray(projectOldUsers,projectNewUsers);
                        projectAddUser = self.diffArray(projectNewUsers,projectOldUsers); //[x] must be added
                        projectDeleteUser =  self.diffArray(projectOldUsers,projectNewUsers); //[c] must be deleted


                        /*_.each(projectOldUsers,function(user){

                         _.each(projectNewUsers,function(user){

                         _.each(projectAddUser,function(user){

                         _.each(projectDeleteUser,function(user){*/
                        var total = projectAddUser.length+projectDeleteUser.length;
                        var counter = 0;
                        if(total==0) self.addDeleteUserProjectCallback(0,0);
                        _.each(projectAddUser,function(user){

                            new ProjectUserModel({project: id,user:user}).save({}, {
                                success: function (model, response) {
                                    self.addDeleteUserProjectCallback(total,++counter);
                                },error: function (model, response) {

                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("User", json.errors[0], "error");
                                }});
                        });
                        _.each(projectDeleteUser,function(user){
                            //use fake ID since backbone > 0.5 : we should destroy only object saved or fetched
                            new ProjectUserModel({id : 1, project: id,user:user}).destroy({
                                success: function (model, response) {
                                    self.addDeleteUserProjectCallback(total,++counter);
                                },error: function (model, response) {

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
    addDeleteUserProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.projectPanel.refresh();
        $("#editproject").modal('hide');
        $("#editproject").remove();
    }
});