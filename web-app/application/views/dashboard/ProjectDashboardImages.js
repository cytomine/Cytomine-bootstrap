var ProjectDashboardImages = Backbone.View.extend({
    imagesView : null, //image view
    imagesTabsView : null,
    refreshImagesThumbs : function() {
        if (this.imagesView == null) {
            console.log("ProjectDashBoardImage:refreshImagesThumbs if");
            this.imagesView = new ImageView({
                page : 0,
                model : new ImageInstanceCollection({project:this.model.get('id')}),
                el:$("#tabs-projectImageThumb"+this.model.get('id')),
                container : window.app.view.components.warehouse
            }).render();
        } else {
            console.log("ProjectDashBoardImage:refreshImagesThumbs else");
            this.imagesView.refresh();
        }
    },
    refreshImagesTable : function() {
        if(this.imagesTabsView==null) {
            console.log("ProjectDashBoardImage:refreshImagesTable if");
            this.imagesTabsView = new ImageTabsView({
                model : new ImageInstanceCollection({project:this.model.get('id')}),
                el:$("#tabs-projectImageListing"+this.model.get('id')),
                container : window.app.view.components.warehouse,
                idProject : this.model.id
            }).render();
        } else {
            console.log("ProjectDashBoardImage:refreshImagesTable else");
            this.imagesTabsView.refresh();
        }
    }

});