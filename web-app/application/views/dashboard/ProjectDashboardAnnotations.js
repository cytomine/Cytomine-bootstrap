/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ProjectDashboardAnnotations = Backbone.View.extend({
    tabsAnnotation: null,
    annotationsViews: [], //array of annotation view
    selectedTerm: [],
    selectedUsers: [],
    selectedImages: [],
    selectedJobs: [],
    allImages: 0,
    allUsers: 0,
    terms: null,
    ontology: null,
    shouldRefresh: true,
    render: function (callback) {
        console.log("ProjectDashboardAnnotations.selectedUsers=" + this.selectedUsers);
        console.log("ProjectDashboardAnnotations.selectedJobs=" + this.selectedJobs);
        var self = this;
        //$(self.el).empty();
        new UserJobCollection({project: window.app.status.currentProject, tree: true}).fetch({
            success: function (collection, response) {
                window.app.models.projectUserJobTree = collection;
                require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function (termTabTpl, termTabContentTpl) {
                    self.doLayout(termTabTpl, termTabContentTpl, callback);
                });
            }
        });
    },
    doLayout: function (termTabTpl, termTabContentTpl, callback) {
        var self = this;
        this.jobTreeLoaded = false;
        this.userTreeLoaded = false;

        self.ontology = window.app.status.currentOntologyModel;

        $(self.el).find("input.allAnnotationsCheckbox,input.onlyReviewedAnnotationsCheckbox").change(function () {
            self.refreshSelectedTermsWithUserFilter();
        });


        $(self.el).find("input.undefinedAnnotationsCheckbox").change(function () {
            if ($(this).is(':checked')) {
                self.refreshAnnotations(-1, self.selectedUsers, self.selectedJobs, self.selectedImages);
                self.selectedTerm.push(-1);
                $("#tabsterm-panel-" + self.model.id + "--1").show();
            } else {
                $("#tabsterm-panel-" + self.model.id + "--1").hide();
                self.selectedTerm = _.without(self.selectedTerm, -1);
            }
            self.updateContentVisibility();
            self.updateDownloadLinks();
        });
        $(self.el).find("input.multipleAnnotationsCheckbox").change(function () {
            if ($(this).is(':checked')) {
                self.refreshAnnotations(-2, self.selectedUsers, self.selectedJobs, self.selectedImages);
                self.selectedTerm.push(-2);
                $("#tabsterm-panel-" + self.model.id + "--2").show();
            } else {
                $("#tabsterm-panel-" + self.model.id + "--2").hide();
                self.selectedTerm = _.without(self.selectedTerm, -2);
            }
            self.updateContentVisibility();
            self.updateDownloadLinks();
        });
        $(self.el).find('#treeAnnotationListing').dynatree({
            checkbox: true,
            selectMode: 2,
            expand: true,
            onExpand: function () {
            },
            children: window.app.status.currentOntologyModel.toJSON(),
            onSelect: function (select, node) {
                //if(!self.activeEvent) return;
                console.log("On select!!!!!");
                if (node.isSelected()) {
                    self.selectedTerm.push(node.data.key);
                    console.log("refresh annotation for term "+node.data.key);
                    self.refreshAnnotations(node.data.key, self.selectedUsers, self.selectedJobs, self.selectedImages);
                    $("#tabsterm-panel-" + self.model.id + "-" + node.data.key).show();

                }
                else {
                    $("#tabsterm-panel-" + self.model.id + "-" + node.data.key).hide();
                    self.selectedTerm = _.without(self.selectedTerm, node.data.key);
                }
                self.updateContentVisibility();
                self.updateDownloadLinks();
            },
            onDblClick: function (node, event) {
                //node.toggleSelect();
            },

            // The following options are only required, if we have more than one tree on one page:
            initId: "treeData-annotations-" + self.model.get('ontology'),
            cookieId: "dynatree-Cb-annotations-" + self.model.get('ontology'),
            idPrefix: "dynatree-Cb-annotations-" + self.model.get('ontology') + "-"
        });
        //expand all nodes
        $(self.el).find('#treeAnnotationListing').dynatree("getRoot").visit(function (node) {
            node.expand(true);
            if (!node.hasChildren()) {
                $(node.span).attr("data-term", node.data.key);
                $(node.span).attr("class", "droppableElem");
            }
        });
        //$("#ontology-annotations-panel-"+self.model.id).panel();

        self.initSelectUser();

        self.initSelectJobs();
        self.initAnnotationsFilter();
        $(self.el).find("#uncheckAllTerms").click(function () {
            self.hideAllTerms();
        });
        $(self.el).find("#checkAllTerms").click(function () {
            self.showAllTerms();
        });
        $(self.el).find("#checkAllUsers").click(function () {
            self.showAllUsers(true);
        });
        $(self.el).find("#uncheckAllUsers").click(function () {
            self.hideAllUsers(true);
        });
        $(self.el).find("#checkAllImages").click(function () {
            self.showAllImages(true);
        });
        $(self.el).find("#uncheckAllImages").click(function () {
            self.hideAllImages(true);
        });
        $(self.el).find("#refreshAnnotations").click(function () {
            self.refreshSelectedTermsWithUserFilter();
        });
        $(self.el).find("#refreshFullPage").click(function () {
            self.hideAllJobs(false);
            new UserJobCollection({project: window.app.status.currentProject, tree: true}).fetch({
                success: function (collection, response) {
                    window.app.models.projectUserJobTree = collection;
                        $(self.el).find("#treeJobListing").dynatree("option", "children",window.app.models.projectUserJobTree.toJSON());
                        $(self.el).find('#treeJobListing').dynatree("getTree").reload();
                        var rootNode = $("#treeJobListing").dynatree("getTree").getNodeByKey(self.model.id);
                        if(rootNode) rootNode.expand(true);
                }
            });
        });
        self.terms = new TermCollection({idOntology: self.model.get('ontology')}).fetch({
            success: function (collection, response) {
                self.terms = collection;

                window.app.status.currentTermsCollection = collection;
                $("#listtabannotation").append(_.template(termTabContentTpl, { project: self.model.id, id: -1, name: "Undefined", className: "noDropZone"}));
                //$("#tabsterm-panel-"+self.model.id+"--1").panel();
                $("#tabsterm-panel-" + self.model.id + "--1").hide();
                $("#listtabannotation").append(_.template(termTabContentTpl, { project: self.model.id, id: -2, name: "Multiple", className: "noDropZone"}));
                //$("#tabsterm-panel-"+self.model.id+"--2").panel();
                $("#tabsterm-panel-" + self.model.id + "--2").hide();
                collection.each(function (term) {
                    //add x term tab
                    $("#listtabannotation").append(_.template(termTabContentTpl, { project: self.model.id, id: term.get("id"), name: term.get("name"), className: "droppableElem"}));
                    //$("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
                    $("#tabsterm-panel-" + self.model.id + "-" + term.get("id")).hide();
                });
                self.initDropZone();
                self.loadingCallBack(callback);
            }});

        new ImageInstanceCollection({project: window.app.status.currentProject, tree: true}).fetch({
            success: function (collection, response) {
                $(self.el).find('#treeImageListing').dynatree({
                    checkbox: true,
                    selectMode: 2,
                    expand: true,
                    onExpand: function () {
                    },
                    children: collection.toJSON(),
                    onSelect: function (select, node) {
                        //if(!self.activeEvent) return;
                        if (node.isSelected()) {
                            self.selectedImages.push(node.data.key);
                            $("#tabsterm-panel-" + self.model.id + "-" + node.data.key).show();
                        }
                        else {
                            $("#tabsterm-panel-" + self.model.id + "-" + node.data.key).hide();
                            self.selectedImages = _.without(self.selectedImages, node.data.key);
                        }
                        if (self.shouldRefresh) {
                            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
                        }
                    },
                    onDblClick: function (node, event) {
                        //node.toggleSelect();
                    },

                    // The following options are only required, if we have more than one tree on one page:
                    initId: "treeData-images-" + self.model.get('ontology'),
                    cookieId: "dynatree-Cb-images-" + self.model.get('ontology'),
                    idPrefix: "dynatree-Cb-images-" + self.model.get('ontology') + "-"
                });
                self.showAllImages();
                self.loadingCallBack(callback);
            }
        });


    },
    loadingCallBack: function (callback) {
        if (this.callbackNbre == undefined) {
            this.callbackNbre = 0;
        }
        this.callbackNbre++;

        if (this.callbackNbre == 2) {
            callback.call();
        }
    },
    initAnnotationsFilter: function () {
        var self = this;
        var select = $(this.el).find("#annotationFilterSelect");
        var annotationFilterCollection = undefined;
        var refreshSelect = function () {
            select.empty();
            select.attr("disabled", "disabled");
            new AnnotationFilterCollection({project: self.model.id}).fetch({
                success: function (collection, response) {
                    annotationFilterCollection = collection;
                    if (_.size(collection) > 0) {
                        $(select).removeAttr("disabled");
                    }
                    collection.each(function (annotationFilter) {
                        var optionFilterTpl = "<option value='<%= id %>'><%= name %></option>";
                        var optionFilter = _.template(optionFilterTpl, annotationFilter.toJSON());
                        select.append(optionFilter);
                    });
                },
                error: function (collection, response) {

                }
            });
        };
        refreshSelect();

        /* Select Annotation Filter */
        var selectButton = $(this.el).find("#selectAnnotationFilter");
        selectButton.click(function () {
            var idAnnotationFilter = select.val();
            if (!idAnnotationFilter) {
                return;
            }
            var annotationFilter = annotationFilterCollection.get(idAnnotationFilter);
            var url = "#tabs-annotations-" + self.model.get("id") + "-" + annotationFilter.get("terms") + "-" + annotationFilter.get("users");

            window.app.controllers.browse.tabs.triggerRoute = false;
            window.app.controllers.browse.navigate(url, true);
            window.app.controllers.browse.tabs.triggerRoute = true;
        });
        /* Save Annotation Filter */
        var saveButton = $(this.el).find("#saveAnnotationFilter");
        var confirmButton = $(this.el).find("#confirmAnnotationFilter");
        var cancelButton = $(this.el).find("#cancelAnnotationFilter");
        var showConfirm = function () {
            $("#liSaveAnnotationFilter").hide();
            $("#liConfirmAnnotationFilter").css("display", "inline");
            $("#inputAnnotationFilterName").focus();
            //$("#inputAnnotationFilterName").tooltip();
        }
        var hideConfirm = function () {
            $("#liSaveAnnotationFilter").css("display", "inline");
            $("#liConfirmAnnotationFilter").hide();
        }
        cancelButton.click(function () {
            hideConfirm();
            return;
        });
        saveButton.click(function () {
            showConfirm();
            return;
        });
        confirmButton.click(function () {
            var name = $("#inputAnnotationFilterName").val();
            if (name == "" || name == undefined) {
                window.app.view.message("Error", "You have to specify a identifier", "error");
                return;
            }
            new AnnotationFilterModel().save(
                {
                    name: name,
                    terms: self.selectedTerm,
                    users: self.selectedUsers,
                    project: self.model.id
                },
                {
                    success: function (model, response) {
                        window.app.view.message("Success", response.message, "success");
                        hideConfirm();
                        refreshSelect();
                    },
                    error: function (model, response) {
                        window.app.view.message("Error", response.message, "error");
                    }
                });
        });
        /* Delete Annotation Filter */
        var deleteButton = $(this.el).find("#deleteAnnotationFilter");
        deleteButton.click(function () {
            var idAnnotationFilter = select.val();
            if (!idAnnotationFilter) {
                return;
            }
            new AnnotationFilterModel({id: idAnnotationFilter}).destroy({
                success: function (model, response) {
                    window.app.view.message("Success", response.message, "success");
                    refreshSelect();
                },
                error: function (model, response) {
                    window.app.view.message("Error", response.message, "error");
                }
            });

        });
    },
    checkTermsAndUsers: function (terms, users) {
        var _terms = (terms != "" && terms != undefined);
        var _users = (users != "" && users != undefined);
        if (!_users && !_terms) {
            return;
        }
        this.hideAllTerms();
        this.hideAllUsers();
        //this.hideAllImages();
        if (terms == "all") {
            this.showAllTerms();
        } else if (terms != "" && terms != undefined) {
            this.selectTerms(terms);
        }
        if (users == "all") {
            this.showAllUsers();
        } else if (users != "" && users != undefined) {
            this.selectUsers(users);
        }
    },
    showAllTerms: function () {
        $(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(true);
    },
    hideAllTerms: function () {
        $(this.el).find("input.undefinedAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(false);
    },
    showAllImages: function (triggerRefresh) {
        var self = this;
        self.allImages = 0;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        $(this.el).find('#treeImageListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) {
                self.allImages++;
                node.select(true);
            }
        });
        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    hideAllImages: function (triggerRefresh) {
        var self = this;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        $(this.el).find('#treeImageListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) {
                node.select(false);
            }
        });
        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    showAllUsers: function (triggerRefresh) {
        var self = this;
        self.allUsers = 0;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        $(this.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) {
                self.allUsers++;
                node.select(true);
            }
        });
        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    hideAllUsers: function (triggerRefresh) {
        var self = this;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        if (this.userTreeLoaded) {
            $(this.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
                if (!node.data.isFolder) {
                    node.select(false);
                }
            });
        }

        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    showAllJobs: function (triggerRefresh) {
        var self = this;
        self.allJobs = 0;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        $(this.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) {
                self.allJobs++;
                node.select(true);
            }
        });
        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    hideAllJobs: function (triggerRefresh) {
        var self = this;
        if (triggerRefresh) {
            this.shouldRefresh = false;
        }
        if (this.jobTreeLoaded) {
            $(this.el).find('#treeJobListing').dynatree("getRoot").visit(function (node) {
                if (!node.data.isFolder) {
                    node.select(false);
                }
            });
        }
        if (triggerRefresh) {
            this.shouldRefresh = true;
        }
        if (triggerRefresh) {
            self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
        }
    },
    initDropZone: function () {
        var self = this;
        var dropHandler = function (event, ui) {

            $(this).css("background-color", "");
            var annotation = $(ui.draggable).attr("data-annotation");
            var term = $(ui.draggable).attr("data-term");
            var newTerm = $(this).attr("data-term");
            if (term == newTerm) {
                return;
            }
            $(ui.draggable).hide();
            new AnnotationTermModel({term: newTerm, userannotation: annotation, clear: true}).save({}, {
                success: function (model, response) {
                    $("#tabsterm-" + self.model.id + "-" + newTerm).append($(ui.draggable));
                    setTimeout(function () {
                        $(ui.draggable).fadeIn('slow');
                    }, 1000);
                    window.app.view.message(response.message, null, "success");
                    //$(ui.draggable).remove();
                    //self.refreshSelectedTermsWithUserFilter();
                },
                error: function (model, response) {
                    $(ui.draggable).show();
                    window.app.view.message(response.message, null, "error");
                }
            });
        };
        $(".noDropZone").droppable({
            over: function (event, ui) {
                $(this).css("background-color", "red");
            },
            out: function () {
                $(this).css("background-color", "");
            },
            drop: function () {
                $(this).css("background-color", "");
            }
        });
        $(".droppableElem").droppable({
            over: function (event, ui) {
                $(this).css("background-color", "lightgreen");
            },
            out: function () {
                $(this).css("background-color", "");
            },
            drop: dropHandler
        });
    },
    initSelectUser: function () {
        var self = this;

        var treeData = {
            id: self.model.id,
            name: "Users",
            title: "Users",
            key: self.model.id,
            "hideCheckbox": true,
            isFolder: true,
            children: []
        };
        window.app.models.projectUser.each(function (user) {
            if (window.app.models.userLayer.get(user.id) == undefined) {
                return;
            }
            treeData.children.push({
                id: user.id,
                key: user.id,
                name: user.prettyName(),
                title: user.prettyName(),
                children: []
            });
        });
        $(self.el).find('#treeUserListing').dynatree({
            checkbox: true,
            selectMode: 2,
            expand: true,
            onExpand: function () {
            },
            children: treeData,
            onSelect: function (select, node) {
                //if(!self.activeEvent) return;
                if (node.isSelected()) {
                    self.hideAllJobs(false);
                    self.selectedUsers.push(node.data.key);
                }
                else {
                    self.selectedUsers = _.without(self.selectedUsers, node.data.key);
                }
                if (self.shouldRefresh) {
                    self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
                }
            },
            onDblClick: function (node, event) {
                //node.toggleSelect();
            },

            // The following options are only required, if we have more than one tree on one page:
            initId: "treeData-user-" + self.model.id,
            cookieId: "dynatree-Cb-user-" + self.model.id,
            idPrefix: "dynatree-Cb-user-" + self.model.id + "-"
        });
        self.userTreeLoaded = true;
        $(self.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
            node.expand(true);
        });
        $(self.el).find('#treeUserListing').dynatree("getTree").selectKey(window.app.status.user.id);

    },
    initSelectJobs: function () {
        var self = this;


        //$(self.el).find('#treeJobListing').empty();

        $(self.el).find('#treeJobListing').dynatree({
            checkbox: true,
            selectMode: 2,
            expand: true,
            onExpand: function () {
            },
            children: window.app.models.projectUserJobTree.toJSON(),
            onSelect: function (select, node) {
                //if(!self.activeEvent) return;
                if (node.isSelected()) {
                    //disable all node from user
                    self.hideAllUsers(false);
                    self.selectedJobs.push(node.data.key);
                }
                else {
                    self.selectedJobs = _.without(self.selectedJobs, node.data.key);
                }

                if (self.shouldRefresh) {
                    self.printAnnotationThumbAllTerms(self.selectedTerm, self.selectedUsers, self.selectedJobs, self.selectedImages);
                }
            },
            onDblClick: function (node, event) {
                //node.toggleSelect();
            },

            // The following options are only required, if we have more than one tree on one page:
            initId: "treeData-job-" + self.model.id,
            cookieId: "dynatree-Cb-job-" + self.model.id,
            idPrefix: "dynatree-Cbjobs-" + self.model.id + "-"
        });


        self.jobTreeLoaded = true;
        //expand root node
        var rootNode = $("#treeJobListing").dynatree("getTree").getNodeByKey(self.model.id);
        if(rootNode) rootNode.expand(true);
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addTermToTab: function (termTabTpl, termTabContentTpl, data) {
        //$("#ultabsannotation").append(_.template(termTabTpl, data));
        $("#listtabannotation").append(_.template(termTabContentTpl, data));

    },
    selectAnnotations: function (selected) {
        var self = this;
        this.terms.each(function (term) {
            $(self.el).find('#treeAnnotationListing').dynatree("getTree").selectKey(term.get("id"), selected);
        });
    },
    updateDownloadLinks: function () {
        var users = this.selectedUsers.join(",");
        var terms = this.selectedTerm.join(",");
        var images = this.selectedImages.join(",");
        var suffix = "&users=" + users + "&terms=" + terms + "&images=" + images + "&reviewed="+ ($('input[name=annotationClass]:checked').val() == "inReviewed");
        $("#downloadAnnotationsCSV").attr("href", "/api/project/" + this.model.id + "/annotation/download?format=csv" + suffix);
        $("#downloadAnnotationsExcel").attr("href", "/api/project/" + this.model.id + "/annotation/download?format=xls" + suffix);
        $("#downloadAnnotationsPDF").attr("href", "/api/project/" + this.model.id + "/annotation/download?format=pdf" + suffix);
    },
    updateContentVisibility: function () {
        var nbUserSelected = _.size(this.selectedUsers);
        var nbTermSelected = _.size(this.selectedTerm);
        var nbJobSelected = _.size(this.selectedJobs);
        var nbSelectedImages = _.size(this.selectedImages);
        nbTermSelected += ($(this.el).find("input.undefinedAnnotationsCheckbox").is(':checked')) ? 1 : 0;
        nbTermSelected += ($(this.el).find("input.multipleAnnotationsCheckbox").is(':checked')) ? 1 : 0;
        if (nbTermSelected > 0 && nbSelectedImages > 0 && (nbUserSelected > 0 || nbJobSelected > 0)) {
            $("#listtabannotation").show();
            $("#downloadAnnotation").show();
        } else {
            $("#listtabannotation").hide();
            $("#downloadAnnotation").hide();
        }
    },
    selectTerms: function (terms) {
        terms = terms.split(",");
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getTree");
        _.each(terms, function (term) {
            var node = tree.getNodeByKey(term);
            node.select(true);
        });

    },
    selectUsers: function (users) {
        users = users.split(",");
        var tree = $(this.el).find('#treeUserListing').dynatree("getTree");
        var user = false;
        _.each(users, function (user) {
            var node = tree.getNodeByKey(user);
            if (node != undefined) {
                user = true;
                node.select(true);
            }
        });
        if (!user) {
            var treeJob = $(this.el).find('#treeJobListing').dynatree("getTree");
            _.each(users, function (user) {
                var node = treeJob.getNodeByKey(user);
                if (node != undefined) {
                    node.select(true);
                    node.getParent().expand(true);
                }

            });
        }

    },
    refreshSelectedTermsWithUserFilter: function () {
        var self = this;
        var users = self.selectedUsers;
        var images = self.selectedImages;
        var jobs = self.selectedJobs;
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getRoot");
        if (!_.isFunction(tree.visit)) {
            return;
        } //tree is not yet loaded
        tree.visit(function (node) {
            if (!node.isSelected()) {
                return;
            }
            self.refreshAnnotations(node.data.key, users, jobs, images);
        });
        if ($(this.el).find("input.undefinedAnnotationsCheckbox").is(':checked')) {
            self.refreshAnnotations(-1, users, jobs, images);
        }
        if ($(this.el).find("input.multipleAnnotationsCheckbox").is(':checked')) {
            self.refreshAnnotations(-2, users, jobs, images);
        }
        self.updateContentVisibility();
        self.updateDownloadLinks();
    },
    /**
     * Refresh all annotation dor the given term
     * @param term annotation term to be refresh (all = 0)
     */
    refreshAnnotations: function (term, users, jobs, images) {
        console.log("refreshAnnotations");
        this.printAnnotationThumb(term, "#tabsterm-" + this.model.id + "-" + term, users, jobs, images);
    },
    clearAnnotations: function (term) {
        console.log("clearAnnotations");
        var self = this;
        $("#tabsterm-" + self.model.id + "-" + term).empty();
    },
    /**
     * Print annotation for the given term
     * @param term term annotation term to be refresh (all = 0)
     * @param $elem  elem that will keep all annotations
     */
    printAnnotationThumbAllTerms: function (terms, users, jobs, images) {
        var self = this;
        self.updateContentVisibility();
        self.updateDownloadLinks();
        if (_.size(users) == 0 && _.size(jobs) == 0) {
            return;
        } //nothing to display
        for (var i = 0; i < terms.length; i++) {
            self.printAnnotationThumb(terms[i], "#tabsterm-" + self.model.id + "-" + terms[i], users, jobs, images);
        }

    },
    printAnnotationThumb: function (idTerm, $elem, users, jobs, images) {
        var self = this;
        console.log("printAnnotationThumb");
        //TODO: problem avec $elem!


        var imagesFilter = undefined;
        //if all image are uncheck, just pass undefined
        if (self.allImages != self.selectedImages.length) {
            imagesFilter = images;
        }

        var usersFilter = undefined;
        //if all users are uncheck, just pass undefined
        if (self.allUsers != users.length) {
            usersFilter = users;
        }

        usersFilter = _.union(users, jobs);

        //print loading info
        $($elem).parent().find("h4").append('<div class="alert alert-info"><i class="icon-refresh" /> Loading...</div>');

        var reviewedRadioValue = $('input[name=annotationClass]:checked').val();
        var reviewed = reviewedRadioValue == "inReviewed"

        var noTerm = (idTerm == -1? true : undefined);
        var multipleTerm = (idTerm == -2? true: undefined);
        idTerm = (idTerm != -1 && idTerm != -2? idTerm: undefined);

        var collection = new AnnotationCollection({project: self.model.id, term: idTerm, noTerm:noTerm,multipleTerm:multipleTerm,users: (reviewed? undefined: usersFilter), reviewUsers:(reviewed? usersFilter:undefined), images: imagesFilter,reviewed:reviewed, max: 30});

        $($elem).empty();
        self.annotationsViews[idTerm] = new AnnotationView({
            page: undefined,
            model: collection,
            term: idTerm,
            el: $($elem),
            noTerm : noTerm,
            multipleTerm : multipleTerm
        }).render();
        $($elem).parent().find("h4").find(".alert").replaceWith("");

    }
});