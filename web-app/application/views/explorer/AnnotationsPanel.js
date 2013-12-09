/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 21/06/11
 * Time: 22:19
 * To change this template use File | Settings | File Templates.
 */

var AnnotationsPanel = Backbone.View.extend({
    tagName: "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.refreshAnnotationsTabsFunc = [];
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/AnnotationsPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });

        return this;
    },
    createTabs: function (idOntology) {
        var self = this;
        require(["text!application/templates/explorer/TermTab.tpl.html", "text!application/templates/explorer/TermTabContent.tpl.html"], function (termTabTpl, termTabContentTpl) {
            var annotationPanel = $("#annotationsPanel" + self.model.id);
            var ulEl = annotationPanel.find(".ultabsannotation");
            var liEl = annotationPanel.find(".listtabannotation");

            //add "All annotation from all term" tab
            ulEl.append(_.template(termTabTpl, { id: "all", image: self.model.id, name: "All"}));
            liEl.append(_.template(termTabContentTpl, { id: "all", image: self.model.id, name: "All"}));
            self.refreshAnnotationsTabsFunc.push({
                index: 0,
                idTerm: "all",
                refresh: function () {
                    self.refreshAnnotations(undefined, $("#tabsterm-" + self.model.id + "-all"))
                }
            });
            var i = 1;
            window.app.status.currentTermsCollection.each(function (term) {
                //add x term tab
                ulEl.append(_.template(termTabTpl, { id: term.get("id"), image: self.model.id, name: term.get("name")}));
                liEl.append(_.template(termTabContentTpl, { id: term.get("id"), image: self.model.id, name: term.get("name")}));

                self.refreshAnnotationsTabsFunc.push({
                    index: i,
                    idTerm: term.get("id"),
                    refresh: function () {
                        self.refreshAnnotations(term.get("id"), $("#tabsterm-" + self.model.id + "-" + term.get("id")))
                    }
                });
                i++;
            });
        });
    },
    refreshAnnotationTabs: function (idTerm) {
        var self = this;
        if (idTerm != undefined) {
            var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object) {
                return (object.idTerm == idTerm);
            });
            obj.refresh.call();
        } else { //refresh the current tab
            var selected = $("#" + self.browseImageView.divId).find("#annotationsPanel" + self.model.id).find(".tabsAnnotation").tabs("option", "selected");
            self.refreshAnnotationsTabsFunc[selected].refresh.call();
        }
    },
    refreshAnnotations: function (idTerm, el) {
        new AnnotationCollection({image: this.model.id, term: idTerm}).fetch({
            success: function (collection, response) {
                el.empty();
                var view = new AnnotationView({
                    page: undefined,
                    model: collection,
                    el: el
                }).render();

            }
        });
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var el = $("#" + self.browseImageView.divId).find('#annotationsPanel' + self.model.get('id'));

        el.html(_.template(tpl, {id: self.model.get('id')}));
        new ProjectModel({id: window.app.status.currentProject}).fetch({
            success: function (model, response) {
                self.createTabs(model.get("ontology"));
            }
        });

        var speed = 500;
        var annotationsPanelBig = $(".annotations-panel-big");
        var annotationsPanelMini = $(".annotations-panel-mini");
        $(".show-annotations-panel-big").on("click", function() {
            annotationsPanelMini.animate({
                bottom : -20
            }, speed, function () {
                annotationsPanelMini.hide();
                annotationsPanelBig.show().animate({
                    bottom : 0
                })
            });
        });
        $(".hide-annotations-panel-big").on("click", function() {
            annotationsPanelBig.animate({
                bottom : -300
            }, speed, function () {
                annotationsPanelBig.show();
                annotationsPanelMini.show().animate({
                    bottom : 0
                })
            });
        });

        $("div .tabsAnnotation").on('shown.bs.tab','a[data-toggle="tab"]', function (e) {
            e.preventDefault();
            //Refresh selected tab
            var idTerm = $(this).attr("data-term");
            var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object) {
                return (object.idTerm == idTerm);
            });
            if (obj) obj.refresh.call();
        });

        return this;
    }
});
