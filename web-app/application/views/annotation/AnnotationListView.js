/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var AnnotationListView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    initialize: function(options) {
        this.container = options.container;
        this.idAnnotation = options.idAnnotation;
    },

    render : function () {
        var self = this;
        require([
            "text!application/templates/annotation/AnnotationList.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });

        return this;
    },
    doLayout: function(tpl) {
        console.log("AnnotationView.render");

        var self = this;
        $(this.el).html(_.template(tpl, {name:"name",area : "area"}));

        console.log("AnnotationView:"+self.model.length);
        self.model.each(function(annotation) {
            //$("#annotationList").append(annotation.get('name') + " <br>");
            var name = annotation.get('name');
            var area = annotation.get('area');
            //$("#tableImage").append("<tr><th>"+ name +"</th><th>" + area + "</th></tr>");

        });
        // $('#tableImage').dataTable();


        var grid;
        var i=0;
        var data = [];
        self.model.each(function(image) {
            data[i] = {
                id: image.id,
                filename: image.get('filename'),
                created: ''
            };
            i++;
        });




 $("#list2").jqGrid({
   	url:'http://localhost:8080/cytomine-web/api/image.json',
	datatype: "json",
   	colNames:['id','filename'],
   	colModel:[
   		{name:'id',index:'id', width:300},
   		{name:'filename', width:400}
   	],
   	rowNum:10,
   	rowList:[10,20,30],
   	pager: '#pager2',
   	sortname: 'id',
    viewrecords: true,
    sortorder: "desc",
    caption:"JSON Example"
});
jQuery("#list2").jqGrid('navGrid','#pager2',{edit:false,add:false,del:false});











$("#list3").jqGrid({
   	url:'api/image.json',
	datatype: "local",
    heighh: 500,
   	colNames:['id','filename'],
   	colModel:[
   		{name:'id',index:'id', width:300},
   		{name:'filename',index:'filename', width:300}
   	],
    rowNum:10,
   	pager: '#pager3',
   	sortname: 'id',
    viewrecords: true,
    sortorder: "asc",
    caption:"Array Example"
});
jQuery("#list3").jqGrid('navGrid','#pager3',{edit:false,add:false,del:false});



for(var j=0;j<=data.length;j++) {
    console.log("addRowData");
	jQuery("#list3").jqGrid('addRowData',j+1,data[j]);
}


       $("#list3").jqGrid('sortGrid','filename',false);
      // $("#list3").jqGrid('sortGrid','filename',true);









        return this;
    },
    /**
     * Init annotation tabs
     */
    initAnnotation : function(){
        var self = this;







    }
});
