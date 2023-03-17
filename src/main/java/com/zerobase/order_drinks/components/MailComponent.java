package com.zerobase.order_drinks.components;

import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.MailText;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

import static com.zerobase.order_drinks.exception.ErrorCode.MAIL_FAIL;

@RequiredArgsConstructor
@Component
public class MailComponent {

    private final JavaMailSender javaMailSender;

    public boolean sendMail(String mail, String uuid){

        boolean result = false;
        MailText mailText = new MailText();
        String text = mailText.text + uuid + mailText.textEnd;
        String subject = mailText.subject;

        MimeMessagePreparator msg = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                mimeMessageHelper.setText(text, true);
                mimeMessageHelper.setTo(mail);
                mimeMessageHelper.setSubject(subject);
            }
        };

        try {
            javaMailSender.send(msg);
            result = true;
        }catch (Exception e){
            throw new CustomException(MAIL_FAIL);
        }

        return result;
    }
}
