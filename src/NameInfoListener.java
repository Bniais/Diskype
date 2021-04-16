import javax.swing.event.DocumentEvent;

/**
 * Classe représentant le listener du champ du nom
 */
public class NameInfoListener extends ConnectionInfoListener {

    /**
     * Consctructeur du listener du champ du nom
     */
    public NameInfoListener(App app){
        super(app);
    }

    @Override
    /**
     * Méthode de réaction à la modification du champ du nom, en faisant les vérifications des contenus des champs
     * @param e L'évènement d'insertion
     */
    public void insertUpdate(DocumentEvent e) {
        //Vérification du nom
        if( !app.isChatVisible() && isNameValid( app.getNameText() ) ){
            app.putDefaultBorder( app.getNameTextField() );// Mettre la bordure de base si le nom est correct

            // Vérification des autres champs
            int port = app.getPortInt();
            if( isIpValid( app.getIpText() ) && isPortValid(port) ){
                app.getConnexionButton().setEnabled(true);// Activer le bouton de connexion si ils sont corrects
            }
        }
        else if( !app.isChatVisible() ){
            // Mettre une bordure pour indiquer que le champ est au mauvais format et désactiver le bouton
            app.putErrorBorder( app.getNameTextField() );
            app.getConnexionButton().setEnabled(false);
        }
    }
}