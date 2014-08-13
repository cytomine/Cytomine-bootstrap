/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 13/04/11
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
var OntologyTreeView = Backbone.View.extend({
    tagName: "div",
    annotationTerm: {},
    //template : _.template($('#project-view-tpl').html()),
    initialize: function (options) {
        this.tree = null;
        this.activeEvent = true;
        this.browseImageView = options.browseImageView;
        this.idAnnotation = null;
    },
    showColors: function () {
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {

            if (node.children != null) {
                return;
            } //title is ok

            var title = node.data.title
            var color = node.data.color
            var htmlNode = "<%=   title %> <span style='background-color:<%=   color %>'>&nbsp;&nbsp;</span> ";
            if (!node.data.isFolder) {
                htmlNode = htmlNode + "(<span id='usercount" + node.data.key + "'>0</span>)";
                htmlNode = htmlNode + " Show: <input type='checkbox' data-term="+node.data.key+" id='showTerm"+node.data.key+"' class='termVisible' checked>";
            }

            var nodeTpl = _.template(htmlNode, {title: title, color: color});


            node.setTitle(nodeTpl);
        });
    },
    render: function () {
        var self = this;
        require(["text!application/templates/explorer/OntologyTreeWrapper.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    doLayout: function (tpl) {
        $(this.el).html(_.template(tpl, {isDesktop: !window.app.view.isMobile}));
        this.tree = $(this.el).find('.tree');
        var self = this;

        $(this.el).find('.tree').dynatree({
            checkbox: true,
            selectMode: 2,
            expand: true,
            onExpand: function () {
                self.computeCorrectTextSize();
            },
            children: this.model.toJSON(),
            onSelect: function (select, node) {

                if (!self.activeEvent) {
                    return;
                }
                if (self.idAnnotation == null) {
                    return;
                } // nothing to do

                if (node.isSelected()) {
                    if (self.browseImageView.isCurrentAnnotationUser()) {
                        console.log("link");


                        if (self.browseImageView.currentAnnotation.get("class") == "be.cytomine.ontology.ReviewedAnnotation") {
                            window.app.view.message("Reviewed annotation", "You cannot add a term to an accepted annotation, you must reject it before!", "error");
                            self.activeEvent = false;
                            node.select(false);
                            self.activeEvent = true;

                        } else {
                            self.linkTerm(node.data.key);
                        }

                    } else {
                        self.browseImageView.addTermToReviewPanel(node.data.key);
                    }

                } else if (!node.isSelected()) {
                    if (self.browseImageView.isCurrentAnnotationUser()) {
                        self.unlinkTerm(node.data.key);
                    } else {
                        self.browseImageView.deleteTermFromReviewPanel(node.data.key);
                    }
                }
            },
            onDblClick: function (node, event) {
                node.toggleSelect();
            },
            onRender: function (node, nodeSpan) {
                $(nodeSpan).find("span.dynatree-icon").css({"background-image": "url(css/custom-theme/images/ui-icons_ffffff_256x240.png)"});
//                self.checkTerm();
//





            },

            // The following options are only required, if we have more than one tree on one page:
            initId: "treeData" + this.model.id,
            cookieId: "dynatree-Cb" + this.model.id,
            idPrefix: "dynatree-Cb" + this.model.id + "-"
        });

        self.showColors();

        //expand root, simpliest way ? 
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            if (node.data.key == self.model.id) {
                node.expand(true);
            }
        });

         self.computeCorrectTextSize();

        $(this.el).find(".seeAllTerm").click(function() {
            $(self.el).find('.tree').dynatree("getRoot").visit(function(node){
                node.expand(true);
            });
            $(self.el).find(".termVisible").prop('checked', true);
            self.browseImageView.refreshLayers();
        });
        $(this.el).find(".hideAllTerm").click(function() {
            $(self.el).find('.tree').dynatree("getRoot").visit(function(node){
                node.expand(true);
            });
            $(self.el).find(".termVisible").prop('checked', false);
            self.browseImageView.refreshLayers();
        });


        $(this.el).find(".termVisible").change(function() {
            self.browseImageView.refreshLayers();
        });

        $(this.el).find(".dynatree-title").removeAttr("href");

        return this;
    },
//    checkTerm : function() {
//        console.log("*****************");
//        console.log($(this.el));
//        console.log($(this.el).find(".termVisible"));
//
//        $(this.el).find(".termVisible").prop('checked', true);
//    },

    computeCorrectTextSize :function() {
        var originalFontSize = 12;
       	var sectionWidth = $(this.el).find('.dynatree-container').width()-75;

        $(this.el).find(".dynatree-node").each(function() {
               var spanWidth = $(this).width();
                if(spanWidth>0) {
                    $(this).css({"font-size" : originalFontSize, "line-height" : originalFontSize/1.2 + "px"});
                }

           });

        $(this.el).find(".dynatree-node").each(function() {
               var spanWidth = $(this).width();
                if(spanWidth>0) {
                    var newFontSize = (sectionWidth/spanWidth) * originalFontSize;
                     newFontSize = Math.min(originalFontSize,newFontSize);
                    $(this).css({"font-size" : newFontSize, "line-height" : newFontSize/1.2 + "px"});
                }

           });
    },
    expand: function () {
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            node.expand(true);
        });
    },
    clear: function () {
        var self = this;
        this.activeEvent = false;
        $(this.el).find('.otherUsersTerms').empty();
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            node.select(false);
            $("#usercount" + node.data.key).text("0");
        });
        this.activeEvent = true;
    },
    clearAnnotation: function () {
        this.idAnnotation = null;
    },
    check: function (idTerm) {
        var self = this;
        self.activeEvent = false;
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            if (node.data.key == idTerm) {
                node.select(true);
            }
        });
        self.activeEvent = true;
    },
    uncheck: function (idTerm) {
        var self = this;
        self.activeEvent = false;
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            if (node.data.key == idTerm) {
                node.select(false);
            }
        });
        self.activeEvent = true;
    },
    refresh: function (idAnnotation) {
        var self = this;
        console.log("refresh term for annotation " + idAnnotation);

        //if(self.browseImageView.currentAnnotation.get("class")=="be.cytomine.ontology.ReviewedAnnotation")  return;

        this.idAnnotation = idAnnotation;
        var refreshTree = function (collection, response) {
            console.log("collection.lenght=" + collection.length);
            self.annotationTerm = collection;
            self.clear();
            self.activeEvent = false;
            //check all term for logged user
            var userAnnotationTerm = collection.filter(function (annotationTerm) {
                return annotationTerm.get("user") == window.app.status.user.id;
            });
            _.each(userAnnotationTerm, function (annotationTerm) {
                var idTerm = annotationTerm.get('term');
                self.check(idTerm);
            });
            //update counters

            if(!window.app.status.currentProjectModel.get('blindMode')) {
                //in blind mode, disable counter
                var idTerms = [];
                collection.each(function (annotationTerm) {
                    idTerms.push(annotationTerm.get("term"));
                });
                idTerms = _.uniq(idTerms);
                _.each(idTerms, function (idTerm) {
                    var annotationTerms = collection.filter(function (annotationTerm) {
                        return annotationTerm.get("term") == idTerm;
                    });
                    $("#usercount" + idTerm).text(_.size(annotationTerms));
                });
            }



            self.activeEvent = true;
        }

        new AnnotationTermCollection({idAnnotation: idAnnotation}).fetch({success: refreshTree});
    },
    getTermsChecked: function () {
        var terms = [];
        $(this.el).find('.tree').dynatree("getRoot").visit(function (node) {
            if (node.isSelected()) {
                terms.push(node.data.key);
            }
        });
        return terms;
    },
    linkTerm: function (idTerm) {
        var self = this;
        new AnnotationTermModel({userannotation: this.idAnnotation, term: idTerm}).save({userannotation: this.idAnnotation, term: idTerm},
            {
                success: function (model, response) {
                    window.app.view.message("Annotation Term", response.message, "success");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "error");
                }
            }
        );
    },
    unlinkTerm: function (idTerm) {
        var self = this;
        console.log("unlinkTerm IdTerm " + idTerm + " : " + this.idAnnotation);

        var annotationTerm = self.annotationTerm.find(function (annotationTerm) {
            return (annotationTerm.get("term") == idTerm && annotationTerm.get("user") == window.app.status.user.id);
        });
        new AnnotationTermModel({ id: annotationTerm.id, userannotation: self.idAnnotation, term: idTerm}).destroy({
                success: function (model, response) {
                    window.app.view.message("Annotation Term", response.message, "success");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "error");
                }
            }
        );
    },

    getTermToShow : function() {
        var terms = []
        _.each($(this.el).find(".termVisible:checked"),function(item) {
            var term = $(item).data("term");
            terms.push(term);

        });
        return terms;
    },

    isTermRestriction : function() {
        //check if 1 or N terms are unchecked
        var all = $(this.el).find(".termVisible").length;
        var visible = $(this.el).find(".termVisible:checked").length;

        return all != visible;
    }


});
