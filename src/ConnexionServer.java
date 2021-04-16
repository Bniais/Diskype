import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe gérant la connexion entre client et serveur, côté serveur
 */
public class ConnexionServer implements Runnable{
    /** Le nombre de caractères utilisés pour représenter une commande */
    private static final int NB_CHAR_CMD = 6;

    /** Le nom de l'utilisation connecté */
    private String name = "";

    /** Le client avec qui la connexion est faite */
    private Socket client;

    /** Le serveur avec qui la connexion est faite */
    private Server server;

    /** Le stream serveur->client */
    private DataOutputStream dataOutputStream;

    /** Le stream client->serveur */
    private DataInputStream dataInputStream;

    /** 
     * Accesseur en lecture du nom
     * @return Le nom de l'utilisateur connecté
     */
    public String getName(){
        return name;
    }
    
    /**
     * Accesseur en écriture du nom
     * @param n Le nom de l'utilisateur connecté
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Constructeur d'une connexion côté serveur
     * @param client Le client avec qui la connexion est faite
     * @param server Le serveur avec qui la connexion est faite
     */
    public ConnexionServer(Socket client, Server server){
        this.client = client;
        this.server = server;

        try{
            //Creation des streams
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            dataInputStream =  new DataInputStream(client.getInputStream());
        }
        catch(IOException e){
            System.out.println("Exception creating streams : " + e);
            stop(null);
        }
        
        new Thread(this).start();
    }

    /** 
     * Envoie un signal à la connexionClient sous forme de String
     * @param sig Le signal à envoyer
     */
    public void sendSignal(String sig){
        try{
            dataOutputStream.writeUTF(sig);
        }
        catch(IOException e){
            System.out.println("erreur sending signal");
            stop(sig);
        }
    }

    /**
     * Thread principal de reception de messages envoyés par le client
     */
    public void run(){
        String msgReceive, msgContent;
        try{
            do{
                //Receptionner le message
                msgReceive = dataInputStream.readUTF();
                
                if(msgReceive.startsWith("/conn@")){//Sous forme /conn@name
                    msgContent = msgReceive.substring(NB_CHAR_CMD);
                    System.out.println("Command to connect received");

                    //Changer le nom de la connexion
                    name = msgContent;

                    //Avertir les utilisateurs de la connexion
                    server.connectUser(this);
                }
                else if(msgReceive.startsWith("/name@")){// Sous forme /name@name
                    //NOT USED

                    msgContent = msgReceive.substring(NB_CHAR_CMD);
                    System.out.println("Command to rename into \"" + msgContent + "\" received from " + name);

                    // Changer le nom de la connexion
                    name = msgContent;

                    //Avertir les utilisateurs du renommage
                    server.renameUser(this);
                }
                else if(msgReceive.startsWith("/send@")){// Sous forme /send@msg
                    msgContent = msgReceive.substring(NB_CHAR_CMD);
                    System.out.println("Command to send message \"" + msgContent + "\" received from " + name);

                    // Avertir les utilisateurs du message
                    server.sendMessage(msgContent, this);
                }
                else if(msgReceive.startsWith("/priv@")){// Sous forme /priv@id@msg
                    System.out.println(msgReceive);
                    msgContent = msgReceive.substring(NB_CHAR_CMD);
                    int dest = Integer.parseInt(msgContent.split("@")[0]);
                    msgContent = msgContent.substring(((Integer)dest).toString().length()+1);

                    System.out.println("Command to send private message \"" + msgContent + "\" to " + dest + " received from " + name);

                    //Avertir le destinataire et l'envoyeur du message
                    server.sendMessage(msgContent, this, dest);
                }
                else if(msgReceive.startsWith("/disc")){// Sous forme /disc
                    System.out.println("Command to disconnect received from " + name);

                    //Send the message to others
                    server.disconnectUser(this);
                }
                else{
                    System.out.println("Unknown message received from " + name + " : " + msgReceive);
                }
            }while(client.isConnected());
        }
        catch(Exception ex){
            System.out.println("exception runnin" + ex);
            stop(null);
        }
    }

    /** 
     * Permet de fermer la connexion et donc d'arrêter le thread
     * @param sig Le signal reçu pour arrêter le serveur, sinon null
     */
    public void stop(String sig){
        if(sig == null || !sig.startsWith("/disc")){
            try{
                System.out.println("Server interupted by exception");
                server.disconnectUser(this);
                if(client.isConnected())
                    client.close();
            }
            catch(IOException e){
                System.out.println("Fermeture session : " + e.getMessage());
            }
        }
       
    }
}
