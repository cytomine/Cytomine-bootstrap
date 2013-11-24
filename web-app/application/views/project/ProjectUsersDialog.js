var ProjectUsersDialog = Backbone.View.extend({
    usersProjectDialog: null,
    initialize: function (options) {
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectUsers.tpl.html"
        ],
            function (projectUsersDialogTpl) {
                self.doLayout(projectUsersDialogTpl);
            });
        return this;
    },
    doLayout: function (projectUsersDialogTpl) {
        var self = this;

        var dialog = _.template(projectUsersDialogTpl, {id: self.model.id, name: self.model.get("name")});
        $("#projectUsers" + self.model.id).replaceWith("");
        $(self.el).append(dialog);

        self.printCreator();
        self.printAdmins();
        self.printUsers();

        //Build dialog

        self.usersProjectDialog = $("#projectUsers" + self.model.id).modal({
            keyboard: true,
            backdrop: true
        });

        self.open();
        return this;
    },
    printCreator: function () {
        var self = this;
        new UserCollection({project: self.model.id, creator: true}).fetch({
            success: function (creator, response) {
                $("#projectCreatorDialog").empty();
                var list = [];
                creator.each(function (user) {
                    list.push(user.prettyName());
                });
                $("#projectCreatorDialog").append(list.join(", "));
            }});
    },
    printAdmins: function () {
        var self = this;
        new UserCollection({project: self.model.id, admin: true}).fetch({
            success: function (admin, response) {
                $("#projectAdminsDialog").empty();
                var list = [];
                admin.each(function (user) {
                    list.push(user.prettyName());
                });
                $("#projectAdminsDialog").append(list.join(", "));
            }});
    },
    printUsers: function () {
        var self = this;
        new UserCollection({project: self.model.id}).fetch({
            success: function (users, response) {
                $("#projectUsersDialog").empty();
                var list = [];
                users.each(function (user) {
                    list.push(user.prettyName());
                });
                $("#projectUsersDialog").append(list.join(", "));
            }});
    },
    open: function () {
        var self = this;
        $("#projectUsers" + self.model.id).modal('show');
    }
});