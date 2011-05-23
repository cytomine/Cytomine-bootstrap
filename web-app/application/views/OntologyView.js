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
    render: function() {
        console.log("OntologyView.render");

        var self = this;
        var tpl = ich.ontologyviewtpl({}, true);
        $(this.el).html(tpl);

        self.initOntologyTabs();
        self.fetchOntologies();

        return this;
    },
    initOntologyTabs : function(){
        var self = this;
        console.log("OntologyView: initOntologyTabs");
        console.log("OntologyView: initOntologyTabs create "+ self.model.length);

                self.model.each(function(ontology) {
                    //add x term tab
                    self.addOntologyToTab(ontology.get("id"),ontology.get("name"));
                });

                if(self.tabsOntologies==null)
                    self.tabsOntologies = $("#tabsontology").tabs();
                self.fetchOntologies();

    },
    addOntologyToTab : function(id, name) {
        var ontologyelem = ich.ontologytitletabtpl({name:name,id:id});
        $("#ultabsontology").append(ontologyelem);
        var contentontologyelem = ich.ontologydivtabtpl({name:name,id:id});
        $("#listtabontology").append(contentontologyelem);
    },
    fetchOntologies : function () {
        console.log("OntologyView: fetchOntologies");

        var self = this;
        //init specific panel
        self.model.each(function(ontology) {
            //$("#tabsontology-"+ontology.get("id")).empty();
            self.buildOntologyTree(ontology);

        });


    },
    buildOntologyTree : function(ontology) {
        console.log("buildOntologyTree for ontology " + ontology.id);
        $("#tabsontology-"+ontology.id).prepend(ontology.get("name"));
        console.log(ontology.toJSON());
        console.log("#treeontology-"+ontology.id);
        $("#treeontology-"+ontology.id).dynatree({
            checkbox: true,
            selectMode: 3,
            expand : true,
            onExpand : function() { console.log("expanding/collapsing");},
            children: ontology.toJSON(),
            onSelect: function(select, node) {

                if (node.isSelected()) {
                    console.log("Check");
                } else if (!node.isSelected()) {
                    console.log("Uncheck");
                }


            },
            onDblClick: function(node, event) {
                console.log("Double click");
            },

            // The following options are only required, if we have more than one tree on one page:
            initId: "treeDataOntology"+this.model.id,
            cookieId: "dynatree-CbOntology"+this.model.id,
            idPrefix: "dynatree-CbOntology"+this.model.id+"-"
        });
         //expand all nodes
        $("#treeontology-"+ontology.id).dynatree("getRoot").visit(function(node){
            node.expand(true);
        });

    }

});
