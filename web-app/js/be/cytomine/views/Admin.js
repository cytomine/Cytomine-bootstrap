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
            id: 'Admin',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Admin',
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



