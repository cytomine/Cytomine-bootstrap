/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 9/01/11
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */

// Path to the blank image should point to a valid location on your server
Ext.BLANK_IMAGE_URL = './extjs/resources/images/default/s.gif';

Ext.onReady(function(){

    Ext.QuickTips.init();

    var successMsg = function(title, msg) {
        Ext.Msg.show({
            title: title,
            msg: msg,
            minWidth: 200,
            modal: true,
            icon: Ext.Msg.INFO,
            buttons: Ext.Msg.OK
        });
    };

    var failureMsg = function(title, msg) {
        Ext.Msg.show({
            title: title,
            msg: msg,
            minWidth: 200,
            modal: true,
            icon: Ext.Msg.ERROR,
            buttons: Ext.Msg.OK
        });
    };

    var loginForm = new Ext.form.FormPanel({
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
            text: 'Submit',
            handler: function(){
                if(loginForm.getForm().isValid()){
                    loginForm.getForm().submit({
                        url: 'http://localhost:8080/cytomine-web/j_spring_security_check',
                        waitMsg: 'Processing Request',
                        success: function(loginForm, resp){
                            successMsg('Success', 'Welcome to Cytomine "'+ resp.result.username);
                            window.location = resp.result.followUrl;
                        },
                        failure : function(loginForm, resp) {
                            //alert(resp.result.error); //TO DO error is null but why ?
                            failureMsg('Error', 'Oops ');
                        }
                    });
                }
            }
        }]
    });

    var loginWindow = new Ext.Window({
        title: 'Welcome to Cytomine',
        layout: 'fit',
        height: 160,
        width: 280,
        closable: false,
        resizable: false,
        draggable: false,
        items: [loginForm]
    });

    loginWindow.show();

}); //end onReady
