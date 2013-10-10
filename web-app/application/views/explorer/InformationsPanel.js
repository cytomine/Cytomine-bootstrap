/**
 * Created with IntelliJ IDEA.
 * User: stevben
 * Date: 23/01/13
 * Time: 12:52
 * To change this template use File | Settings | File Templates.
 */

var InformationsPanel = SideBarPanel.extend({
    tagName: "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/InformationsPanel.tpl.html","text!application/templates/image/ImageReviewAction.tpl.html"
        ], function (tpl,tplaction) {
            self.doLayout(tpl,tplaction);
        });
        return this;
    },




    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl,tplaction) {
        var self = this;
        var json = self.model.toJSON();
        json.originalFilename = self.model.getVisibleName(window.app.status.currentProjectModel.get('blindMode'));
        var content = _.template(tpl,json);
        $('#informationsPanel' + self.model.get('id')).html(content);
        var el = $('#informationsPanel' + self.model.get('id'));
        var elContent1 = el.find(".infoPanelContent1");
        var sourceEvent1 = el.find(".toggle-content1");
        var elContent2 = el.find(".infoPanelContent2");
        var sourceEvent2 = el.find(".toggle-content2");
        this.initToggle(el, elContent1, sourceEvent1, "infoPanelContent1");
        this.initToggle(el, elContent2, sourceEvent2, "infoPanelContent2");


        var content =_.template(tplaction, json);
        var panel = $('#informationsPanel' + self.model.get('id'));
        panel.find("#actionExploreButton").append(content);

        var action = new ImageReviewAction({el:panel.find("#actionExploreButton"),model:self.model, container : this});
        action.configureAction();
        panel.find("#actionExploreButton").find("button").removeClass("btn-inverse")

        $("#getNext"+self.model.id).click(function() {
            new ImageInstanceModel({next:true, id:self.model.id}).fetch({
                success:function (model, response) {
                    if(model.get('project')) {
                        if(self.browseImageView.getMode()=='image' || model.get('reviewUser')!=null) {
                            window.app.controllers.browse.tabs.goToImage(model.id,model.get('project'), self.model.id, self.browseImageView.getMode(),model,null);
                        } else {
                            new ImageReviewModel({id: model.id}).save({}, {
                                success: function (review, response) {
                                    model.fetch({
                                        success:function (model, response2) {
                                           window.app.view.message("Image", response.message, "success");
                                            window.app.controllers.browse.tabs.goToImage(model.id,model.get('project'), self.model.id, self.browseImageView.getMode(),model,null);
                                        }
                                    });
                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Image", json.errors, "error");
                                }});
                        }
                    } else {
                        window.app.view.message("Next image", "This is the last image", "error");
                    }
                }
            });
        });
        $("#getNext"+self.model.id).show();


        $("#getPrevious"+self.model.id).click(function() {
            new ImageInstanceModel({previous:true, id:self.model.id}).fetch({
                success:function (model, response) {
                    if(model.get('project')) {
                        if(self.browseImageView.getMode()=='image' || model.get('reviewUser')!=null) {
                            window.app.controllers.browse.tabs.goToImage(model.id,model.get('project'), self.model.id, self.browseImageView.getMode(),model,null);
                        } else {
                            new ImageReviewModel({id: model.id}).save({}, {
                                success: function (review, response) {
                                    model.fetch({
                                        success:function (model, response2) {
                                            window.app.view.message("Image", response.message, "success");
                                            window.app.controllers.browse.tabs.goToImage(model.id,model.get('project'), self.model.id, self.browseImageView.getMode(),model,null);
                                        }
                                    });
                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Image", json.errors, "error");
                                }});
                        }
                    } else {
                        window.app.view.message("Previous image", "This is the first image", "error");
                    }
                }
            });
        });
        $("#getPrevious"+self.model.id).show();
    }
});
