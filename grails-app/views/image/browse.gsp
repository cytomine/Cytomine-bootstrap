<%--
  Created by IntelliJ IDEA.
  User: stevben
  Date: 13/01/11
  Time: 13:27
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>${scan.filename}</title>
  <style type="text/css">
  .olControlPanel div {
    display:block;
    width:  24px;
    height: 24px;
    position : fixed;
    padding-top:200px;
    margin: 5px;
    background-color:white;
  }

  .olControlPanel .olControlMouseDefaultsItemActive {
    width:  22px;
    height: 22px;
    background-color: blue;
    background-image: url("http://localhost:8080/cytomine-web/theme/default/img/pan_on.png");
  }
  .olControlPanel .olControlMouseDefaultsItemInactive {
    width:  22px;
    height: 22px;
    background-color: orange;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/theme/default/img/pan_off.png");
  }
  .olControlPanel .olControlDrawFeatureItemActive {
    width:  22px;
    height: 22px;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/theme/default/img/draw_line_on.png");
  }
  .olControlPanel .olControlDrawFeatureItemInactive {
    width:  22px;
    height: 22px;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/theme/default/img/draw_line_off.png");
  }
  .olControlPanel .olControlZoomBoxItemInactive {
    width:  22px;
    height: 22px;
    background-color: orange;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/img/drag-rectangle-off.png");
  }
  .olControlPanel .olControlZoomBoxItemActive {
    width:  22px;
    height: 22px;
    background-color: blue;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/img/drag-rectangle-on.png");
  }
  .olControlPanel .olControlZoomToMaxExtentItemInactive {
    width:  18px;
    height: 18px;
    background-image: url("http://localhost:8080/cytomine-web/js/openlayers/img/zoom-world-mini.png");
  }

  </style>
  <script type="text/javascript">
    function selected (evt) {
      // alert(evt.feature.id + " selected on " + this.name);
    }
    var layerAnnotation;
    Ext.namespace('Cytomine');

    function initOpenLayers(){
      Ext.Ajax.request({
        url : '/cytomine-web/api/image/${scan.id}.json',
        success: function (response) {
          var image = Ext.decode( response.responseText );
          var scan = new Cytomine.Project.Scan(${urls}, image.id, image.filename, image.path, image.metadataUrl);
          console.log(image.metadataUrl);
          layerAnnotation = new Cytomine.Project.AnnotationLayer( "totolayer", ${scan.id});
          layerAnnotation.loadToMap(scan);
          layerAnnotation.loadAnnotations(scan);

          Cytomine.scans[${scan.id}] = scan;
          Cytomine.annotationLayers[${scan.id}] = layerAnnotation;
          Cytomine.currentLayer = Cytomine.annotationLayers[${scan.id}];
          document.getElementById('noneToggle').checked = true;
        },
        failure: function (response) { console.log('failure : ' + response.responseText);}
      });

    }

    Ext.onReady(function () {
      initOpenLayers();
      if (Cytomine.overview != null) {
        Cytomine.overview.close();
        Cytomine.overview = null;
      }
      Cytomine.overview = new Ext.Window({
        id : 'overviewMapPanel',
        title  : 'Overview',
        el : 'overviewMap',
        x : 10,
        y : 500
      }).show();
    });

  </script>

</head>
<body onload="init()">
<div id="map${scan.id}"></div>
<div id="overviewMap"></div>

</body>
</html>