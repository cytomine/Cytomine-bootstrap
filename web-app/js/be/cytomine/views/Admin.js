Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Store');



Cytomine.Admin = {


    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
        //var userView = new Cytomine.Views.User();
        return new Ext.Panel({
            id: 'Users',
            bodyCssClass: 'overflow-auto',
            iconCls: 'user_gray',
            title: 'Users',
            closable:true,
            items: [
                Cytomine.Views.User.grid()
        ],
        listeners : {
            show: function(p) {
                if (Cytomine.toolbar != null) Cytomine.toolbar.hide();
                if (Cytomine.overview != null) Cytomine.overview.hide();
            }
        }

    });

}
};



