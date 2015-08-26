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


cytomine.customUI.project = [
        "project-annotations-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
        "project-properties-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
        "project-jobs-tab":["ADMIN_PROJECT":false,"USER_PROJECT":false,"GUEST_PROJECT":false],
        "project-configuration-tab":["ADMIN_PROJECT":true,"USER_PROJECT":false,"GUEST_PROJECT":false],
]


