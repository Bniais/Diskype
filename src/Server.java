import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

/**
 * Classe représentant le serveur princpal, qui gère les différentes connexions et envois de messages
 */
public class Server {
    public static void main(String[] args){
        new Server();
    }

    /** La liste de connexions avec des clients */
    private List<ConnexionServer> connexions;

    /** 
     * Constructeur de Serveur, qui crée la liste de connexions, ouvre le serveur et attend des clients 
     */
    public Server(){
        connexions = new ArrayList<ConnexionServer>();
        ServerSocket server = null;

        try{
            //Créer le serveur
            server = new ServerSocket(4444);

            //Attendre des connexions
            while(true){    
                System.out.println("Waiting for client, port 4444");
                connexions.add(new ConnexionServer(server.accept(), this));
            }
        }
        catch(IOException e){
            System.out.println("Exception accept : " + e.getMessage());
            try {
                server.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    
    /**
     * Obtient un nom unique à partir d'un nom de base
     * @param baseName Le nom de base
     * @param connexionSource La connexion qui demande un nom valide
     * @return Le nom unique
     */
    public String getValidName(String baseName, ConnexionServer connexionSource){
        boolean found;
        String name = baseName;

        //On augmente le # tant que le nom n'est pas unique
        do{
            found = false;
            for(ConnexionServer c : connexions){
                if(c != connexionSource && c.getName().equals(name)){
                    //On a trouvé un nom identique
                    found = true;
                    String i = name.split("#")[1];
                    if(i.equals("")){ //Normalement ne peut pas arriver, car les noms sont AAA#0
                        name += "0";
                    }
                    else{    
                        //On incrémente l'id du #
                        try{
                            int n = Integer.parseInt(i);
                            name = name.substring(0, name.split("#")[0].length()+1);
                            name += ((Integer)(n+1)).toString();
                        }
                        catch(Exception e){
                            name += "1";
                        }
                    }
                    break;
                } 
            }
        }while(found);

        return name;
    }

    /**
     * Connecte un utilisateur, lui donne les utilisateurs connectés, et informe les autres utilisateurs
     * @param connexionSource L'utilisateur qui souhaite se connecter
     */
	public void connectUser(ConnexionServer connexionSource) {
        //Vérifier et modifer le nom si besoin
        String name = connexionSource.getName();
        name = getValidName(name, connexionSource);
        connexionSource.setName(name);

        //Envoyer les message
        int iSrc = connexions.indexOf(connexionSource);
        for(ConnexionServer c : connexions) {  
            if(c!=connexionSource){
                //Dire au nouvel utilisateur qui est déjà connecté
                connexionSource.sendSignal("/user@" + connexions.indexOf(c) + "@" + c.getName());
            }
            //Dire aux utilisateurs que quelqu'un s'est connecté
            c.sendSignal("/conn@" + iSrc + "@" + name);
        }
	}

    /**
     * Envoit un message global
     * @param msgContent Le texte à envoyer
     * @param connexionSource L'utilisateur qui envoit le message
     */
	public void sendMessage(String msgContent, ConnexionServer connexionSource) {
        int iSrc = connexions.indexOf(connexionSource);
        for(ConnexionServer c : connexions) {
            c.sendSignal("/send@" + iSrc + "@" + msgContent);
        }
	}

    /**
     * Renome un utilisateur et informe les autres, pas utilisée ni terminée
     * @param connexionSource L'utilisateur qui souhaite se renommer
     */
	public void renameUser(ConnexionServer connexionSource){
        int iSrc = connexions.indexOf(connexionSource);
        
        String name = connexionSource.getName();
        name = getValidName(name, connexionSource);
        connexionSource.setName(name);
        
        for(ConnexionServer c : connexions) {
            c.sendSignal("/name@" + iSrc + "@" + name);
        }
	}

    /**
     * Déconnecte un utilisateur, lui donne les utilisateurs connectés, et informe les autres utilisateurs
     * @param connexionSource Le client qui souhaite se déconnecter
     */
    public void disconnectUser(ConnexionServer connexionSource){
        int iSrc = connexions.indexOf(connexionSource);
        if(iSrc != -1){
            //Informe tout le monde
            for(ConnexionServer c : connexions) {
                c.sendSignal("/disc@" + iSrc);
            }

            //Supprime la connexion de la liste
            connexions.remove(connexionSource);
        }        
	}

    /**
     * Envoie un message privé
     * @param msgContent Le contenu du message
     * @param connexionSource L'utilisateur qui a envoyé le message
     * @param dest L'id de l'utilisateur à qui envoyer
     */
	public void sendMessage(String msgContent, ConnexionServer connexionSource, int dest) {
        if(dest < connexions.size()){
            int iSrc = connexions.indexOf(connexionSource);

            //Envoie le message au destinataire
            connexions.get(dest).sendSignal("/priv"+ "@" + iSrc + "@" + dest + "@" + msgContent);

            //Valide l'envoi à l'envoyeur
            connexionSource.sendSignal("/priv" + "@" + iSrc + "@" + dest + "@" + msgContent);
        }
	}
}