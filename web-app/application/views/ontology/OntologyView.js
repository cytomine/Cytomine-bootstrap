var OntologyView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    $tabsOntologies : null,
    ontologiesPanel : null,
    idOntology : null,
    addOntologyDialog : null,
    allTerms : [],
    events: {
        "click .addOntology": "showAddOntologyPanel",
        "click .refreshOntology" : "refresh"
    },
    initialize: function(options) {
        this.container = options.container;
        this.idOntology = options.idOntology;
        this.idTerm =  options.idTerm;
    },
    //browse all ontologies to retrieve each term and add it in window.app.models.terms
    retrieveTerm : function(ontologies) {
        var self = this;
        allTerms = [];
        ontologies.each(function(ontology) {
            var terms = self.retrieveChildren(ontology.attributes);
            allTerms = _.union(allTerms,terms);

        });
        window.app.models.terms = new TermCollection(allTerms);
    },
    retrieveChildren : function(parent) {
        var self = this;
        if(parent['children'].length==0) return [];
        var children = [];
        _.each(parent['children'],function(elem) {
            children.push(elem);
            children = _.union(children,self.retrieveChildren(elem));
        });
        return children;
    },
    refresh : function() {
        var self = this;
        window.app.models.ontologies.fetch({
            success : function (collection, response) {
                self.retrieveTerm(window.app.models.ontologies);
                self.render();
            }});
    },
    refresh : function(idOntology, idTerm) {
        var self = this;
        this.idOntology = idOntology;
        this.idTerm = idTerm;
        window.app.models.ontologies.fetch({
            success : function (collection, response) {
                self.retrieveTerm(window.app.models.ontologies);
                self.render();
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

        this.scrollToOntology(this.idOntology);
        return this;
    },
    scrollToOntology :function(idOntology) {
        var targetOffset = $(".ontology"+idOntology);
        if (targetOffset.length > 0) {
            var topOffset = targetOffset.offset().top;
            topOffset = topOffset - 110;
            $('html,body').animate({scrollTop: topOffset}, 1000);
        }
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
        require(["text!application/templates/ontology/OntologyTab.tpl.html", "text!application/templates/ontology/OntologyTabContent.tpl.html"],
            function(ontologyTabTpl, ontologyTabContentTpl) {
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

                //Hack loading...
                setTimeout(function(){
                    $("#ontologyLoading").remove();
                },1500);


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
