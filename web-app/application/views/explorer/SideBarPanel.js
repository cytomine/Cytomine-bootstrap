/**
 * Created with IntelliJ IDEA.
 * User: stevben
 * Date: 23/01/13
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

var SideBarPanel = Backbone.View.extend({

    initToggle: function (el, elContent, sourceEvent, storageKey) {
        var self = this;
        sourceEvent.click(function (e) {
            if (elContent.is(':hidden')) {
                self.show($(this), elContent, storageKey);
            } else {
                self.hide($(this), elContent, storageKey);
            }
        });

        if (storageKey && window.localStorage.getItem(storageKey)) {
            var pref = window.localStorage.getItem(storageKey);
            if (pref.visible) {
                this.show(sourceEvent, elContent, storageKey);
            } else {
                this.hide(sourceEvent, elContent, storageKey);
            }
        }
    },

    show: function (link, elContent, storageKey) {
        if (storageKey) {
            window.localStorage.setItem(storageKey, { visible: true});
        }
        link.removeClass("glyphicon-plus");
        link.addClass("glyphicon-minus");
        elContent.show();
    },

    hide: function (link, elContent, storageKey) {
        if (storageKey) {
            window.localStorage.setItem(storageKey, { visible: false});
        }
        link.addClass("glyphicon-plus");
        link.removeClass("glyphicon-minus");
        elContent.hide();
    }
});

