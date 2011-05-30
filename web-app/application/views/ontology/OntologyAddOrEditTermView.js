var OntologyAddOrEditTermView = Backbone.View.extend({
    ontologyPanel : null, //Ontology panel in ontology view
    ontologyDialog : null, //dialog (add, edit)
    ontology : null,
    $tree : null,
    $panel : null,
    $textboxName : null,
    $colorChooser : null,
    $inputOldColor : null,
    $inputNewColor : null,
    $errorMessage : null,
    $errorLabel : null,
    action : null,
    initialize: function(options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        this.ontology = options.ontology;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyAddOrEditTermView.tpl.html"
        ],
               function(ontologyAddOrEditTermViewTpl) {
                   self.doLayout(ontologyAddOrEditTermViewTpl);
               });
        return this;
    },
    doLayout : function(ontologyAddOrEditTermViewTpl) {

        var self = this;

        if(self.model==null) {
            //dialog to add
            self.action = "Add";
            //create an empty model (not saved)
            self.createNewEmpty();
        }
        else self.action = "Edit"; //dialog to edit

        //remove older dialog
        self.$termDialog  = $(self.el).find("#dialog-"+self.action+"-ontology-term");
        self.$termDialog.replaceWith("");

        var dialog = _.template(ontologyAddOrEditTermViewTpl, {
            oldColor : self.model.get('color'),
            action : self.action
        });
        console.log(dialog);
        $(self.el).append(dialog);

        self.$panel = $(self.el);
        self.$termDialog  = $(self.el).find("#dialog-"+self.action+"-ontology-term");
        self.$tree = self.$panel.find("#" + self.action +"ontologytermtree");
        self.$from = self.$panel.find($("#form-" + self.action +"-ontology-term"));
        self.$textboxName = self.$panel.find("#" + self.action +"termname");

        self.$colorChooser = self.$panel.find('#colorpicker1');
        self.$inputOldColor = self.$panel.find('#oldColor');
        self.$inputNewColor = self.$panel.find('#color1');


        self.$errorMessage =  self.$panel.find("#" + self.action +"ontologytermerrormessage");
        self.$errorLabel =  self.$panel.find("#" + self.action +"ontologytermerrorlabel");

        self.$from.submit(function () {
            if(self.action == "Edit")
                self.updatedOntologyTerm();
            else  self.createOntologyTerm();
            return false;});
        self.$from.find("input").keydown(function(e){
            if (e.keyCode == 13) { //ENTER_KEY
                self.$from.submit();
                return false;
            }
        });
        self.clearOntologyTermPanel();
        self.buildNameInfo();
        self.buildColorInfo();
        self.buildParentInfo();

        //Build dialog
        console.log("OntologyTermDialog: build dialog:"+self.$termDialog.length);
        self.ontologyDialog = self.$termDialog.dialog({
            width: "1000",
            autoOpen : false,
            modal:true,
            buttons : {
                "Save" : function() {
                    self.$from.submit();
                },
                "Cancel" : function() {
                    self.$termDialog.dialog("close");
                }
            }
        });
        self.open();
        return this;
    },
    /**
     * Create new empty model with default value
     */
    createNewEmpty : function() {
        var self = this;
        console.log("createNewEmpty");
        self.model = new TermModel({id:-1,name:"",color:"#ff0000"});
    },

    buildNameInfo : function () {
        console.log("buildNameInfo");
        var self = this;
        self.$textboxName.val(self.model.get("name"));

        self.$textboxName.bind('keyup mouseup change',function(e){
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
            var color = "#119b04"
            var htmlNode = "<label style='color:{{color}}'>{{title}}</label>"
            var nodeTpl = _.template(htmlNode, {title : self.$textboxName.val(), color : color});
            node.setTitle(nodeTpl);
        });
    },

    buildColorInfo : function() {
        console.log("buildColorInfo");
        var self = this;

        console.log(self.$inputNewColor.attr('id'));
        var colorPicker = self.$colorChooser.farbtastic('#color1');

        var color = self.model.get('color');

        self.$inputOldColor.val(color);
        self.$inputNewColor.val(color);
        self.$inputOldColor.css("background", color);
        self.$inputNewColor.css("background", color);
        self.$inputNewColor.css("color", self.$inputOldColor.css("color"));
    },

    buildParentInfo : function() {
        console.log("buildParentInfo");
        var self = this;
        console.log("buildOntologyTree for ontology " + self.ontology.id);

        self.$tree.empty() ;
        self.$tree.dynatree({
            children: self.ontology.toJSON(),
            onExpand : function() { console.log("expanding/collapsing");},
            onRender: function(node, nodeSpan) {
                self.$tree.find("a.dynatree-title").css("color", "black");
            },
            onClick: function(node, event) {
            },
            onSelect: function(select, node) {
            },
            onCustomRender: function(node) {
            },
            onDblClick: function(node, event) {
            },
            dnd: {
                onDragStart: function(node) {
                    /** This function MUST be defined to enable dragging for the tree.
                     *  Return false to cancel dragging of node.
                     */
                    if(node.data.key!=self.model.id) return false;
                    return true;
                },
                onDragStop: function(node) {
                },
                autoExpandMS: 1000,
                preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                onDragEnter: function(node, sourceNode) {
                    logMsg("tree.onDragEnter(%o, %o)", node, sourceNode);
                    return true;
                },
                onDragOver: function(node, sourceNode, hitMode) {
                },
                onDrop: function(node, sourceNode, hitMode, ui, draggable) {
                    logMsg("tree.onDrop(%o, %o, %s)", node, sourceNode, hitMode);
                    if(!node.data.isFolder && hitMode=="over")
                    {
                        console.log("NOT A FOLDER");
                    }
                    else sourceNode.move(node, hitMode);
                },
                onDragLeave: function(node, sourceNode) {
                }
            },
            generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "" + self.action +"treeDataOntology-"+self.model.id,
            cookieId: "" + self.action +"dynatree-Ontology-"+self.model.id,
            idPrefix: "" + self.action +"dynatree-Ontology-"+self.model.id+"-" ,
            debugLevel: 0
        });
        //expand all nodes
        self.$tree.dynatree("getRoot").visit(function(node){
            node.expand(true);
        });

        //if add panel, add the "temp" model to the tree (event if it's not yet a part of the ontology)
        if(self.action=="Add") {
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.ontology.id);
            var childNode = node.addChild({
                title: "",
                key : -1,
                tooltip: "This folder and all child nodes were added programmatically.",
                isFolder: false
            });
        }

        //make the new term node visible
        var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
        var title = node.data.title
        var color = "#9ac400"
        var htmlNode = "<label style='color:{{color}}'>{{title}}</label>"
        var nodeTpl = _.template(htmlNode, {title : title, color : color});
        node.setTitle(nodeTpl);

    },

    getNewName : function() {
        var self = this;
        return self.$textboxName.val();
    },
    getNewParent : function() {
        var self = this;
        var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
        return node.parent.data.id;
    },
    getNewColor : function() {
        return  this.$inputNewColor.val();
    },
    refresh : function() {

    },
    open: function() {
        var self = this;
        self.clearOntologyTermPanel();
        self.ontologyDialog.dialog("open") ;
    },
    clearOntologyTermPanel : function() {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();
    },
    updatedOntologyTerm : function() {
        console.log("updatedOntologyTerm...");
        var self = this;

        self.$errorMessage.empty();
        self.$errorLabel.hide();

        var id = self.model.id;
        var name =  self.getNewName();
        var idOldParent = self.model.get("parent");
        var idParent = self.getNewParent();
        var isOldParentOntology = true;
        if(idOldParent!=null && window.app.models.ontologies.get(idOldParent)==undefined) {
            isOldParentOntology = false;
        }
        var isParentOntology = true;

        if(window.app.models.ontologies.get(idParent)==undefined) {
            isParentOntology = false;
        }
        console.log("isOldParentOntology="+isOldParentOntology + " isParentOntology=" + isParentOntology);
        var color = self.getNewColor();
        console.log("update term "+ name + " with parent=" + idParent + "(old parent =" + idOldParent+ ") and color="+color);

        self.model.set({name:name,color:color});
        self.model.save({name:name,color:color},{
            success: function (model, response) {
                console.log(response);
                //TODO: check it relation/term is changed
                console.log("isOldParentOntology="+isOldParentOntology + " isParentOntology=" + isParentOntology);
                if(idParent!=idOldParent)
                {
                    if(isOldParentOntology&&isParentOntology) {self.close();}
                    else if(isOldParentOntology)  self.addRelation(id,idParent); //parent was ontology so nothing to delete
                    else if(isParentOntology) self.resetRelation(id,idOldParent,null); //new parent is ontology so nothing to add
                    else {
                        self.resetRelation(id,idOldParent,idParent);
                    }
                }
                else self.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                console.log("json.project="+json.errors);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }
        } ); //TODO: catch error
    },

    createOntologyTerm : function() {
        console.log("createOntologyTerm...");

        var self = this;

        self.$errorMessage.empty();
        self.$errorLabel.hide();

        var id = self.model.id;
        var name =  self.getNewName();
        var isParentOntology = true;
        var idParent = self.getNewParent();
        if(window.app.models.ontologies.get(idParent)==undefined) {
            isParentOntology = false;
        }
        console.log("isParentOntology=" + isParentOntology);
        var color = self.getNewColor();
        console.log("create term "+ name + " with parent=" + idParent + " and color="+color);

        self.model.set({name:name,color:color});
        self.model = new TermModel({name:name,color:color,ontology:self.ontology.id}).save({name:name,color:color,ontology:self.ontology.id},{
            success: function (model, response) {
                console.log(response);
                //TODO: check it relation/term is changed
                console.log("isParentOntology=" + isParentOntology);

                if(isParentOntology) {
                    //no link "parent" with a term
                    self.close();
                }
                else {
                    self.addRelation(id,idParent);
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                console.log("json.project="+json.errors);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }
        } ); //TODO: catch error

    },
    resetRelation : function(child,oldParent,newParent) {
        var self = this;
        new RelationTermModel({term1:oldParent, term2:child}).destroy({
            success : function (model, response) {
                console.log("destroy old relation");
                //create relation with new parent
                if(newParent!=null) {
                    self.addRelation(child,newParent);
                }
                else {
                    self.close();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                console.log("json.project="+json.errors);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    addRelation : function(child,newParent) {
        var self = this;
        new RelationTermModel({}).save({term1:newParent, term2:child},{
            success : function (model, response) {
                console.log("create new relation");
                self.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                console.log("json.project="+json.errors);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    close : function() {
        var self = this;
        this.ontologyPanel.refresh();
        console.log(self.ontologyDialog.length);
        self.$termDialog.dialog("close") ;
    }
});