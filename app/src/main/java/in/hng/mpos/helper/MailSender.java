package in.hng.mpos.helper;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.rediiffmailpro.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.rediffmailpro.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try{

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = df.format(c);
            File logfile = Environment.getExternalStorageDirectory();
            File myFile = new File(logfile.getAbsolutePath() + "/mPOSlog/" +formattedDate+".log" );
            File file = new File(logfile.getAbsolutePath() + "/mPOSlog/" );

            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setFrom(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);

                Multipart multipart = new MimeMultipart();
                MimeBodyPart textPart = new MimeBodyPart();
                String textContent = "Please find the mpOS log file attached for the date " + formattedDate;
                textPart.setText(textContent);
                multipart.addBodyPart(textPart);

            if(file.exists()){

                File[] listFiles = file.listFiles();
                long purgeTime = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000);
                for(File listFile : listFiles) {
                    if(listFile.lastModified() > purgeTime) {
                        MimeBodyPart attachementPart = new MimeBodyPart();
                        attachementPart.attachFile(listFile);
                        multipart.addBodyPart(attachementPart);
                    }
                }
            }

               // MimeBodyPart attachementPart = new MimeBodyPart();
               // attachementPart.attachFile(myFile);
               // multipart.addBodyPart(attachementPart);


            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

            message.setContent(multipart);
            Transport.send(message);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}

