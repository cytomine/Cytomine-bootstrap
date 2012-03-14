var OntologyView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    $tabsOntologies : null,
    ontologiesPanel : null,
    idOntology : null,
    addOntologyDialog : null,
    events: {
        "click .addOntology": "showAddOntologyPanel",
        "click .refreshOntology" : "refresh"
    },
    initialize: function(options) {
        this.container = options.container;
        this.idOntology = options.idOntology;
        this.idTerm =  options.idTerm;
    },
    refresh : function() {
        var self = this;
        var i = 0;
        var deferRender = function(){
            i++;
            if (i == 2) self.render();
        }
        window.app.models.terms.fetch({
            success : function (collection, response) {
                deferRender();
            }
        });
        window.app.models.ontologies.fetch({
            success : function (collection, response) {
                deferRender();
            }});
    },
    refresh : function(idOntology, idTerm) {
        var self = this;
        this.idOntology = idOntology;
        this.idTerm = idTerm;
        var i = 0;
        var deferRender = function(){
            i++;
            if (i == 2) self.render();
        }
        window.app.models.terms.fetch({
            success : function (collection, response) {
                deferRender();
            }
        });
        window.app.models.ontologies.fetch({
            success : function (collection, response) {
                deferRender();
            }});
    },
    select : function(idOntology) {
        var self = this;
        this.idOntology = idOntology;
        self.render();
    },
    render : function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyList.tpl.html"
        ],
                function(tpl) {
                    self.doLayout(tpl);
                });

        return this;
    },
    doLayout: function(tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, {}));
        self.$tabsOntologies = $(self.el).find("#ontology");
        self.initOntologyTabs();
        return this;
    },
    showAddOntologyPanel : function() {
        var self = this;
        $('#addontology').remove();
        self.addOntologyDialog = new AddOntologyDialog({ontologiesPanel:self,el:self.el}).render();
    },
    select : function(idOntology,idTerm) {
        var self = this;
        var selectedOntologyIndex = 0;
        var index = 0;
        self.model.each(function(ontology) {
            if(idOntology== ontology.get("id")) {
                selectedOntologyIndex = index;
            }
            index = index + 1;
        });
        if (idTerm != undefined) self.ontologiesPanel[selectedOntologyIndex].selectTerm(idTerm);
    },
    /**
     * Init annotation tabs
     */
    initOntologyTabs : function(){
        var self = this;
        require(["text!application/templates/ontology/OntologyTab.tpl.html", "text!application/templates/ontology/OntologyTabContent.tpl.html"], function(ontologyTabTpl, ontologyTabContentTpl) {
            self.ontologiesPanel = new Array();
            //add "All annotation from all term" tab
            self.model.each(function(ontology) {
                var elem = self.addOntologyToTab(ontologyTabTpl, ontologyTabContentTpl, { id : ontology.get("id"), name : ontology.get("name")});
                //create project search panel
                var view = new OntologyPanelView({
                    model : ontology,
                    el:elem,
                    container : self,
                    ontologiesPanel : self
                });
                view.render();
                self.ontologiesPanel.push(view);
            });

            if(!self.alreadyBuild) {
                $("#ontology h3 a").click(function() {
                    window.location = $(this).attr('href'); //follow link
                    return false;
                });
            }
        });
    },
    /**
     * Add the the tab with ontology info
     * @param id  ontology id
     * @param name ontology name
     */
    addOntologyToTab : function(ontologyTabTpl, ontologyTabContentTpl, data) {
        return this.$tabsOntologies.append(_.template(ontologyTabContentTpl, data));
    }
});
