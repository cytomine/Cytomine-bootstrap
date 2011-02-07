/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 28/01/11
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */

Cytomine.Dashboard = {


    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
        return new Ext.Panel({
            id: 'Dashboard',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Dashboard',
            items: [
            //    Cytomine.Security.User.grid(),
                Cytomine.Rest.Project.grid()
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

