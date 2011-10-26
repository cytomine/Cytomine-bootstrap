var SoftwareModel = Backbone.Model.extend({
   url : function() {
      var base = 'api/software';
      var format = '.json';
      if (this.isNew()) return base + format;
      return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
   }
});