package inventaireenvoyable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
//import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

/**
 * A creation of Inventory instances.
 * @author aparent
 */
public class Inventory implements Iterable<Item>, java.io.Serializable{
    
    private final String aName;
    private final HashMap<Item, Integer> aInventoryList;
    private String aPassword;
//    private final File aFile;
    
    /**
     * Constructs the Inventory Object from the given .txt file. 
     * Will define the list of elements associated with the Inventory by the file 
     * it has configured itself, so user error becomes less common, hopefully eradicated.
     * 
     * @param pName the name if the Inventory, or rather, the name of the department with which it is associated
     * @param pPassword in order to access the Inventory
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public Inventory(String pName, String pPassword) throws FileNotFoundException, IOException{
        this.aName = pName;
        this.aPassword = pPassword;
        this.aInventoryList = new HashMap<>();
    }
    
    /**
     * 
     * @return aName the name of the Inventory 
     */
    public String getName(){
        return this.aName;
    }
    
    /**
     * Stock however many of an Item you wish
     * 
     * @param pItem The Item to be stocked
     * @param pQuantity how many of that Item are to be stocked
     * @throws IOException 
     */
    private void stock(Item pItem, int pQuantity) throws IOException{
        int amount = 0;
        boolean isFound = false;
        Iterator<Item> allItems = this.allIterator();
        while(allItems.hasNext()){
            Item next = allItems.next();
            if(pItem.equals(next)){
                pItem = next;
                isFound = true;
                break;
            }
        }
        if (isFound){
            amount = aInventoryList.get(pItem);
        }
        amount += pQuantity;
        aInventoryList.put(pItem, amount);
        this.update();
    }
    
    /**
     * Reduces the quantity of an Item
     * 
     * @param pItem The Item to be disposed
     * @param pQuantity how many of tha tItem are to be disposed of
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private void dispose(Item pItem, int pQuantity) throws IOException{
        int amount = aInventoryList.get(pItem);
	amount -= pQuantity;
        if (!(amount < 0)){
            aInventoryList.put(pItem, amount);
        }
        this.update();
    }
    
    /**
     * Deletes an item from the inventory entirely.
     * Is only ever called by items that have a quantity of 0, so the method disregards quantity.
     * 
     * @param pItem the Item to be deleted
     */
    private void delete(Item pItem){
        aInventoryList.remove(pItem);
    }
    
    /**
     * Updates the entirety of the associated .txt file to make it consistent with aInventoryList
     * 
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void update() throws FileNotFoundException, IOException{
        try{
            try (FileOutputStream fileOut = new FileOutputStream("inventaire.ser"); 
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                    out.writeObject(this);
                }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the number of a specified Item that are available
     * 
     * @param pItem the Item whose quantity is desired
     * @return 
     */
    public int available(Item pItem){
	if( aInventoryList.containsKey(pItem)){
            return aInventoryList.get(pItem);
        }
        else{
            return 0;
        }
    }

    /**
     * 
     * @return an iterator over only the elements of non-zero quantity
     */
    @Override
    public Iterator<Item> iterator(){
	Iterator<Item> thisIterator = this.allIterator();
        HashMap<Item, Integer> availableItems = new HashMap<>();
        while(thisIterator.hasNext()){
            Item thisItem = thisIterator.next();
            if(this.available(thisItem) != 0){
                availableItems.put(thisItem, this.available(thisItem));
            }
        }
        return availableItems.keySet().iterator();
    }
    
    public Iterator<Item> twoIterator(){
        Iterator<Item> thisIterator = this.allIterator();
        HashMap<Item, Integer> twoItems = new HashMap<>();
        while(thisIterator.hasNext()){
            Item thisItem = thisIterator.next();
            if(this.available(thisItem) <= 2){
                twoItems.put(thisItem, this.available(thisItem));
            }
        }
        return twoItems.keySet().iterator();
    }
    
    /**
     * 
     * @return an iterator of all items in aInventoryList with a quantity of zero
     */
    public Iterator<Item> noneIterator(){
        Iterator<Item> thisIterator = this.allIterator();
        HashMap<Item, Integer> unavailableItems = new HashMap<>();
        while(thisIterator.hasNext()){
            Item thisItem = thisIterator.next();
            if(this.available(thisItem) == 0){
                unavailableItems.put(thisItem, this.available(thisItem));
            }
        }
        return unavailableItems.keySet().iterator();
    }
    
