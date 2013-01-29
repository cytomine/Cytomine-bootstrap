/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */

var OverviewMapPanel = SideBarPanel.extend({
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
            "text!application/templates/explorer/OverviewMap.tpl.html"
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
        var content = _.template(tpl, {id: self.model.get('id'), isDesktop: !window.app.view.isMobile});
        $('#overviewMap' + self.model.get('id')).html(content);
        var el = $('#overviewMap' + self.model.get('id'));
        var elContent = el.find(".overview-panel");
        var sourceEvent = el.find(".toggle-content ");
        this.initToggle(el, elContent, sourceEvent, "overview-panel");

    }
});
