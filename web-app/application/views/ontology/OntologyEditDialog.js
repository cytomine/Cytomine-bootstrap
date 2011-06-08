var EditOntologyDialog = Backbone.View.extend({
    ontologyPanel : null,
    editOntologyDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyEditDialog.tpl.html"
        ],
               function(ontologyEditDialogTpl) {
                   self.doLayout(ontologyEditDialogTpl);
               });
        return this;
    },
    doLayout : function(ontologyEditDialogTpl) {

        var self = this;
        $("#editontology").replaceWith("");
        $("#addontology").replaceWith("");
        var dialog = _.template(ontologyEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#login-form-edit-ontology").submit(function () {
            self.editOntology();
            return false;
        });
        $("#login-form-edit-ontology").find("input").keydown(function(e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-ontology").submit();
                return false;
            }
        });


        //Build dialog
        console.log("EditOntologyDialog: build dialog");
        self.editOntologyDialog = $("#editontology").dialog({
            width: 500,
            autoOpen : false,
            modal:true,
            buttons : {
                "Save" : function() {
                    $("#login-form-edit-ontology").submit();
                },
                "Cancel" : function() {
                    $("#editontology").dialog("close");
                }
            }
        });
        self.open();
        self.fillForm();
        return this;

    },
    fillForm : function() {
        console.log("fillForm");
        var self = this;
       $("#ontology-edit-name").val(self.model.get('name'));
    },
    refresh : function() {
    },
    open: function() {
        var self = this;
        self.clearEditOntologyPanel();
        self.editOntologyDialog.dialog("open");
    },
    clearEditOntologyPanel : function() {
        var self = this;
        console.log($(self.el).html());
        $("#ontologyediterrormessage").empty();
        $("#ontologyediterrorlabel").hide();
        $("#ontology-edit-name").val("");
    },
    editOntology : function() {
        console.log("editOntology...");
        var self = this;

        $("#ontologyediterrormessage").empty();
        $("#ontologyediterrorlabel").hide();

        var name = $("#ontology-edit-name").val().toUpperCase();

        //edit ontology
        var ontology = self.model;
        ontology.unset('children'); //remove children (terms), they are not use by server
        ontology.save({name : name}, {
            success: function (model, response) {
                console.log(response);

                window.app.view.message("Ontology", response.message, "");
                self.editOntologyDialog.dialog('close');
                self.ontologyPanel.refresh();


            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                console.log("json.ontology=" + json.errors);

                $("#ontologyediterrorlabel").show();

                console.log($("#ontologyediterrormessage").append(json.errors));
            }
        }
                );
    }
});