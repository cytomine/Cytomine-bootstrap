Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Notifications');

/**
 * @class Cytomine.Notifications
 * @singleton
 */
Cytomine.Notifications = {
    /**
     * Affiche un message d'erreur
     * @param {String} msg
     * @param {String} title
     * @param {String} iconCls
     */
    error: function(msg, title, iconCls) {
        /*
        Ext.MessageBox.show({
           title: title == '' ? 'Erreur' : title,
           msg: msg,
           buttons: Ext.MessageBox.OK,
           icon: Ext.MessageBox.ERROR,
           iconCls: iconCls
       });
       */
    },
    /**
     * Affiche un message de confirmation
     * @param {String} msg
     * @param {String} title
     * @param {void} confirmAction Fonction callback appellée si la réponse est "oui"
     * @param {String} iconCls
     * @param {void} cancelAction Fonction callback applée si la réponse est "non" (optionel)
     */
    confirm: function(msg, title, confirmAction, iconCls, cancelAction) {
        Ext.MessageBox.show({
            title: title == '' ? 'Confirmation' : title,
            msg: msg,
            buttons: Ext.MessageBox.YESNO,
            icon: Ext.MessageBox.WARNING,
            iconCls: iconCls,
            fn: function(buttonId) {
                if (buttonId == 'yes')
                    confirmAction();
                else {
                    if (cancelAction)
                        cancelAction();
                }

            }
       });
    }
};