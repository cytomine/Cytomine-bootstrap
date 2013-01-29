var JobResultView = Backbone.View.extend({
    software: null,
    project: null,
    jobs: null,
    parent: null,

    initialize: function (options) {
        this.software = options.software;
        this.project = options.project;
        this.jobs = options.jobs;
    },
    render: function () {
        var self = this;
        console.log("self.software.get('resultName')=" + self.software.get('resultName'));
        switch (self.software.get('resultName')) {
            case 'ValidateAnnotation':
                self.valideAnnotation();
                break;
            case 'ValidateEvolution':
                self.valideEvolution();
                break;
            case 'DownloadFiles':
                self.downloadFiles();
                break;
            case 'Default':
                self.defaultResult();
                break;
            default:
                self.defaultResult();
                break;
        }
        return this;
    },
    valideAnnotation: function () {
        var self = this;
        new RetrievalAlgoResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            software: self.software
        }).render();
    },
    valideEvolution: function () {
        var self = this;
        new EvolutionAlgoResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            software: self.software
        }).render();
    },
    defaultResult: function () {
        var self = this;
        new DefaultResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            software: self.software
        }).render();
    },
    downloadFiles: function () {
        var self = this;
        new DownloadFiles({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            software: self.software
        }).render();
    }
});