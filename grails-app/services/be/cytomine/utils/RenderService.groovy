package be.cytomine.utils

import grails.gsp.PageRenderer

class RenderService {

    PageRenderer groovyPageRenderer

    String createShareMessage(model) {
        groovyPageRenderer.render view: '/mail/share', model: model
    }

    String createWelcomeMessage(model) {
        groovyPageRenderer.render view: '/mail/welcome', model: model
    }

    String createForgotPasswordMessage(model) {
        groovyPageRenderer.render view: '/mail/forgot_password', model: model
    }

    String createForgotUsernameMessage(model) {
        groovyPageRenderer.render view: '/mail/forgot_username', model: model
    }

    String createNewImagesAvailableMessage(model) {
        groovyPageRenderer.render view: '/mail/new_image', model: model
    }
}
