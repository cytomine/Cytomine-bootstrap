var OntologyPanelView = Backbone.View.extend({
    $tree: null,
    $infoOntology: null,
    $infoTerm: null,
    $panel: null,
    $addTerm: null,
    $editTerm: null,
    $deleteTerm: null,

    initialize: function (options) {
        this.container = options.container;
    },
    initEvents: function () {
        var self = this;
        console.log("initEvents:" + self.model.id);
        $("#buttonAddTerm" + this.model.id).click(function () {
            self.addTerm();
        });
        $("#buttonEditTerm" + this.model.id).click(function () {
            self.editTerm();
        });
        $("#buttonDeleteTerm" + this.model.id).click(function () {
            self.deleteTerm();
        });
        $("#buttonEditOntology" + this.model.id).click(function () {
            self.editOntology();
        });
        $("#buttonDeleteOntology" + this.model.id).click(function () {
            self.deleteOntology();
        });
    },
    refresh: function () {
        var self = this;
        self.container.refresh(self.model.id);
    },

    render : function () {
        var self = this;
        $(self.el).empty();
        require([
            "text!application/templates/ontology/OntologyTabContent.tpl.html"
        ],
            function (tpl) {
                $(self.el).html(_.template(tpl, { id: self.model.get("id"), name: self.model.get("name"), projects : "", users : "", creator : ""}));

                self.$panel = $(".ontology" + self.model.id);
                self.$tree = self.$panel.find("#treeontology-" + self.model.id);
                self.$infoOntology = self.$panel.find("#infoontology-" + self.model.id);
                self.$infoTerm = self.$panel.find("#infoterm-" + self.model.id);

                self.$addTerm = self.$panel.find('#dialog-add-ontology-term');
                self.$editTerm = self.$panel.find('#dialog-edit-ontology-term');
                self.$deleteTerm = self.$panel.find('#dialogsTerm');

                //defered
                self.printCreator();
                self.printUsers();
                self.printProjects();
                self.buildOntologyTree();

                self.initEvents();
            });

        return this;
    },

    getCurrentTermId: function () {
        var node = this.$tree.dynatree("getActiveNode");
        if (node == null) {
            return null;
        }
        else {
            return node.data.id;
        }
    },

    addTerm: function () {
        var self = this;
        self.$addTerm.remove();

        new OntologyAddOrEditTermView({
            ontologyPanel: self,
            el: self.el,
            ontology: self.model,
            model: null //add component so no term
        }).render();
    },

    editTerm: function () {
        var self = this;
        console.log("OntologyPanelView.editTerm");
        self.$editTerm.remove();

        var node = self.$tree.dynatree("getActiveNode");

        if (node == null) {
            window.app.view.message("Term", "You have to select a term first", "error");
            return;
        }

        new TermModel({id: node.data.id}).fetch({
            success: function (model, response) {
                new OntologyAddOrEditTermView({
                    ontologyPanel: self,
                    el: self.el,
                    model: model,
                    ontology: self.model
                }).render();
            }});
        return false;
    },
    deleteTerm: function () {
        var self = this;
        var idTerm = self.getCurrentTermId();
        var term = window.app.models.terms.get(idTerm);
        self.buildDeleteTermConfirmDialog(term);
    },
    editOntology: function () {
        var self = this;
        $('#editontology').remove();
        self.editOntologyDialog = new EditOntologyDialog({ontologyPanel: self, el: self.el, model: self.model}).render();
    },
    deleteOntology: function () {
        var self = this;
        require(["text!application/templates/ontology/OntologyDeleteConfirmDialog.tpl.html"], function (tpl) {
            new ConfirmDialogView({
                el: '#dialogsDeleteOntologyAccept',
                template: _.template(tpl, {ontology: self.model.get('name')}),
                dialogAttr: {
                    backdrop: false,
                    dialogID: '#delete-ontology-confirm'
                }
            }).render();

            $('#deleteOntologyButton').click(function (event) {
                event.preventDefault();
                new TaskModel({project: self.model.id}).save({}, {
                        success: function (taskResponse, response) {
                            var task = taskResponse.get('task');

                            console.log("task"+task.id);
                            var timer = window.app.view.printTaskEvolution(task, $("#deleteOntologyDialogContent"), 1000);


                            new OntologyModel({id: self.model.id,task: task.id}).destroy(
                                {
                                    success: function (model, response) {
                                        window.app.view.message("Project", response.message, "success");
                                        self.container.refresh(null)
                                        clearInterval(timer);
                                        $('#delete-ontology-confirm').modal('hide').remove();
                                        $('body').removeClass('modal-open');
                                        $('.modal-backdrop').remove();
                                    },
                                    error: function (model, response) {
                                        window.app.view.message("Ontology", "Errors!", "error");
                                        clearInterval(timer);
                                        var json = $.parseJSON(response.responseText);
                                        window.app.view.message("Ontology", json.errors[0], "error");
                                    }
                                }
                            );
                            return false;
                        },
                        error: function (model, response) {
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Task", json.errors, "error");
                        }
                    }
                );
                return false;
            });
        });
    },
    selectTerm: function (idTerm) {
        var self = this;
        self.$tree.dynatree("getTree").activateKey(idTerm);
    },
    buildDeleteTermConfirmDialog: function (term) {
        var self = this;
        require(["text!application/templates/ontology/OntologyDeleteTermConfirmDialog.tpl.html"], function (tpl) {
            var dialog = new ConfirmDialogView({
                el: '#dialogsTerm',
                template: _.template(tpl, {term: term.get('name'), ontology: self.model.get('name')}),
                dialogAttr: {
                    backdrop: true,
                    dialogID: '#delete-term-confirm'
                }
            }).render();

            $('#deleteTermButton').click(function (event) {
                event.preventDefault();
                self.removeTerm(term, dialog);
                return false;
            });
        });
    },
    /**
     * Delete a term which can have relation but no annotation
     * @param term term that must be deleted
     */
    removeTerm: function (term, dialog) {
        var self = this;
        new TermModel({id: term.id}).destroy({
            success: function (model, response) {
                window.app.view.message("Term", response.message, "success");
                self.refresh();
                dialog.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                $("#delete-term-error-message").empty();
                $("#delete-term-error-label").show();
                $("#delete-term-error-message").append(json.errors)
            }});
    },
    printCreator: function () {
        var self = this;
        new UserCollection({ontology: self.model.id, creator: true}).fetch({
            success: function (creator, response) {
                $("#ontologyCreator").empty();
                creator.each(function (user) {
                    $("#ontologyCreator").append(user.prettyName());
                });
            }});
    },
    printUsers: function () {
        var self = this;
        new UserCollection({ontology: self.model.id}).fetch({
            success: function (users, response) {
                var usernames = []
                users.each(function (user) {
                    usernames.push(user.prettyName());
                });
                $("#ontologyUsers").html(usernames.join(", "));
            }});
    },
    printProjects : function() {
        var projectsLinked = [];
        _.each(this.model.get("projects"), function (project) {
            var tpl = _.template("<a href='#tabs-dashboard-<%=   idProject %>'><%=   projectName %></a>", {idProject: project.id, projectName: project.name});
            projectsLinked.push(tpl);
        });
        $("#projectsLinked").html(projectsLinked.join(", "));
    },
    buildOntologyTree: function () {
        var self = this;
        var currentTime = new Date();

        self.$tree.empty();
        $("#treeontology-" + self.model.id).dynatree({
            children: self.model.toJSON(),
            onExpand: function () {
            },
            onClick: function (node, event) {
            },
            onSelect: function (select, node) {
            },
            onActivate: function (node) {
            },
            onDblClick: function (node, event) {
            },
            onRender: function (node, nodeSpan) {
                self.$tree.find("a.dynatree-title").css("color", "black");
            },
            //generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "treeDataOntology-" + self.model.id + currentTime.getTime(),
            cookieId: "dynatree-Ontology-" + self.model.id + currentTime.getTime(),
            idPrefix: "dynatree-Ontology-" + self.model.id + currentTime.getTime() + "-",
            debugLevel: 0
        });

        self.colorizeOntologyTree();
        self.expandOntologyTree();
    },
    colorizeOntologyTree: function () {
        var self = this;
        $("#treeontology-" + self.model.id).dynatree("getRoot").visit(function (node) {
            if (node.children != null) {
                return;
            } //title is ok
            var title = node.data.title
            var color = node.data.color
            var htmlNode = "<a href='#ontology/<%=   idOntology %>/<%=   idTerm %>' onClick='window.location.href = this.href;'><%=   title %> <span style='background-color:<%= color %>'>&nbsp;&nbsp;&nbsp;&nbsp;</span></a>";
            var nodeTpl = _.template(htmlNode, {idOntology: self.model.id, idTerm: node.data.id, title: title, color: color});
            node.setTitle(nodeTpl);
        });
    },
    expandOntologyTree: function () {
        var self = this;
        //expand all nodes
        $("#treeontology-" + self.model.id).dynatree("getRoot").visit(function (node) {
            node.expand(true);
        });
    }
});