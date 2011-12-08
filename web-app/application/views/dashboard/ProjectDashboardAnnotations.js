var ProjectDashboardAnnotations = Backbone.View.extend({
    tabsAnnotation : null,
    annotationsViews : [], //array of annotation view
    selectedTerm: new Array(),
    selectUser : null,
    terms : null,
    ontology : null,
    doLayout : function (termTabTpl, termTabContentTpl, callback) {
        var self = this;
        this.selectUser =  "#filterAnnotationByUser"+self.model.id;
        new OntologyModel({id:self.model.get('ontology')}).fetch({
            success : function(model, response) {
                self.ontology = model;
                $(self.el).find("input.undefinedAnnotationsCheckbox").change(function(){
                    if ($(this).attr("checked") == "checked") {
                        self.updateContentVisibility();
                        self.refreshAnnotations(-1,self.getSelectedUser());
                        self.selectedTerm.push(-1);
                        $("#tabsterm-panel-"+self.model.id+"--1").show();
                    } else {
                        self.updateContentVisibility();
                        $("#tabsterm-panel-"+self.model.id+"--1").hide();
                        self.selectedTerm = _.without(self.selectedTerm, -1);
                    }
                });
                $(self.el).find("input.multipleAnnotationsCheckbox").change(function(){
                    if ($(this).attr("checked") == "checked") {
                        self.updateContentVisibility();
                        self.refreshAnnotations(-2,self.getSelectedUser());
                        self.selectedTerm.push(-2);
                        $("#tabsterm-panel-"+self.model.id+"--2").show();
                    } else {
                        self.updateContentVisibility();
                        $("#tabsterm-panel-"+self.model.id+"--2").hide();
                        self.selectedTerm = _.without(self.selectedTerm, -2);
                    }
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
                            self.updateContentVisibility();
                            self.refreshAnnotations(node.data.key,self.getSelectedUser());
                            $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).show();
                            self.selectedTerm.push(node.data.key);
                        }
                        else {
                            self.updateContentVisibility();
                            $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).hide();
                            self.selectedTerm = _.without(self.selectedTerm, node.data.key);
                        }
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
                        $(node.span).attr("class", "droppableNode");
                    }
                });
                $("#ontology-annotations-panel-"+self.model.id).panel();

                self.initSelectUser();

                $(self.el).find("#hideAllAnnotations").click(function(){
                    self.hideAllUsers();
                    self.hideAllTerms();
                });

                $(self.el).find("#showAllAnnotations").click(function(){
                    self.showAllUsers();
                    self.showAllTerms();
                });

                $(self.el).find("#refreshAnnotations").click(function(){
                    self.refreshSelectedTermsWithUserFilter();
                });
                new TermCollection({idOntology:self.model.get('ontology')}).fetch({
                    success : function (collection, response) {
                        self.terms = collection;
                        window.app.status.currentTermsCollection = collection;
                        $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : -1, name : "Undefined", className : "noDropZone"}));
                        $("#tabsterm-panel-"+self.model.id+"--1").panel();
                        $("#tabsterm-panel-"+self.model.id+"--1").hide();
                        $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : -2, name : "Multiple", className : "noDropZone"}));
                        $("#tabsterm-panel-"+self.model.id+"--2").panel();
                        $("#tabsterm-panel-"+self.model.id+"--2").hide();
                        collection.each(function(term) {
                            //add x term tab
                            $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : term.get("id"), name : term.get("name"), className : "termDropZone"}));
                            $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
                            $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).hide();
                        });
                        self.initDropZone(collection);
                        callback.call();

                    }});

            }
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
        if (_users && !_terms) {
            this.selectUsers(users);
            this.showAllTerms();
        } else if (_users && _terms) {
            this.selectUsers(users);
            this.selectTerms(terms);

        } else if (!_users && _terms) {
            this.showAllUsers();
            this.selectTerms(terms);
        }
        //self.projectDashboardAnnotations.refreshSelectedTermsWithUserFilter();
    },
    showAllTerms : function() {
        $(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").attr("checked", "checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(true);
    },
    showAllUsers : function() {
        $(this.selectUser).multiselect("checkAll");
    },
    hideAllTerms : function() {
        $(this.el).find("input.undefinedAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.undefinedAnnotationsCheckbox").trigger("change");
        $(this.el).find("input.multipleAnnotationsCheckbox").removeAttr("checked");
        $(this.el).find("input.multipleAnnotationsCheckbox").trigger("change");
        this.selectAnnotations(false);
    },
    hideAllUsers : function() {
        $(this.selectUser).multiselect("uncheckAll");
    },
    initDropZone : function (termCollection) {
        var self = this;
        var dropHandler = function(event, ui) {
            $(this).css("background-color", "");
            $(ui.draggable).hide();
            var annotation = $(ui.draggable).attr("data-annotation");
            var term = $(ui.draggable).attr("data-term");
            var newTerm = $(this).attr("data-term");
            if (term == newTerm) return;
            new AnnotationTermModel({term : newTerm, annotation : annotation, clear : true}).save({},{
                success : function(model, response) {
                    window.app.view.message(response.message, null,"success");
                    $(ui.draggable).remove();
                    self.refreshSelectedTermsWithUserFilter();
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
        $(".droppableNode").droppable({
            over: function(event, ui) {
                $(this).css("background-color", "lightgreen");
            },
            out: function() {
                $(this).css("background-color", "");
            },
            drop: dropHandler
        });
        $(".termDropZone").droppable({
            over: function(event, ui) {
                $(this).css("background-color", "lightgreen");
            },
            out: function() {
                $(this).css("background-color", "");
            },
            drop: dropHandler
        });
    },
    render : function(callback) {
        var self = this;
        require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {
            self.doLayout(termTabTpl, termTabContentTpl, callback);
        });
    },
    initSelectUser : function () {
        var self = this;
        $(self.selectUser).empty();
        $(self.selectUser).multiselect({
            selectedText: "# of # users selected",
            noneSelectedText : "No user are selected",
            checkAll: function(){
                console.log("click on user :"+self.selectedTerm + " users="+self.getSelectedUser());
                self.printAnnotationThumbAllTerms(self.selectedTerm,self.getSelectedUser());
            },
            uncheckAll: function(){
                console.log("click on user :"+self.selectedTerm + " users="+self.getSelectedUser());
                self.printAnnotationThumbAllTerms(self.selectedTerm,self.getSelectedUser());
            }
        });


        new UserCollection({project:self.model.id}).fetch({
            success : function (collection, response) {
                window.app.status.currentUsersCollection = collection;

                collection.each(function(user) {
                    var option = _.template("<option value='<%= id %>'> <%= firstname %> <%= lastname %></option>", user.toJSON() );
                    $(self.selectUser).append(option);
                });
                $(self.selectUser).multiselect("refresh");
                $(self.selectUser).multiselect("checkAll");

                $(self.selectUser).bind("multiselectclick", function(event, ui){
                    //self.refreshAnnotations(undefined,self.getSelectedUser());
                    self.printAnnotationThumbAllTerms(self.selectedTerm,self.getSelectedUser());
                    /*
                     event: the original event object
                     ui.value: value of the checkbox
                     ui.text: text of the checkbox
                     ui.checked: whether or not the input was checked
                     or unchecked (boolean)
                     */
                });
            }});

//                    <option value="5">Option 5</option>
        //filterAnnotationByUser<%=   id %>
    },
    getSelectedUser : function() {
        var userArray = $(this.selectUser).multiselect("getChecked");
        var userId = new Array();
        _.each(userArray,function(user) {
            userId.push($(user).attr("value"));
        });
        if(userId.length==0)userId.push(-1); //WHY ?
        return userId;
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
    updateContentVisibility : function () {
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getRoot");
        if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
        var nbTermSelected = 0;
        tree.visit(function(node){
            if (!node.isSelected()) return;
            nbTermSelected++;
        });
        nbTermSelected += ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") ? 1 : 0;
        nbTermSelected += ($(this.el).find("input.multipleAnnotationsCheckbox").attr("checked") == "checked") ? 1 : 0;
        if (nbTermSelected > 0){
            $("#listtabannotation").show();
        } else {
            $("#listtabannotation").hide();
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
        _.each(users, function (user) {
            $(".ui-multiselect-menu").find("input[value='"+user+"']").click();
        });
    },
    refreshSelectedTermsWithUserFilter : function () {
        var self = this;
        var users = self.getSelectedUser();
        var tree = $(this.el).find('#treeAnnotationListing').dynatree("getRoot");
        if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
        tree.visit(function(node){
            if (!node.isSelected()) return;
            console.log("REFRESH  "+ node.data.key);
            self.refreshAnnotations(node.data.key,users);
        });
        if ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") {
            self.refreshAnnotations(-1,users);
        }
        if ($(this.el).find("input.multipleAnnotationsCheckbox").attr("checked") == "checked") {
            self.refreshAnnotations(-2,users);
        }
        self.updateContentVisibility();
    },
    /**
     * Refresh all annotation dor the given term
     * @param term annotation term to be refresh (all = 0)
     */
    refreshAnnotations : function(term,users) {
        console.log("refreshAnnotations");
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
        console.log("printAnnotationThumb="+users);
        var self = this;
        for(var i=0;i<terms.length;i++) {
            console.log("printAnnotationThumb loop="+users);
            self.printAnnotationThumb(terms[i],"#tabsterm-"+self.model.id+"-"+terms[i],users);
        }
    },
    printAnnotationThumb : function(idTerm,$elem,users){
        var self = this;
        console.log("users="+users);
        /*var idTerm = 0;
         if(term==0) {idTerm = undefined;}
         else idTerm = term*/
        new AnnotationCollection({project:self.model.id,term:idTerm,users:users}).fetch({
            success : function (collection, response) {
                console.log("success");
                if (self.annotationsViews[idTerm] != null && users==undefined) { //only refresh
                    self.annotationsViews[idTerm].refresh(collection,users);
                    return;
                }
                $($elem).empty();
                self.annotationsViews[idTerm] = new AnnotationView({
                    page : undefined,
                    model : collection,
                    idTerm : idTerm,
                    el:$($elem)
                }).render();

                //self.annotationsViews[term].refresh(collection);
                $("#listtabannotation > div").tsort();
            }
        });
    }
});