var ontologyUsersDialog = Backbone.View.extend({
    usersOntologyDialog:null,
    initialize:function (options) {
        _.bindAll(this, 'render');
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyUsers.tpl.html"
        ],
            function (ontologyUsersDialogTpl) {
                self.doLayout(ontologyUsersDialogTpl);
            });
        return this;
    },
    doLayout:function (ontologyUsersDialogTpl) {
        var self = this;

        var dialog = _.template(ontologyUsersDialogTpl, {id:self.model.id, name:self.model.get("name")});
        $("#ontologyUsers" + self.model.id).replaceWith("");
        $(self.el).append(dialog);

        self.printCreator();
        self.printUsers();

        //Build dialog
        console.log("Open element:" + $("#ontologyUsers" + self.model.id).length);
        self.usersOntologyDialog = $("#ontologyUsers" + self.model.id).modal({
            keyboard:true,
            backdrop:false
        });
        $("#closeUserOntologyDialog" + self.model.id).click(function () {
            console.log("close!");
            $("#ontologyUsers" + self.model.id).modal('hide');
            $("#ontologyUsers" + self.model.id).remove();
        });
        self.open();
        return this;
    },
    printCreator:function () {
        var self = this;
        new UserCollection({ontology:self.model.id, creator:true}).fetch({
            success:function (creator, response) {
                $("#ontologyCreator").empty();
                creator.each(function (user) {
                    $("#ontologyCreator").append(user.prettyName());
                });
            }});
    },
    printUsers:function () {
        var self = this;
        new UserCollection({ontology:self.model.id}).fetch({
            success:function (users, response) {
                $("#ontologyUsers").empty();
                users.each(function (user) {
                    $("#ontologyUsers").append(user.prettyName() + " | ");
                });
            }});
    },
    open:function () {
        var self = this;
        $("#ontologyUsers" + self.model.id).modal('show');
    }
});