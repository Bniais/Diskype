import javax.swing.event.DocumentEvent;

/**
 * Classe représentant le listener du champ de l'ip
 */
public class IpInfoListener extends ConnectionInfoListener {

    /**
     * Consctructeur du listener du champ de l'ip
     */
    public IpInfoListener(App app){
        super(app);
    }

    @Override
    /**
     * Méthode de réaction à la modification du champ de l'ip, en faisant les vérifications des contenus des champs
     * @param e L'évènement d'insertion
     */
    public void insertUpdate(DocumentEvent e) {
        // Vérification de l'ip
        if( isIpValid( app.getIpText() ) ){
            app.putDefaultBorder( app.getIpTextField() );// Mettre la bordure de base si l'ip est correct

            // Vérification des autres champs
            int port = app.getPortInt();
            if( isNameValid( app.getNameText() ) && isPortValid(port) ){
                app.getConnexionButton().setEnabled(true);// Activer le bouton de connexion si ils sont corrects
            }
        }
        else{
            // Mettre une bordure pour indiquer que le champ est au mauvais format désactiver le bouton
            app.putErrorBorder( app.getIpTextField() );
            app.getConnexionButton().setEnabled(false);
        }
    }
}