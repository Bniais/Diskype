import java.util.regex.Pattern;
import java.awt.event.KeyListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyEvent;

/**
 * Classe abstraite pour les différents champs d'informations de connexion
 */
public abstract class ConnectionInfoListener implements DocumentListener, KeyListener {

    /** Les ports min et max autorisés */
    private static final int MIN_PORT = 0, //exclus
                            MAX_PORT = 65536; //exclus

    /** Chaine de caractère pour regex de vérification d'ip. Autorise 999.999.999.999 même si c'est pas valide. */
    private static final String IP_REGEXP = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

    /** Pattern de regex pour la vérification de l'ip */
    private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);

    /** Pattern de regex pour la vérification du nom. Autorise les pseudos de 3 caractères ou plus contenannt des chiffres ou des lettres. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3}[a-zA-Z0-9]*$");

    /* L'application (interface) */
    protected App app;

    /** Constructeur du listener des champs d'information de connexion */
    protected ConnectionInfoListener(App app){
        super();
        this.app = app;
    }

    @Override
    /**
     * Méthode réagissant aux effacements des champs d'informations, appel la même méthode que l'insertion
     * @param e L'évènement auquel on réagit
     */
    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }
    
    @Override
    /**
     * Méthode réagissant aux modifications des champs d'informations, appel la même méthode que l'insertion 
     * @param e L'évènement auquel on réagit
     */
    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    //Les sous-classes redéfinieront le insertUpdate

    @Override
    public void keyTyped(KeyEvent e) {//do nothing
    }

    @Override
    /**
     * Méthode de réaction à l'appuie de touches, pour appuyer sur le bouton en appuyant sur entrer
     * @param e L'évènement auquel on réagit
     */
    public void keyPressed(KeyEvent e) {
        //On réagit à la toucher entrer pour appuyer sur le bouton connexion
        if( e.getKeyCode() == KeyEvent.VK_ENTER){
            app.getConnexionButton().doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {//do nothing
    }

    /**
     * Détermine si le port est valide
     * @return Vrai si il l'est, sinon Faux
     */
    public boolean isPortValid(int port){
        return port > MIN_PORT && port < MAX_PORT;
    }

    /**
     * Détermine si l'ip est valide grace au regex défini
     * @return Vrai si il l'est, sinon Faux
     */
    public boolean isIpValid(String ip){
        return IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * Détermine si le nom est valide grace au regex défini
     * @return Vrai si il l'est, sinon Faux
     */
    public boolean isNameValid(String name){
        return NAME_PATTERN.matcher(name).matches() && !name.equals("Moi");
    }
}