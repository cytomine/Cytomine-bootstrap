var ProjectDescriptionDialog = Backbone.View.extend({
    descriptionProjectDialog: null,
    initialize: function () {
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectDescription.tpl.html"
        ],
            function (projectDescriptionDialogTpl) {
                self.doLayout(projectDescriptionDialogTpl);
            });
        return this;
    },
    doLayout: function (projectDescriptionDialogTpl) {
        var self = this;

        var dialog = _.template(projectDescriptionDialogTpl, {id: self.model.id, name: self.model.get("name")});
        $("#projectDescription" + self.model.id).replaceWith("");
        $(self.el).append(dialog);

        self.printDescriptionProject();

        //Build dialog
        console.log("Open element:" + $("#projectDescription" + self.model.id).length);
        self.descriptionProjectDialog = $("#projectDescription" + self.model.id).modal({
            keyboard: true,
            backdrop: false
        });

        //Add event button on the modal window.
        $("#closeDescriptionProjectDialog").click(function (event) {
            event.preventDefault();
            $("#projectDescription" + self.model.id).modal('hide');
            $("#projectDescription" + self.model.id).remove();
            return false;
        });

        $("#saveDescriptionProjectDialog").click(function (event) {
            event.preventDefault();
            self.saveDescriptionProject();

            $("#projectDescription" + self.model.id).modal('hide');
            $("#projectDescription" + self.model.id).remove();
            return false;
        });

        self.open();
        return this;
    },
    printDescriptionProject : function () {
        var self = this;
        var project = window.app.status.currentProjectModel;
        console.log(project);
        console.log("Description Project: " + project.description);
        $("#textAreaDescriptionProject" + self.model.id).val(project.get("description"));
    },
    saveDescriptionProject :function () {
        var self = this;
        var project = window.app.status.currentProjectModel;
        project.save({ description : $("#textAreaDescriptionProject" + self.model.id).val()}, {
            success: function (model, response) {
                window.app.view.message("Projet Description", response.message, "success");
                return false;
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Project Description", json.errors, "error");
            }
        });
    },
    open: function () {
        var self = this;
        $("#projectDescription" + self.model.id).modal('show');
    }
});