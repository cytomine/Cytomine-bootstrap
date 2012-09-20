var JsonMin = {

    conversion : null,

    createConvertTable : function() {
        this.conversion = new Object();
        this.conversion["class"]="cl0";
    },

    getCompressKey : function(key) {
        var compressKey = this.conversion[key];
        if(compressKey!=undefined) return compressKey
        else {
            console.log("Oups! The key " + key+ " has no its corresponding compress key! This model has not this attributs or there is a bug in compression :-)");
            return key;
        }
    }
};