    /**
     * 
     * @return an iterator of all elements
     */
    public Iterator<Item> allIterator(){
	return aInventoryList.keySet().iterator();
    }
    
     /**
     * Launches a GUI, to manage the system. At first, it will ask for a password, 
     * and will only allow operations to take place if the password is given.
     * See void manage for the list of operations.
     * 
     */
    public final void login(){
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        frame.setTitle("Inventaire Informatique");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPasswordField pass = new JPasswordField();
        panel.add(pass);
        JButton loginButton = new JButton("S'identifier");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener((ActionEvent e) -> {
            String inputPass = pass.getText();
            boolean goodPass = this.loginAttempt(inputPass);
            if(!goodPass){
                JOptionPane.showMessageDialog(frame.getComponent(0), "Accès refusé");
            }
            else{
                frame.setVisible(false);
                JFrame frame2 = new JFrame();
                JPanel panel2 = new JPanel();
                frame.setTitle("Accès accordé");
                Container c2 = frame2.getContentPane();
                Dimension d2 = new Dimension(200, 50);
                c2.setPreferredSize(d2);
                panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
                JLabel welcomeText = new JLabel();
                welcomeText.setText("Re-bienvenue");
                welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel2.add(welcomeText);
                JButton continueButton = new JButton("Continuer");
                continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                continueButton.addActionListener((ActionEvent f) ->{
                    this.manage();
                    frame2.setVisible(false);
                });
                panel2.add(continueButton);
                frame2.add(panel2);
                frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame2.pack();
                frame.setResizable(false);
                frame2.setLocationRelativeTo(null);
                frame2.setVisible(true);
            }
        });
        panel.add(loginButton);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * Attempt to log in
     * 
     * @param aAttempt the password attempt
     * @return a boolean describing whether or not the login was successful
     */
    private boolean loginAttempt(String aAttempt){
        return (aAttempt.equals(aPassword));
    }
    
    /**
     * 
     * @param pItem the Item to be strung
     * @return a String describing an Item within the Inventory
     */
    public final String singleToString(Item pItem){
        if(aInventoryList.containsKey(pItem)){
            String phrase = pItem.getName() + ", " + pItem.getDescription() + ": " + this.available(pItem) + " disponible";
            return phrase;
        }
        else{
            String phrase = "Vous ne devriez pas voir ce message. Contactez Alec si ce message se présente.\n";
            return phrase;
        }
    }
    
     /**
     * Creates a GUI menu with 4 options:
     *  The inventory can be viewed fore a simple stock count.
     *  Items may be added to the Inventory, as needed.
     *  Items may be disposed of, once broken, sold, or used.
     *  The program may be exited at any time.
     * The entirety of this is managed through an easy-to navigate interface.
     * 
     */
    private void manage(){
        JFrame invFrame = new JFrame();
        JPanel invPanel = new JPanel();
        invFrame.setTitle("Gérer l'inventaire");
        Container c = invFrame.getContentPane();
        Dimension d = new Dimension(200, 200);
        c.setPreferredSize(d);
        JButton viewButton = new JButton("Voir l'inventaire");
        viewButton.setToolTipText("Voir une liste de tous les articles en stock");
        JButton stockButton = new JButton("Stocker");
        stockButton.setToolTipText("Entrer des nouveaux itèmes dans l'inventaire");
        JButton disposeButton = new JButton("Disposer");
        disposeButton.setToolTipText("Enlever des itèmes donné de l'inventaire");
        JButton deleteButton = new JButton("Supprimer");
        deleteButton.setToolTipText("Entièrement supprimer des itèmes qui ne seront plus en stock");
        JButton changeButton = new JButton("Changer le mot de passe");
        changeButton.setToolTipText("Au besoin");
        JButton exitButton = new JButton("Quitter");
        exitButton.setToolTipText("Quitter et envoyer un rapport");
        viewButton.addActionListener((ActionEvent e) -> {
            view();
        });
        stockButton.addActionListener((ActionEvent e) -> {
            stock();
        });
        disposeButton.addActionListener((ActionEvent e) -> {
            dispose();
        });
        deleteButton.addActionListener((ActionEvent e) -> {
            delete();
        });
        changeButton.addActionListener((ActionEvent e) -> {
            changePassword();
        });
        exitButton.addActionListener((ActionEvent e) -> {
            try {
                update();
                invFrame.dispose();
            } 
            catch (IOException ex) {
                Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        invPanel.add(viewButton);
        invPanel.add(stockButton);
        invPanel.add(disposeButton);
        invPanel.add(deleteButton);
        invPanel.add(changeButton);
        invPanel.add(exitButton);
        GridLayout invLayout = new GridLayout(6, 1);
        invPanel.setLayout(invLayout);
        invFrame.add(invPanel);
        invFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                try {
                    update();
                    invFrame.dispose();
                } catch (IOException ex) {
                    Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        invFrame.pack();
        invFrame.setResizable(false);
        invFrame.setLocationRelativeTo(null);
        invFrame.setVisible(true);
    }
    
    /**
     * Pops up a window which lists all items currently in the inventory.
     * 
     */
    private void view(){
        Iterator<Item> itemsAvailable = iterator();
        JFrame viewFrame = new JFrame();
        JPanel viewPanel = new JPanel();
        viewFrame.setTitle("Votre inventaire");
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
        while(itemsAvailable.hasNext()){
            Item thisOne = itemsAvailable.next();
            JLabel viewLabel = new JLabel();
            viewLabel.setText(singleToString(thisOne));
            viewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            viewPanel.add(viewLabel);
        }
        JButton backButton = new JButton("Retour");
        backButton.addActionListener((ActionEvent f) -> {
            viewFrame.dispose();
        });
        viewPanel.add(backButton);
        viewFrame.add(viewPanel);
        viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        viewFrame.pack();
        viewFrame.setLocationRelativeTo(null);
        viewFrame.setVisible(true);
    }
    
    /**
     * Allows the stocking of any item previously created, or the definition of a new item.
     * If an item is created that is identical to an existing one, the two will be merged.
     * 
     */
    private void stock(){
        Iterator<Item> allItems = allIterator();
        JFrame stockFrame = new JFrame();
        JPanel stockPanel = new JPanel();
        stockFrame.setTitle("Stocker un article");
        stockPanel.setLayout(new BoxLayout(stockPanel, BoxLayout.Y_AXIS));
        JButton newItemButton = new JButton("Nouvel article");
        JButton continueButton = new JButton("Continuer");
        JButton backButton = new JButton("Retour");
        ButtonGroup stockGroup = new ButtonGroup();
        while(allItems.hasNext()){
            Item thisOne = allItems.next();
            JRadioButtonMenuItem stockRadio = new JRadioButtonMenuItem(singleToString(thisOne));
            stockRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
            stockGroup.add(stockRadio);
            stockPanel.add(stockRadio);
        }
        newItemButton.addActionListener((ActionEvent f) -> {
            JFrame newItemFrame = new JFrame();
            JPanel newItemPanel = new JPanel();
            newItemFrame.setTitle("Créer un article");
            newItemPanel.setLayout(new BoxLayout(newItemPanel, BoxLayout.Y_AXIS));
            JTextField newItemName = new JTextField();
            JTextField newItemDescription = new JTextField();
            JTextField newItemQuantity = new JTextField();
            newItemPanel.add(new JLabel("Nom de l'article:"));
            newItemPanel.add(newItemName);
            newItemPanel.add(new JLabel("Description de l'article:"));
            newItemPanel.add(newItemDescription);
            newItemPanel.add(new JLabel("Combien de cet article mettez-vous en stock?"));
            newItemPanel.add(newItemQuantity);
            JButton submitButton = new JButton("Soumettre");
            submitButton.addActionListener((ActionEvent g) -> {
                boolean isInput = false;
                String inputName = newItemName.getText();
                String inputDescription = newItemDescription.getText();
                Item inputItem = new Item(inputName, inputDescription);
                String inputQuantity = newItemQuantity.getText();
                int toInt = Integer.parseInt(inputQuantity);
                Iterator<Item> allItems2 = allIterator();
                while(allItems2.hasNext()){
                    Item thisItem = allItems2.next();
                    if(thisItem.equals(inputItem)){
                        try {
                            stock(thisItem, toInt);
                        } catch (IOException ex) {
                            Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        isInput = true;
                        break;
                    }
                }
                if(!isInput){
                    try {
                        stock(new Item(inputName, inputDescription), toInt);
                    } catch (IOException ex) {
                        Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                newItemFrame.dispose();
                stockFrame.dispose();
            });
            newItemPanel.add(submitButton);
            newItemFrame.add(newItemPanel);
            newItemFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            newItemFrame.pack();
            newItemFrame.setResizable(false);
            newItemFrame.setLocationRelativeTo(null);
            newItemFrame.setVisible(true);
        });
        continueButton.addActionListener((ActionEvent f) -> {
            for(Enumeration<AbstractButton> stockButtons = stockGroup.getElements(); stockButtons.hasMoreElements();){
                AbstractButton thisOne = stockButtons.nextElement();
                if(thisOne.isSelected()){
                    Iterator<Item> allItems2 = allIterator();
                    String selectedString = thisOne.getText();
                    while(allItems2.hasNext()){
                        Item toStock = allItems2.next();
                        if(selectedString.equals(singleToString(toStock))){
                            JFrame quanFrame = new JFrame();
                            JPanel quanPanel = new JPanel();
                            quanFrame.setTitle("Combien à mettre en stock?");
                            quanPanel.setLayout(new BoxLayout(quanPanel, BoxLayout.Y_AXIS));
                            JLabel text = new JLabel("Combien de " + toStock.getName() + "s voulez-vous mettre en stock?");
                            quanPanel.add(text);
                            JTextField quantity = new JTextField();
                            quanPanel.add(quantity);
                            JButton submitButton = new JButton("Soumettre");
                            submitButton.addActionListener((ActionEvent g) -> {
                                String inputInt = quantity.getText();
                                int toInt = Integer.parseInt(inputInt);
                                if(toInt < 0){
                                    JFrame warningFrame = new JFrame();
                                    JPanel warningPanel = new JPanel();
                                    warningFrame.setTitle("Stock négatif");
                                    warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
                                    JLabel warningLabel = new JLabel("Pour disposer d'un article, utilisez l'option «Disposer» au menu principal.");
                                    warningPanel.add(warningLabel);
                                    JButton warningButton = new JButton("D'accord");
                                    warningButton.addActionListener((ActionEvent h) -> {
                                        warningFrame.dispose();
                                        quanFrame.dispose();
                                        stockFrame.dispose();
                                    });
                                    warningPanel.add(warningButton);
                                    warningFrame.add(warningPanel);
                                    warningFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    warningFrame.pack();
                                    warningFrame.setResizable(false);
                                    warningFrame.setLocationRelativeTo(null);
                                    warningFrame.setVisible(true);
                                }
                                else{
                                    try {
                                        stock(toStock, toInt);
                                    } catch (IOException ex) {
                                        Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    quanFrame.dispose();
                                    stockFrame.dispose();
                                }
                            });
                            quanPanel.add(submitButton);
                            quanFrame.add(quanPanel);
                            quanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            quanFrame.pack();
                            quanFrame.setResizable(false);
                            quanFrame.setLocationRelativeTo(null);
                            quanFrame.setVisible(true);
                        }
                    }
                }
            }
        });
        backButton.addActionListener((ActionEvent f) -> {
            stockFrame.dispose();
        });
        stockPanel.add(newItemButton);
        stockPanel.add(continueButton);
        stockPanel.add(backButton);
        stockFrame.add(stockPanel);
        stockFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stockFrame.pack();
        stockFrame.setResizable(false);
        stockFrame.setLocationRelativeTo(null);
        stockFrame.setVisible(true);
    }
    
    /**
     * Allows the removal of any Item currently in the Inventory.
     * 
     */
    private void dispose(){
        Iterator<Item> availableItems = iterator();
        JFrame disposeFrame = new JFrame();
        JPanel disposePanel = new JPanel();
        disposeFrame.setTitle("Disposer d'un objet");
        disposePanel.setLayout(new BoxLayout(disposePanel, BoxLayout.Y_AXIS));
        JButton continueButton = new JButton("Continuer");
        JButton backButton = new JButton("Retour");
        continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup disposeGroup = new ButtonGroup();
        while(availableItems.hasNext()){
            Item thisOne = availableItems.next();
            JRadioButtonMenuItem disposeRadio = new JRadioButtonMenuItem(singleToString(thisOne));
            disposeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
            disposeGroup.add(disposeRadio);
            disposePanel.add(disposeRadio);
        }
        continueButton.addActionListener((ActionEvent f) -> {
            for(Enumeration<AbstractButton> disposeButtons = disposeGroup.getElements(); disposeButtons.hasMoreElements();){
                AbstractButton thisOne = disposeButtons.nextElement();
                if(thisOne.isSelected()){
                    Iterator<Item> allAvailable = iterator();
                    String selectedString = thisOne.getText();
                    while(allAvailable.hasNext()){
                        Item toDispose = allAvailable.next();
                        if(selectedString.equals(singleToString(toDispose))){
                            JFrame quanFrame = new JFrame();
                            JPanel quanPanel = new JPanel();
                            quanFrame.setTitle("Disposer de combien?");
                            quanPanel.setLayout(new BoxLayout(quanPanel, BoxLayout.Y_AXIS));
                            JLabel text = new JLabel("Combien de " + toDispose.getName() + "s voulez-vous disposer?");
                            quanPanel.add(text);
                            JTextField quantity = new JTextField();
                            quanPanel.add(quantity);
                            JButton submitButton = new JButton("Soumettre");
                            submitButton.addActionListener((ActionEvent g) -> {
                                String inputInt = quantity.getText();
                                int toInt = Integer.parseInt(inputInt);
                                if(toInt < 0){
                                    JFrame warningFrame = new JFrame();
                                    JPanel warningPanel = new JPanel();
                                    warningFrame.setTitle("Disposition négative");
                                    warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
                                    JLabel warningLabel = new JLabel("Pour stocker un article, utilisez l'option «Stocker» au menu principal");
                                    warningPanel.add(warningLabel);
                                    JButton warningButton = new JButton("D'accord");
                                    warningButton.addActionListener((ActionEvent h) -> {
                                        warningFrame.dispose();
                                        quanFrame.dispose();
                                        disposeFrame.dispose();
                                    }); 
                                    warningPanel.add(warningButton);
                                    warningFrame.add(warningPanel);
                                    warningFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    warningFrame.pack();
                                    warningFrame.setResizable(false);
                                    warningFrame.setLocationRelativeTo(null);
                                    warningFrame.setVisible(true);
                                }
                                else if(toInt > available(toDispose)){
                                    JFrame warningFrame = new JFrame();
                                    JPanel warningPanel = new JPanel();
                                    warningFrame.setTitle("Pas assez");
                                    warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
                                    JLabel warningLabel = new JLabel("Il y en a pas assez pour qu'on se dispose d'autant d'articles!");
                                    warningPanel.add(warningLabel);
                                    JButton warningButton = new JButton("D'accord");
                                    warningButton.addActionListener((ActionEvent h) -> {
                                        warningFrame.dispose();
                                        quanFrame.dispose();
                                        disposeFrame.dispose();
                                    }); 
                                    warningPanel.add(warningButton);
                                    warningFrame.add(warningPanel);
                                    warningFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    warningFrame.pack();
                                    warningFrame.setResizable(false);
                                    warningFrame.setLocationRelativeTo(null);
                                    warningFrame.setVisible(true);
                                }
                                else{
                                    try {
                                        dispose(toDispose, toInt);
                                    } catch (IOException ex) {
                                        Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    quanFrame.dispose();
                                    disposeFrame.dispose();
                                }
                            });
                            quanPanel.add(submitButton);
                            quanFrame.add(quanPanel);
                            quanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            quanFrame.pack();
                            quanFrame.setResizable(false);
                            quanFrame.setLocationRelativeTo(null);
                            quanFrame.setVisible(true);
                        }
                    }
                }
            }
        });
        backButton.addActionListener((ActionEvent f) -> {
            disposeFrame.dispose();
        });
        disposePanel.add(continueButton);
        disposePanel.add(backButton);
        disposeFrame.add(disposePanel);
        disposeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        disposeFrame.pack();
        disposeFrame.setResizable(false);
        disposeFrame.setLocationRelativeTo(null);
        disposeFrame.setVisible(true);
    }
    
    /**
     * Entirely Remove an item from the InventoryList
     * 
     */
    private void delete(){
        Iterator<Item> unavailableItems = noneIterator();
        JFrame deleteFrame = new JFrame();
        JPanel deletePanel = new JPanel();
        deleteFrame.setTitle("Supprimer un objet");
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        JButton continueButton = new JButton("Continuer");
        JButton backButton = new JButton("Retour");
        continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup deleteGroup = new ButtonGroup();
        while(unavailableItems.hasNext()){
            Item thisOne = unavailableItems.next();
            JRadioButtonMenuItem deleteRadio = new JRadioButtonMenuItem(singleToString(thisOne));
            deleteRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
            deleteGroup.add(deleteRadio);
            deletePanel.add(deleteRadio);
        }
        continueButton.addActionListener((ActionEvent e) -> {
            for(Enumeration<AbstractButton> deleteButtons = deleteGroup.getElements(); deleteButtons.hasMoreElements();){
                AbstractButton thisOne = deleteButtons.nextElement();
                if(thisOne.isSelected()){
                    Iterator<Item> noneAvailable = noneIterator();
                    String selectedString = thisOne.getText();
                    while(noneAvailable.hasNext()){
                        Item toDelete = noneAvailable.next();
                        if(selectedString.equals(singleToString(toDelete))){
                            delete(toDelete);
                            deleteFrame.dispose();
                        }
                    }
                }
            }
        });
        backButton.addActionListener((ActionEvent e) -> {
            deleteFrame.dispose();
        });
        deletePanel.add(continueButton);
        deletePanel.add(backButton);
        deleteFrame.add(deletePanel);
        deleteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        deleteFrame.pack();
        deleteFrame.setResizable(false);
        deleteFrame.setLocationRelativeTo(null);
        deleteFrame.setVisible(true);
    }
    
    private void changePassword(){
        JFrame changeFrame = new JFrame();
        JPanel changePanel = new JPanel();
        changeFrame.setTitle("Changement de mot de passe");
        changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.Y_AXIS));
        JLabel currentLabel = new JLabel("Mot de passe actuel :");
        JPasswordField currentText = new JPasswordField();
        JLabel newLabel = new JLabel("Nouveau mot de passe :");
        JPasswordField newText = new JPasswordField();
        JLabel newLabel2 = new JLabel("Encore une fois :");
        JPasswordField newText2 = new JPasswordField();
        changePanel.add(currentLabel);
        changePanel.add(currentText);
        changePanel.add(newLabel);
        changePanel.add(newText);
        changePanel.add(newLabel2);
        changePanel.add(newText2);
        JButton continueButton = new JButton("Continuer");
        JButton backButton = new JButton("Retour");
        continueButton.addActionListener((ActionEvent e) -> {
            String oldPassword = currentText.getText();
            if(!(oldPassword.equals(this.aPassword))){
                JOptionPane.showMessageDialog(changeFrame.getComponent(0), "Mauvais mot de passe");
            }
            else{
                String newPassword = newText.getText();
                String newPassword2 = newText2.getText();
                if(!(newPassword.equals(newPassword2))){
                    JOptionPane.showMessageDialog(changeFrame.getComponent(0), "Les mots de passe ne sont pas identiques");
                }
                else{
                    this.aPassword = newPassword;
                    JOptionPane.showMessageDialog(changeFrame.getComponent(0), "Mot de passe changé");
                    changeFrame.dispose();
                }
            }
        });
        backButton.addActionListener((ActionEvent e) -> {
            changeFrame.dispose();
        });
        changePanel.add(continueButton);
        changePanel.add(backButton);
        changeFrame.add(changePanel);
        changeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        changeFrame.pack();
        changeFrame.setResizable(false);
        changeFrame.setLocationRelativeTo(null);
        changeFrame.setVisible(true);
    }
    
    /**
     * Ensures that the input e-mail address in exit is a @fournier-fils.com address
     * 
     * @param input 
     * @return if Fournier & Fils
     */
    public boolean isGoodAddress(String input){
        char[] inputArray = input.toCharArray();
        int i = 0;
        while(i < inputArray.length){
            if(inputArray[i] == '@'){
                break;
            }
            i++;
            if(i == inputArray.length){
                return false;
            }
        }
        String end = input.substring(i);
        return ((end.equals("@fournier-fils.com")) || (end.equals("@swatcrete.com")));
    }
}