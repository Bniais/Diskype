import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Classe de listener pour le bouton de connexion
 */
public class ConnexionButtonListener implements ActionListener {
    /** L'application (interface) */
    private App app;

    /**
     * Constructeur du listener de bouton de connexion, en spécifiant l'application
     * @param app L'application liée
     */
    public ConnexionButtonListener(App app){
        super();
        this.app = app;
    }

    @Override
    /**
     * Méthode de réaction au clic bouton, en se connectant ou déconnecant 
     * @param e L'évènement auquel on réagit
     */
    public void actionPerformed(ActionEvent e) {          

        if(app.isChatVisible()){ //On se déconnecte si le chat est visible
            app.deconnexionServer(false);
        }
        else{ //On essaie de se connecter si le chat n'est pas visible
            if(app.connexionServer(app.getIpText(), app.getPortInt())){
                app.setConnexion(new ConnexionClient(app));
            }
        }
    }
    
}
