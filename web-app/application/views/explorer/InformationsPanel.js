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
            "text!application/templates/explorer/InformationsPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var content = _.template(tpl, self.model.toJSON());
        $('#informationsPanel' + self.model.get('id')).html(content);
        var el = $('#informationsPanel' + self.model.get('id'));
        var elContent1 = el.find(".infoPanelContent1");
        var sourceEvent1 = el.find(".toggle-content1");
        var elContent2 = el.find(".infoPanelContent2");
        var sourceEvent2 = el.find(".toggle-content2");
        this.initToggle(el, elContent1, sourceEvent1, "infoPanelContent1");
        this.initToggle(el, elContent2, sourceEvent2, "infoPanelContent2");

        $("#getNext"+self.model.id).click(function() {
            new ImageInstanceModel({next:true, id:self.model.id}).fetch({
                success:function (model, response) {
                    if(model.get('project')) {

                        if(self.browseImageView.divPrefixId=='tabs-image' || model.get('reviewUser')!=null) {
                            window.location = "#" + self.browseImageView.divPrefixId + "-"+model.get('project')+"-"+model.id+"-";
                            $("#closeTab" +self.browseImageView.divPrefixId+ "-"+self.model.id).click();
                        } else {
                            new ImageReviewModel({id: model.id}).save({}, {
                                success: function (model, response) {
                                    window.app.view.message("Image", response.message, "success");
                                    window.location = '#'+ self.browseImageView.divPrefixId  + '-' + self.model.get('project') + '-' + response.imageinstance.id + '-';
                                    $("#closeTab" +self.browseImageView.divPrefixId+ "-"+ self.model.id).click();
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

                        if(self.browseImageView.divPrefixId=='tabs-image' || model.get('reviewUser')!=null) {
                            window.location = "#" + self.browseImageView.divPrefixId + "-"+model.get('project')+"-"+model.id+"-";
                            $("#closeTab" +self.browseImageView.divPrefixId+ "-"+self.model.id).click();
                        } else {
                            new ImageReviewModel({id: model.id}).save({}, {
                                success: function (model, response) {
                                    window.app.view.message("Image", response.message, "success");
                                    window.location = '#'+ self.browseImageView.divPrefixId  + '-' + self.model.get('project') + '-' + response.imageinstance.id + '-';
                                    $("#closeTab" +self.browseImageView.divPrefixId+ "-"+ self.model.id).click();
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
