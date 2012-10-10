var CrudGridView = Backbone.View.extend({

    customFields:{
        color:{
            colorPickerElement:function (value, options) {
                var el = _.template('<input type="text" name="color" value="<%=   value %>">', { value:value});
                setTimeout(function () {
                    $("#color").ColorPicker({
                        color:value,
                        onShow:function (colorPicker) {
                            $(colorPicker).fadeIn(500);
                            return false;
                        },
                        onSubmit:function (hsb, hex, rgb, el) {
                            $(el).val("#" + hex);
                            $(el).ColorPickerHide();
                        },
                        onBeforeShow:function () {
                            $(this).ColorPickerSetColor(this.value);
                        },
                        onHide:function (colorPicker) {
                            $(colorPicker).fadeOut(500);
                            return false;
                        }
                    });
                }, 200);
                return el;
            },
            colorPickerValue:function (elem, operation, value) {
                if (operation === 'get') {
                    return $(elem).val();
                } else if (operation === 'set') {
                    $('input', elem).val(value);
                }
            }
        }
    },
    initialize:function (options) {
        this.title = options.title;
        this.pager = "pager" + this.title;
        this.colNames = options.colNames;
        this.colModel = options.colModel;
        this.url = options.url;
        this.restURL = options.restURL;
        if (this.restURL != undefined && this.restURL.charAt(this.restURL.length - 1) != "/") this.restURL += "/";
    },

    render:function () {
        var self = this;
        require(["text!application/templates/utils/CrudGridView.tpl.html"],
            function (tpl) {
                self.doLayout(tpl);
            });
    },

    doLayout:function (tpl) {
        var html = _.template(tpl, {pager:this.pager, title:this.title});
        $(this.el).html(html);
        this.initGrid();
        this.renderTable();
        this.initToolbar();
    },

    initGrid:function () {
        jQuery.extend(
            jQuery.jgrid.edit, {
                ajaxEditOptions:{ contentType:"application/json" },
                recreateForm:true,
                serializeEditData:function (postData) {
                    return JSON.stringify(postData);
                },
                afterSubmit:function (response, postdata) {
                    var res = jQuery.parseJSON(response.responseText);
                    return [true, "", res.d];
                }
            }
        );
        jQuery.extend(
            jQuery.jgrid.del, {
                ajaxDelOptions:{ contentType:"application/json" },
                recreateForm:true,
                serializeEditData:function (postData) {
                    return JSON.stringify(postData);
                },
                afterSubmit:function (response, postdata) {
                    var res = jQuery.parseJSON(response.responseText);
                    return [true, "", res.d];
                }
            }
        );
    },

    initToolbar:function () {
        var self = this;
        var dialogWidth = 600;
        var dialogHeight = 500;
        var left = $(window).width() / 2 - (dialogWidth / 2);
        var top = $(window).height() / 2 - (dialogHeight / 2);
        var grid = $(self.el).find(".grid");
        //Add button
        var addButton = $(this.el).find(".crud-toolbar").find("a[name=add]");
        addButton.button({
            text:true,
            icons:{
                primary:"ui-icon-plus"

            }
        });
        addButton.click(function () {
            grid.jqGrid('editGridRow', 'new', {
                top:top,
                left:left,
                width:dialogWidth,
                height:dialogHeight,
                mtype:'POST',
                reloadAfterSubmit:true,
                modal:true,
                closeOnEscape:true,
                closeAfterAdd:true,
                url:self.restURL,
                afterSubmit:function (response, postdata) {
                    //Add roles to the new user
                    var responseJSON = $.parseJSON(response.responseText);
                    _.each(postdata.authorities.split(","), function (authority) {
                        new UserSecRole({ user:responseJSON.user.id, role:authority}).save();
                    });
                    return [true, "", null]
                }
            });
        });
        //Edit button
        var editButton = $(this.el).find(".crud-toolbar").find("a[name=edit]");
        editButton.button({
            text:true,
            icons:{
                primary:"ui-icon-pencil"

            }
        });
        editButton.click(function () {
            var gr = grid.jqGrid('getGridParam', 'selrow');
            var row = grid.getRowData(gr);
            if (gr != null) {
                grid.jqGrid('editGridRow', gr, {
                    top:top,
                    left:left,
                    width:dialogWidth,
                    height:dialogHeight,
                    mtype:'PUT',
                    reloadAfterSubmit:true,
                    modal:true,
                    closeOnEscape:true,
                    closeAfterEdit:true,
                    url:self.restURL + row['id']
                });
            } else {
                window.app.view.message("Error", "Please select a row", "error");
            }
        });
        //Delete button
        var deleteButton = $(this.el).find(".crud-toolbar").find("a[name=delete]");
        deleteButton.button({
            text:true,
            icons:{
                primary:"ui-icon-trash"

            }
        });
        deleteButton.click(function () {
            var gr = grid.jqGrid('getGridParam', 'selrow');
            var row = grid.getRowData(gr);
            if (gr != null) grid.jqGrid('delGridRow', gr, {
                top:top,
                left:left,
                width:dialogWidth,
                height:dialogHeight,
                mtype:'DELETE',
                reloadAfterSubmit:false,
                beforeSubmit:function (postdata, formid) {
                    new UserModel({id:postdata}).fetch({success:function (model, response) {
                        var authorities = model.get("authorities");
                        _.each(authorities.split(","), function (authority) {
                            alert(authority);
                        });
                    }});

                    alert(postdata);
                    return[true, ""];
                },
                modal:true,
                closeOnEscape:true,
                closeAfterEdit:true,
                url:self.restURL + row['id']
            });
            else {
                window.app.view.message("Error", "Please select a row", "error");
            }
        });
    },

    renderTable:function () {
        var self = this;
        var grid = $(self.el).find(".grid");
        grid.jqGrid({
            datatype:"json",
            url:this.url,
            width:900,
            height:500,
            colNames:this.colNames,
            colModel:this.colModel,
            onSelectRow:function (id) {
            },
            loadComplete:function (data) {
                grid.setGridWidth(Math.max(500, $(self.el).width() - 300), true);
                grid.setGridHeight(Math.max(300, $(self.el).height() - 500), true);
            },
            sortname:'id',
            viewrecords:true,
            sortorder:"asc",
            caption:this.title,
            modal:false,
            pager:"#" + this.pager,
            editurl:self.restURL,
            jsonReader:{
                repeatitems:false,
                id:"0"
            }
        });

        $(window).bind('resize',function () {
            grid.setGridWidth(Math.max(500, $(self.el).width() - 300), true);
            grid.setGridHeight(Math.max(300, $(self.el).height() - 200), true);
        }).trigger('resize');
    }

});

