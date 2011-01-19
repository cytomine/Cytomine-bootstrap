Ext.namespace('Cytomine.auth');

// Path to the blank image should point to a valid location on your server
Ext.BLANK_IMAGE_URL = './extjs/resources/images/default/s.gif';



Ext.onReady(function(){

    Ext.QuickTips.init();

    Cytomine.auth.loginForm = new Ext.form.FormPanel({
        frame:true,
        renderTo: 'login',
        width:280,
        labelWidth:80,
        defaults: {
            width: 165
        },
        items: [
            new Ext.form.TextField({
                id:"j_username",
                fieldLabel:"Login",
                allowBlank:false,
                blankText:"Enter your username"
            }),
            new Ext.form.TextField({
                id:"j_password",
                fieldLabel:"Password",
                inputType: 'password',
                allowBlank:false,
                blankText:"Enter your password"
            }),
            new Ext.form.Checkbox({
                id:"remember_me",
                fieldLabel:"Remember me"
            })
        ],
        buttons: [{
            id : 'submit_login',
            text: 'Submit',
            handler: function(){
                if(Cytomine.auth.loginForm.getForm().isValid()){
                    Cytomine.auth.loginForm.getForm().submit(Cytomine.auth.handleLoginResponse);
                }
            }
        }],
        keys: [
            { key: [Ext.EventObject.ENTER], handler: function() {
                Cytomine.auth.loginForm.getForm().submit(Cytomine.auth.handleLoginResponse)
            }
            }
        ]
    });


    Cytomine.auth.handleLoginResponse =  {
        url: '/cytomine-web/j_spring_security_check',
        waitMsg: 'Processing Request',
        success: function(loginForm, resp){
            //alert(resp.result.followUrl);
            window.location = resp.result.followUrl;
            //successMsg('Success', 'Welcome to Cytomine "'+ resp.result.username);

        },
        failure : function(loginForm, resp) {
            //alert(resp.result.error); //TO DO error is null but why ?
            failureMsg('Error', 'Oops ');
        }
    };

    var loginWindow = new Ext.Window({
        title: 'Welcome to Cytomine',
        layout: 'fit',
        height: 160,
        width: 280,
        closable: false,
        resizable: false,
        draggable: false,
        items: [Cytomine.auth.loginForm]
    });

    loginWindow.show();

}); //end onReady
