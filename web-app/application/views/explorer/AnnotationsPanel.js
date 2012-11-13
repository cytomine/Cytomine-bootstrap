/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 21/06/11
 * Time: 22:19
 * To change this template use File | Settings | File Templates.
 */

var AnnotationsPanel = Backbone.View.extend({
    tagName:"div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize:function (options) {
        this.refreshAnnotationsTabsFunc = [];
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render:function () {
        var self = this;
        require([
            "text!application/templates/explorer/AnnotationsPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });

        return this;
    },
    createTabs:function (idOntology) {
        var self = this;
        require(["text!application/templates/explorer/TermTab.tpl.html", "text!application/templates/explorer/TermTabContent.tpl.html"], function (termTabTpl, termTabContentTpl) {

            //add "All annotation from all term" tab
            self.addTermToTab(termTabTpl, termTabContentTpl, { id:"all", image:self.model.id, name:"All"});
            self.refreshAnnotationsTabsFunc.push({
                index:0,
                idTerm:"all",
                refresh:function () {
                    self.refreshAnnotations(undefined, $("#tabsterm-" + self.model.id + "-all"))
                }
            });
            var i = 1;
            window.app.status.currentTermsCollection.each(function (term) {
                //add x term tab
                self.addTermToTab(termTabTpl, termTabContentTpl, { id:term.get("id"), image:self.model.id, name:term.get("name")});
                self.refreshAnnotationsTabsFunc.push({
                    index:i,
                    idTerm:term.get("id"),
                    refresh:function () {
                        self.refreshAnnotations(term.get("id"), $("#tabsterm-" + self.model.id + "-" + term.get("id")))
                    }
                });
                i++;
            });
            $("#"+self.browseImageView.divId).find("#annotationsPanel" + self.model.id).find(".tabsAnnotation").tabs({
                add:function (event, ui) {

                },
                select:function (event, ui) {
                    var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object) {
                        return (object.index == ui.index);
                    });
                    obj.refresh.call();
                }
            });
            $("#"+self.browseImageView.divId).find("#annotationsPanel" + self.model.id).find(".tabs-bottom .ui-tabs-nav, .tabs-bottom .ui-tabs-nav > *")
                .removeClass("ui-corner-all ui-corner-top")
                .addClass("ui-corner-bottom");

        });
    },
    refreshAnnotationTabs:function (idTerm) {
        var self = this;
        if (idTerm != undefined) {
            var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object) {
                return (object.idTerm == idTerm);
            });
            obj.refresh.call();
        } else { //refresh the current tab
            var selected = $("#"+self.browseImageView.divId).find("#annotationsPanel" + self.model.id).find(".tabsAnnotation").tabs("option", "selected");
            self.refreshAnnotationsTabsFunc[selected].refresh.call();
        }
    },
    refreshAnnotations:function (idTerm, el) {
        new AnnotationCollection({image:this.model.id, term:idTerm}).fetch({
            success:function (collection, response) {
                el.empty();
                var view = new AnnotationView({
                    page:undefined,
                    model:collection,
                    el:el
                }).render();

            }
        });
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addTermToTab:function (termTabTpl, termTabContentTpl, data) {
        var self = this;
        $("#"+self.browseImageView.divId).find("#annotationsPanel" + this.model.id).find(".ultabsannotation").append(_.template(termTabTpl, data));
        $("#"+self.browseImageView.divId).find("#annotationsPanel" + this.model.id).find(".listtabannotation").append(_.template(termTabContentTpl, data));
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout:function (tpl) {
        var self = this;
        var el = $("#"+self.browseImageView.divId).find('#annotationsPanel' + self.model.get('id'));
        var width = parseInt($(window).width());
        el.html(_.template(tpl, {id:self.model.get('id')}));
        new ProjectModel({id:window.app.status.currentProject}).fetch({
            success:function (model, response) {
                self.createTabs(model.get("ontology"));
            }
        });
        el.css("padding", "0px");
        el.css("margin", "0px");
        el.css("width", "16px");
        el.css("height", "16px");
        el.css("bottom", "0px");
        el.find("div.panel_button").click(function () {
            width = parseInt($(window).width());
            el.find("div.panel_button").toggle();
            el.css("bottom", "0px");
            el.animate({
                height:"302px"
            }, "fast").animate({
                    width:width
                }, "fast").find("div.panel_content").fadeIn();

            //Refresh selected tab
            var tabSelected = el.find(".tabsAnnotation").tabs('option', 'selected');
            var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object) {
                return (object.index == tabSelected);
            });
            obj.refresh.call();
            return false;
        });


        el.find("div#hide_button").click(function () {
            el.animate({
                height:"16px"
            }, "fast")
                .animate({
                    width:"16px"
                }, "fast");

            setTimeout(function () {
                el.find("div.panel_content").hide();
                el.css("bottom", "0px");
            }, 1000);

            return false;

        });
        el.find("div.previous_button").click(function () {
            alert("prev" + self.currentAnnotation);
        });
        el.find("div.next_button").click(function () {
            alert("next" + self.currentAnnotation);
        });
        return this;
    }
});
