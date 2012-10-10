var ProjectDashboardImages = Backbone.View.extend({
    imagesView:null,
    imagesTabsView:null,
    refreshImagesThumbs:function () {
        if (this.imagesView == null) {
            this.imagesView = new ImageView({
                page:0,
                model:new ImageInstanceCollection({project:this.model.get('id')}),
                el:$("#tabs-projectImageThumb" + this.model.get('id')),
                container:window.app.view.components.warehouse
            }).render();
        } else {
            this.imagesView.refresh();
        }
    },
    refreshImagesTable:function () {
        if (this.imagesTabsView == null) {
            this.imagesTabsView = new ImageTabsView({
                model:new ImageInstanceCollection({project:this.model.get('id')}),
                el:$("#tabs-projectImageListing" + this.model.get('id')),
                container:window.app.view.components.warehouse,
                idProject:this.model.id
            }).render();
        } else {
            this.imagesTabsView.refresh();
        }
    }
});