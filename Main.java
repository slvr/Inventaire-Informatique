/**
 * This is an open source project, any person coming into this source code
 * may fork the version they encounter and alter it as they see fit, so long as 
 * their changes be indicated clearly in javadocs documentation and clearly marked as edits.
 */

package inventaireenvoyable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author aparent
 */
public class Main {
    
    /**
     * The initial launch method.
     * In this version, we merely create an Inventory item and make modifications from there,
     * future versions will read input from a text file and create a dynamic Inventory from there.
     * @param args  
     * @throws java.io.IOException  
     */
    public static void main(String[] args) throws IOException{
        Inventory i;
        try{
            try (FileInputStream fileIn = new FileInputStream("inventaire.ser");
                    ObjectInputStream in = new ObjectInputStream(fileIn)) {
                i = (Inventory) in.readObject();
            }
            i.login();
            sendAnEmail(i);
        }
        catch(FileNotFoundException | InvalidClassException e){
            i = new Inventory("Informatique", "***");
            i.login();
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
    
    public static void sendAnEmail(Inventory i){
        JFrame exitFrame = new JFrame();
        JPanel exitPanel = new JPanel();
        exitFrame.setTitle("Quitter l'inventaire");
        exitPanel.setLayout(new BoxLayout(exitPanel, BoxLayout.Y_AXIS));
        exitPanel.add(new JLabel("Envoyer le rapport à quelle addresse courriel?"));
        JTextField exitText = new JTextField();
        exitPanel.add(exitText);
        JButton exitButton = new JButton("Envoyer");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener((ActionEvent e) -> {
            String inputAddress = exitText.getText();
            boolean isGoodAddress = i.isGoodAddress(inputAddress);
            if (!isGoodAddress) {
                JOptionPane.showMessageDialog(exitFrame.getComponent(0), "Entrez une addresse Fournier & Fils");
            }
            else {
                Iterator<Item> twoItems = i.twoIterator();
                String message = "L'inventaire contient maintenant un montant minime des articles suivants : \n\n";
                while (twoItems.hasNext()) {
                    Item thisItem = twoItems.next();
                    String thisString = i.singleToString(thisItem);
                    String concat = message.concat(thisString + "\n");
                    message = concat;
                }
                final String host = "smtp-mail.outlook.com";
                final String username = "aparent@fournier-fils.com";
                final String password = "ap4Rent!";
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp-mail.outlook.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.user", username);
                props.put("mail.password", password);
                Session session = Session.getInstance(props, 
                        new javax.mail.Authenticator(){
                        protected PasswordAuthentication getPasswordAuthentication(){
                            return new PasswordAuthentication(username, password);
                        }
                });
                session.setDebug(true);
                try{
                    InternetAddress address = new InternetAddress(username);
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(address);
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(inputAddress));
                    msg.setSubject("Avis de commande du système d'inventaire");
                    msg.setText(message);
                    InternetAddress[] theAddress = new InternetAddress[1];
                    Transport.send(msg, theAddress);
                } 
                catch (AddressException ex) {
                    JOptionPane.showMessageDialog(exitFrame.getComponent(0), "Adresse invalide");
                    ex.printStackTrace();
                } 
                catch (MessagingException ex) {
                    JOptionPane.showMessageDialog(exitFrame.getComponent(0), "Erreur d'envoi");
                    ex.printStackTrace();
                }
            }
        });
        exitPanel.add(exitButton);
        exitFrame.add(exitPanel);
        exitFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        exitFrame.pack();
        exitFrame.setResizable(false);
        exitFrame.setLocationRelativeTo(null);
        exitFrame.setVisible(true);
    }   
}