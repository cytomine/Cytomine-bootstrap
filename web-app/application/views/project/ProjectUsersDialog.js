var projectUsersDialog = Backbone.View.extend({
    usersProjectDialog : null,
    initialize: function(options) {
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectUsers.tpl.html"
        ],
                function(projectUsersDialogTpl) {
                    self.doLayout(projectUsersDialogTpl);
                });
        return this;
    },
    doLayout : function(projectUsersDialogTpl) {
        var self = this;

        var dialog = _.template(projectUsersDialogTpl, {id:self.model.id,name:self.model.get("name")});
        $("#projectUsers"+self.model.id).replaceWith("");
        $(self.el).append(dialog);

        self.printCreator();
        self.printAdmins();
        self.printUsers();

        //Build dialog
        console.log("Open element:"+$("#projectUsers"+self.model.id).length);
        self.usersProjectDialog = $("#projectUsers"+self.model.id).modal({
            keyboard : true,
            backdrop : false
        });
        $("#closeUserProjectDialog").click(function()
        {$("#projectUsers"+self.model.id).modal('hide');
            $("#projectUsers"+self.model.id).remove();
            window.location.hash = "#project";
            return false;});
        self.open();
        return this;
    },
    printCreator : function() {
        var self = this;
        new UserCollection({project:self.model.id, creator:true}).fetch({
                success : function (creator, response) {
                    $("#projectCreator").empty();
                    creator.each(function(user) {
                        $("#projectCreator").append(user.prettyName());
                 });
        }});
    },
    printAdmins : function() {
        var self = this;
        new UserCollection({project:self.model.id, admin:true}).fetch({
                success : function (admin, response) {
                    $("#projectAdmins").empty();
                    admin.each(function(user) {
                        $("#projectAdmins").append(user.prettyName() + " | ");
                 });
        }});
    },
    printUsers : function() {
        var self = this;
        new UserCollection({project:self.model.id}).fetch({
                success : function (users, response) {
                    $("#projectUsers").empty();
                    users.each(function(user) {
                        $("#projectUsers").append(user.prettyName() + " | ");
                 });
        }});
    },
    open: function() {
        var self = this;
        $("#projectUsers"+self.model.id).modal('show');
    }
});