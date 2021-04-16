import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/** Classe principale qui crée et modifie l'interfaces en fonction des messages reçus et actions effectuées */
public class App extends JFrame {


    /* _________________CONSTANTES________________ */

    /** Je sais pas ce que ça représente mais ça enlève un warning */
    private static final long serialVersionUID = 2761903103503161038L;

    /** Bordures de couleurs pour avertir qu'un champ d'information est correct ou faux */
    private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.RED),
                                DEFAULT_BORDER = BorderFactory.createLineBorder(Color.GRAY);

    /** Les différents styles de messages */
    private transient MutableAttributeSet announceStyle, messageMeStyle, messageStyle;
    
    /**
     * Initialise les styles d'écritures, j'aurais voulu le faire statiquement mais je n'y arrive pas
     */
    public void setupStyles() {
        announceStyle = new SimpleAttributeSet();
        StyleConstants.setAlignment(announceStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setForeground(announceStyle, Color.BLACK);
        StyleConstants.setBold(announceStyle, true);

        messageMeStyle = new SimpleAttributeSet();
        StyleConstants.setAlignment(messageMeStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(messageMeStyle, Color.BLACK);
        StyleConstants.setBold(messageMeStyle, false);

        messageStyle = new SimpleAttributeSet();
        StyleConstants.setAlignment(messageStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(messageStyle, Color.BLACK);
        StyleConstants.setBold(messageStyle, false);
    }

    /** Génération de couleur aléatoire */
    private static final int MAX_COLOR_SUM = 210*3;



    /* ___________ELEMENTS D'INTERFACES___________ */

    /** Bouton de connexion */
    private JButton connexionButton;

    /** Champs d'entrée des informations de connexion */
    private JTextField ipTextField, portTextField, nameTextField;

    /** Zones de texte qui contiennent les conversations de chaque onglet de discussion */
    private ArrayList<JTextPane> discussionAreas = new ArrayList<JTextPane>(), messageAreas = new ArrayList<JTextPane>();

    /** Zone de texte qui affiche les utilisateurs connectés */
    private JTextPane connecteArea;

    /** Les noms des différents onglets, pour savoir à quel utilisateur on parle */
    private ArrayList<String> ongletsNames = new ArrayList<String>();

    /** La partie centrale de l'interface, composés d'onglets (un principal et un par message privé) */
    private JTabbedPane center;

    /** La partie gauche de l'interface, où sont affichés les connectés */
    private JPanel left;


    /* ____________________RESEAU___________________ */

    /** Le serveur avec qui l'appli est connectée */
    private transient Socket server;

    /** La liste des utilisateurs connéctés */
    private transient List<User> utilisateurs;

    /** La connexion côté client */
    private transient ConnexionClient connexionClient;

    /** L'id de l'utilisateur principal (celui qui utilise cette instance interface) */
    private int id = -1;

    /** La liste des derniers envoyeurs (id) de messages de chaque onglet */
    private ArrayList<String> lastSends = new ArrayList<String>();

    /** Est-ce que les instructions du serveur ont été envoyés */
    private boolean showHelp = false;



    /* ________________ACCESSEURS_______________ */

    /**
     * Accesseur en lecture de l'ip
     * @return L'ip entré dans le champ sous forme de String
     */
    public String getIpText() {
        return ipTextField.getText().trim();
    }

    /**
     * Accesseur en lecture du nom
     * @return Le nom entré dans le champ
     */
    public String getNameText() {
        return nameTextField.getText().trim();
    }

    /**
     * Accesseur en lecture du port
     * @return Le port entré dans le champ sous forme de String
     */
    public String getPortText() {
        return portTextField.getText().trim();
    }

    /**
     * Accesseur en lecture du port qui est transformé en int
     * @return Le port entré dans le champ sous forme de int, ou -1 si le format est mauvais
     */
    public int getPortInt() {
        int port = -1;

        try {
            port = Integer.parseInt(getPortText());
        } catch (NumberFormatException ex) {
            // Do nothing, just not a int
        }

        return port;
    }

    /**
     * Accesseur en lecture du composant du champ d'ip
     * @return Le champ d'ip
     */
    public JTextField getIpTextField() {
        return ipTextField;
    }

    /**
     * Accesseur en lecture du composant du champ de port
     * @return Le champ de port
     */
    public JTextField getPortTextField() {
        return portTextField;
    }

    /**
     * Accesseur en lecture du composant du champ de nom
     * @return Le champ de nom
     */
    public JTextField getNameTextField() {
        return nameTextField;
    }

    /**
     * Accesseur en lecture du composant du bouton de connexion
     * @return Le bouton de connexion
     */
    public JButton getConnexionButton() {
        return connexionButton;
    }

    /**
     * Accesseur en lecture du serveur
     * @return Le serveur dont l'app est connecté
     */
    public Socket getServer() {
        return server;
    }

    /**
     * Accesseur de l'état du chat
     * @return Vrai s'il est visible, sinon Faux
     */
    public boolean isChatVisible() {
        return center.isVisible();
    }

    /**
     * Accesseur en écriture de la connexion du côté client.
     * @param connexionClient La connexion à mettre dans connexionClient
     */
    public void setConnexion(ConnexionClient connexionClient) {
        this.connexionClient = connexionClient;
    }

    /* ______________CONTROLER INTERFACE_______________ */
    
    /**
     * Permet de mettre le chat en visible ou invisible
     * @param b Vrai si on doit le mettre visible, sinon Faux
     */
    public void setChatVisible(boolean b) {
        center.setVisible(b);
        left.setVisible(b);
    }

    /**
     * Permet d'activer ou désactiver l'édition des champs d'information de connexion
     * @param b Vrai si on doit l'activer, sinon Faux
     */
    public void setTextFieldsEditable(boolean b) {
        ipTextField.setEditable(b);
        nameTextField.setEditable(b);
        portTextField.setEditable(b);
    }

    /**
     * Met une bordure rouge d'erreur au TextField passé en paramètre
     * @param c Le TextField auquel appliquer la bordure
     */
    public void putErrorBorder(JTextField c) {
        c.setBorder(RED_BORDER);
    }

    /**
     * Met une bordure grise par défaut au TextField passé en paramètre
     * @param c Le TextField auquel appliquer la bordure
     */
    public void putDefaultBorder(JTextField c) {
        c.setBorder(DEFAULT_BORDER);
    }

    /**
     * Obtient une couleur pseudo-aléatoire à partir d'une chaine de caractère.
     * Deux chaines indentiques auront toujours la même couleur.
     * Deux chaines proches auront des couleurs très différentes.
     * @param s La chaine de caractère à traduire en couleur
     * @return La couleur générée
     */
    public Color getNewColor(String s) {
        int r = 0, g = 0, b = 0;
        for (int i = 0; i < s.length(); i++) {
            r += (19278 % (78 * (7*s.length() - 0.2  * i  * s.charAt(i)   + 2)  + 72)  + (13*s.length()-11*i) * (31781 % (17 * s.length() - 12 * i + 124)) * s.charAt(i)) % (527 * i + 18);
            g += (15895 % (49 * (11*s.length() - 0.4  * i * s.charAt(i)  + 7)  + 399) + (31*s.length()-17*i) * (51254 % (122 * i + 478))                  * s.charAt(i))% (624 * i + 243);
            b += ( 8893 % (89 * (13*s.length() - 0.47 * i * s.charAt(i)  + 3)  + 36)  + (27*s.length()-13*i) * (92487 % (12 * s.length() - i + 398))      * s.charAt(i)) % (849 * i + 8);
            r += b%(721*s.charAt(i)) + g%(483*s.charAt(i));
            g += b%(437*s.charAt(i)) + r%(327*s.charAt(i));
            b += r%(891*s.charAt(i)) + g%(141*s.charAt(i));
        }
        if(r%255 + g%255 + b%255 < MAX_COLOR_SUM)
            return new Color(r % 256, g % 256, b % 256);
        else
            return new Color(r % 220, g % 200, b % 220); //On ne met pas sur base 256 afin de ne pas obtenir de couleurs trop proches du blanc
    }

    /**
     * Construit la partie supérieure de l'interface, avec les champs d'informations, et attribue les listeners
     */
    public void buildTopPanel() {
        JPanel top = new JPanel();

        GridBagLayout tGrid = new GridBagLayout();
        tGrid.preferredLayoutSize(top);
        top.setLayout(tGrid);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 0, 10);

        {// LIGNE 1

            //LABEL NOM
            c.weightx = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            top.add(new JLabel("Nom"), c);

            //CHAMP NOM
            c.gridx += 2;
            c.weightx = 2;
            c.gridwidth = 6;
            nameTextField = new JTextField(10);
            top.add(nameTextField, c);
            putErrorBorder(nameTextField);

            c.gridx += 6;
            c.weightx = 0.2;
            c.gridwidth = 4;
            top.add(new JLabel(""), c);

            //BOUTON CONNEXION
            c.weightx = 1;
            c.gridx += 4;
            c.gridwidth = 7;
            connexionButton = new JButton("Connexion");
            connexionButton.setEnabled(false);
            top.add(connexionButton, c);
        }

        {// LIGNE 2

            //LABEL IP
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.gridwidth = 2;
            top.add(new JLabel("IP"), c);

            //CHAMP IP
            c.gridx += 2;
            c.weightx = 2;
            c.gridwidth = 6;
            ipTextField = new JTextField(10);
            ipTextField.setText("127.0.0.1"); //Valeur locale par défaut pour gagner du temps, on peut l'enlever
            putDefaultBorder(ipTextField);
            top.add(ipTextField, c);

            c.gridx += 6;
            c.weightx = 0.2;
            c.gridwidth = 2;
            top.add(new JLabel(""), c);

            //LABEL PORT
            c.gridx += 2;
            c.weightx = 0;
            c.gridwidth = 2;
            JLabel portText = new JLabel("Port");
            top.add(portText, c);

            c.gridx += 2;
            c.weightx = 0;
            c.gridwidth = 1;
            top.add(new JLabel(""), c);

            //CHAMP PORT
            c.gridx += 1;
            c.weightx = 2;
            portTextField = new JTextField(10);
            portTextField.setText("4444"); //Port par défaut pour gagner du temps, on peut l'enlever
            putDefaultBorder(portTextField);
            top.add(portTextField, c);
        }

        // Ajouter les listeners
        portTextField.getDocument().addDocumentListener(new PortInfoListener(this));

        nameTextField.getDocument().addDocumentListener(new NameInfoListener(this));

        ipTextField.getDocument().addDocumentListener(new IpInfoListener(this));

        portTextField.addKeyListener(new PortInfoListener(this));

        nameTextField.addKeyListener(new PortInfoListener(this));

        ipTextField.addKeyListener(new PortInfoListener(this));

        connexionButton.addActionListener(new ConnexionButtonListener(this));

        this.getContentPane().add(top, BorderLayout.NORTH);
    }

    public void buildLeftPanel() {
        // LEFT
        left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(120, 10));
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Connectés title
        centerPanel.add(new JLabel("Connectés"));
        left.add(centerPanel, BorderLayout.NORTH);

        // Connectés textArea
        connecteArea = new JTextPane();
        connecteArea.setEditable(false);
        JScrollPane scrollLeft = new JScrollPane(connecteArea);
        left.add(scrollLeft);

        this.getContentPane().add(left, BorderLayout.WEST);
        left.setVisible(false);
    }

    public void removeOnglet(){
        //TODO remove onglet
        //Permet de retirer un onglet de messagerie privée et de mettre à jour la liste des onglets
        //Ceci est une amélioration possible
    }


    /**
     * Construit un nouveau espace de discussion (contenu d'onglet) en lui attribuant un titre. 
     * Plusieurs interfaces centrales doivent être construites : une par onglet.
     * @param title Le titre de l'onglet
     * @return L'interface centrale
     */
    public JPanel buildDiscussionPanel(String title){
        JPanel discussionPanel = new JPanel(new GridBagLayout());
        lastSends.add("");
        ongletsNames.add(ongletsNames.isEmpty()?title : title.split(" ")[2]);
        int idOnglet = ongletsNames.size() - 1;

        GridBagConstraints d = new GridBagConstraints();
        d.fill = GridBagConstraints.BOTH;
        d.anchor = GridBagConstraints.NORTH;
        d.insets = new Insets(0, 10, 0, 10);
        d.gridwidth = 1;
        d.gridheight = 1;

        // Discussion title
        d.weightx = 1;
        d.weighty = 0;
        d.gridx = 0;
        d.gridy = 0;
        JPanel centerPanelDiscussion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanelDiscussion.add(new JLabel(title));
        discussionPanel.add(centerPanelDiscussion, d);

        // Discussion textArea
        d.gridy += 1;
        d.gridx = 0;
        d.gridheight = 6;
        d.weightx = 1;
        d.weighty = 1;
        discussionAreas.add(new JTextPane());

        discussionAreas.get(idOnglet).setEditable(false);
        JScrollPane scrollCenter = new JScrollPane(discussionAreas.get(idOnglet));
        discussionPanel.add(scrollCenter, d);

        // Message title
        d.gridy += 6;
        d.gridx = 0;
        d.gridheight = 1;
        d.weightx = 1;
        d.weighty = 0;
        d.insets = new Insets(10, 10, 0, 10);
        discussionPanel.add(new JLabel("Message"), d);

        // Message textArea
        d.gridy++;
        d.gridx = 0;
        d.gridheight = 3;
        d.weightx = 1;
        d.weighty = 0.5;
        d.insets = new Insets(0, 10, 0, 10);
        messageAreas.add(new JTextPane());

        JScrollPane scrollCenterMessage = new JScrollPane(messageAreas.get(idOnglet));
        discussionPanel.add(scrollCenterMessage, d);

        // Envoyer Button
        d.gridy += 3;
        d.gridx = 0;
        d.gridheight = 1;
        d.weightx = 1;
        d.weighty = 0;
        d.insets = new Insets(26, 10, 0, 10);
        JButton sendButton = new JButton("Envoyer");
        discussionPanel.add(sendButton, d);

        //Ajouter le listener d'envoi de messages
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int tabId = center.getSelectedIndex();
                if(tabId == 0 && !messageAreas.get(idOnglet).getText().startsWith("@"))
                    connexionClient.sendMessage(messageAreas.get(idOnglet).getText());
                else{
                    String dest;
                    if(tabId != 0){
                        dest = ongletsNames.get(tabId);
                    }
                    else{
                        dest = messageAreas.get(idOnglet).getText().split(" ")[0].substring(1);
                    }

                    for(int i=0; i<utilisateurs.size(); i++){ //Chercher le destinataire
                        if(utilisateurs.get(i).getRealName().equals(dest)){
                            if(i==id){
                                addText(discussionAreas.get(idOnglet), " Vous ne pouvez pas envoyer un message à vous-même :D\n\n", announceStyle, true);
                                lastSends.set(tabId, "");
                            }
                            else{
                                connexionClient.sendPrivateMessage(messageAreas.get(idOnglet).getText().startsWith("@") ? messageAreas.get(idOnglet).getText().substring(dest.length()+2) : messageAreas.get(idOnglet).getText(), i);

                                if(tabId == 0){
                                    for (int j = 0; j < ongletsNames.size(); j++){
                                        if (ongletsNames.get(j).equals(dest)) {
                                            center.setSelectedIndex(j);
                                            break;
                                        }
                                    }
                                    messageAreas.get(0).setText("");
                                }                                    
                            }
                            return;
                        }
                    }
                    addText(discussionAreas.get(idOnglet), "\nNous n'avons pas pu trouver l'utilisateur " + dest ,announceStyle, false);
                    addText(discussionAreas.get(idOnglet), " :(\n\n", announceStyle, true);
                    lastSends.set(tabId, "");
                }
                    
            }
        });

        //Permet d'envoyer des messages en appuyant sur entrer, on pourrait améliorer en n'envoyant pas de message quand on shift+enter
        InputMap iMap = messageAreas.get(idOnglet).getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap aMap = messageAreas.get(idOnglet).getActionMap();
        String enter = "enter";
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
        aMap.put(enter, new AbstractAction() {
            private static final long serialVersionUID = 8750446249630071598L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                sendButton.doClick();
            }
        });
        return discussionPanel;
    }

    /**
     * Construit la première partie centrale de l'application, qui contient le TabbedPane et l'onglet principal
     */
    public void buildCenterPanel() {
        center = new JTabbedPane();
        center.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                center.setBackgroundAt(center.getSelectedIndex(), Color.WHITE);
            }
        });
        
        center.addTab("Discussion Principale", buildDiscussionPanel("Discussion"));
        
        getContentPane().add(center, BorderLayout.CENTER);
        center.setVisible(false);
    }

    /**
     * Essaie de se connecter au serveur avec l'ip et le port indiqué
     * @param ip L'ip indiqué
     * @param port Le port indiqué
     * @return Vrai si la connexion a réussi, sinon Faux
     */
    public boolean connexionServer(String ip, int port) {
        try {
            connecteArea.setText("");
            messageAreas.get(0).setText("");

            utilisateurs = new ArrayList<User>();
            lastSends.set(0, "");
                

            server = new Socket();
            server.setSoTimeout(500);
            server.connect(new InetSocketAddress(ip, port), 500);

            setChatVisible(true);
            connexionButton.setText("Deconnexion");
            setTextFieldsEditable(false);

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /*REACTION MESSAGES RESEAU */
    /**
     * Ajoute du texte à une zone de texte
     * 
     * @param tp             La zone de texte où ajouter le texte
     * @param s              Le texte à ajouter
     * @param style          Le style du texte à ajouter
     * @param remplaceSmiley Indique si l'on doit rechercher des smileys dans ce
     *                       texte
     */
    public void addText(JTextPane tp, String s, MutableAttributeSet style, boolean remplaceSmiley) {
        tp.getStyledDocument().setParagraphAttributes(tp.getStyledDocument().getLength(), 1, style, false);

        int lengthDebut = tp.getStyledDocument().getLength();
        try {
            tp.getStyledDocument().insertString(tp.getStyledDocument().getLength(), s, style);
        } catch (BadLocationException e) {
            System.out.println(e);
        }

        if (remplaceSmiley) {
            for (Emoji e : Emoji.values()) {
                e.remplaceSmiley(tp.getStyledDocument(), lengthDebut);
            }
        }

        tp.setCaretPosition(tp.getDocument().getLength());
    }

    /**
     * Retire un utilisateur de la liste des connectés
     * 
     * @param id L'id de l'utilisateur à retirer
     */
    public void removeConnected(int id) {
        utilisateurs.remove(id);
        System.out.println("REMOVE " + id);
        try {
            System.out
                    .println(connecteArea.getStyledDocument().getText(0, connecteArea.getStyledDocument().getLength()));
            String[] names = (connecteArea.getStyledDocument().getText(0, connecteArea.getStyledDocument().getLength()))
                    .split("\n");
            int offset = 0;
            for (int i = 0; i < id; i++) {
                offset += names[i].length() + 1;
            }
            connecteArea.getStyledDocument().remove(offset, names[id].length() + 1);
        } catch (BadLocationException e) {
            System.out.println(e);
        }
    }

    /**
     * Connecte un utilisateur
     * 
     * @param userId   L'id de l'utilisateur
     * @param userName Le nom de l'utilisateur
     */
    public void connectUser(int userId, String userName) {
        if (id == -1) { // Cet utilisateur est nous-même
            id = userId;
            nameTextField.setText(userName);
        }

        utilisateurs.add(userId, new User(userName, getNewColor(userName), id == userId));

        addText(discussionAreas.get(0), "Connexion de ", announceStyle, false);
        addText(discussionAreas.get(0), userName, utilisateurs.get(userId).getStyle(), false);
        addText(discussionAreas.get(0), "\n", announceStyle, false);

        if (id == userId && !showHelp) {
            showHelp = true;
            // Afficher aide
            addText(discussionAreas.get(0), "\n\nBienvenu sur ce Chat !<3\n", announceStyle, true);
            addText(discussionAreas.get(0),
                    "Vous pouvez envoyer des messages privés en commançant votre message par @nomUtilisateur ;)\n",
                    announceStyle, true);
            addText(discussionAreas.get(0),
                    "Vous pouvez également envoyer des smileys, à vous de trouver quels symboles utiliser ! :)\n\n",
                    announceStyle, true);

        }

        lastSends.set(0, "");
        connectedUser(userId, userName, false);
    }

    /**
     * Ajoute un utilisateur qui était déjà connecté à la liste des utilisateurs
     * 
     * @param userId   L'id de l'utilisateur
     * @param userName Le nom de l'utilisateur
     * @param other    Indique si la notification vient de notre propre connexion ou
     *                 d'une autre
     */
    public void connectedUser(int userId, String userName, boolean other) {
        if (other)
            utilisateurs.add(userId, new User(userName, getNewColor(userName), false));

        addText(connecteArea, utilisateurs.get(userId).getRealName() + "\n", utilisateurs.get(userId).getStyle(),
                false);
    }

    /**
     * Renomme un utilisatuer (TODO)
     * 
     * @param userId   L'id de l'utilisateur à renommer
     * @param userName Son nouveau nom
     */
    public void renameUser(int userId, String userName) {
        // Changer dans la liste des connectés
        // Changer dans les onglets
        // Changer dans les log du chat ?
        // Changer la couleur ?
    }

    /**
     * Reception (ou envoi) d'un message global, et donc affichage de ce message
     * dans l'espace de discussion principal
     * 
     * @param idSource L'id de l'envoyeur
     * @param message  Le texte du message
     */
    public void sendMessage(int idSource, String message) {
        if (idSource == id) { // Distinguer le cas où l'envoyer est nous-même ou un autre
            if (!lastSends.get(0).equals(utilisateurs.get(idSource).getName()))
                addText(discussionAreas.get(0), "\n" + " : " + utilisateurs.get(idSource).getName() + "\n",
                        utilisateurs.get(idSource).getMeStyle(), false);

            addText(discussionAreas.get(0), message + "       \n", messageMeStyle, true);
            messageAreas.get(0).setText("");
        } else {
            if (!lastSends.get(0).equals(utilisateurs.get(idSource).getName()))
                addText(discussionAreas.get(0), "\n" + utilisateurs.get(idSource).getName() + " : " + "\n",
                        utilisateurs.get(idSource).getStyle(), false);
            addText(discussionAreas.get(0), "       " + message + "\n", messageStyle, true);
        }
        lastSends.set(0, utilisateurs.get(idSource).getName());
        if (center.getSelectedIndex() != 0 && center.getTabCount() != 0)
            center.setBackgroundAt(0, Color.RED);
    }

    /**
     * Reception (ou envoi) d'un message privé
     * 
     * @param idSource L'id de l'envoyeur
     * @param idDest   L'id du destinataire
     * @param message  Le texte du message
     */
    public void sendPrivateMessage(int idSource, int idDest, String message) {

        // On regarde si le destinataire ou l'envoyeur correspond à un onglet existant
        for (int i = 0; i < ongletsNames.size(); i++) {
            if (utilisateurs.get(idSource).getName().equals(ongletsNames.get(i))) {
                System.out.println("Onglet existant receveur, on ajoute");
                if (!lastSends.get(i).equals(utilisateurs.get(idSource).getName()))
                    addText(discussionAreas.get(i), "\n" + utilisateurs.get(idSource).getRealName() + " : " + "\n",
                            utilisateurs.get(idSource).getStyle(), false);

                addText(discussionAreas.get(i), "       " + message + "\n", messageStyle, true);
                lastSends.set(i, utilisateurs.get(idSource).getName());
                if (center.getSelectedIndex() != i)
                    center.setBackgroundAt(i, Color.RED);
                return;
            } else if (utilisateurs.get(idDest).getName().equals(ongletsNames.get(i))) {
                System.out.println("Onglet existant envoyeur, on ajoute");
                if (!lastSends.get(i).equals(utilisateurs.get(idSource).getName()))
                    addText(discussionAreas.get(i), "\n" + " : " + utilisateurs.get(idSource).getName() + "\n",
                            utilisateurs.get(idSource).getMeStyle(), false);

                addText(discussionAreas.get(i), message + "       \n", messageMeStyle, true);
                lastSends.set(i, utilisateurs.get(idSource).getName());
                center.setSelectedIndex(i);
                messageAreas.get(i).setText("");
                return;
            }
        }

        // Pas d'onglet existant, on en crée un
        center.addTab(utilisateurs.get(id == idSource ? idDest : idSource).getRealName(), buildDiscussionPanel(
                "Discussion avec " + utilisateurs.get(id == idSource ? idDest : idSource).getRealName()));

        int i = center.getTabCount() - 1;
        if (utilisateurs.get(idSource).getName().equals(ongletsNames.get(i))) { // Distinguer le cas destinataire ou
                                                                                // envoyeur
            if (!lastSends.get(i).equals(utilisateurs.get(idSource).getName()))
                addText(discussionAreas.get(i), "\n" + utilisateurs.get(idSource).getName() + " : " + "\n",
                        utilisateurs.get(idSource).getStyle(), false);

            addText(discussionAreas.get(i), "       " + message + "\n", messageStyle, true);
            lastSends.set(i, utilisateurs.get(idSource).getName());
            if (center.getSelectedIndex() != i)
                center.setBackgroundAt(i, Color.RED);
            return;
        } else if (utilisateurs.get(idDest).getName().equals(ongletsNames.get(i))) {
            System.out.println("Onglet existant envoyeur, on ajoute");
            if (!lastSends.get(i).equals(utilisateurs.get(idSource).getName()))
                addText(discussionAreas.get(i), "\n" + " : " + utilisateurs.get(idSource).getName() + "\n",
                        utilisateurs.get(idSource).getMeStyle(), false);

            addText(discussionAreas.get(i), message + "       \n", messageMeStyle, true);
            lastSends.set(i, utilisateurs.get(idSource).getName());
            center.setSelectedIndex(i);
            return;
        }
    }

    /**
     * Déconnecte un utilisateur
     * 
     * @param userId L'id de l'utilisateur à déconnecter
     */
    public void disconnectUser(int userId) {
        if (userId != -1) {
            System.out.println("dc" + userId);
            addText(discussionAreas.get(0), "Déconnexion de ", announceStyle, false);
            addText(discussionAreas.get(0), utilisateurs.get(userId).getRealName(), utilisateurs.get(userId).getStyle(),
                    false);
            addText(discussionAreas.get(0), "\n", announceStyle, false);
            removeConnected(userId);

            // On pourrait aussi afficher le message de déconnexion dans les onglets privés,
            // et désactiver le bouton envoyer dans les chats privés où le correspondant est
            // déconnecté, mais on ne peut pas envoyer de message déjà, donc déjà bien

            if (userId == id) {
                deconnexionServer(false);
            } else if (userId < id) {
                id--;
            }
            lastSends.set(0, "");
        }

    }

    /**
     * Se déconnecte du serveur
     * @param fromStop Indique si le message provient de la connexion ou de l'utilisateur
     */
    public void deconnexionServer(boolean fromStop) {
        try {
            nameTextField.setText(nameTextField.getText().split("#")[0]);
            setChatVisible(false);
            connexionButton.setText("Connexion");
            setTextFieldsEditable(true);
            id = -1;
            if (!fromStop) {
                connexionClient.stop();
            }

            server.close();
        } catch (Exception e) {
            // Do nothing
        }
    }

    

    

    /**
     * Fonction principale qui démarre l'application
     * @param args Les arguments d'execution (inutiles)
     */
    public static void main(String[] args) {
        new App();
    }

    /**
     * Construit l'application en construisant les différentes parties de l'interface
     */
    public App() {
        setupStyles();
        this.setTitle("Diskype");
        this.setSize(800, 700);
        this.setMinimumSize(new Dimension(600, 500));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel container = new JPanel();
        this.setContentPane(container);

        container.setLayout(new BorderLayout(0, 10));
        container.setBorder(new EmptyBorder(0, 10, 0, 0));

        container.add(new JLabel(""), BorderLayout.EAST);
        container.add(new JLabel(""), BorderLayout.SOUTH);
        buildTopPanel();
        buildCenterPanel();
        buildLeftPanel();
        

        App me = this;
        this.addWindowListener(new WindowAdapter() { //Se déconnecter quand la fenetre se quitte
            @Override
            public void windowClosing(WindowEvent e) {
                me.deconnexionServer(false);
            }
        });

        setVisible(true);
    }
}