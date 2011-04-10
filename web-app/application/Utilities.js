// Watcher
// -------
// Class for polling a given model (or collection) and firing a callback
// when it changes.
//
// - `model` model to watch
// - `callback` function to call when a change occurs to the model
// - `interval` interval to polling the server (in milliseconds)
var Watcher = function(model, callback, interval) {
    _.bindAll(this, 'fetch', 'destroy');
    var that = this;
    this.model = model;
    this.callback = callback;
    this.interval = interval || 1000;
    this.current = JSON.stringify(this.model);
    this.watcher = setInterval(this.fetch, this.interval);
};

Watcher.prototype.fetch = function() {
    var that = this;
    this.model.fetch({
        silent:true,
        success: function() {
            var state = JSON.stringify(that.model);
            if (that.current !== state) {
                that.current = state;
                that.callback && that.callback();
            }
        },
        error: function() {}
    });
};

Watcher.prototype.destroy = function() {
    window.clearInterval(this.watcher);
};

// Status
// ------
// Class for polling a certain endpoint and firing a callback if it is down.
//
// - `url` URL to poll
// - `callback` function to call if the URL request results in an error
// - `interval` interval to poll the URL (in milliseconds)
var Status = function(url, errorcallback, successcallback, interval) {
    _.bindAll(this, 'start', 'error', 'stop');
    this.url = url;
    this.errorcallback = errorcallback;
    this.successcallback = successcallback;
    this.interval = interval || 1000;
    this.start();
};

Status.prototype.start = function() {
    var self = this;
    var ajaxFn = function() {
        $.ajax({
            url: self.url,
            type: 'GET',
            success : self.successcallback,
            error: self.error
        });
    };
    if (!this.watcher) {
        var that = this;
        this.watcher = setInterval(ajaxFn, this.interval);
    }
};

Status.prototype.error = function() {
    this.stop();
    this.errorcallback(this);
};

Status.prototype.stop = function() {
    if (this.watcher) {
        window.clearInterval(this.watcher);
        delete this.watcher;
    }
};
