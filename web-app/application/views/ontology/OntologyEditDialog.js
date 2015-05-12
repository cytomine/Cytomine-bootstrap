/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var EditOntologyDialog = Backbone.View.extend({
    ontologyPanel: null,
    editOntologyDialog: null,
    initialize: function (options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyEditDialog.tpl.html"
        ],
            function (ontologyEditDialogTpl) {
                self.doLayout(ontologyEditDialogTpl);
            });
        return this;
    },
    doLayout: function (ontologyEditDialogTpl) {

        var self = this;
        $("#editontology").replaceWith("");
        $("#addontology").replaceWith("");
        var dialog = _.template(ontologyEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#login-form-edit-ontology").submit(function () {
            self.editOntology();
            return false;
        });
        $("#login-form-edit-ontology").find("input").keydown(function (e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-ontology").submit();
                return false;
            }
        });


        //Build dialog
        self.editOntologyDialog = $("#editontology").modal({
            keyboard: true,
            backdrop: true
        });
        $('#editOntologyButton').click(function (event) {
            event.preventDefault();
            $("#login-form-edit-ontology").submit();
            return false;
        });
        /*$('#closeEditOntologyDialog').click(function (event) {
            event.preventDefault();
            $("#editontology").modal("hide");
            $("#editontology").remove();
            return false;
        });*/
        self.open();
        self.fillForm();
        return this;

    },
    fillForm: function () {
        var self = this;
        $("#ontology-edit-name").val(self.model.get('name'));
    },
    refresh: function () {
    },
    open: function () {
        var self = this;
        self.clearEditOntologyPanel();
    },
    clearEditOntologyPanel: function () {
        var self = this;
        $("#ontologyediterrormessage").empty();
        $("#ontologyediterrorlabel").hide();
        $("#ontology-edit-name").val("");
    },
    close : function () {
        $('#editontology').modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
    },
    editOntology: function () {
        var self = this;
        $("#ontologyediterrormessage").empty();
        $("#ontologyediterrorlabel").hide();

        var name = $("#ontology-edit-name").val().toUpperCase();

        //edit ontology
        var ontology = self.model;
        ontology.unset('children'); //remove children (terms), they are not use by server
        ontology.save({name: name}, {
                success: function (model, response) {
                    window.app.view.message("Ontology", response.message, "success");
                    self.close();
                    self.ontologyPanel.refresh();
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#ontologyediterrorlabel").show();
                    $("#errormessage").append(json.errors);
                }
            }
        );
    }
});