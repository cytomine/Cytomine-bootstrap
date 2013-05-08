var ExplorerTabs = Backbone.View.extend({
    tagName: "div",
    triggerRoute: true,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.tabs = []; //that we are browsing
        this.container = options.container;
        this.dashboard = null;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require(["text!application/templates/explorer/Tabs.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        $(this.el).html(_.template(tpl, {}));
        return this;
    },
    showLastTab : function(avoid) {
        console.log("avoid="+avoid);
        var i=(this.tabs.length-1);
        while(i>=0) {
            console.log("i="+i);
            console.log(this.tabs[i].idImage);
            if(this.tabs[i].idImage!=avoid) {
                var id = this.tabs[i].view.divPrefixId + "-" + this.tabs[i].idImage;  //$("#tabs-image-16829").tab('show');
                console.log("show:"+id);
                console.log($("#"+id).length);


                var millisecondsToWait = 250;
               setTimeout(function() {
                   // Whatever you want to do after the wait
                   console.log("#"+id);
                   $("#"+id).tab('show');
               }, millisecondsToWait);

                break;
            }
            i--;
        }
    },
    /**
     *  Add a Tab containing a BrowseImageView instance
     *  @idImage : the id of the Image we want to display
     *  @options : some init options we want to pass the the BrowseImageView Instance
     */
    addBrowseImageView: function (idImage, options) {
        var self = this;
        var tab = this.getImageView(idImage);
        if (tab != null) {
            //open tab if already exist
            tab.view.show(options);
            self.showTab(idImage, "image");
            return;
        }
        tab = this.getImageView("review-" + idImage);
        if (tab != null) {
            //close review tab for this image if already exist
            $("#closeTabtabs-review-" + idImage).click()
        }

        var tabs = $("#explorer-tab-content");
        var view = new BrowseImageView({
            initCallback: function () {
                view.show(options)
            },
            el: tabs
        });
        self.tabs.push({idImage: idImage, view: view});

        new ImageInstanceModel({id: idImage}).fetch({
            success: function (model, response) {
                view.model = model;
                view.render();
                $("#closeTabtabs-image-" + idImage).on("click", function (e) {
                    var idImage = $(this).attr("data-image");
                    self.removeTab(idImage, "image");
                    self.showLastTab(idImage);
                });
                self.showTab(idImage, "image");

            }
        });
    },
    addReviewImageView: function (idImage, options) {
        console.log("addReviewImageView:" + idImage);
        var self = this;
        var tab = this.getImageView("review-" + idImage);
        if (tab != null) {
            //open tab if already exist
            tab.view.show(options);
            self.showTab(idImage, "review");
            return;
        }
        tab = this.getImageView(idImage);
        if (tab != null) {
            //close image tab for this image if already exist
            $("#closeTabtabs-image-" + idImage).click()
        }

        var tabs = $("#explorer-tab-content");
        var view = new BrowseImageView({
            initCallback: function () {
                view.show(options)
            },
            el: tabs,
            review: true
        });
        self.tabs.push({idImage: "review-" + idImage, view: view});

        new ImageInstanceModel({id: idImage}).fetch({
            success: function (model, response) {
                view.model = model;
                view.render();
                $("#closeTabtabs-review-" + idImage).on("click", function (e) {
                    var idImage = $(this).attr("data-image");
                    self.removeTab(idImage, "review");
                    self.showLastTab(idImage);
                });
                self.showTab(idImage, "review");

                if (model.get("inReview") == false && model.get("reviewed") == false) {

                    self.removeTab(idImage, "review");
                    window.app.view.message("Review image", "You must first start reviewing picture before review it!", "warning");
                }
            }
        });
    },

    /**
     * Return the reference to a BrowseImageView instance
     * contained in a tab
     * @param idImage the ID of an Image contained in a BrowseImageView
     */
    getImageView: function (idImage) {
        var object = _.detect(this.tabs, function (object) {
            return object.idImage == idImage;
        });
        return object != null ? object : null;
    },
    /**
     * Remove a Tab
     * @param index the identifier of the Tab
     */
    removeTab: function (idImage, prefix) {
        var self = this;
        var browseImageView = null

        if (prefix != "review") {
            browseImageView = this.getImageView(idImage);
        }
        else {
            browseImageView = this.getImageView("review-" + idImage);
        }

        browseImageView.view.stopBroadcastingInterval();
        browseImageView.view.stopWatchOnlineUsersInterval();
        var indexOf = this.tabs.indexOf(browseImageView);

        this.tabs.splice(indexOf, 1);
        var tabs = $('#explorer-tab');
        //Remove Tab
        $('#tabs-' + prefix + '-' + idImage).parent().remove();
        //Remove dropdown
        $('#tabs-' + prefix + '-' + idImage + "-dropdown").parent().remove();
        //Remove content
        $('#tabs-' + prefix + '-' + window.app.status.currentProject + '-' + idImage + '-').remove();

//        console.log("removeTabx");
//        console.log(this.tabs.length);
//        if(this.tabs.length!=0) {
//            var lastTab = this.tabs[this.tabs.length-1];
//            console.log("lastTab");
//            console.log(lastTab);
//            window.location = "#"+lastTab.view.divId;
//        }
    },
    /**
     * Show a tab
     * @param idImage the identifier of the Tab
     */
    showTab: function (idImage, prefix) {
        console.log("showTab2");
        var tabs = $('#explorer-tab');
        window.app.controllers.browse.tabs.triggerRoute = false;
        $('#tabs-' + prefix + '-' + idImage).click();
        window.app.controllers.browse.tabs.triggerRoute = true;
    },
    /**
     * Return the number of opened tabs
     */
    size: function () {
        return _.size(this.tabs);
    },
    /**
     * Close all the Tabs
     */
    closeAll: function () {
        var tabs = $(this.el).children('.tabs');
        this.tabs = [];
        $(this.el).hide();
        $(this.el).parent().find('.noProject').show();
    },
    /**
     * Add a ProjectDashBoardView instance in the first Tab
     * @param dashboard the ProjectDashBoardView instance
     */
    addDashboard: function (dashboard) {
        var self = this;
        this.dashboard = dashboard;
        var tabs = $('#explorer-tab');
        tabs.append(_.template("<li><a id='dashboardLink-<%= idProject %>' href='#tabs-dashboard-<%= idProject %>' data-toggle='tab'><i class='icon-road' /> Dashboard</a></li>", { idProject: window.app.status.currentProject}));
        tabs.append(_.template("<li><a href='#tabs-images-<%= idProject %>' data-toggle='tab'><i class='icon-picture' /> Images</a></li>", { idProject: window.app.status.currentProject}));
        tabs.append(_.template("<li><a href='#tabs-annotations-<%= idProject %>' data-toggle='tab'><i class='icon-pencil' /> Annotations</a></li>", { idProject: window.app.status.currentProject}));
	    tabs.append(_.template("<li><a class='annotationTabLink' href='#tabs-properties-<%= idProject %>' data-toggle='tab'><i class='icon-list' /> Properties</a></li>", { idProject: window.app.status.currentProject}));
        tabs.append(_.template("<li><a href='#tabs-algos-<%= idProject %>' data-toggle='tab'><i class='icon-tasks' /> Jobs</a></li>", { idProject: window.app.status.currentProject}));
        tabs.append(_.template("<li><a href='#tabs-config-<%= idProject %>' data-toggle='tab'><i class='icon-wrench' /> Configuration</a></li>", { idProject: window.app.status.currentProject}));
        tabs.append(_.template("<li><a href='#tabs-reviewdash-<%= idProject %>' data-toggle='tab'><i class='icon-chevron-down' /> Review</a></li>", { idProject: window.app.status.currentProject}));


        $(document).on('click','a[data-toggle="tab"]', function (e) {
            var hash = this.href.split("#")[1];
            $("#" + hash).attr('style', 'width:100%;min-height:500px;overflow:auto;');
            if (self.triggerRoute) {
                window.app.controllers.browse.navigate("#" + hash, self.triggerRoute);
            }
        });

        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
    },
    /**
     * Ask to the dashboard view to refresh
     */
    getDashboard: function () {
        return this.dashboard;
    }
});
