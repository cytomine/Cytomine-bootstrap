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
            id: 'Projects',
            bodyCssClass: 'overflow-auto',
            iconCls: 'layer-stack',
            title: 'Projects',
            items: [
                Cytomine.Views.Project.grid(), Cytomine.Views.Project.detailPanel
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

