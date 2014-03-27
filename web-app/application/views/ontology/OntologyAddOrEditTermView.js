var OntologyAddOrEditTermView = Backbone.View.extend({
    ontologyPanel: null, //Ontology panel in ontology view
    ontologyDialog: null, //dialog (add, edit)
    ontology: null,
    $tree: null,
    $panel: null,
    $textboxName: null,
    $colorChooser: null,
    $inputOldColor: null,
    $inputNewColor: null,
    $errorMessage: null,
    $errorLabel: null,
    $addFolderButton: null,
    action: null,
    events: {
        "click .addFolder": "addFolder"
    },
    initialize: function (options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        this.ontology = options.ontology;
        //_.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        console.log("OntologyAddOrEditTermView.render");
        require([
            "text!application/templates/ontology/OntologyAddOrEditTermView.tpl.html"
        ],
            function (ontologyAddOrEditTermViewTpl) {
                self.doLayout(ontologyAddOrEditTermViewTpl);
            });
        return this;
    },
    doLayout: function (ontologyAddOrEditTermViewTpl) {

        var self = this;
        if (self.model == null) {
            //dialog to add
            self.action = "Add";
            //create an empty model (not saved)
            self.createNewEmpty();
        }
        else {
            self.action = "Edit";
        } //dialog to edit

        //remove older dialog
        self.$termDialog = $(self.el).find("#dialog-" + self.action + "-ontology-term");

        $("#dialog-Edit-ontology-term").replaceWith("");
        $("#dialog-Add-ontology-term").replaceWith("");

        var dialog = _.template(ontologyAddOrEditTermViewTpl, {
            oldColor: self.model.get('color'),
            action: self.action
        });
        $(self.el).append(dialog);

        self.$panel = $(self.el);
        self.$termDialog = $(self.el).find("#dialog-" + self.action + "-ontology-term");
        self.$tree = self.$panel.find("#" + self.action + "ontologytermtree");
        self.$from = self.$panel.find($("#form-" + self.action + "-ontology-term"));
        self.$textboxName = self.$panel.find("#" + self.action + "termname");

        self.$colorChooser = self.$panel.find('#colorpicker1AddOrEditTerm');
        self.$inputOldColor = self.$panel.find('#oldColorAddOrEditTerm');
        self.$inputNewColor = self.$panel.find('#color1AddOrEditTerm');

        self.$addFolderButton = self.$panel.find('.addFolder');

        self.$errorMessage = self.$panel.find("#" + self.action + "ontologytermerrormessage");
        self.$errorLabel = self.$panel.find("#" + self.action + "ontologytermerrorlabel");

        self.$from.submit(function () {
            if (self.action == "Edit") {
                self.updatedOntologyTerm();
            }
            else {
                self.createOntologyTerm();
            }
            return false;
        });
        self.$from.find("input").keydown(function (e) {
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
        self.ontologyDialog = $(self.$termDialog).modal({
            keyboard: true,
            backdrop: true
        });
        $("#AddOrEditTermButton").click(function (event) {
            event.preventDefault();
            self.$from.submit();
            return false;
        });
        $("#closeAddOrEditTermDialog").click(function (event) {
            event.preventDefault();
            self.$termDialog.modal('hide');
            self.$termDialog.remove();
            return false;
        });
        self.open();
        return this;
    },
    /**
     * Create new empty model with default value
     */
    createNewEmpty: function () {
        var self = this;
        self.model = new TermModel({id: -1, name: "", color: "#ff0000"});
    },

    buildNameInfo: function () {
        var self = this;
        self.$textboxName.bind('keyup mouseup change click', function (e) {
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
            var color = "#119b04";
            var htmlNode = "<span style='color:<%=   color %>'><%=   title %></span>";
            var nodeTpl = _.template(htmlNode, {title: self.$textboxName.val(), color: color});
            if (node != null) {
                node.setTitle(nodeTpl);
            }
        });
        self.$textboxName.val(self.model.get("name"));

    },

    buildColorInfo: function () {
        var self = this;
        var newColorField = $('#color1AddOrEditTerm');
        newColorField.pickAColor();
        var color = self.model.get('color');
        self.$inputOldColor.val(color.replace("#",""));
        self.$inputNewColor.val(color.replace("#",""));
        self.$inputOldColor.css("background", color);
        self.$inputNewColor.css("background", color);
        $("#color1AddOrEditTerm").on("change", function () {
            self.$inputNewColor.css("background", self.getNewColor());
        });
    },
    addFolder: function () {
        //create a new node as a folder

        //automatically put the new term node under this folder

        //MOVE IN ADD/UPDATED when you save it, check if the parent node is this new folder, if its true save the relation

    },

    buildParentInfo: function () {
        var self = this;
        self.$addFolderButton.button({
            icons: {secondary: "ui-icon-folder-collapsed" }
        });

        self.$tree.empty();
        self.$tree.dynatree({
            children: self.ontology.toJSON(),
            onExpand: function () {
                self.$tree.find("a").attr( "href" ,window.location.hash);
            },
            onRender: function (node, nodeSpan) {
                self.$tree.find("a.dynatree-title").css("color", "black");
            },
            onClick: function (node, event) {
            },
            onSelect: function (select, node) {
            },
            onCustomRender: function (node) {
            },
            onDblClick: function (node, event) {
            },
            dnd: {
                onDragStart: function (node) {
                    /** This function MUST be defined to enable dragging for the tree.
                     *  Return false to cancel dragging of node.
                     */
                    if (node.data.key != self.model.id) {
                        return false;
                    }
                    return true;
                },
                onDragStop: function (node) {
                },
                autoExpandMS: 1000,
                preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                onDragEnter: function (node, sourceNode) {
                    return true;
                },
                onDragOver: function (node, sourceNode, hitMode) {
                },
                onDragLeave: function (node, sourceNode) {
                },
                onDrop: function (node, sourceNode, hitMode, ui, draggable) {
                    if (hitMode == "over") {
                        sourceNode.move(node, hitMode);
                        node.data.isFolder = true;
                        node.render();
                    }
                    else {
                        sourceNode.move(node, hitMode);
                    }
                }
            },
            generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "" + self.action + "treeDataOntology-" + self.model.id,
            cookieId: "" + self.action + "dynatree-Ontology-" + self.model.id,
            idPrefix: "" + self.action + "dynatree-Ontology-" + self.model.id + "-",
            debugLevel: 0
        });

        //if add panel, add the "temp" model to the tree (event if it's not yet a part of the ontology)
        if (self.action == "Add") {
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.ontology.id);
            var childNode = node.addChild({
                title: "",
                key: -1,
                tooltip: "This folder and all child nodes were added programmatically.",
                isFolder: false
            });
        }

        //expand all nodes
        self.$tree.dynatree("getRoot").visit(function (node) {
            node.expand(true);
        });

        if (self.action == "Edit") {
            self.$textboxName.click();
        }




    },

    getNewName: function () {
        var self = this;
        return self.$textboxName.val();
    },
    getNewParent: function () {
        var self = this;
        var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
        return node.parent.data.id;
    },
    getNewColor: function () {
        return  "#" + this.$inputNewColor.val();
    },
    refresh: function () {

    },
    open: function () {
        var self = this;
        self.clearOntologyTermPanel();
    },
    clearOntologyTermPanel: function () {
        var self = this;

        self.$errorMessage.empty();
        self.$errorLabel.hide();
    },
    updatedOntologyTerm: function () {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();
        var id = self.model.id;
        var name = self.getNewName();
        var idOldParent = self.model.get("parent");
        var idParent = self.getNewParent();
        var isOldParentOntology = (idOldParent == null || self.ontology.id == idOldParent);
        var isParentOntology = (self.ontology.id == idParent);
        var color = self.getNewColor();
        self.model.set({name: name, color: color});
        self.model.save({name: name, color: color}, {
            success: function (model, response) {
                //TODO: check it relation/term is changed
                if ((idParent != idOldParent) && !(isOldParentOntology && isParentOntology)) {
                    if (isOldParentOntology) {
                        //parent was ontology so nothing to delete
                        console.log("//parent was ontology so nothing to delete");
                        self.addRelation(id, idParent);
                    }
                    else if (isParentOntology) {
                        //new parent is ontology so nothing to add
                        console.log("//new parent is ontology so nothing to add");
                        self.resetRelation(id, idOldParent, null);
                    } else {
                        //resetRelation
                        console.log("resetRelation");
                        self.resetRelation(id, idOldParent, idParent);
                    }
                }
                else {
                    self.close();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Term", json.errors, "error");
            }
        }); //TODO: catch error
    },

    createOntologyTerm: function () {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();
        console.log("createOntologyTerm");
        var id = self.model.id;
        var name = self.getNewName();
        var isParentOntology = true;
        var idParent = self.getNewParent();
        console.log("idParent=" + idParent);
        console.log("self.ontology.id=" + self.ontology.id);
        if (self.ontology.id != idParent) {
            isParentOntology = false;
        }
        var color = self.getNewColor();
        self.model.set({name: name, color: color});
        self.model = new TermModel({name: name, color: color, ontology: self.ontology.id}).save({name: name, color: color, ontology: self.ontology.id}, {
            success: function (model, response) {
                console.log("createOntologyTerm.success");
                //TODO: check success relation/term is changed
                if (isParentOntology) {
                    //no link "parent" with a term
                    window.app.view.message("Term", response.message, "success");
                    self.close();
                }
                else {
                    self.addRelation(response.term.id, idParent);
                }
            },
            error: function (model, response) {
                console.log("createOntologyTerm.error");
                var json = $.parseJSON(response.responseText);
//                self.$errorLabel.show();
//                self.$errorMessage.append(json.errors);
                window.app.view.message("Term", json.errors, "error");
            }
        }); //TODO: catch error

    },
    resetRelation: function (child, oldParent, newParent) {
        console.log("reset relation");
        var self = this;
        console.log("resetRelation1");
        //use fake ID since backbone > 0.5 : we should destroy only object saved or fetched
        new RelationTermModel({id: 1, term1: oldParent, term2: child}).destroy({
            success: function (model, response) {
                console.log("resetRelation2");
                //create relation with new parent
                if (newParent != null) {
                    self.addRelation(child, newParent);
                }
                else {
                    window.app.view.message("Term", response.message, "success");
                    self.close();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    addRelation: function (child, newParent) {
        console.log("add relation");
        var self = this;
        new RelationTermModel({}).save({term1: newParent, term2: child}, {
            success: function (model, response) {
                console.log("add relation2");
                window.app.view.message("Term", response.message, "success");
                self.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    close: function () {
        var self = this;
        this.ontologyPanel.refresh();
        self.$termDialog.modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();

    }
});