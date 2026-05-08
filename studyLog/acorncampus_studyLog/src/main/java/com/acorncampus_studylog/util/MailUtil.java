package com.acorncampus_studylog.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Gmail SMTP 이메일 발송 유틸 (TLS 587) */
public class MailUtil {

    private static final String HOST;
    private static final String PORT;
    private static final String USER;
    private static final String PASSWORD;
    private static final String FROM;
    private static final String BASE_URL;

    static {
        Properties p = new Properties();
        try (InputStream is = MailUtil.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (is == null) throw new RuntimeException("mail.properties 파일을 찾을 수 없습니다.");
            p.load(is);
        } catch (IOException e) {
            throw new RuntimeException("mail.properties 로드 실패", e);
        }
        HOST     = p.getProperty("mail.host");
        PORT     = p.getProperty("mail.port");
        USER     = p.getProperty("mail.user");
        PASSWORD = p.getProperty("mail.password");
        FROM     = p.getProperty("mail.from");
        BASE_URL = p.getProperty("mail.app.baseUrl");
    }

    /**
     * 비밀번호 재설정 이메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param token   재설정 토큰 (URL 파라미터로 포함됨)
     */
    public static void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = BASE_URL + "/user/pwd-reset/reset.do?token=" + token;
        String subject   = "[학습 블로그] 비밀번호 재설정 안내";
        String html      = buildResetEmailHtml(resetLink);
        send(toEmail, subject, html);
    }

    // ── 내부 메서드 ────────────────────────────────────────────

    private static void send(String toEmail, String subject, String htmlContent) {
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host",            HOST);
        smtpProps.put("mail.smtp.port",            PORT);
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패: " + toEmail, e);
        }
    }

    private static String buildResetEmailHtml(String resetLink) {
        return """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">비밀번호 재설정</h2>
                  <p>아래 버튼을 클릭하여 비밀번호를 재설정하세요.<br>링크는 발급 후 <strong>30분</strong> 동안만 유효합니다.</p>
                  <a href="%s"
                     style="display:inline-block; padding:12px 24px; background:#4f46e5;
                            color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                    비밀번호 재설정하기
                  </a>
                  <p style="color:#888; font-size:13px;">
                    본인이 요청하지 않았다면 이 이메일을 무시하세요.
                  </p>
                </div>
                """.formatted(resetLink);
    }
}
