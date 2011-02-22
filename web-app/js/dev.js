Ext.onReady(function () {

    /* LOG AUTOMATICALLY FOR DEV*/
    var login = function () {

        var form = Cytomine.auth.loginForm.getForm();
        form.findField("j_username").setValue("stevben");
        form.findField("j_password").setValue("password");
        //form.submit(Cytomine.auth.handleLoginResponse);
    }

    setTimeout(login, 500);
});