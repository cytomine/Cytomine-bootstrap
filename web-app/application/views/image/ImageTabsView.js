var ImageTabsView = Backbone.View.extend({
    tagName : "div",
    images : null, //array of images that are printed
    idProject : null,
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        this.idProject = options.idProject;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;

        console.log("ImageTabsView:"+self.idProject);

        $(self.el).append('<table id=\"tablegrid\"><tr><td  WIDTH=\"50%\"><table id=\"listimage'+self.idProject+'\"></table><div id=\"pagerimage'+this.idProject+'\"></div></td><td width=50></td><td WIDTH=\"40%\">info</td></tr></table>');


        var lastsel;
        $(self.el).find("#listimage"+self.idProject).jqGrid({
            datatype: "local",
            //height: 100%,
            //height: "100%",
            width: "700",
            colNames:['id','filename','type','added','See'],
            colModel:[
                {name:'id',index:'id', width:30},
                {name:'filename',index:'filename', width:300},
                {name:'type',index:'type', width:40},
                {name:'added',index:'added', width:70,sorttype:"date"},
                {name:'See',index:'See', width:150,sortable:false}
            ],
            onSelectRow: function(id){
                if(id && id!==lastsel){
                    alert("Click on "+id);
                    lastsel=id;
                }
            },
            rowNum:10,
            pager: '#pagerimage'+self.idProject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Array Example"
        });
        $(self.el).find("#listimage"+self.idProject).jqGrid('navGrid','#pagerimage'+self.idProject,{edit:false,add:false,del:false});

        var i=0;
        var data = [];

        self.model.each(function(image) {
            console.log(image.get('created'));
            var createdDate = new Date();
            createdDate.setTime(image.get('created'));

            var url = '<a href=\"#tabs-image-'+self.idProject+'-' + image.id + '-\">Click here to see the image</a>'
            console.log(url);
            data[i] = {
                id: image.id,
                filename: image.get('filename'),
                type : image.get('mime'),
                added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate(),
                See : url
            };
            i++;
        });

        for(var j=0;j<=data.length;j++) {
            console.log("addRowData");
            $(self.el).find("#listimage"+self.idProject).jqGrid('addRowData',j+1,data[j]);
        }


        $(self.el).find("#listimage"+self.idProject).jqGrid('sortGrid','filename',false);
        // $("#list3").jqGrid('sortGrid','filename',true);
        return this;
    },
    refresh : function(newImages) {
        var self = this;
    }
});
