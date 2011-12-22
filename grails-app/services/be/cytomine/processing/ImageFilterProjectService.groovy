package be.cytomine.processing

import be.cytomine.project.Project
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.ModelService


class ImageFilterProjectService extends ModelService {

    static transactional = true
    def aclPermissionFactory
    def aclService
    def aclUtilService
    def springSecurityService

    @PreAuthorize("hasPermission(#project,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def get(Project project, ImageFilter image) {
        return ImageFilterProject.findByImageFilterAndProject(image, project)
    }

    @PreAuthorize("hasPermission(#project ,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        return ImageFilterProject.findAllByProject(project)
    }

    @PreAuthorize("hasPermission(#project,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def add(Project project, ImageFilter imageFilter) {
        ImageFilterProject.link(imageFilter, project)
    }

    @PreAuthorize("hasPermission(#project,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def delete(Project project, ImageFilter imageFilter) {
        ImageFilterProject.unlink(imageFilter, project)
    }

    @Override
    add(Object json) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    update(Object domain, Object json) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    delete(Object domain, Object json) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}
