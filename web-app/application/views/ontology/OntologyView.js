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

var OntologyView = Backbone.View.extend({
    tagName: "div",
    self: this,
    alreadyBuild: false,
    ontologiesPanel: null,
    idOntology: null,
    addOntologyDialog: null,
    allTerms: [],
    events: {
        "click #ontologyAddButton": "showAddOntologyPanel",
        "click #ontologyRefreshButton": "refreshOntology"
    },
    initialize: function (options) {
        this.container = options.container;
        this.idOntology = options.idOntology;
        this.idTerm = options.idTerm;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyList.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });

        return this;
    },
    doLayout: function (tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, {}));

        if(window.app.status.user.model.get('guest')) {
            $("#ontologyAddButton").remove();
        }

        self.fillOntologyMenu();
        return this;
    },
    fillOntologyMenu: function () {
        var self = this;
        console.log("fillOntologyMenu");
        new OntologyCollection({light: true}).fetch({
            success: function (collection, response) {
                console.log("ontology fetch");
                collection.each(function (ontology) {
                    console.log("add ontology");
                    $("#menuOntologyUl").append('<li data-id="' + ontology.id + '" id="consultOntology-' + ontology.id + '"><a href="#ontology/' + ontology.id + '"><i class="icon-arrow-right"></i> ' + ontology.get('name') + '</a></li>');
                });
                self.selectOntology();
            }});
    },
    select: function (idOntology, idTerm) {
        var self = this;
        if (self.idOntology == idOntology) {
            return;
        }
        self.idOntology = idOntology;
        self.idTerm = idTerm;
        self.selectOntology();
    },
    selectOntology: function () {
        var self = this;

        //if param => select
        if (self.idOntology == null) {
            console.log($("#menuOntologyUl").children()[1]);
            var firstOntologyNode = $("#menuOntologyUl").children();
            self.idOntology = $(firstOntologyNode).attr("data-id")
        }
        $("#menuOntologyUl").children().removeClass("active");
        $("#consultOntology-" + self.idOntology).addClass("active");
        self.printOntology();
        $(window).scrollTop(0);
    },
    printOntology: function () {
        console.log("printOntology");
        var self = this;

        $("#ontologyPanelView").append('<div id="ontologyLoading" class="alert alert-info"><i class="icon-refresh" /> Loading...</div>');

        new OntologyModel({id: self.idOntology}).fetch({
            success: function (model, response) {
                window.app.models.terms = window.app.retrieveTerm(model);
                console.log("OntologyModel succes " + self.idOntology);
                require(["text!application/templates/ontology/OntologyTabContent.tpl.html"],
                    function (ontologyTabContentTpl) {

                        //create project search panel
                        var view = new OntologyPanelView({
                            model: model,
                            el: $("#ontologyPanelView"),
                            container: self
                        }).render();

                        if (self.idTerm != null) {
                            view.selectTerm(self.idTerm)
                        }
                    });

            }});
    },
    refreshOntology: function () {
        this.render();
    },
    refresh: function (idOntology) {
        console.log("refresh idOntology=" + idOntology);
        this.idOntology = idOntology;
        this.render();
    },
    refresh: function (idOntology, idTerm) {
        this.idOntology = idOntology;
        this.idTerm = idTerm;
        this.render();
    },
    showAddOntologyPanel: function () {
        var self = this;
        $('#addontology').remove();
        self.addOntologyDialog = new AddOntologyDialog({ontologiesPanel: self, el: self.el}).render();
    }
});
