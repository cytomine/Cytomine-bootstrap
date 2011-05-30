/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var OntologyView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    $tabsOntologies : null,
    idOntology : null,
    initialize: function(options) {
        this.container = options.container;
        this.idOntology = options.idOntology;
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
        console.log("OntologyView.render");

        var self = this;
        $(this.el).html(_.template(tpl, {}));

        self.$tabsOntologies = $(self.el).find("#tabsontology");

        self.initOntologyTabs();

        return this;
    },
    select : function(idOntology) {
        console.log("select " + idOntology);
        var self = this;
        var selectedOntologyIndex = 0;
        var index = 0;
        self.model.each(function(ontology) {
            //get index of selected ontology
            if(idOntology== ontology.get("id")) {
                selectedOntologyIndex = index;
            }
            index = index + 1;
        });
        console.log("activate = " + selectedOntologyIndex);
        self.$tabsOntologies.accordion( "activate" , selectedOntologyIndex );
    },
    /**
     * Init annotation tabs
     */
    initOntologyTabs : function(){
        var self = this;
        require(["text!application/templates/ontology/OntologyTab.tpl.html", "text!application/templates/ontology/OntologyTabContent.tpl.html"], function(ontologyTabTpl, ontologyTabContentTpl) {
            console.log("OntologyView: initOntologyTabs");
            console.log("OntologyView: initOntologyTabs create "+ self.model.length);
            //add "All annotation from all term" tab
            var selectedOntologyIndex = 0;
            var index = 0;
            self.model.each(function(ontology) {
                //add x term tab
                self.addOntologyToTab(ontologyTabTpl, ontologyTabContentTpl, { id : ontology.get("id"), name : ontology.get("name")});
                //create project search panel
                new OntologyPanelView({
                    model : ontology,
                    el:$(self.el).find("#tabsontology-"+ontology.id),
                    container : self,
                    ontologiesPanel : self
                }).render();

                //get index of selected ontology
                if(self.idOntology== ontology.get("id")) {
                    selectedOntologyIndex = index;
                }
                index = index + 1;
            });
            //self.fetchOntologies();
            if(!self.alreadyBuild)
                self.$tabsOntologies.accordion();
            console.log("activate = " + selectedOntologyIndex);
            self.$tabsOntologies.accordion( "activate" , selectedOntologyIndex );
        });
    },
    /**
     * Add the the tab with ontology info
     * @param id  ontology id
     * @param name ontology name
     */
    addOntologyToTab : function(ontologyTabTpl, ontologyTabContentTpl, data) {
        this.$tabsOntologies.append("<h3><a href=\"#\">"+data.name+"</a></h3>");
        this.$tabsOntologies.append(_.template(ontologyTabContentTpl, data));
        //tabs;
        /*         $("#ultabsontology").append(_.template(ontologyTabTpl, data));
         $("#listtabontology").append(_.template(ontologyTabContentTpl, data)); */
    }
});
