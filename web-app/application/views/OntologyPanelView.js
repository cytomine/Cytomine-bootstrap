var OntologyPanelView = Backbone.View.extend({
    tree : null,
    info : null,
    expanse : false,
    addOntologyTermDialog : null,
    events: {
        "click .addTerm": "addTerm",
        "click .renameTerm": "renameTerm",
        "click .deleteTerm": "deleteTerm"
    },
    initialize: function(options) {
        this.container = options.container;
        this.ontologiesPanel = options.ontologiesPanel;
    },
    render: function() {
        console.log("OntologyPanelView.render");
        var self = this;
        self.tree = $(self.el).find("#treeontology-"+self.model.id);
        self.info = $(self.el).find("#infoontology-"+self.model.id);
        console.log($(self.el));
        self.buildOntologyTree();
        self.initButton();
        return this;
    },
    addTerm : function() {
        console.log("addTerm");
        var self = this;
        $('#dialog-add-ontology-term').remove();
        var nodeWithoutLeaf = self.getNodeWithoutLeaf();

        self.addOntologyTermDialog = new OntologyAddTermView({ontologyPanel:self,el:self.el,parents:nodeWithoutLeaf,model:self.model}).render();


    },
    getNodeWithoutLeaf : function() {
        var self = this;
        var nodeWithoutLeaf = new Array();
        self.tree.dynatree("getRoot").visit(function(node){
            //console.log(node);

            if(node.data.isFolder)
                nodeWithoutLeaf.push(node);
        });
        return nodeWithoutLeaf;
    },
    renameTerm : function() {
        console.log("renameTerm");
        var node = this.tree.dynatree("getActiveNode");
        node.data.title = "My new title";
        node.render();
    },
    deleteTerm : function() {
        console.log("deleteTerm");

    },
    buildOntologyTree : function() {
        var self = this;
        console.log("buildOntologyTree for ontology " + self.model.id);

        console.log(self.model.toJSON());
        console.log(self.tree.length);
        self.tree.dynatree({
            children: self.model.toJSON(),
            onExpand : function() { console.log("expanding/collapsing");},
            onClick: function(node, event) {
                console.log("onClick");
                // Display list of selected nodes
                console.log(node);
                var s = node.data.title;
                self.info.text(s);

            },
            onSelect: function(select, node) {
                console.log("onSelect");
                // Display list of selected nodes
                var s = node.tree.getSelectedNodes().join(", ");
                self.info.text(s);


            },
            onCustomRender: function(node) {

            },
            onDblClick: function(node, event) {
                console.log("Double click");
            },
            generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "treeDataOntology-"+self.model.id,
            cookieId: "dynatree-Ontology-"+self.model.id,
            idPrefix: "dynatree-Ontology-"+self.model.id+"-" ,
            debugLevel: 2
        });
        //expand all nodes
        self.tree.dynatree("getRoot").visit(function(node){
            node.expand(true);
        });


        //remove all node withouth children
        self.tree.dynatree("getRoot").visit(function(node){
            //console.log(node);

            if(!node.data.isFolder)
            {
                console.log("node.deactivate()");
                node.deactivate();
            }
        });

    },
    initButton : function() {
        var self = this;
        $("#buttonExpanseOntology"+self.model.id).button({
            icons : {secondary: "ui-icon-circle-arrow-n" }
        });

        $('#buttonAddTerm'+self.model.id).button({
            icons : {secondary: "ui-icon-plus" }
        });
        $('#buttonRenameTerm'+self.model.id).button({
            icons : {secondary: "ui-icon-pencil" }
        });
        $('#buttonDeleteTerm'+self.model.id).button({
            icons : {secondary: "ui-icon-trash" }
        });

        $("#buttonExpanseOntology"+self.model.id).click(function(){
            self.tree.dynatree("getRoot").visit(function(node){
                node.expand(self.expanse);
            });
            self.expanse=!self.expanse;
            if(self.expanse) {
                $("#buttonExpanseOntology"+self.model.id).button({icons : {secondary: "ui-icon-circle-arrow-s" }});
                $("#buttonExpanseOntology"+self.model.id).find(".ui-button-text").empty().append("Expand all");
            }
            else {
                $("#buttonExpanseOntology"+self.model.id).button({icons : {secondary: "ui-icon-circle-arrow-n" }});
                $("#buttonExpanseOntology"+self.model.id).find(".ui-button-text").empty().append("Collapse all");
            }
            return false;
        });
        /*$("#buttonCollapseOntology"+self.model.id).click(function(){
         self.tree.dynatree("getRoot").visit(function(node){
         node.expand(false);
         });
         return false;
         });*/
    }
});