Ext.namespace('Cytomine.auth');

// Path to the blank image should point to a valid location on your server
Ext.BLANK_IMAGE_URL = './extjs/resources/images/default/s.gif';


var App = new Ext.App({});

Ext.onReady(function(){

    Ext.QuickTips.init();

    Cytomine.auth.loginForm = new Ext.form.FormPanel({
        formId: 'login_form',
        frame:true,
        renderTo: 'login',
        width:300,
        labelWidth:80,
        defaults: {
            width: 185
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

    var footer = new Ext.Panel({
        html : 'footer'
    });


    Cytomine.auth.handleLoginResponse =  {
        url: '../j_spring_security_check',
        waitMsg: 'Processing Request',
        success: function(loginForm, resp){
            window.location = resp.result.followUrl;
        },
        failure : function(loginForm, resp) {
            var jsonData = Ext.util.JSON.decode(resp.response.responseText);


            App.setAlert(false, ""+ jsonData.message);
        }
    };

    var loginWindow = new Ext.Window({
        title: 'Cytomine Restricted Area',
        layout: 'fit',
        iconCls: 'ulg',
        height: 160,
        width: 300,
        closable: false,
        resizable: false,
        draggable: false,
        items: [Cytomine.auth.loginForm]
    });

    loginWindow.show();

}); //end onReady
