import javax.swing.event.DocumentEvent;

/**
 * Classe représentant le listener du champ du port
 */
public class PortInfoListener extends ConnectionInfoListener {

    /**
     * Consctructeur du listener du champ du port
     */
    public PortInfoListener(App app){
        super(app);
    }

    @Override
    /**
     * Méthode de réaction à la modification du champ du port, en faisant les vérifications des contenus des champs
     * @param e L'évènement d'insertion
     */
    public void insertUpdate(DocumentEvent e) {
        // Vérfication du port
        int port = app.getPortInt();
        if( isPortValid(port) ){
            app.putDefaultBorder( app.getPortTextField() ); //Mettre la bordure de base si le port est correct

            //Vérification des autres champs
            if( isIpValid( app.getIpText() ) && isNameValid( app.getNameText() ) ){ 
                app.getConnexionButton().setEnabled(true); //Activer le bouton de connexion si ils sont corrects
            }
        }
        else{
            //Mettre une bordure pour indiquer que le champ est au mauvais format et désactiver le bouton
            app.putErrorBorder( app.getPortTextField() );
            app.getConnexionButton().setEnabled(false);
        } 
    }
}