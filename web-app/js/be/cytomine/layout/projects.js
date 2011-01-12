Ext.namespace('ULg');

/**
 * @class ULg.Projects
 * @singleton
 */
ULg.Projects = {
    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
        var view = new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                url: '/cytomine-web/api/scan.json',
                autoLoad: true,
                root: 'scan',
                fields:[
                    'id','filename', {name:'path', mapping:'data.path'}
                ]
            }),
            tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="{id}">',
                    '<div class="thumb"><img src="http://139.165.108.140:38/adore-djatoka/resolver?url_ver=Z39.88-2004&rft_id={path}&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.scale=192" title="{filename}"></div>',
                    '<span class="x-editable">{filename}</span></div>',
                    '</tpl>'
            ),
            listeners: {
                click: function(dataview, index, node, e) {
                    alert(dataview.getStore().getAt(index).get('filename'));
                    /*ULg.annotations.Manager.items = [];
                     ULg.Viewer.show(dataview.getStore().getAt(index).get('id'),
                     dataview.getStore().getAt(index).get('path'));*/
                }
            }
        });

        /*
         var images = new Ext.Panel({
         id:'images',
         title:'Images',
         region:'center',
         margins: '5 5 5 0',
         layout:'fit',

         items: view
         });
         */

        return new Ext.Panel({
            id: 'scans',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: ULg.lang.Projects.title,
            items: [view]
        });
    }
};
