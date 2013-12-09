var UploadFormView = Backbone.View.extend({

    statusLabels: {
        uploadedLabel: '<span class="label label-inverse">UPLOADED</span>',
        errorFormatLabel: '<span class="label label-important">ERROR FORMAT</span>',
        convertedLabel: '<span class="label label-info">TO DEPLOY</span>',
        deployedLabel: '<span class="label label-success">DEPLOYED</span>',
        errorConvertLabel: '<span class="label label-important">ERROR CONVERT</span>',
        uncompressed : '<span class="label label-success">UNCOMPRESSED</span>',
        to_deploy : '<span class="label label-info">TO DEPLOY</span>'
    },
    fileUploadErrors: {
        maxFileSize: 'File is too big',
        minFileSize: 'File is too small',
        acceptFileTypes: 'Filetype not allowed',
        maxNumberOfFiles: 'Max number of files exceeded',
        uploadedBytes: 'Uploaded bytes exceed file size',
        emptyResult: 'Empty file upload result'
    },
    initialize: function (options) {
        this.uploadDataTables = null;
    },
    events: {

    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/upload/UploadForm.tpl.html"
        ],
            function (tpl) {

                if(!window.app.status.user.model.get('guest')) {
                    self.doLayout(tpl);
                } else {
                    window.app.view.message("error","Cannot upload if you are guest!",5000);
                }

            });

        return this;
    },
    defineFileUpload: function (uploadTpl, downloadTpl) {
        var self = this;
        var uploadFormView = this;

        var url = window.app.uploadServer +"/upload"
        $.widget('cytomine.fileupload', $.blueimp.fileupload, {

            options: {
                multipart : true,
                // By default, files added to the widget are uploaded as soon
                // as the user clicks on the start buttons. To enable automatic
                // uploads, set the following option to true:
                autoUpload: false,
                // The following option limits the number of files that are
                // allowed to be uploaded using this widget:
                maxNumberOfFiles: undefined,
                // The maximum allowed file size:
                maxFileSize: undefined,
                //maxFileSize: 10,
                // The minimum allowed file size:
                minFileSize: 1,
                // The regular expression for allowed file types, matches
                // against either file type or file name:
                acceptFileTypes: /.+$/i,
                // The regular expression to define for which files a preview
                // image is shown, matched against the file type:
                previewFileTypes: /^image\/(gif|jpeg|png)$/,
                // The maximum file size for preview images:
                previewMaxFileSize: 5000000, // 5MB
                // The maximum width of the preview images:
                previewMaxWidth: 80,
                // The maximum height of the preview images:
                previewMaxHeight: 80,
                // By default, preview images are displayed as canvas elements
                // if supported by the browser. Set the following option to false
                // to always display preview images as img elements:
                previewAsCanvas: true,
                // The expected data type of the upload response, sets the dataType
                // option of the $.ajax upload requests:
                dataType: 'json',
//                xhrFields: {withCredentials: true},
//                url: 'http://localhost:9090/upload',
//                forceIframeTransport: true,
                url: url,
//                xhrFields: {
//                    withCredentials: true
//                },
                beforeSend: function (xhr, req) {
                    console.log('************');

                    //wait for request that get the signature from server
                    while(self.waitForSigned) {

                    }

                    //add headers for auth
                    for (var prop in self.headers) {
                        if(self.headers.hasOwnProperty(prop)){
                            xhr.setRequestHeader(prop,self.headers[prop]);
                        }
                    }

                    //we cannot edit "content-type" so we add content-type-full that will erase content-type on server
                    xhr.setRequestHeader("content-type-full", "null");

                    //update url
                    var $form = $("#fileupload");
                    req.url = req.url + "?" +  $form.prop('action').split("?")[1]

                },
                //            alert(" SUBMIT TO "+"http://localhost:9090/"+$form.prop('action'));
                // The add callback is invoked as soon as files are added to the fileupload
                // widget (via file input selection, drag & drop or add API call).
                // See the basic file upload widget for more information:
                add: function (e, data) {
                    var that = $(this).data('fileupload'),
                        files = data.files;
                    console.log(that);
                    that._adjustMaxNumberOfFiles(-files.length);
                    data.isAdjusted = true;
                    data.files.valid = data.isValidated = that._validate(files);
                    data.context = that._renderUpload(files)
                        .appendTo(that._files)
                        .data('data', data);

                    // Force reflow:
                    that._reflow = that._transition && data.context[0].offsetWidth;
                    data.context.addClass('in');
                    if ((that.options.autoUpload || data.autoUpload) &&
                        data.isValidated) {
                        data.submit();
                    }
                },
                // Callback for the start of each file upload request:
                send: function (e, data) {
                    if (!data.isValidated) {
                        var that = $(this).data('fileupload');
                        if (!data.isAdjusted) {
                            that._adjustMaxNumberOfFiles(-data.files.length);
                        }
                        if (!that._validate(data.files)) {
                            return false;
                        }
                    }
                    if (data.context && data.dataType &&
                        data.dataType.substr(0, 6) === 'iframe') {
                        // Iframe Transport does not support progress events.
                        // In lack of an indeterminate progress bar, we set
                        // the progress to 100%, showing the full animated bar:
                        data.context.find('.progressbar div').css(
                            'width',
                            parseInt(100, 10) + '%'
                        );
                    }
                },
                // Callback for successful uploads:
                done: function (e, data) {
                    var that = $(this).data('fileupload'),
                        template,
                        preview;
                    if (data.context) {
                        data.context.each(function (index) {
                            var file = ($.isArray(data.result) &&
                                data.result[index]) || {error: 'emptyResult'};
                            if (file.error) {
                                that._adjustMaxNumberOfFiles(1);
                            }
                            that._transitionCallback(
                                $(this).removeClass('in'),
                                function (node) {
                                    template = that._renderDownload([file]);
                                    preview = node
                                        .find('.preview img, .preview canvas');
                                    if (preview.length) {
                                        template.find('.preview img')
                                            .prop('width', preview.prop('width'))
                                            .prop('height', preview.prop('height'));
                                    }
                                    template
                                        .replaceAll(node);
                                    // Force reflow:
                                    that._reflow = that._transition &&
                                        template[0].offsetWidth;
                                    template.addClass('in');
                                }
                            );
                        });
                    } else {
                        template = that._renderDownload(data.result)
                            .appendTo(that._files);
                        // Force reflow:
                        that._reflow = that._transition && template[0].offsetWidth;
                        template.addClass('in');
                    }

                },
                // Callback for failed (abort or error) uploads:
                fail: function (e, data) {
                    var that = $(this).data('fileupload'),
                        template;
                    that._adjustMaxNumberOfFiles(data.files.length);
                    if (data.context) {
                        data.context.each(function (index) {
                            if (data.errorThrown !== 'abort') {
                                var file = data.files[index];
                                file.error = file.error || data.errorThrown ||
                                    true;
                                that._transitionCallback(
                                    $(this).removeClass('in'),
                                    function (node) {
                                        template = that._renderDownload([file])
                                            .replaceAll(node);
                                        // Force reflow:
                                        that._reflow = that._transition &&
                                            template[0].offsetWidth;
                                        template.addClass('in');
                                    }
                                );
                            } else {
                                that._transitionCallback(
                                    $(this).removeClass('in'),
                                    function (node) {
                                        node.remove();
                                    }
                                );
                            }
                        });
                    } else if (data.errorThrown !== 'abort') {
                        that._adjustMaxNumberOfFiles(-data.files.length);
                        data.context = that._renderUpload(data.files)
                            .appendTo(that._files)
                            .data('data', data);
                        // Force reflow:
                        that._reflow = that._transition && data.context[0].offsetWidth;
                        data.context.addClass('in');
                    }
                },

                // Callback for upload progress events:
                progress: function (e, data) {
                    console.log(data.context)
                    if (data.context) {
                        console.log(data.loaded);
                        console.log(data.total);
                        console.log(data.loaded / data.total * 100+"%");
                        data.context.find('.progressbar div').css(
                            'width',
                            parseInt(data.loaded / data.total * 100, 10) + '%'
                        );
                    }
                },
                // Callback for global upload progress events:
                progressall: function (e, data) {
                    $(this).find('.fileupload-progressbar div').css(
                        'width',
                        parseInt(data.loaded / data.total * 100, 10) + '%'
                    );
                },
                // Callback for uploads start, equivalent to the global ajaxStart event:
                start: function () {
                    $(this).find('.fileupload-progressbar')
                        .addClass('in').find('div').css('width', '0%');
                },
                // Callback for uploads stop, equivalent to the global ajaxStop event:
                stop: function () {
                    $(this).find('.fileupload-progressbar')
                        .removeClass('in').find('div').css('width', '0%');
                },
                // Callback for file deletion:
                destroy: function (e, data) {
                    var that = $(this).data('fileupload');
                    if (data.url) {
                        $.ajax(data);
                    }
                    that._adjustMaxNumberOfFiles(1);
                    that._transitionCallback(
                        data.context.removeClass('in'),
                        function (node) {
                            node.remove();
                        }
                    );
                }
            },

            // Link handler, that allows to download files
            // by drag & drop of the links to the desktop:
            _enableDragToDesktop: function () {
                var link = $(this),
                    url = link.prop('href'),
                    name = decodeURIComponent(url.split('/').pop())
                        .replace(/:/g, '-'),
                    type = 'application/octet-stream';
                link.bind('dragstart', function (e) {
                    try {
                        e.originalEvent.dataTransfer.setData(
                            'DownloadURL',
                            [type, name, url].join(':')
                        );
                    } catch (err) {
                    }
                });
            },

            _adjustMaxNumberOfFiles: function (operand) {
                if (typeof this.options.maxNumberOfFiles === 'number') {
                    this.options.maxNumberOfFiles += operand;
                    if (this.options.maxNumberOfFiles < 1) {
                        this._disableFileInputButton();
                    } else {
                        this._enableFileInputButton();
                    }
                }
            },

            _formatFileSize: function (bytes) {
                if (typeof bytes !== 'number') {
                    return '';
                }
                if (bytes >= 1000000000) {
                    return (bytes / 1000000000).toFixed(2) + ' GB';
                }
                if (bytes >= 1000000) {
                    return (bytes / 1000000).toFixed(2) + ' MB';
                }
                return (bytes / 1000).toFixed(2) + ' KB';
            },

            _hasError: function (file) {
                if (file.error) {
                    return file.error;
                }
                // The number of added files is subtracted from
                // maxNumberOfFiles before validation, so we check if
                // maxNumberOfFiles is below 0 (instead of below 1):
                if (this.options.maxNumberOfFiles < 0) {
                    return 'maxNumberOfFiles';
                }
                // Files are accepted if either the file type or the file name
                // matches against the acceptFileTypes regular expression, as
                // only browsers with support for the File API report the type:
                if (!(this.options.acceptFileTypes.test(file.type) ||
                    this.options.acceptFileTypes.test(file.name))) {
                    return 'acceptFileTypes';
                }
                if (this.options.maxFileSize &&
                    file.size > this.options.maxFileSize) {
                    return 'maxFileSize';
                }
                if (typeof file.size === 'number' &&
                    file.size < this.options.minFileSize) {
                    return 'minFileSize';
                }
                return null;
            },

            _validate: function (files) {
                var that = this,
                    valid = !!files.length;
                $.each(files, function (index, file) {
                    file.error = that._hasError(file);
                    if (file.error) {
                        valid = false;
                    }
                    console.log(file.error +" valid="+valid);
                });

                return valid;
            },

            _renderTemplate: function (tpl, files) {
//                console.log(uploadFormView);
//                console.log(uploadFormView.fileUploadErrors);
//                console.log(tpl);
//                console.log(files);
                var nodes = _.template(tpl, {fileUploadErrors: uploadFormView.fileUploadErrors, o: {
                    files: files,
                    formatFileSize: this._formatFileSize,
                    options: this.options
                }});
                return $(this.options.templateContainer).html(nodes).children();
            },

            _renderUpload: function (files) {
                var that = this,
                    options = this.options,
                    nodes = this._renderTemplate(options.uploadTemplate, files);
                nodes.find('.preview span').each(function (index, node) {
                    var file = files[index];
                    if (false && options.previewFileTypes.test(file.type) &&
                        (!options.previewMaxFileSize ||
                            file.size < options.previewMaxFileSize)) {
                        window.loadImage(
                            files[index],
                            function (img) {
                                $(node).append(img);
                                // Force reflow:
                                that._reflow = that._transition &&
                                    node.offsetWidth;
                                $(node).addClass('in');
                            },
                            {
                                maxWidth: options.previewMaxWidth,
                                maxHeight: options.previewMaxHeight,
                                canvas: options.previewAsCanvas
                            }
                        );
                    }
                });
                return nodes;
            },

            _renderDownload: function (files) {
                var nodes = this._renderTemplate(
                    this.options.downloadTemplate,
                    files
                );
                nodes.find('a').each(this._enableDragToDesktop);
                return nodes;
            },

            _startHandler: function (e) {
                e.preventDefault();
                var button = $(this),
                    tmpl = button.closest('.template-upload'),
                    data = tmpl.data('data');
                if (data && data.submit && !data.jqXHR && data.submit()) {
                    button.prop('disabled', true);
                }
            },

            _cancelHandler: function (e) {
                e.preventDefault();
                var tmpl = $(this).closest('.template-upload'),
                    data = tmpl.data('data') || {};
                if (!data.jqXHR) {
                    data.errorThrown = 'abort';
                    e.data.fileupload._trigger('fail', e, data);
                } else {
                    data.jqXHR.abort();
                }
            },

            _deleteHandler: function (e) {
                e.preventDefault();
                var button = $(this);
                e.data.fileupload._trigger('destroy', e, {
                    context: button.closest('.template-download'),
                    url: button.attr('data-url'),
                    type: button.attr('data-type'),
                    dataType: e.data.fileupload.options.dataType
                });
            },

            _transitionCallback: function (node, callback) {
                var that = this;
                if (this._transition && node.hasClass('fade')) {
                    node.bind(
                        this._transitionEnd,
                        function (e) {
                            // Make sure we don't respond to other transitions events
                            // in the container element, e.g. from button elements:
                            if (e.target === node[0]) {
                                node.unbind(that._transitionEnd);
                                callback.call(that, node);
                            }
                        }
                    );
                } else {
                    callback.call(this, node);
                }
            },

            _initTransitionSupport: function () {
                var that = this,
                    style = (document.body || document.documentElement).style,
                    suffix = '.' + that.options.namespace;
                that._transition = style.transition !== undefined ||
                    style.WebkitTransition !== undefined ||
                    style.MozTransition !== undefined ||
                    style.MsTransition !== undefined ||
                    style.OTransition !== undefined;
                if (that._transition) {
                    that._transitionEnd = [
                        'MSTransitionEnd',
                        'webkitTransitionEnd',
                        'transitionend',
                        'oTransitionEnd'
                    ].join(suffix + ' ') + suffix;
                }
            },

            _initButtonBarEventHandlers: function () {
                var fileUploadButtonBar = $('.fileupload-buttonbar'),
                    filesList = this._files,
                    ns = this.options.namespace;
                fileUploadButtonBar.find('.start')
                    .bind('click.' + ns, function (e) {
                        e.preventDefault();
                        filesList.find('.start button').click();
                    });
                fileUploadButtonBar.find('.cancel')
                    .bind('click.' + ns, function (e) {
                        e.preventDefault();
                        filesList.find('.cancel button').click();
                    });
                fileUploadButtonBar.find('.delete')
                    .bind('click.' + ns, function (e) {
                        e.preventDefault();
                        filesList.find('.delete input:checked')
                            .siblings('button').click();
                    });
                fileUploadButtonBar.find('.toggle')
                    .bind('change.' + ns, function (e) {
                        filesList.find('.delete input').prop(
                            'checked',
                            $(this).is(':checked')
                        );
                    });
            },

            _destroyButtonBarEventHandlers: function () {
                this.element.find('.fileupload-buttonbar button')
                    .unbind('click.' + this.options.namespace);
                this.element.find('.fileupload-buttonbar .toggle')
                    .unbind('change.' + this.options.namespace);
            },

            _initEventHandlers: function () {
                $.blueimp.fileupload.prototype._initEventHandlers.call(this);
                var eventData = {fileupload: this};
                this._files
                    .delegate(
                        '.start button',
                        'click.' + this.options.namespace,
                        eventData,
                        this._startHandler
                    )
                    .delegate(
                        '.cancel button',
                        'click.' + this.options.namespace,
                        eventData,
                        this._cancelHandler
                    )
                    .delegate(
                        '.delete button',
                        'click.' + this.options.namespace,
                        eventData,
                        this._deleteHandler
                    );
                this._initButtonBarEventHandlers();
                this._initTransitionSupport();
            },

            _destroyEventHandlers: function () {
                this._destroyButtonBarEventHandlers();
                this._files
                    .undelegate('.start button', 'click.' + this.options.namespace)
                    .undelegate('.cancel button', 'click.' + this.options.namespace)
                    .undelegate('.delete button', 'click.' + this.options.namespace);
                $.blueimp.fileupload.prototype._destroyEventHandlers.call(this);
            },

            _enableFileInputButton: function () {
                this.element.find('.fileinput-button input')
                    .prop('disabled', false)
                    .parent().removeClass('disabled');
            },

            _disableFileInputButton: function () {
                this.element.find('.fileinput-button input')
                    .prop('disabled', true)
                    .parent().addClass('disabled');
            },

            _initTemplates: function () {
                this.options.templateContainer = document.createElement(
                    this._files.prop('nodeName')
                );
                this.options.uploadTemplate = uploadTpl
                this.options.downloadTemplate = downloadTpl
            },

            _initFiles: function () {
                this._files = this.element.find('.files-list');
            },

            _create: function () {
                this._initFiles();
                $.blueimp.fileupload.prototype._create.call(this);
                this._initTemplates();
            },

            destroy: function () {
                $.blueimp.fileupload.prototype.destroy.call(this);
            },

            enable: function () {
                $.blueimp.fileupload.prototype.enable.call(this);
                this.element.find('input, button').prop('disabled', false);
                this._enableFileInputButton();
            },

            disable: function () {
                this.element.find('input, button').prop('disabled', true);
                this._disableFileInputButton();
                $.blueimp.fileupload.prototype.disable.call(this);
            }
        });
    },
    getStatusLabel: function (model) {
        if (model.uploaded) {
            return this.statusLabels.uploadedLabel;
        } else if (model.converted) {
            return this.statusLabels.convertedLabel;
        } else if (model.deployed) {
            return this.statusLabels.deployedLabel;
        } else if (model.error_format) {
            return this.statusLabels.errorFormatLabel;
        } else if (model.error_convert) {
            return this.statusLabels.errorConvertLabel;
        } else if (model.uncompressed) {
            return this.statusLabels.uncompressed;
        } else if (model.to_deploy) {
            return this.statusLabels.to_deploy;
        }

        return "?";//this.statusLabels.deployedLabel;
    },
    appendUploadedFile: function (model, target) {
        var rowTpl = "<tr><td><%= originalFilename %></td><td><%= created %></td><td><%= size %></td><td><%= contentType %></td><td><%= status %></td></tr>";
        model.set({status: this.getStatusLabel(model)});
        target.append(_.template(rowTpl, model.toJSON()));
    },
    injectFnReloadAjax: function () {
        $.fn.dataTableExt.oApi.fnReloadAjax = function (oSettings, sNewSource, fnCallback, bStandingRedraw) {
            if (typeof sNewSource != 'undefined' && sNewSource != null) {
                oSettings.sAjaxSource = sNewSource;
            }
            this.oApi._fnProcessingDisplay(oSettings, true);
            var that = this;
            var iStart = oSettings._iDisplayStart;
            var aData = [];

            this.oApi._fnServerParams(oSettings, aData);

            oSettings.fnServerData(oSettings.sAjaxSource, aData, function (json) {
                /* Clear the old information from the table */
                that.oApi._fnClearTable(oSettings);

                /* Got the data - add it to the table */
                var aData = (oSettings.sAjaxDataProp !== "") ?
                    that.oApi._fnGetObjectDataFn(oSettings.sAjaxDataProp)(json) : json;

                for (var i = 0; i < aData.length; i++) {
                    that.oApi._fnAddData(oSettings, aData[i]);
                }

                oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
                that.fnDraw();

                if (typeof bStandingRedraw != 'undefined' && bStandingRedraw === true) {
                    oSettings._iDisplayStart = iStart;
                    that.fnDraw(false);
                }

                that.oApi._fnProcessingDisplay(oSettings, false);

                /* Callback user function - for event handlers etc */
                if (typeof fnCallback == 'function' && fnCallback != null) {
                    fnCallback(oSettings);
                }
            }, oSettings);
        }
    },
    renderUploadedFiles: function () {
        var self = this;
        var uploadTable = $('#uploaded_files');
        var loadingDiv = $("#loadingUploadedFiles");
        var uploadedFileCollectionUrl = new UploadedFileCollection({ dataTables: true}).url();
        uploadTable.hide();
        loadingDiv.show();
        //this.injectFnReloadAjax();
        self.uploadDataTables = uploadTable.dataTable({
            "sDom": "<'row'<'col-md-5'l><'col-md-5'f>r>t<'row'<'col-md-5'i><'col-md-5'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 25,
            "bDestroy": true,
            "bProcessing": true,
            /*"aoColumnDefs": [
             { "sWidth": "20%", "aTargets": [ 0 ] },
             { "sWidth": "20%", "aTargets": [ 1 ] },
             { "sWidth": "20%", "aTargets": [ 2 ] },
             { "sWidth": "20%", "aTargets": [ 3 ] },
             { "sWidth": "20%", "aTargets": [ 4 ] }
             ],*/
            "aoColumns": [
                { "mDataProp": "filename" },
                { "mDataProp": "created" },
                { "mDataProp": "size" },
                { "mDataProp": "contentType" },
                { "mDataProp": "uploaded" }
            ],
            "aoColumnDefs": [
                {
                    "fnRender": function (o, val) {
                        return self.getStatusLabel(o.aData);
                    },
                    "aTargets": [ 4 ]
                }

            ],
            "sAjaxSource": uploadedFileCollectionUrl
        });
        uploadTable.show();
        loadingDiv.hide();

        $(document).on('click', "#refreshUploadedFiles", function (e) {
            e.preventDefault();
            self.uploadDataTables.fnReloadAjax();
        });
    },
    refreshProjectAndStorage : function() {
        var self = this;
        var $form = $("#fileupload");

        var linkProjectSelect = $("#linkProjectSelect");
        var linkStorageSelect = $("#linkStorageSelect");
        var idProject = null;
        if($("#linkWithProject").is(':checked')) {

            idProject = linkProjectSelect.val();
        }
        var idStorage = linkStorageSelect.val();

        $form.prop('action',"upload?idProject=@PROJECT@&idStorage=@STORAGE@&cytomine="+window.location.protocol + "//" + window.location.host);

        if(idProject==null){
            $form.prop('action', $form.prop('action').replace("idProject=@PROJECT@", ""));
        } else {
            $form.prop('action', $form.prop('action').replace("@PROJECT@", idProject));
        }
        $form.prop('action', $form.prop('action').replace("@STORAGE@", idStorage));





        var date = new Date().strftime('%a, %d %b %Y %H:%M:%S +0000');
        var forwardURI = "/upload";
        var method = "POST";
        var query = $form.prop('action').split("?")[1]



        self.waitForSigned = true;
        //get signature for the request
        $.get( "api/signature.json?date="+encodeURIComponent(date)+"&forwardURI="+forwardURI+"&method="+method+"&queryString="+encodeURIComponent(query), function( data ) {
            var headers = {};
            headers.authorization = "CYTOMINE " + data.publicKey + ":" + data.signature;
            headers.dateFull = date //cannot edit date header in browser, so dateFull will overwrite it
            self.headers = headers;
            self.waitForSigned = false;
        });

    },
    waitForSigned : false,
    headers : null,
    doLayout: function (tpl) {
        var self = this;
        $(this.el).html(tpl);

        var linkProjectSelect = $("#linkProjectSelect");
        var linkWithProject = $("#linkWithProject");
        var linkStorageSelect = $("#linkStorageSelect");
        linkProjectSelect.attr("disabled", "disabled");
        linkWithProject.removeAttr("checked");

        linkWithProject.off('change');
        linkWithProject.on('change', function (event) {
            self.refreshProjectAndStorage()
            if ($(this).is(':checked')) {
                linkProjectSelect.removeAttr("disabled");
            } else {
                linkProjectSelect.attr("disabled", "disabled");
            }
        });
        new ProjectCollection().fetch({
            success: function (collection, response) {
                var optionTpl = "<option value='<%= id %>'><%= name %></option>";
                collection.each(function (project) {
                    var selectOption = _.template(optionTpl, project.toJSON());
                    linkProjectSelect.append(selectOption);
                });
                self.refreshProjectAndStorage();
            }
        });
        new StorageCollection().fetch({
            success: function (collection, response) {
                var optionTpl = "<option value='<%= id %>' <%= selected %>><%= name %></option>";
                collection.each(function (storage) {
                    var selected = "";
                    if  (storage.get("user") == window.app.status.user.id) {
                        selected = "selected";
                    }
                    storage.set({ "selected" : selected});
                    var selectOption = _.template(optionTpl, storage.toJSON());
                    linkStorageSelect.append(selectOption);
                });
                self.refreshProjectAndStorage();
            }
        });

        linkStorageSelect.change(function() {
            self.refreshProjectAndStorage();
        });
        linkProjectSelect.change(function() {
            self.refreshProjectAndStorage();
        });

        // Render uploaded file
        this.renderUploadedFiles();
        // Render Upload Form
        require(["text!application/templates/upload/upload.tpl.html", "text!application/templates/upload/download.tpl.html"], function (uploadTpl, downloadTpl) {
            self.defineFileUpload(uploadTpl, downloadTpl);
            $('#fileupload').fileupload({
                limitConcurrentUploads: 10,
                maxFileSize: 100000000000  //100GB
                /*acceptFileTypes : "/(\.|\/)(gif|jpe?g|png|tif|tiff|svs|vms|mrxs|scn|ndpi|jp2)$/i",*/
            });

            // Enable iframe cross-domain access via redirect page:
            var redirectPage = window.location.href.replace(
                /\/[^\/]*$/,
                '/cors/result.html?%s'
            );
            var $form = $("#fileupload");

            $form.submit(function(data) {
                $.post(window.app.uploadServer + "/"+$form.prop('action'), $(this).serialize());
            });
            $('#fileupload').bind('fileuploadsubmit', function (e, data) {
                var linkProjectSelect = $("#linkProjectSelect");
                var linkWithProject = $("#linkWithProject");
                var idProject = null;
                if (linkWithProject.is(':checked')) {
                    idProject = linkProjectSelect.val();
                }
                var idStorage = linkStorageSelect.val();

                var $form = $('#fileupload');

                var input1 = $("<input>").attr("type", "hidden").attr("name", "idProject").val(idProject);
                $form.append($(input1));
                var input2 = $("<input>").attr("type", "hidden").attr("name", "idStorage").val(idStorage);
                $form.append($(input2));


                data.formData = {idProject: idProject, idStorage : idStorage};
                return true;
            });
            $('#fileupload').bind('fileuploadsend', function (e, data) {
                if (data.dataType.substr(0, 6) === 'iframe') {
                    var target = $('<a/>').prop('href', data.url)[0];
                    if (window.location.host !== target.host) {
                        data.formData.push({
                            name: 'redirect',
                            value: redirectPage
                        });
                    }
                }
            });

            // Open download dialogs via iframes,
            // to prevent aborting current uploads:
            $('#fileupload .files').delegate(
                'a:not([rel^=gallery])',
                'click',
                function (e) {
                    e.preventDefault();
                    $('<iframe style="display:none;"></iframe>')
                        .prop('src', this.href)
                        .appendTo(document.body);
                }
            );
        });


    }

});