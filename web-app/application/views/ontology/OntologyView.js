var OntologyView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    ontologiesPanel : null,
    idOntology : null,
    addOntologyDialog : null,
    allTerms : [],
    events: {
        "click #ontologyAddButton": "showAddOntologyPanel",
        "click #ontologyRefreshButton" : "refreshOntology"
    },
    initialize: function(options) {
        this.container = options.container;
        this.idOntology = options.idOntology;
        this.idTerm =  options.idTerm;
    },
    render : function () {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyList.tpl.html"
        ],
            function(tpl) {
                self.doLayout(tpl);
            });

        return this;
    },
    doLayout: function(tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, {}));
        self.fillOntologyMenu();
        return this;
    },
    fillOntologyMenu : function() {
        var self = this;
        console.log("fillOntologyMenu");
        new OntologyCollection({light:true}).fetch({
            success : function (collection, response) {
                console.log("ontology fetch");
                collection.each(function(ontology) {
                    console.log("add ontology");
                    $("#menuOntologyUl").append('<li data-id="'+ontology.id+'" id="consultOntology-' + ontology.id + '"><a href="#ontology/'+ontology.id +'"><i class="icon-arrow-right"></i> ' + ontology.get('name') + '</a></li>');
                });
                self.selectOntology();
        }});
    },
    select : function(idOntology,idTerm) {
        var self = this;
        if(self.idOntology==idOntology) return;
        self.idOntology =  idOntology;
        self.idTerm = idTerm;
        self.selectOntology();
    },
    selectOntology : function() {
        console.log("selectOntology:"+this.idOntology);
        var self = this;
        if($("#menuOntologyUl").children().length==1) return;
        //if param => select
        if(self.idOntology==null) {
            console.log($("#menuOntologyUl").children()[1]);
            var firstOntologyNode =  $("#menuOntologyUl").children()[1];
            self.idOntology = $(firstOntologyNode).attr("data-id")

        }
        console.log("self.idOntology="+self.idOntology);
        $("#menuOntologyUl").children().removeClass("active");
        $("#consultOntology-" + self.idOntology).addClass("active");
        self.printOntology();
    },
    printOntology : function() {
        console.log("printOntology");
        var self = this;

        $("#ontologyPanelView").append('<div id="ontologyLoading" class="alert alert-info"><i class="icon-refresh" /> Loading...</div>');

        new OntologyModel({id:self.idOntology}).fetch({
            success : function (model, response) {
                self.retrieveTerm(model);
                console.log("OntologyModel succes "+self.idOntology);
                require(["text!application/templates/ontology/OntologyTabContent.tpl.html"],
                    function(ontologyTabContentTpl) {
                            $("#ontologyPanelView").empty();
                            $("#ontologyPanelView").append(_.template(ontologyTabContentTpl, model.toJSON()));
                            //create project search panel
                            var view = new OntologyPanelView({
                                model : model,
                                el:$("#ontologyPanelView"),
                                container : self
                            });

                            view.render();
                        if(self.idTerm!=null) {
                            view.selectTerm(self.idTerm)
                        }
//
//                        //Hack loading...
//                        setTimeout(function(){
//                            $("#ontologyLoading").remove();
//                        },1500);

//
//                        if(!self.alreadyBuild) {
//                            $("#ontology h3 a").click(function() {
//                                window.location = $(this).attr('href'); //follow link
//                                return false;
//                            });
//                        }
                    });

         }});



    },
    refreshOntology : function() {
        this.render();
    },
    refresh : function(idOntology) {
        console.log("refresh idOntology="+idOntology);
        this.idOntology =  idOntology;
        this.render();
    },
    refresh : function(idOntology, idTerm) {
        this.idOntology =  idOntology;
        this.idTerm = idTerm;
        this.render();
    },
    showAddOntologyPanel : function() {
        var self = this;
        $('#addontology').remove();
        self.addOntologyDialog = new AddOntologyDialog({ontologiesPanel:self,el:self.el}).render();
    },












//
//    refresh : function() {
//        var self = this;
//        window.app.models.ontologies.fetch({
//            success : function (collection, response) {
//                self.retrieveTerm(window.app.models.ontologies);
//                self.render();
//            }});
//    },
//    refresh : function(idOntology, idTerm) {
//        var self = this;
//        this.idOntology = idOntology;
//        this.idTerm = idTerm;
//        window.app.models.ontologies.fetch({
//            success : function (collection, response) {
//                self.retrieveTerm(window.app.models.ontologies);
//                self.render();
//            }});
//    },
//    select : function(idOntology) {
//        var self = this;
//        this.idOntology = idOntology;
//        self.render();
//    },
//
//    scrollToOntology :function(idOntology) {
//        var targetOffset = $(".ontology"+idOntology);
//        if (targetOffset.length > 0) {
//            var topOffset = targetOffset.offset().top;
//            topOffset = topOffset - 110;
//            $('html,body').animate({scrollTop: topOffset}, 1000);
//        }
//    },
//    showAddOntologyPanel : function() {
//        var self = this;
//        $('#addontology').remove();
//        self.addOntologyDialog = new AddOntologyDialog({ontologiesPanel:self,el:self.el}).render();
//    },
//    select : function(idOntology,idTerm) {
//        var self = this;
//        var selectedOntologyIndex = 0;
//        var index = 0;
//        self.model.each(function(ontology) {
//            if(idOntology== ontology.get("id")) {
//                selectedOntologyIndex = index;
//            }
//            index = index + 1;
//        });
//        if (idTerm != undefined) self.ontologiesPanel[selectedOntologyIndex].selectTerm(idTerm);
//    },
//    /**
//     * Init annotation tabs
//     */
//    initOntologyTabs : function(){
//        var self = this;
//        require(["text!application/templates/ontology/OntologyTab.tpl.html", "text!application/templates/ontology/OntologyTabContent.tpl.html"],
//            function(ontologyTabTpl, ontologyTabContentTpl) {
//                self.ontologiesPanel = new Array();
//                //add "All annotation from all term" tab
//                self.model.each(function(ontology) {
//                    var elem = self.addOntologyToTab(ontologyTabTpl, ontologyTabContentTpl, { id : ontology.get("id"), name : ontology.get("name")});
//                    //create project search panel
//                    var view = new OntologyPanelView({
//                        model : ontology,
//                        el:elem,
//                        container : self,
//                        ontologiesPanel : self
//                    });
//                    view.render();
//                    self.ontologiesPanel.push(view);
//                });
//
//                //Hack loading...
//                setTimeout(function(){
//                    $("#ontologyLoading").remove();
//                },1500);
//
//
//                if(!self.alreadyBuild) {
//                    $("#ontology h3 a").click(function() {
//                        window.location = $(this).attr('href'); //follow link
//                        return false;
//                    });
//                }
//            });
//    },
//    /**
//     * Add the the tab with ontology info
//     * @param id  ontology id
//     * @param name ontology name
//     */
//    addOntologyToTab : function(ontologyTabTpl, ontologyTabContentTpl, data) {
//        return this.$tabsOntologies.append(_.template(ontologyTabContentTpl, data));
//    },


    //browse all ontologies to retrieve each term and add it in window.app.models.terms
    retrieveTerm : function(ontology) {
        var self = this;
        window.app.models.terms = new TermCollection(self.retrieveChildren(ontology.attributes));
    },
    retrieveChildren : function(parent) {
        var self = this;
        if(parent['children'].length==0) return [];
        var children = [];
        _.each(parent['children'],function(elem) {
            children.push(elem);
            children = _.union(children,self.retrieveChildren(elem));
        });
        return children;
    }
});
