Ext.namespace('Cytomine');
Ext.namespace('Cytomine.lang');

Ext.namespace('Cytomine.Project');

Cytomine.Project.tabTitle = "Project"

Ext.namespace('Cytomine.Browser');

Cytomine.title = "Cytomine";


Ext.namespace('ULg');
Ext.namespace('ULg.lang');

Ext.namespace('ULg.lang.Viewer');

ULg.lang.Viewer.title = "Gestionnaire d'annotations";

ULg.lang.Viewer.thumb = {
    title: "Miniature",
    zoomin: "Zoom +",
    zoomout: "Zoom -",
    zoomfit: "Zoom fit",
    zoomreal: "Zoom 1:1",
    moveleft: "Déplacement vers la gauche",
    moveright: "Déplacement vers la droite",
    moveup: "Déplacement vers le haut",
    movedown: "Déplacement vers le bas"
}

ULg.lang.Viewer.layers = {
    title: "Couches",
    layer: "Couche",
    visibility: "Visibilité",
    transparency: "Transparence"
};

ULg.lang.Viewer.annotations = {
    title: "Annotations",
    toolMove: "Déplacement",
    toolSelect: "Sélection",
    toolRectangle: "Rectangle",
    toolCircle: "Ellipse",
    toolPolygon: "Polyline",
    changeWarning: "Une forme est en cours d'édition, voullez-vous vraiment changer d'outil ?",
    changeTitle: "Attention",
    color: "Couleur",
    filterVisible: "Uniquement les annotations visibles",
    filterVisibleFull: "Uniquement compl&egrave;tement visible",
    contextProperties: "Propri&eacute;t&eacute;s",
    contextDelete: "Supprimer",
    contextDeleteWarming: "Êtes-vous certain de vouloir supprimer cette annotation ?",
    contextDeleteTitle: "Suppression d'une annotation"
};

ULg.lang.Viewer.filter = {
    title: "Filtrer par termes",
    filter: "Filtre",
    showAll: "Tout afficher"
};

ULg.lang.Viewer.status = {
    mouse: "Position souris",
    size: "Taille de l'image",
    apidoc: "Documentation API"
};

Ext.namespace('ULg.lang.Shapes');
ULg.lang.Shapes.unknowType = "Type de forme inconnu: ";
ULg.lang.Shapes.createTitle = "Création d'une forme depuis un objet JSON";

Ext.namespace('ULg.lang.Projects');
ULg.lang.Projects.title = 'Gestionnaire de projets';

Ext.namespace('ULg.lang.ontologies');
ULg.lang.ontologies.title = "Gestionnaire d'ontologies";

ULg.lang.ontologies.tree = {
    title: "Termes",
    filter: "Filtre",
    add: "Créer un nouveau terme",
    showAll: "Tout afficher",
    contextEdit: "Modifier",
    contextDelete: "Supprimer",
    contextDeleteWarming: "Êtes-vous certain de vouloir supprimer ce terme ?",
    contextDeleteTitle: "Suppression d'un terme"
};

ULg.lang.ontologies.relations = {
    title: 'Relations',
    is_a: 'Sp&eacute;cialisations',
    intersection: 'Intersection',
    union: 'Union',
    disjoint: 'Disjoint'
};

ULg.lang.ontologies.grid = {
    remove: "Supprimer",
    id: "Identifiant",
    name: "Nom"
};

ULg.lang.ontologies.properties = {
    title: "Propriétés",
    id: 'Identifiant',
    name: "Nom",
    createdAt: "Cr&eacute;&eacute; le"
};

ULg.lang.ontologies.add = {
    title: "Ajout d'un terme",
    name: "Nom",
    validate: "Valider",
    close: "Fermer",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible d'ajouter le terme"
};

ULg.lang.ontologies.remove = {
    title: "Suppression d'un terme",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de supprimer le terme"
};

ULg.lang.ontologies.link = {
    title: "Ajout d'un lien entre deux termes",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible d'ajouter le lien"
};

ULg.lang.ontologies.unlink = {
    title: "Suppression d'un lien entre deux termes",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de supprimer le lien"
};

Ext.namespace('ULg.lang.annotations');

ULg.lang.annotations.load = {
    title: "Chargement des annotations",
    contactErrorMsg: "Erreur coté serveur",
    serverErrorMsg: "Impossible de sauvegarder l'annotation"
};

ULg.lang.annotations.save = {
    title: "Sauvegarde d'une annotation",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de sauvegarder l'annotation"
};

ULg.lang.annotations.remove = {
    title: "Suppression d'une annotation",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de supprimer l'annotation"
};

ULg.lang.annotations.link = {
    title: "Modification d'une annotation",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de lier l'annotation au terme"
};

ULg.lang.annotations.unlink = {
    title: "Modification d'une annotation",
    contactErrorMsg: "Impossible de contacter le serveur",
    serverErrorMsg: "Impossible de modifier retirer le lien entre l'annotation et le terme"
};

ULg.lang.annotations.properties = {
    title: "&Eacute;dition d'une annotation",
    contactErrorMsg: "Erreur coté serveur",
    serverErrorMsg: "Impossible de sauvegarder l'annotation",
    comment: 'Commentaire',
    name: "Nom",
    createdAt: "Cr&eacute;&eacute; le",
    validate: "Valider",
    close: "Fermer"
};

ULg.lang.annotations.grid = {
    title: "Termes li&eacute;es",
    remove: "Supprimer",
    id: "Identifiant",
    name: "Nom"
};

ULg.lang.annotations.tree = {
    title: "Termes",
    filter: "Filtre",
    showAll: "Tout afficher"
}