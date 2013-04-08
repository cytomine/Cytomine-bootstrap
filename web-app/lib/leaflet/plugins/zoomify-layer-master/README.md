# ZoomifyLayer


ZoomifyLayer allows you to display [Zoomify](http://www.zoomify.com/ "Zoomify") images with the [Leaflet](http://leafletjs.com/ "Leaflet") mapping library.

# Usage:

Create a map and set its position:

```javascript
var map = new L.Map('map');
map.setView(new L.LatLng(0,0), 0);
```

Then create the layer itself and add it to the map.

```javascript
var imageUrl = 'http://mapy.vugtk.cz/kreibich/zoomify/beroun/',
    imageSize = { width: 7112, height: 5377};

var layer = new ZoomifyLayer(imageUrl, imageSize);
map.addLayer(layer);
```

The map will automatically center the image and zoom it as much as possible while still displaying the whole image.

For a working (though minimal) example look in the demo directory.
