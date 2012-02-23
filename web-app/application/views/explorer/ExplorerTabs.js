var ExplorerTabs = Backbone.View.extend({
    tagName : "div",
    triggerRoute : true,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function(options) {
        this.tabs = [], //that we are browsing
                this.container = options.container
    },
    /**
     * Grab the layout and call ask for render
     */
    render : function() {
        var self = this;
        require(["text!application/templates/explorer/Tabs.tpl.html"], function(tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function(tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, {}));
        $(".closeTab" ).live("click",function(e) {
            e.preventDefault();
            var index = $( "li", $(".nav-tabs") ).index( $( this ).parent() );
            var idImage = $(this).attr("data-image");
            self.removeTab(index, idImage);
            self.selectLastTab();
        });
        /*tabs.tabs().find( ".ui-tabs-nav" ).sortable({ axis: "x" });*/
        /*tabs.tabs({
         add: function(event, ui) {

         ///$("#"+ui.panel.id).parent().parent().css('height', "100%");
         if (ui.panel.id != ("tabs-dashboard-"+window.app.status.currentProject)
         && (ui.panel.id != "tabs-images-"+window.app.status.currentProject)
         && ui.panel.id != ("tabs-annotations-"+window.app.status.currentProject)
         && ui.panel.id != ("tabs-algos-"+window.app.status.currentProject)) {
         //Append cross for close
         tabs.find("ul").find("a[href=#"+ui.panel.id+"]").find("span").attr("style", "display:inline;");
         tabs.find("ul").find("a[href=#"+ui.panel.id+"]").append("<span class='ui-icon ui-icon-close' style='display:inline;cursor:pointer;'>&nbsp;&nbsp;&nbsp;&nbsp;</span>");
         tabs.find("ul").find("a[href=#"+ui.panel.id+"]").find("span.ui-icon-close" ).click(function(e) {
         e.preventDefault();
         var index = $( "li", tabs ).index( $( this ).parent() );
         self.removeTab(index);
         });
         //Append open a new window icon
         //tabs.find("ul").find("a[href=#"+ui.panel.id+"]").find("span").attr("style", "display:inline;");
         //tabs.find("ul").find("a[href=#"+ui.panel.id+"]").append("<span class='ui-icon ui-icon-extlink' style='display:inline;cursor:pointer;'>&nbsp;&nbsp;&nbsp;&nbsp;</span>");
         //tabs.find("ul").find("a[href=#"+ui.panel.id+"]").find("span.ui-icon-extlink" ).click(function(e) {
         //    e.preventDefault();
         //    var hash = $(this).parent().attr("href");
         //    var url = "http://" + window.location.host + "/" + hash;
         //    var windowName = "Cytomine (2)";
         //    var index = $( "li", tabs ).index( $( this ).parent() );
         //    self.removeTab(index);
         //    window.app.secondaryWindow = window.open(url, windowName);
         //});
         self.triggerRoute = false;
         tabs.tabs('select', '#' + ui.panel.id);
         self.triggerRoute = true;
         } else {
         //$(ui.panel).css({ 'background-image': 'url(http://subtlepatterns.com/patterns/whitey.png)'});
         }
         //$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;overflow:hidden;');
         },
         show: function(event, ui){
         //$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;overflow:hidden;');
         return true;
         },
         select: function(event, ui) {
         if (self.triggerRoute) window.app.controllers.browse.navigate("#"+ui.panel.id, self.triggerRoute);

         }
         });

         $("ul.tabs a").css('height', $("ul.tabs").height());*/
        return this;
    },
    /**
     *  Add a Tab containing a BrowseImageView instance
     *  @idImage : the id of the Image we want to display
     *  @options : some init options we want to pass the the BrowseImageView Instance
     */
    addBrowseImageView : function(idImage, options) {
        var self = this;
        var tab = this.getBrowseImageView(idImage);
        if (tab != null) {
            tab.view.show(options);
            return;
        }
        var tabs = $("#explorer-tab-content");
        new ImageInstanceModel({id : idImage}).fetch({
            success : function(model, response) {
                var view = new BrowseImageView({
                    model : model,
                    initCallback : function(){view.show(options)},
                    el: tabs
                }).render();
                self.showTab(idImage);
                self.tabs.push({idImage : idImage,view : view});
            }
        });
    },
    /**
     * Return the reference to a BrowseImageView instance
     * contained in a tab
     * @param idImage the ID of an Image contained in a BrowseImageView
     */
    getBrowseImageView : function(idImage) {
        var object  = _.detect(this.tabs, function(object) {
            return object.idImage == idImage;
        });
        return object != null ? object : null;
    },
    /**
     * Remove a Tab
     * @param index the identifier of the Tab
     */
    removeTab : function (index, idImage) {
        this.tabs.splice(index,1);
        var tabs = $(this.el).children('.nav-tab');
        var target = $('#tabs-image-'+idImage).attr("href");
        $(target).remove();
        window.app.controllers.browse.tabs.triggerRoute = false;
        $('#tabs-image-'+idImage).parent().remove();
        window.app.controllers.browse.tabs.triggerRoute = true;
    },
    selectLastTab : function () {
        var tabs = $("#explorer > .browser").find(".nav-tabs");
        tabs.last().find("a").first().click();
    },
    /**
     * Show a tab
     * @param idImage the identifier of the Tab
     */
    showTab : function(idImage) {
        var tabs = $("#explorer > .browser").find(".nav-tabs");
        window.app.controllers.browse.tabs.triggerRoute = false;
        $('a[href=#tabs-image-'+window.app.status.currentProject+'-'+idImage+'-]').click();
        window.app.controllers.browse.tabs.triggerRoute = true;
    },
    /**
     * Return the number of opened tabs
     */
    size : function() {
        return _.size(this.tabs);
    },
    /**
     * Close all the Tabs
     */
    closeAll : function() {
        var tabs = $(this.el).children('.tabs');
        for (var i = tabs.tabs('length') - 1; i >= 0; i--) {
            tabs.tabs('remove', i);
        }
        this.tabs = [];
        tabs.tabs("destroy");
        $(this.el).hide();
        $(this.el).parent().find('.noProject').show();
    },
    /**
     * Add a ProjectDashBoardView instance in the first Tab
     * @param dashboard the ProjectDashBoardView instance
     */
    addDashboard : function(dashboard) {
        var self = this;
        var tabs = $('#explorer-tab-content');
        $(".nav-tabs").append(_.template("<li><a href='#tabs-dashboard-<%= idProject %>' data-toggle='tab'><i class='icon-road' /> Dashboard</a></li>",{ idProject : window.app.status.currentProject}));
        $(".nav-tabs").append(_.template("<li><a href='#tabs-images-<%= idProject %>' data-toggle='tab'><i class='icon-picture' /> Images</a></li>",{ idProject : window.app.status.currentProject}));
        $(".nav-tabs").append(_.template("<li><a href='#tabs-annotations-<%= idProject %>' data-toggle='tab'><i class='icon-pencil' /> Annotations</a></li>",{ idProject : window.app.status.currentProject}));
        $(".nav-tabs").append(_.template("<li><a href='#tabs-algos-<%= idProject %>' data-toggle='tab'><i class='icon-cog' /> Configuration</a></li>",{ idProject : window.app.status.currentProject}));

        $('a[data-toggle="tab"]').live('show', function (e) {
            var hash = this.href.split("#")[1];
            $("#"+hash).attr('style', 'width:100%;height:100%;overflow:hidden;');
            if (self.triggerRoute) window.app.controllers.browse.navigate("#"+hash, self.triggerRoute);
        });


        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
        this.tabs.push({
            idImage : 0,
            view : dashboard
        });
        this.tabs.push({
            idImage : 1,
            view : dashboard
        });
        this.tabs.push({
            idImage : 2,
            view : dashboard
        });
        this.tabs.push({
            idImage : 3,
            view : dashboard
        });
    },
    /**
     * Ask to the dashboard view to refresh
     */
    getDashboard : function () {
        var dashboardTab = _.detect(this.tabs, function(object) {
            return object.idImage == 0;
        });
        return dashboardTab;
    }
});
