package be.cytomine.utils

import org.springframework.core.io.FileSystemResource
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper

import javax.mail.internet.MimeMessage

class MailService {

    static String DEFAULT_EMAIL = "cytomine.ulg@gmail.com"

    static transactional = false

    def send(String from, String[] to, String cc, String subject, String message, def attachment) {

        if (!from) from = DEFAULT_EMAIL

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.starttls.required","true");
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.auth", "true" );
        props.put("mail.smtp.port","587");

        //Create Mail Sender
        def sender = new JavaMailSenderImpl()
        sender.setJavaMailProperties(props)
        sender.setUsername(DEFAULT_EMAIL)
        sender.setPassword("C3=8wj9R")
        sender.setDefaultEncoding("UTF-8")
        MimeMessage mail = sender.createMimeMessage()
        MimeMessageHelper helper = new MimeMessageHelper(mail, true)

        helper.setReplyTo("noreply@cytomine.be")
        helper.setFrom(from)
        helper.setTo(to)
        //helper.setCc(cc)
        helper.setSubject(subject)
        helper.setText("",message)
        attachment?.each {
            helper.addInline((String) it.cid, new FileSystemResource((File)it.file))
        }
        
        sender.send(mail);
    }
}
