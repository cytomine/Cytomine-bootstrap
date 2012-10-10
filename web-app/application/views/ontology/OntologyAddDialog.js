var AddOntologyDialog = Backbone.View.extend({
    ontologiesPanel:null,
    addOntologyDialog:null,
    initialize:function (options) {
        this.container = options.container;
        this.ontologiesPanel = options.ontologiesPanel;
        _.bindAll(this, 'render');
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyAddDialog.tpl.html"
        ],
            function (ontologyAddDialogTpl) {
                self.doLayout(ontologyAddDialogTpl);
            });
        return this;
    },
    doLayout:function (ontologyAddDialogTpl) {

        var self = this;
        var dialog = _.template(ontologyAddDialogTpl, {});
        $("#editontology").replaceWith("");
        $("#addontology").replaceWith("");
        $(self.el).append(dialog);
        var user = window.app.status.user.model;
        $("#ontologyuser").append(user.get('username') + " (" + user.get('firstname') + " " + user.get('lastname') + ")");
        $("#login-form-add-ontology").submit(function () {
            self.createOntology();
            return false;
        });
        $("#login-form-add-ontology").find("input").keydown(function (e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-ontology").submit();
                return false;
            }
        });

        //Build dialog
        self.addOntologyDialog = $("#addontology").modal({
            keyboard:true,
            backdrop:false
        });
        $('#saveOntologyButton').click(function () {
            $("#login-form-add-ontology").submit();
            return false;
        });
        $('#closeAddOntologyDialog').click(function () {
            $("#addontology").modal("hide");
            $("#addontology").remove();
            return false;
        });
        self.open();
        return this;

    },
    refresh:function () {
    },
    open:function () {
        var self = this;
        self.clearAddOntologyPanel();
    },
    clearAddOntologyPanel:function () {
        $("#errormessage").empty();
        $("#ontologyerrorlabel").hide();
        $("#ontology-name").val("");
    },
    createOntology:function () {
        var self = this;

        $("#errormessage").empty();
        $("#ontologyerrorlabel").hide();

        var name = $("#ontology-name").val().toUpperCase();
        var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
        var users = [];

        $('input[type=checkbox][name=usercheckbox]:checked').each(function (i, item) {
            users.push($(item).attr("value"))
        });

        //create ontology
        var ontology = new OntologyModel({name:name}).save({name:name}, {
                success:function (model, response) {
                    window.app.view.message("Ontology", response.message, "success");
                    var id = response.ontology.id;
                    self.ontologiesPanel.refresh(id);
                    $("#addontology").modal("hide");
                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#ontologyerrorlabel").show();
                    $("#errormessage").append(json.errors)
                }
            }
        );

    }
});