var ImageTabsView = Backbone.View.extend({
    tagName : "div",
    images : null, //array of images that are printed
    idProject : null,
    imagesProject : null, //collection containing the images contained in the project
    searchPanel : null,
    initialize: function(options) {
        this.idProject = options.idProject;
        this.container = options.container;
        this.imagesProject = options.model;
        this.listproject = "listimage"+this.idProject;
        this.pageproject = "pagerimage"+this.idProject;
        this.tab = 2;
        this.timeoutHnd = null
    },
    render: function() {
        var self = this;
        /*$(self.el).empty();
         $(self.el).append('<table id=\"listimage'+self.idProject+'\" align=\"center\"></table><div id=\"pagerimage'+this.idProject+'\"></div>');*/
        self.renderImageTable();
        /*self.renderImageListProject();
         self.loadDataImageListProject(self.model);*/
    },
    refresh : function() {
        this.refreshImageList();
    },
    refreshImageList : function() {

        var self = this;
        //clear grid
        $("#"+self.listproject).jqGrid("clearGridData", true);
        //get imagesproject on server
        self.imagesProject.fetch({
            success : function (collection, response) {
                //print data from project image table
                self.loadDataImageListProject(collection);
            }
        });
    },
    loadDataImageListProject : function(collection) {
        var self = this;
        var data = new Array();
        var i = 0;

        collection.each(function(image) {
            var url = _.template("<a href='' class='btn btn-primary' style='color : #FFF'';><i class='icon-eye-open icon-white'></i> Explore</a>",{});
            //'<a href="#tabs-image-'+self.idProject+'-' + image.id + '-" class="btn btn-primary" style="color : #FFF";><i class="icon-eye-open icon-white"></i> Explore</a>';
            data[i] = {
                id: image.id,
                base : image.get('baseImage'),
                thumb : image.get('thumb'),
                filename: image.get('filename'),
                slide : image.get('slide'),
                type : image.get('mime'),
                width : image.get('width'),
                height : image.get('height'),
                magnification : image.get('magnification'),
                resolution : image.get('resolution'),
                annotations : image.get('numberOfAnnotations'),
                added : image.get('created'),
                See : url
            };
            i++;
        });

        for(var j=0;j<data.length;j++) {
            $("#"+self.listproject).jqGrid('addRowData',data[j].id,data[j]);
        }
        $("#"+self.listproject).jqGrid('sortGrid','filename',false);
    },
    renderImageTable : function () {
        var self = this;
        self.dataTablesBootstrap();
        self.imagesProject.fetch({
            success : function (collection, response) {
                //print data from project image table
                var tbody = $('#projectImageTable'+self.idProject).find("tbody");
                var exploreButtonTpl = "<a href='#tabs-image-<%= project %>-<%= image %>-' class='btn btn-primary' style='color : #FFF'';><i class='icon-eye-open icon-white'></i> Explore</a>";
                var thumbImgTpl = "<img src='<%= thumb %>' alt='<%= filename %>' style='max-height: 75px;'/>";
                var rowTpl = "<tr><td><%= thumImg %></td><td><%= filename %></td><td><%= mime %></td><td><%= width %></td><td><%= height %></td><td><%= magnification %></td><td><%= resolution %></td><td><%= numberOfAnnotations %></td><td><%= created %></td><td><%= action %></td></tr>"
                collection.each(function (image) {
                 var exploreButton = _.template(exploreButtonTpl,{ project : self.idProject, image : image.get("id")});
                 var thumImg = _.template(thumbImgTpl,{ thumb : image.get("thumb"), filename : image.get("filename")});
                 image.set({"action" : exploreButton});
                 image.set({"thumImg" : thumImg});
                 tbody.append(_.template(rowTpl, image.toJSON()));
                 });

                $('#projectImageTable'+self.idProject).dataTable( {
                    "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records per page"
                    }
                    /*"bProcessing": true,
                    "bServerSide": true,
                    "sAjaxSource":  self.imagesProject.url() + "?dataTables=true"*/
                });

                $('#projectImageListing'+self.idProject).hide();
                $('#projectImageTable'+self.idProject).show();
            }
        });
    },
    renderImageListProject : function() {
        var self = this;
        var thumbColName = 'thumb';

        var container = $("#tabs-images-"+self.idProject);
        var gridWidth = container.width() ; //90% of the windows. JQGrid does not like % as width
        var gridHeight = $(window).height() - 235;
        $("#"+self.listproject).jqGrid({
            datatype: "local",
            width: gridWidth,
            height:gridHeight,
            colNames:['id','base','Thumb','Filename','Format','sample','Width', 'Height', 'Magnification', 'Resolution','Annotations','Added','Action'],
            colModel:[
                {name:'id',index:'id', width:1, sorttype:"int"},
                {name:'base',index:'base', width:1, sorttype:"int"},
                {name:thumbColName,index:thumbColName, width:40},
                {name:'filename',index:'filename', width:220},
                {name:'type',index:'type', width:50},
                {name:'slide',index:'slide', width:50},
                {name:'width',index:'width', width:50},
                {name:'height',index:'height', width:50},
                {name:'magnification',index:'magnification', width:50},
                {name:'resolution',index:'resolution', width:75},
                {name:'annotations',index:'annotations', width:50},
                {name:'added',index:'added', width:90,sorttype:"date",formatter:self.dateFormatter},
                {name:'see',index:'see', width:90,sortable:false}
            ],
            onSelectRow: function(id){
                var checked = $("#"+self.listproject).find("#" + id).find(".cbox").attr('checked');
                if(checked) $("#"+self.listproject).find("#" + id).find("td").css("background-color", "CD661D");
                else $("#"+self.listproject).find("#" + id).find("td").css("background-color", "a0dc4f");
            },
            loadComplete: function() {
                $("#"+self.listproject).find("tr").each(function(index) {
                    if(index!=0) {
                        //0 is not a valid row

                        //replace the text of the thumb by a <img element with its src value
                        var thumbplace = $(this).find('[aria-describedby$="_'+thumbColName+'"]');
                        $(thumbplace).html('<img src="'+$(thumbplace).text()+'" width=30/>');
                    }
                });
            },
            //rowNum:10,
            pager: '#'+self.pageproject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            rowNum:30,
            rowList:[10,30,50,100],
            caption:""
        });
        $("#"+self.listproject).jqGrid('navGrid','#'+self.listproject,{edit:false,add:false,del:false});
        $("#"+self.listproject).jqGrid('hideCol',"id");
        $("#"+self.listproject).jqGrid('hideCol',"base");
        $("#"+self.listproject).jqGrid('hideCol',"slide");

    },
    dateFormatter : function (cellvalue, options, rowObject)
    {
        // do something here
        var createdDate = new Date();
        createdDate.setTime(cellvalue);

        return createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
    },
    dataTablesBootstrap : function () {
        /* Default class modification */
        /* Default class modification */
        $.extend( $.fn.dataTableExt.oStdClasses, {
            "sWrapper": "dataTables_wrapper form-inline"
        } );

        /* API method to get paging information */
        $.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
        {
            return {
                "iStart":         oSettings._iDisplayStart,
                "iEnd":           oSettings.fnDisplayEnd(),
                "iLength":        oSettings._iDisplayLength,
                "iTotal":         oSettings.fnRecordsTotal(),
                "iFilteredTotal": oSettings.fnRecordsDisplay(),
                "iPage":          Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
                "iTotalPages":    Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
            };
        }

        /* Bootstrap style pagination control */
        $.extend( $.fn.dataTableExt.oPagination, {
            "bootstrap": {
                "fnInit": function( oSettings, nPaging, fnDraw ) {
                    var oLang = oSettings.oLanguage.oPaginate;
                    var fnClickHandler = function ( e ) {
                        e.preventDefault();
                        if ( oSettings.oApi._fnPageChange(oSettings, e.data.action) ) {
                            fnDraw( oSettings );
                        }
                    };

                    $(nPaging).addClass('pagination').append(
                            '<ul>'+
                                    '<li class="prev disabled"><a href="#">&larr; '+oLang.sPrevious+'</a></li>'+
                                    '<li class="next disabled"><a href="#">'+oLang.sNext+' &rarr; </a></li>'+
                                    '</ul>'
                    );
                    var els = $('a', nPaging);
                    $(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
                    $(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
                },

                "fnUpdate": function ( oSettings, fnDraw ) {
                    var iListLength = 5;
                    var oPaging = oSettings.oInstance.fnPagingInfo();
                    var an = oSettings.aanFeatures.p;
                    var i, j, sClass, iStart, iEnd, iHalf=Math.floor(iListLength/2);

                    if ( oPaging.iTotalPages < iListLength) {
                        iStart = 1;
                        iEnd = oPaging.iTotalPages;
                    }
                    else if ( oPaging.iPage <= iHalf ) {
                        iStart = 1;
                        iEnd = iListLength;
                    } else if ( oPaging.iPage >= (oPaging.iTotalPages-iHalf) ) {
                        iStart = oPaging.iTotalPages - iListLength + 1;
                        iEnd = oPaging.iTotalPages;
                    } else {
                        iStart = oPaging.iPage - iHalf + 1;
                        iEnd = iStart + iListLength - 1;
                    }

                    for ( i=0, iLen=an.length ; i<iLen ; i++ ) {
                        // Remove the middle elements
                        $('li:gt(0)', an[i]).filter(':not(:last)').remove();

                        // Add the new list items and their event handlers
                        for ( j=iStart ; j<=iEnd ; j++ ) {
                            sClass = (j==oPaging.iPage+1) ? 'class="active"' : '';
                            $('<li '+sClass+'><a href="#">'+j+'</a></li>')
                                    .insertBefore( $('li:last', an[i])[0] )
                                    .bind('click', function (e) {
                                        e.preventDefault();
                                        oSettings._iDisplayStart = (parseInt($('a', this).text(),10)-1) * oPaging.iLength;
                                        fnDraw( oSettings );
                                    } );
                        }

                        // Add / remove disabled classes from the static elements
                        if ( oPaging.iPage === 0 ) {
                            $('li:first', an[i]).addClass('disabled');
                        } else {
                            $('li:first', an[i]).removeClass('disabled');
                        }

                        if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
                            $('li:last', an[i]).addClass('disabled');
                        } else {
                            $('li:last', an[i]).removeClass('disabled');
                        }
                    }
                }
            }
        } );
    }
});
