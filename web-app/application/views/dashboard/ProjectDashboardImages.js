var ProjectDashboardImages = Backbone.View.extend({
    imagesView: null,
    imagesTabsView: null,
    refreshImagesThumbs: function () {
        console.log("refreshImagesThumbs");
        if (this.imagesView == null) {
            this.imagesView = new ImageView({
                page: 0,
                model: new ImageInstanceCollection({project: this.model.get('id')}),
                el: $("#tabs-projectImageThumb" + this.model.get('id')),
                container: window.app.view.components.warehouse
            }).render();
        } else {
            this.imagesView.refresh();
        }
    },
    refreshImagesTable: function () {
        console.log("refreshImagesTable");
        if (this.imagesTabsView == null) {
            console.log(this.imagesTabsView);
            this.imagesTabsView = new ImageTabsView({
                model: new ImageInstanceCollection({project: this.model.get('id')}),
                el: $("#tabs-projectImageListing" + this.model.get('id')),
                container: this,
                idProject: this.model.id,
                project: this.model
            }).render();
        } else {
            console.log("this.imagesTabsView.refresh()");
            //this.imagesTabsView.refresh();
        }
    }
});