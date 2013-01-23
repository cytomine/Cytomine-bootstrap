/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */

var OntologyPanel = SideBarPanel.extend({
    tagName:"div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize:function (options) {
        this.ontologyTreeView = null;
        this.callback = options.callback;
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render:function () {
        var self = this;
        new ProjectModel({id:window.app.status.currentProject}).fetch({
            success:function (model, response) {
                var idOntology = model.get('ontology');

                require([
                    "text!application/templates/explorer/OntologyTree.tpl.html"
                ], function (tpl) {
                    self.doLayout(tpl);
                    new OntologyModel({id:idOntology}).fetch({
                        success:function (model, response) {
                            self.ontologyTreeView = new OntologyTreeView({
                                el:$("#ontologytreecontent" + self.model.get("id")),
                                browseImageView:self.browseImageView,
                                model:model
                            }).render();
                            self.callback(self.ontologyTreeView);
                        }
                    });
                });
            }
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout:function (tpl) {
        var el = $('#ontologyTree' + this.model.get('id'));
        el.html(_.template(tpl, {id:this.model.get('id')}));
        var elContent = el.find(".ontologytreecontent");
        var sourceEvent = el.find(".toggle-content");
        this.initToggle(el, elContent, sourceEvent, "ontologytreecontent");

    }
});
