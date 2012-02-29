var ProjectDashboardAnnotations = Backbone.View.extend({
    tabsAnnotation : null,
    annotationsViews : [], //array of annotation view
    selectedTerm: [],
    selectedUsers: [],
    terms : null,
    ontology : null,
    shouldRefresh : true,
    render : function(callback) {
        var self = this;
        require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {
            self.doLayout(termTabTpl, termTabContentTpl, callback);
        });
    },
    doLayout : function (termTabTpl, termTabContentTpl, callback) {
        var self = this;
        new OntologyModel({id:self.model.get('ontology')}).fetch({
            success : function(model, response) {
                self.ontology = model;
                $(self.el).find("input.undefinedAnnotationsCheckbox").change(function(){
                    if ($(this).attr("checked") == "checked") {
                        self.refreshAnnotations(-1,self.selectedUsers);
                        self.selectedTerm.push(-1);
                        $("#tabsterm-panel-"+self.model.id+"--1").show();
                    } else {
                        $("#tabsterm-panel-"+self.model.id+"--1").hide();
                        self.selectedTerm = _.without(self.selectedTerm, -1);
                    }
                    self.updateContentVisibility();self.updateDownloadLinks();
                });
                $(self.el).find("input.multipleAnnotationsCheckbox").change(function(){
                    if ($(this).attr("checked") == "checked") {
                        self.refreshAnnotations(-2,self.selectedUsers);
                        self.selectedTerm.push(-2);
                        $("#tabsterm-panel-"+self.model.id+"--2").show();
                    } else {
                        $("#tabsterm-panel-"+self.model.id+"--2").hide();
                        self.selectedTerm = _.without(self.selectedTerm, -2);
                    }
                    self.updateContentVisibility();self.updateDownloadLinks();
                });
                $(self.el).find('#treeAnnotationListing').dynatree({
                    checkbox: true,
                    selectMode: 2,
                    expand : true,
                    onExpand : function() {},
                    children: model.toJSON(),
                    onSelect: function(select, node) {
                        //if(!self.activeEvent) return;
                        if (node.isSelected()) {
                            self.refreshAnnotations(node.data.key,self.selectedUsers);
                            $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).show();
                            self.selectedTerm.push(node.data.key);
                        }
                        else {
                            $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).hide();
                            self.selectedTerm = _.without(self.selectedTerm, node.data.key);
                        }
                        self.updateContentVisibility();self.updateDownloadLinks();
                    },
                    onDblClick: function(node, event) {
                        //node.toggleSelect();
                    },

                    // The following options are only required, if we have more than one tree on one page:
                    initId: "treeData-annotations-"+self.model.get('ontology'),
                    cookieId: "dynatree-Cb-annotations-"+self.model.get('ontology'),
                    idPrefix: "dynatree-Cb-annotations-"+self.model.get('ontology')+"-"
                });
                //expand all nodes
                $(self.el).find('#treeAnnotationListing').dynatree("getRoot").visit(function(node){
                    node.expand(true);
                    if(!node.hasChildren()) {
                        $(node.span).attr("data-term", node.data.key);
                        $(node.span).attr("class", "droppableElem");
                    }
                });
                //$("#ontology-annotations-panel-"+self.model.id).panel();

                self.initSelectUser();
                self.initAnnotationsFilter();
                $(self.el).find("#uncheckAllTerms").click(function(){
                    self.hideAllTerms();
                });
                $(self.el).find("#checkAllTerms").click(function(){
                    self.showAllTerms();
                });
                $(self.el).find("#checkAllUsers").click(function(){
                    self.showAllUsers();
                });
                $(self.el).find("#uncheckAllUsers").click(function(){
                    self.hideAllUsers();
                });
                $(self.el).find("#refreshAnnotations").click(function(){
                    self.refreshSelectedTermsWithUserFilter();
                });
                self.terms = new TermCollection({idOntology:self.model.get('ontology')}).fetch({
                    success : function (collection, response) {
                        self.terms = collection;
                        window.app.status.currentTermsCollection = collection;
                        $("#listtabannotation").append(_.template(termTabContentTpl, { project : self.model.id, id : -1, name : "Undefined", className : "noDropZone"}));
                        //$("#tabsterm-panel-"+self.model.id+"--1").panel();
                        $("#tabsterm-panel-"+self.model.id+"--1").hide();
                        $("#listtabannotation").append(_.template(termTabContentTpl, { project : self.model.id, id : -2, name : "Multiple", className : "noDropZone"}));
                        //$("#tabsterm-panel-"+self.model.id+"--2").panel();
                        $("#tabsterm-panel-"+self.model.id+"--2").hide();
                        collection.each(function(term) {
                            //add x term tab
                            $("#listtabannotation").append(_.template(termTabContentTpl, { project : self.model.id, id : term.get("id"), name : term.get("name"), className : "droppableElem"}));
                            //$("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
                            $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).hide();
                        });
                        self.initDropZone();
                        callback.call();

                    }});

            }
        });
    },
    initAnnotationsFilter : function() {
        var self = this;
        var select = $(this.el).find("#annotationFilterSelect");
        var annotationFilterCollection = undefined;
        var refreshSelect = function(){
            select.empty();
            select.attr("disabled", "disabled");
            new AnnotationFilterCollection({project : self.model.id}).fetch({
                success : function (collection, response) {
                    annotationFilterCollection = collection;
                    if (_.size(collection) > 0) $(select).removeAttr("disabled");
                    collection.each (function (annotationFilter) {
                        var optionFilterTpl = "<option value='<%= id %>'><%= name %></option>";
                        var optionFilter = _.template(optionFilterTpl, annotationFilter.toJSON());
                        select.append(optionFilter);
                    });
                },
                error : function (collection, response) {

                }
            });
        };
        refreshSelect();

        /* Select Annotation Filter */
        var selectButton = $(this.el).find("#selectAnnotationFilter");
        selectButton.click(function(){
            var idAnnotationFilter = select.val();
            if (!idAnnotationFilter) return;
            var annotationFilter = annotationFilterCollection.get(idAnnotationFilter);
            var url = "#tabs-annotations-"+self.model.get("id")+"-"+annotationFilter.get("terms")+"-"+annotationFilter.get("users");

            window.app.controllers.browse.tabs.triggerRoute = false;
            window.app.controllers.browse.navigate(url, true);
            window.app.controllers.browse.tabs.triggerRoute = true;
        });
        /* Save Annotation Filter */
        var saveButton = $(this.el).find("#saveAnnotationFilter");
        var confirmButton = $(this.el).find("#confirmAnnotationFilter");
        var cancelButton = $(this.el).find("#cancelAnnotationFilter");
        var showConfirm = function() {
            $("#liSaveAnnotationFilter").hide();
            $("#liConfirmAnnotationFilter").css("display", "inline");
            $("#inputAnnotationFilterName").focus();
            $("#inputAnnotationFilterName").tooltip();
        }
        var hideConfirm = function () {
            $("#liSaveAnnotationFilter").css("display", "inline");
            $("#liConfirmAnnotationFilter").hide();
        }
        cancelButton.click(function(){
            hideConfirm();
            return;
        });
        saveButton.click(function (){
            showConfirm();
            return;
        });
        confirmButton.click(function(){
            var name = $("#inputAnnotationFilterName").val();
            if (name == "" || name == undefined) {
                window.app.view.message("Error", "You have to specify a identifier", "error");
                return;
            }
            new AnnotationFilterModel().save(
                {
                    name : name,
                    terms : self.selectedTerm,
                    users : self.selectedUsers,
                    project : self.model.id
                },
                {
                    success: function(model, response) {
                        window.app.view.message("Success", response.message, "success");
                        hideConfirm();
                        refreshSelect();
                    },
                    error : function (model, response) {
                        window.app.view.message("Error", response.message, "error");
                    }
                });
        });
        /* Delete Annotation Filter */
        var deleteButton = $(this.el).find("#deleteAnnotationFilter");
        deleteButton.click(function(){
            var idAnnotationFilter = select.val();
            if (!idAnnotationFilter) return;
            new AnnotationFilterModel({id : idAnnotationFilter}).destroy({
                success: function(model, response) {
                    window.app.view.message("Success", response.message, "success");
                    refreshSelect();
                },
                error : function (model, response) {
                    window.app.view.message("Error", response.message, "error");
                }
            });

        });
    },
    checkTermsAndUsers : function(terms, users) {
        var _terms = (terms !="" && terms!= undefined);
        var _users = (users != "" && users != undefined);
        if (!_users && !_terms) {
            return;
        }
        this.hideAllTerms();
        this.hideAllUsers();
        if (terms == "all") {
            this.showAllTerms();
        } else if (terms !="" && terms!= undefined) {
            this.selectTerms(terms);
        }
        if (users == "all") {
            this.showAllUsers();
        } else if (users != "" && users != undefined) {
            this.selectUsers(users);
        }
    },
    showAllTerms : function() {
        $(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(true);
    },
    hideAllTerms : function() {
        $(this.el).find("input.undefinedAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(false);
    },
    showAllUsers : function() {
        var self = this;
        this.shouldRefresh = false;
        $(this.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) node.select(true);
        });
        this.shouldRefresh = true;
        self.printAnnotationThumbAllTerms(self.selectedTerm,self.selectedUsers);
    },
    hideAllUsers : function() {
        $(this.el).find('#treeUserListing').dynatree("getRoot").visit(function (node) {
            if (!node.data.isFolder) node.select(false);
        });
    },
    initDropZone : function () {
        var self = this;
        var dropHandler = function(event, ui) {

            $(this).css("background-color", "");
            var annotation = $(ui.draggable).attr("data-annotation");
            var term = $(ui.draggable).attr("data-term");
            var newTerm = $(this).attr("data-term");
            if (term == newTerm) return;
            $(ui.draggable).hide();
            new AnnotationTermModel({term : newTerm, annotation : annotation, clear : true}).save({},{
                success : function(model, response) {
                    $("#tabsterm-"+self.model.id+"-"+newTerm).append($(ui.draggable));
                    setTimeout(function(){$(ui.draggable).fadeIn('slow');}, 1000);
                    window.app.view.message(response.message, null,"success");
                    //$(ui.draggable).remove();
                    //self.refreshSelectedTermsWithUserFilter();
                },
                error : function(model, response) {
                    $(ui.draggable).show();
                    window.app.view.message(response.message, null, "error");
                }
            });
        };
        $(".noDropZone").droppable({
            over: function(event, ui) {
                $(this).css("background-color", "red");
            },
            out: function() {
                $(this).css("background-color", "");
            },
            drop: function() {
                $(this).css("background-color", "");
            }
        });
        $(".droppableElem").droppable({
            over: function(event, ui) {
                $(this).css("background-color", "lightgreen");
            },
            out: function() {
                $(this).css("background-color", "");
            },
            drop: dropHandler
        });
    },
    initSelectUser : function () {
        var self = this;
        new UserCollection({project:self.model.id}).fetch({
            success : function (collection, response) {
                window.app.status.currentUsersCollection = collection;
                var treeData = {
                    id : self.model.id,
                    name : "Users",
                    title : "Users",
                    key : self.model.id,
                    "hideCheckbox": true,
                    isFolder : true,
                    children : []
                };
                collection.each(function(user) {
                    treeData.children.push({
                        id : user.id,
                        key : user.id,
                        name : user.prettyName(),
                        title : user.prettyName(),
                        children : []
                    });
                });
                $(self.el).find('#treeUserListing').dynatree({
                    checkbox: true,
                    selectMode: 2,
                    expand : true,
                    onExpand : function() {},
                    children: treeData,
                    onSelect: function(select, node) {
                        //if(!self.activeEvent) return;
                        if (node.isSelected()) {
                            self.selectedUsers.push(node.data.key);
                        }
                        else {
                            self.selectedUsers = _.without(self.selectedUsers, node.data.key);
                        }
                        if (self.shouldRefresh) self.printAnnotationThumbAllTerms(self.selectedTerm,self.selectedUsers);
                    },
                    onDblClick: function(node, event) {
                        //node.toggleSelect();
                    },

                    // The following options are only required, if we have more than one tree on one page:
                    initId: "treeData-user-"+self.model.id,
                    cookieId: "dynatree-Cb-user-"+self.model.id,
                    idPrefix: "dynatree-Cb-user-"+self.model.id+"-"
                });

                $(self.el).find('#treeUserListing').dynatree("getRoot").visit(function(node){
                    node.expand(true);
                });
                $(self.el).find('#treeUserListing').dynatree("getTree").selectKey(window.app.status.user.id);
            }});
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addTermToTab : function(termTabTpl, termTabContentTpl, data) {
        //$("#ultabsannotation").append(_.template(termTabTpl, data));
        $("#listtabannotation").append(_.template(termTabContentTpl, data));

    },
    selectAnnotations : function (selected) {
        var self = this;
        this.terms.each(function(term) {
            $(self.el).find('#treeAnnotationListing').dynatree("getTree").selectKey(term.get("id"), selected);
        });
    },
    updateDownloadLinks : function () {
        var users = this.selectedUsers.join(",");
        var terms = this.selectedTerm.join(",");
        var prefix = "&users=" + users + "&terms=" + terms;
        $("#downloadAnnotationsCSV").attr("href", "/api/project/"+this.model.id+"/annotation/download?format=csv" + prefix);
        $("#downloadAnnotationsExcel").attr("href", "/api/project/"+this.model.id+"/annotation/download?format=xls" + prefix);
        $("#downloadAnnotationsPDF").attr("href", "/api/project/"+this.model.id+"/annotation/download?format=pdf" + prefix);
    },
    updateContentVisibility : function () {
        var nbUserSelected = _.size(this.selectedUsers);
        var nbTermSelected = _.size(this.selectedTerm);
        nbTermSelected += ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") ? 1 : 0;
        nbTermSelected += ($(this.el).find("input.multipleAnnotationsCheckbox").attr("checked") == "checked") ? 1 : 0;
        if (nbTermSelected > 0 && nbUserSelected > 0){
            $("#listtabannotation").show();
            $("#downloadAnnotation").show();
        } else {
            $("#listtabannotation").hide();
            $("#downloadAnnotation").hide();
        }
    },
    selectTerms : function(terms) {
        terms = terms.split(",");
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getTree");
        _.each(terms, function (term) {
            var node = tree.getNodeByKey(term);
            node.select(true);
        });

    },
    selectUsers : function(users) {
        users = users.split(",");
        var tree = $(this.el).find('#treeUserListing').dynatree("getTree");
        _.each(users, function(user ) {
            var node = tree.getNodeByKey(user);
            node.select(true);
        });
    },
    refreshSelectedTermsWithUserFilter : function () {
        var self = this;
        var users = self.selectedUsers;
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getRoot");
        if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
        tree.visit(function(node){
            if (!node.isSelected()) return;
            self.refreshAnnotations(node.data.key,users);
        });
        if ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") {
            self.refreshAnnotations(-1,users);
        }
        if ($(this.el).find("input.multipleAnnotationsCheckbox").attr("checked") == "checked") {
            self.refreshAnnotations(-2,users);
        }
        self.updateContentVisibility();self.updateDownloadLinks();
    },
    /**
     * Refresh all annotation dor the given term
     * @param term annotation term to be refresh (all = 0)
     */
    refreshAnnotations : function(term,users) {
        this.printAnnotationThumb(term,"#tabsterm-"+this.model.id+"-"+term,users);
    },
    clearAnnotations : function (term) {
        console.log("clearAnnotations");
        var self = this;
        $("#tabsterm-"+self.model.id+"-"+term).empty();
    },
    /**
     * Print annotation for the given term
     * @param term term annotation term to be refresh (all = 0)
     * @param $elem  elem that will keep all annotations
     */
    printAnnotationThumbAllTerms : function(terms,users) {
        var self = this;
        self.updateContentVisibility();
        self.updateDownloadLinks();
        if (_.size(users) == 0) return; //nothing to display
        for(var i=0;i<terms.length;i++) {
            self.printAnnotationThumb(terms[i],"#tabsterm-"+self.model.id+"-"+terms[i],users);
        }

    },
    printAnnotationThumb : function(idTerm,$elem,users){
        var self = this;
        new AnnotationCollection({project:self.model.id,term:idTerm,users:users}).fetch({
            success : function (collection, response) {
                if (self.annotationsViews[idTerm] != null && users==undefined) { //only refresh
                    self.annotationsViews[idTerm].refresh(collection,users);
                    return;
                }
                $($elem).empty();
                self.annotationsViews[idTerm] = new AnnotationView({
                    page : undefined,
                    model : collection,
                    term : idTerm,
                    el:$($elem)
                }).render();
                //$("#listtabannotation > div").tsort();
            }
        });
    }
});