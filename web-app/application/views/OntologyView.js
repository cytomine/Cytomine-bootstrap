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
    tabsOntologies : null,
    // template : _.template($('#image-view-tpl').html()),

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

        self.initOntologyTabs();


        return this;
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

            var first = true;
            self.model.each(function(ontology) {
                //add x term tab
                if(!first) return;
                self.addOntologyToTab(ontologyTabTpl, ontologyTabContentTpl, { id : ontology.get("id"), name : ontology.get("name")});
                first = false;
            });
             self.fetchOntologies();
            if(self.tabsOntologies==null)
                self.tabsOntologies = $("#tabsontology").tabs({show: function(event, ui){
                    $(this).attr('style', 'width:100%;height:100%;overflow:auto');
                    return true;
                 }});
        });
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addOntologyToTab : function(ontologyTabTpl, ontologyTabContentTpl, data) {
        $("#ultabsontology").append(_.template(ontologyTabTpl, data));
        $("#listtabontology").append(_.template(ontologyTabContentTpl, data));




    },
    fetchOntologies : function () {
        console.log("OntologyView: fetchOntologies");

        var self = this;
        //init specific panel
        var first = true;
        self.model.each(function(ontology) {
            if(!first) return;
            //create project search panel
            new OntologyPanelView({
                model : ontology,
                el:$("#tabsontology-"+ontology.id),
                container : self,
                ontologiesPanel : self
            }).render();
            first = false;
        });


    }


});
