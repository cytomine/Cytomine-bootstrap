package be.cytomine.utils

import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.security.ForgotPasswordToken
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class NotificationService {

    def grailsApplication
    def cytomineMailService
    def secUserService
    def imageProcessingService
    def renderService

    public def notifyNewImageAvailable(SecUser currentUser, AbstractImage abstractImage, def projects) {
        User recipient = null
        if (currentUser instanceof User) {
            recipient = (User) currentUser
        } else if (currentUser instanceof UserJob) {
            UserJob userJob = (UserJob) currentUser
            recipient = userJob.getUser()
        }

        // send email to uploader + all project admin
        def users = [recipient]
        projects.each {
            users.addAll(secUserService.listAdmins(it))
        }
        users.unique()

        log.info "Send mail to $users"

        String macroCID = null

        def attachments = []

        String thumbURL = abstractImage.getThumbURL()
        if (thumbURL) {
            macroCID = UUID.randomUUID().toString()
            BufferedImage bufferedImage = imageProcessingService.getImageFromURL(thumbURL)
            if (bufferedImage != null) {
                File macroFile = File.createTempFile("temp", ".jpg")
                macroFile.deleteOnExit()
                ImageIO.write(bufferedImage, "JPG", macroFile)
                attachments << [ cid : macroCID, file : macroFile]
            }
        }

        def imagesInstances = []
        for (imageInstance in ImageInstance.findAllByBaseImage(abstractImage)) {
            String urlImageInstance = UrlApi.getBrowseImageInstanceURL(imageInstance.getProject().id, imageInstance.getId())
            imagesInstances << [urlImageInstance : urlImageInstance, projectName : imageInstance.project.getName()]

        }
        String message = renderService.createNewImagesAvailableMessage([
                abstractImageFilename : abstractImage.getOriginalFilename(),
                cid : macroCID,
                imagesInstances : imagesInstances,
                by: grailsApplication.config.grails.serverURL,
        ])

        cytomineMailService.send(
                null,
                (String[]) users.collect{it.getEmail()},
                null,
                "Cytomine : a new image is available",
                message.toString(),
                attachments)

    }

    def notifyWelcome(User sender, User guestUser, ForgotPasswordToken forgotPasswordToken) {
        String welcomeMessage = renderService.createWelcomeMessage([
                senderFirstname : sender.getFirstname(),
                senderLastname : sender.getLastname(),
                senderEmail : sender.getEmail(),
                username : guestUser.getUsername(),
                tokenKey : forgotPasswordToken.getTokenKey(),
                expiryDate : forgotPasswordToken.getExpiryDate(),
                by: grailsApplication.config.grails.serverURL,
        ])
        String mailTitle = sender.getFirstname() + " " + sender.getLastname() + " invited you to join Cytomine"
        cytomineMailService.send(
                null,
                (String[]) [guestUser.getEmail()],
                null,
                mailTitle,
                welcomeMessage,
                null)
    }

    def notifyShareAnnotation(User sender, def receiversEmail, def request, def attachments, def cid) {
        String subject = request.JSON.subject
        String shareMessage = renderService.createShareMessage([
                from: request.JSON.from,
                to: request.JSON.to,
                comment: request.JSON.comment,
                annotationURL: request.JSON.annotationURL,
                shareAnnotationURL: request.JSON.shareAnnotationURL,
                by: grailsApplication.config.grails.serverURL,
                cid : cid
        ])

        cytomineMailService.send(
                cytomineMailService.NO_REPLY_EMAIL,
                receiversEmail,
                sender.getEmail(),
                subject,
                shareMessage,
                attachments)
    }

    def notifyForgotUsername(User user) {
        String message = renderService.createForgotUsernameMessage([
                username : user.getUsername(),
                by: grailsApplication.config.grails.serverURL
        ])
        cytomineMailService.send(
                cytomineMailService.NO_REPLY_EMAIL,
                (String[]) [user.getEmail()],
                "",
                "Cytomine : your username is $user.username",
                message)
    }

    def notifyForgotPassword(User user, ForgotPasswordToken forgotPasswordToken) {
        String message = renderService.createForgotPasswordMessage([
                username : user.getUsername(),
                tokenKey : forgotPasswordToken.getTokenKey(),
                expiryDate : forgotPasswordToken.getExpiryDate(),
                by: grailsApplication.config.grails.serverURL
        ])

        cytomineMailService.send(
                cytomineMailService.NO_REPLY_EMAIL,
                (String[]) [user.getEmail()],
                "",
                "Cytomine : reset your password",
                message,
                null)
    }
}
