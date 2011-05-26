var OntologyAddOrEditTermView = Backbone.View.extend({
    ontologyPanel : null, //Ontology panel in ontology view
    ontologyDialog : null, //dialog (add, edit)
    ontology : null,
    tree : null,
    action : null,
    initialize: function(options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        this.parents = options.parents;
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
            self.createNewEmpty();
        }
        else {
            //dialog to edit
            self.action = "Edit";
        }

        var color = self.model.get('color');
        if(!color.contains("#")) color = "#" + color;

        var dialog = _.template(ontologyAddOrEditTermViewTpl, {oldColor : color,action : self.action});
        $(self.el).append(dialog);
        self.tree = $("#" + self.action +"ontologytermtree");
        console.log(self.model.id + " " + self.model.get('name'));

        $("#form-" + self.action +"-ontology-term").submit(function () {
            console.log(self.action);
            if(self.action == "Edit")
                self.updatedOntologyTerm();
            else  self.createOntologyTerm();

            return false;});
        $("#form-" + self.action +"-ontology-term").find("input").keydown(function(e){
            if (e.keyCode == 13) { //ENTER_KEY
                $("#form-" + self.action +"-ontology-term").submit();
                return false;
            }
        });

        self.buildNameInfo();
        self.buildColorInfo();
        $("#" + self.action +"ontologytermtree").empty() ;
        self.buildParentInfo();

        //Build dialog
        console.log("OntologyTermDialog: build dialog:"+$("#dialog-" + self.action +"-ontology-term").length);
        self.ontologyDialog = $("#dialog-" + self.action +"-ontology-term").dialog({
            width: "1000",
            autoOpen : false,
            modal:true,
            buttons : {
                "Save" : function() {
                    $("#form-" + self.action +"-ontology-term").submit();
                },
                "Cancel" : function() {
                    $("#dialog-" + self.action +"-ontology-term").dialog("close");
                }
            }
        });

        self.open();
        return this;
    },
    createNewEmpty : function() {
        var self = this;
        console.log("createNewEmpty");
        self.model = new TermModel({id:-1,name:"",color:"#ff0000"});
    },

    buildNameInfo : function () {
        console.log("buildNameInfo");
        var self = this;
        $("#" + self.action +"termname").val(self.model.get("name"));


        $("#" + self.action +"termname").bind('keyup mouseup change',function(e){
            var node = self.tree.dynatree("getTree").getNodeByKey(self.model.id);
            var color = "#9ac400"
            var htmlNode = "<label style='color:{{color}}'>{{title}}</label>"
            var nodeTpl = _.template(htmlNode, {title : $("#" + self.action +"termname").val(), color : color});

            node.setTitle(nodeTpl);
        });
    },

    buildColorInfo : function() {
        console.log("buildColorInfo");
        var self = this;

        console.log("$('#colorpicker1')="+$('#colorpicker1').length  + " ");
        var colorPicker = $('#colorpicker1').farbtastic('#color1');
        console.log(colorPicker);

        //$('#colorpicker1').setColor(self.model.get('color'));
        var color = self.model.get('color');
        if(!color.contains("#"))
            color = "#" + color;

        $("#oldcolor").val(color);
        $("#color1").val(color);
        $("#oldcolor").css("background", color);
        $("#color1").css("background", color);
        $("#color1").css("color", $("#oldcolor").css("color"));

    },

    buildParentInfo : function() {
        console.log("buildParentInfo");
        var self = this;
        console.log("buildOntologyTree for ontology " + self.ontology.id);

        console.log(self.model.toJSON());
        console.log(self.tree.length);
        self.tree.dynatree({
            children: self.ontology.toJSON(),
            onExpand : function() { console.log("expanding/collapsing");},
            onClick: function(node, event) {
                console.log("onClick");
                // Display list of selected nodes
                console.log(node);
                var s = node.data.title;

            },
            onSelect: function(select, node) {
                console.log("onSelect");
                // Display list of selected nodes

            },
            onCustomRender: function(node) {

            },
            onDblClick: function(node, event) {
                console.log("Double click");
            },

            dnd: {
                onDragStart: function(node) {
                    /** This function MUST be defined to enable dragging for the tree.
                     *  Return false to cancel dragging of node.
                     */
                    logMsg("tree.onDragStart(%o)", node);
                    if(node.data.key!=self.model.id) return false;
                    return true;
                },
                onDragStop: function(node) {
                    // This function is optional.
                    logMsg("tree.onDragStop(%o)", node);
                },
                autoExpandMS: 1000,
                preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                onDragEnter: function(node, sourceNode) {
                    /** sourceNode may be null for non-dynatree droppables.
                     *  Return false to disallow dropping on node. In this case
                     *  onDragOver and onDragLeave are not called.
                     *  Return 'over', 'before, or 'after' to force a hitMode.
                     *  Return ['before', 'after'] to restrict available hitModes.
                     *  Any other return value will calc the hitMode from the cursor position.
                     */
                    logMsg("tree.onDragEnter(%o, %o)", node, sourceNode);
                    // Prevent dropping a parent below it's own child
//                if(node.isDescendantOf(sourceNode))
//                    return false;
                    // Prevent dropping a parent below another parent (only sort
                    // nodes under the same parent)
//                if(node.parent !== sourceNode.parent)
//                    return false;
//              if(node === sourceNode)
//                  return false;
                    // Don't allow dropping *over* a node (would create a child)
//        return ["before", "after"];
                    return true;
                },
                onDragOver: function(node, sourceNode, hitMode) {
                    /** Return false to disallow dropping this node.
                     *
                     */
                    logMsg("tree.onDragOver(%o, %o, %o)", node, sourceNode, hitMode);
                    // Prohibit creating childs in non-folders (only sorting allowed)

                    /*if( !node.isFolder && hitMode == "over" )
                     return "after"; */
                },
                onDrop: function(node, sourceNode, hitMode, ui, draggable) {
                    /** This function MUST be defined to enable dropping of items on
                     * the tree.
                     */
                    console.log(node);
                    console.log(sourceNode);
                    console.log(hitMode);
                    logMsg("tree.onDrop(%o, %o, %s)", node, sourceNode, hitMode);
                    if(!node.data.isFolder && hitMode=="over")
                    {
                        console.log("NOT A FOLDER");
                    }
                    else sourceNode.move(node, hitMode);
                    // expand the drop target
//        sourceNode.expand(true);
                },
                onDragLeave: function(node, sourceNode) {
                    /** Always called if onDragEnter was called.
                     */
                    logMsg("tree.onDragLeave(%o, %o)", node, sourceNode);
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
        self.tree.dynatree("getRoot").visit(function(node){
            console.log("node="+node.data.id + " " + node.data.key + " " + node.data.title);
            node.expand(true);
        });
        if(self.action=="Add") {
            var node = self.tree.dynatree("getTree").getNodeByKey(self.ontology.id);
            var childNode = node.addChild({
                title: "",
                key : -1,
                tooltip: "This folder and all child nodes were added programmatically.",
                isFolder: false
            });
        }





        console.log("self.model.id="+self.model.id);
        var node = self.tree.dynatree("getTree").getNodeByKey(self.model.id);
        var title = node.data.title
        var color = "#9ac400"
        var htmlNode = "<label style='color:{{color}}'>{{title}}</label>"
        var nodeTpl = _.template(htmlNode, {title : title, color : color});

        node.setTitle(nodeTpl);

    },

    getNewName : function() {
        var self = this;
        console.log("getNewName size:"+$("#" + self.action +"termname").length);
        console.log("getNewName: "+$("#" + self.action +"termname").val);
        return $("#" + self.action +"termname").val();
    },
    getNewParent : function() {
        var self = this;
        var node = self.tree.dynatree("getTree").getNodeByKey(self.model.id);
        console.log(node);
        return node.parent.data.id;
    },
    getNewColor : function() {
        return  $("#color1").val();
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
        $("#" + self.action +"ontologytermerrormessage").empty();
        $("#" + self.action +"ontologytermerrorlabel").hide();


    },
    updatedOntologyTerm : function() {
        console.log("updatedOntologyTerm...");
        var self = this;

        $("#" + self.action +"ontologytermerrormessage").empty();
        $("#" + self.action +"ontologytermerrorlabel").hide();
        var id = self.model.id;
        var name =  self.getNewName();
        var idOldParent = self.model.get("parent");
        var idParent = self.getNewParent();
        var isOldParentOntology = true;
        if(idOldParent!=null && window.app.models.ontologies.get(idOldParent)==undefined) {
            isOldParentOntology = false;
        }
        var isParentOntology = true;
        console.log(window.app.models.ontologies.get(idParent));
        console.log(window.app.models.ontologies.get(idParent)==undefined);
        if(window.app.models.ontologies.get(idParent)==undefined) {
            isParentOntology = false;
        }
        console.log("isOldParentOntology="+isOldParentOntology + " isParentOntology=" + isParentOntology);
        var color = self.getNewColor();
        console.log("update term "+ name + " with parent=" + idParent + "(old parent =" + idOldParent+ ") and color="+color);

        console.log(window.app.models.ontologies.get(47));
        console.log(window.app.models.ontologies.get(470));

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
                $("#" + self.action +"ontologytermerrorlabel").show();
                $("#" + self.action +"ontologytermerrormessage").append(json.errors);
            }
        } ); //TODO: catch error
    },

    createOntologyTerm : function() {
        console.log("createOntologyTerm...");

        var self = this;
        $("#" + self.action +"ontologytermerrormessage").empty();
        $("#" + self.action +"ontologytermerrorlabel").hide();
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
                $("#" + self.action +"ontologytermerrorlabel").show();
                $("#" + self.action +"ontologytermerrormessage").append(json.errors);
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
                $("#" + self.action +"ontologytermerrorlabel").show();
                $("#" + self.action +"ontologytermerrormessage").append(json.errors);
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
                $("#" + self.action +"ontologytermerrorlabel").show();
                ("#" + self.action +"ontologytermerrormessage").append(json.errors);
            }});
    },
    close : function() {
        console.log("refresh");
        this.ontologyPanel.refresh();
        $("#dialog-" + self.action +"-ontology-term").dialog("close") ;
    }
});