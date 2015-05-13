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

var AddOntologyDialog = Backbone.View.extend({
    ontologiesPanel: null,
    addOntologyDialog: null,
    initialize: function (options) {
        this.container = options.container;
        this.ontologiesPanel = options.ontologiesPanel;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyAddDialog.tpl.html"
        ],
            function (ontologyAddDialogTpl) {
                self.doLayout(ontologyAddDialogTpl);
            });
        return this;
    },
    doLayout: function (ontologyAddDialogTpl) {

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
            keyboard: true,
            backdrop: true
        });
        $('#saveOntologyButton').click(function (event) {
            event.preventDefault();
            $("#login-form-add-ontology").submit();
            return false;
        });
        /*$('#closeAddOntologyDialog').click(function (event) {
         event.preventDefault();
         $("#addontology").modal("hide");
         $("#addontology").remove();
         return false;
         });*/
        self.open();
        return this;

    },
    refresh: function () {
    },
    open: function () {
        var self = this;
        self.clearAddOntologyPanel();
    },
    clearAddOntologyPanel: function () {
        $("#errormessage").empty();
        $("#ontologyerrorlabel").hide();
        $("#ontology-name").val("");
    },
    close : function () {
        $('#addontology').modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
    },
    createOntology: function () {
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
        var ontology = new OntologyModel({name: name}).save({name: name}, {
                success: function (model, response) {
                    window.app.view.message("Ontology", response.message, "success");
                    var id = response.ontology.id;
                    self.ontologiesPanel.refresh(id);
                    self.close();
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#ontologyerrorlabel").show();
                    $("#errormessage").append(json.errors)
                }
            }
        );

    }
});