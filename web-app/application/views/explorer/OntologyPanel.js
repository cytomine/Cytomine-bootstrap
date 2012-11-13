/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */

var OntologyPanel = Backbone.View.extend({
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
                var ontology = new OntologyModel({id:idOntology}).fetch({
                    success:function (model, response) {
                        self.ontologyTreeView = new OntologyTreeView({
                            el:$('#'+self.browseImageView.divId).find("#ontologyTree" + self.model.get("id")),
                            browseImageView:self.browseImageView,
                            model:model
                        }).render();
                        self.callback(self.ontologyTreeView);
                    }
                });

                require([
                    "text!application/templates/explorer/OntologyTree.tpl.html"
                ], function (tpl) {
                    self.doLayout(tpl);
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

        new DraggablePanelView({
            el:$('#'+this.browseImageView.divId).find('#ontologyTree' + this.model.get('id')),
            className:"ontologyPanel",
            template:_.template(tpl, {id:this.model.get('id')})
        }).render();
    }
});
