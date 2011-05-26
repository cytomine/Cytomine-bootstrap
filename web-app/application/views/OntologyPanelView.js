var OntologyPanelView = Backbone.View.extend({
    tree : null,
    info : null,
    expanse : false,
    addOntologyTermDialog : null,
    editOntologyTermDialog : null,
    deleteOntologyTermDialog : null,
    events: {
        "click .addTerm": "addTerm",
        "click .editTerm": "editTerm",
        "click .deleteTerm": "deleteTerm"
    },
    initialize: function(options) {
        this.container = options.container;
        this.ontologiesPanel = options.ontologiesPanel;
    },
    refresh : function() {
        var self = this;
        self.model.fetch({
            success : function (model, response) {
                console.log(self.model);
                self.clear();
                self.render();
            }});

    },
    clear : function() {
        var self = this;
        // var rootNode = self.tree.dynatree("getRoot");
        // rootNode.removeChildren();
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
    editTerm : function() {
        console.log("editTerm");
        var self = this;
        $('#dialog-edit-ontology-term').remove();

        var node = this.tree.dynatree("getActiveNode");

        if(node==null) {
            alert("click!");
            return;
        }

        var term = window.app.models.terms.get(node.data.id);
        console.log("edit term="+term.id);
        console.log(term);

        self.model.fetch({
            success : function (model, response) {
                self.editOntologyTermDialog = new OntologyEditTermView({ontologyPanel:self,el:self.el,model:term,ontology:self.model}).render();
            }});



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

    deleteTerm : function() {
        console.log("deleteTerm");

    },
    buildOntologyTree : function() {
        var self = this;
        console.log("buildOntologyTree for ontology " + self.model.id);
        var currentTime = new Date()
        console.log(self.model.toJSON());
        console.log(self.tree.length);
        console.log("Empty:"+self.tree.length);
        console.log("Build tree:"+self.tree.length);
        self.tree.empty();
        $(self.el).find("#treeontology-"+self.model.id).dynatree({
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
            //generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "treeDataOntology-"+self.model.id + currentTime.getTime(),
            cookieId: "dynatree-Ontology-"+self.model.id+ currentTime.getTime(),
            idPrefix: "dynatree-Ontology-"+self.model.id+ currentTime.getTime()+"-" ,
            debugLevel: 2
        });
        //expand all nodes

        console.log("root="+self.tree.dynatree("getRoot"));
        self.tree.dynatree("getRoot").visit(function(node){
            console.log("node="+node.data.title);
            node.expand(true);
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
        $('#buttonEditTerm'+self.model.id).button({
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