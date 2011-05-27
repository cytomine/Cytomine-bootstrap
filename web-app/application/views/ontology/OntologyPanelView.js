var OntologyPanelView = Backbone.View.extend({
    $tree : null,
    $info : null,
    $panel : null,
    $addTerm : null,
    $editTerm : null,
    $deleteTerm : null,
    $buttonAddTerm : null,
    $buttonEditTerm : null,
    $buttonDeleteTerm : null,
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
    render: function() {
        console.log("OntologyPanelView.render");
        var self = this;

        self.$panel = $(self.el);
        self.$tree = self.$panel.find("#treeontology-"+self.model.id);
        self.$info = self.$panel.find("#infoontology-"+self.model.id);

        self.$addTerm = self.$panel.find('#dialog-add-ontology-term');
        self.$editTerm = self.$panel.find('#dialog-edit-ontology-term');
        self.$deleteTerm = self.$panel.find('#dialogsTerm');

        self.$buttonAddTerm = self.$panel.find($('#buttonAddTerm'+self.model.id));
        self.$buttonEditTerm = self.$panel.find($('#buttonEditTerm'+self.model.id));
        self.$buttonDeleteTerm = self.$panel.find($('#buttonDeleteTerm'+self.model.id));

        self.buildOntologyTree();
        self.buildButton();
        self.buildInfoPanel();

        return this;
    },

    refresh : function() {
        var self = this;
        self.model.fetch({
            success : function (model, response) {
                self.clear();
                self.render();
            }});

    },

    clear : function() {
        var self = this;
        self.$panel.empty();
        require([
            "text!application/templates/ontology/OntologyTabContent.tpl.html"
        ],
               function(tpl) {
                   console.log("OntologyPanelView.render");
                   self.$panel.html(_.template(tpl, { id : self.model.get("id"), name : self.model.get("name")}));
                   return this;
               });

        return this;
    },

    addTerm : function() {
        console.log("addTerm");
        var self = this;
        self.$addTerm.remove();

        self.addOntologyTermDialog = new OntologyAddOrEditTermView({
            ontologyPanel:self,
            el:self.el,
            ontology:self.model,
            model:null //add component so no term
        }).render();
    },

    editTerm : function() {
        console.log("editTerm");
        var self = this;
        self.$editTerm.remove();

        var node = self.$tree.dynatree("getActiveNode");

        if(node==null) {
            alert("You must select a term (we must replace this message with a beautiful dialog)!");
            return;
        }

        new TermModel({id:node.data.id}).fetch({
            success : function (model, response) {
                console.log("edit term="+model.id + "name=" +model.get('name'));
                self.editOntologyTermDialog = new OntologyAddOrEditTermView({
                    ontologyPanel:self,
                    el:self.el,
                    model:model,
                    ontology:self.model
                }).render();
            }});
    },

    getCurrentTermId : function() {
        var node = this.$tree.dynatree("getActiveNode");
        if(node==null) return null;
        else node.data.id;
    },

    deleteTerm : function() {
        console.log("deleteTerm");
        var self = this;

        var idTerm = self.getCurrentTermId();
        new AnnotationCollection({term:idTerm}).fetch({
            success : function (collection, response) {
                if(collection.length==0) self.buildDeleteTermConfirmDialog(term);
                else self.buildDeleteTermWithAnnotationConfirmDialog();
            }});
    },

    buildDeleteTermConfirmDialog : function(term) {
        console.log("term is not linked with annotations");
        var self = this;
        require(["text!application/templates/ontology/OntologyDeleteTermConfirmDialog.tpl.html"], function(tpl) {
            // $('#dialogsTerm').empty();
            var dialog =  new ConfirmDialogView({
                el:'#dialogsTerm',
                template : _.template(tpl, {term : term.get('name'),ontology : self.model.get('name')}),
                dialogAttr : {
                    dialogID : self.$deleteTerm,
                    width : 400,
                    height : 200,
                    buttons: {
                        "OK": function() {
                            console.log("delete");
                            //TODO:delete term
                        },
                        "Cancel": function() {
                            console.log("no delete");
                            //doesn't work! :-(
                            self.$deleteTerm.dialog( "close" ) ;
                        }
                    },
                    close :function (event) {
                    }
                }
            }).render();
        });
    },
    buildDeleteTermWithAnnotationConfirmDialog : function() {
          console.log("term is linked with annotations");
        //TODO:ask confirmation (and delete term  with annotation? or not...)
    },

    buildButton : function() {
        var self = this;

        self.$buttonAddTerm.button({
            icons : {secondary: "ui-icon-plus" }
        });
        self.$buttonEditTerm.button({
            icons : {secondary: "ui-icon-pencil" }
        });
        self.$buttonDeleteTerm.button({
            icons : {secondary: "ui-icon-trash" }
        });
    },
    buildInfoPanel : function() {

    },

    buildOntologyTree : function() {
        var self = this;
        console.log("buildOntologyTree for ontology " + self.model.id);
        var currentTime = new Date();

        self.$tree.empty();
        self.$tree.dynatree({
            children: self.model.toJSON(),
            onExpand : function() { console.log("expanding/collapsing");},
            onClick: function(node, event) {
                self.updateInfoPanel(node.data.id,node.data.title);
            },
            onSelect: function(select, node) {
                 self.updateInfoPanel(node.data.id);
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
        self.$tree.dynatree("getRoot").visit(function(node){
            node.expand(true);
        });
    },

    updateInfoPanel : function(idTerm,name) {
        var self = this;
        //bad code with html but waiting to know what info is needed...
        self.$info.empty();
        self.$info.append("<div id=\"termchart-"+self.model.id +"\"><h3>"+name+"</h3><div id=\"terminfo-"+self.model.id +"\"></div>");

        var statsCollection = new StatsCollection({term:idTerm});
                var statsCallback = function(collection, response) {

                    console.log("stats="+collection.length);

                    collection.each(function(stat) {
                        console.log(stat.get('key')+" " + stat.get('value'));
                        $("#terminfo-"+self.model.id).append("Project "+stat.get('key') + ": " + stat.get('value') + " annotations<br>");
                    });

                    $("#termchart-"+self.model.id).panel({
                        collapsible:false
                    });

                }
                statsCollection.fetch({
                    success : function(model, response) {
                        statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
    }
});