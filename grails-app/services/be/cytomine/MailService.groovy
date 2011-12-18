package be.cytomine

import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import javax.mail.internet.MimeMessage

class MailService {

    static transactional = false

    def send(String from, String[] to, String cc, String subject, String message) {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.starttls.required","true");
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.auth", "true" );
        props.put("mail.smtp.port","587");

        //Create Mail Sender
        def sender = new JavaMailSenderImpl()
        sender.setJavaMailProperties(props)
        sender.setUsername("cytomine.ulg@gmail.com")
        sender.setPassword("C3=8wj9R")
        sender.setDefaultEncoding("UTF-8")
        MimeMessage mail = sender.createMimeMessage()
        MimeMessageHelper helper = new MimeMessageHelper(mail, true)

        helper.setFrom(from)
        helper.setTo(to)
        helper.setCc(cc)
        helper.setSubject(subject)
        helper.setText("",message)
        sender.send(mail);
    }
}
