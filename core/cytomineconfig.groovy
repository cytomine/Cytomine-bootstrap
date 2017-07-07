dataSource.url='jdbc:postgresql://db:5432/docker'
dataSource.username='docker'
dataSource.password='docker'

grails.admin.client='info@cytomine.be'
grails.integration.aurora.url='http://localhost:8000/api/image/notify.json?test=true'
grails.integration.aurora.username='xxx'
grails.integration.aurora.password='xxx'
grails.integration.aurora.interval='60000'

cytomine.customUI.global = [
        dashboard: ["ALL"],
        search : ["ROLE_ADMIN"],
        project: ["ALL"],
        ontology: ["ROLE_ADMIN"],
        storage : ["ROLE_USER","ROLE_ADMIN"],
        activity : ["ALL"],
        feedback : ["ROLE_USER","ROLE_ADMIN"],
        explore : ["ROLE_USER","ROLE_ADMIN"],
        admin : ["ROLE_ADMIN"],
        help : ["ALL"]
]


grails.mongo.options.connectionsPerHost=10
grails.mongo.options.threadsAllowedToBlockForConnectionMultiplier=5

