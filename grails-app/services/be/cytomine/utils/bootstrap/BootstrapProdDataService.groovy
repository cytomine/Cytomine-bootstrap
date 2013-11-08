package be.cytomine.utils.bootstrap

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.Mime
import be.cytomine.image.NestedFile
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:31
 */
class BootstrapProdDataService {


    def grailsApplication

    def termService
    def secUserService
    def permissionService
    def imagePropertiesService
    def abstractImageService




}
