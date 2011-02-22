Ext.namespace('Cytomine');

Cytomine.Admin = {


    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
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

