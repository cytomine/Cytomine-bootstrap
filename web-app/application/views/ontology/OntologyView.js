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
        console.log("selectOntology:" + this.idOntology);
        var self = this;
        if ($("#menuOntologyUl").children().length == 1) {
            return;
        }
        //if param => select
        if (self.idOntology == null) {
            console.log($("#menuOntologyUl").children()[1]);
            var firstOntologyNode = $("#menuOntologyUl").children()[1];
            self.idOntology = $(firstOntologyNode).attr("data-id")

        }
        console.log("self.idOntology=" + self.idOntology);
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
