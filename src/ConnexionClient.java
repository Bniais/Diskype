import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe gérant la connexion entre client et serveur, côté client
 */
public class ConnexionClient implements Runnable {
    /** Le nombre de caractères utilisés pour représenter une commande */
    private static final int NB_CHAR_CMD = 6;

    /** Le serveur avec qui la connexion est faite */
    private Socket server;

    /** L'application avec qui la connexion est faite */
    private App app;

    /** Le stream serveur->client */
    private DataInputStream dataInputStream;

    /** Le stream client->serveur */
    private DataOutputStream dataOutputStream;
    
    /**
     * Constructeur d'une connexion côté client
     * @param app L'application avec qui la connexion est faite
     */
    public ConnexionClient(App app){
        this.server = app.getServer();
        this.app = app;

        try{
            server.setSoTimeout(1000*666); //666secs = disconnet
            
            //Création des streams
            dataInputStream =  new DataInputStream(server.getInputStream());
            dataOutputStream = new DataOutputStream(server.getOutputStream());
        }
        catch(IOException e){
            System.out.println("Erreur création streams : " + e);
        } 
        new Thread(this).start();
    }

    /** 
     * Extrait un int d'un String, doit seulement contenir un int et rien d'autre 
     * @param s La String d'où extraire le int
     * @return L'entier contenu dans la String, sinon -1 si mauvais format
     */
    public int getIntFromString(String s){
        try{
            return Integer.parseInt(s);
        }
        catch(Exception e){
            return -1;
        }
    }

    /**
     * Envoie un message au serveur
     * @param msg Le message à envoyer
     */
    public void sendMessage(String msg){
        try {
            dataOutputStream.writeUTF("/send@" + msg);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Envoie un message privé au serveur déstiné à un utilisateur précis
     * @param msg Le message à envoyer
     * @param id L'id du destinataire
     */
    public void sendPrivateMessage(String msg, int id) {
        try {
            dataOutputStream.writeUTF("/priv@" + id + "@" + msg );
        } catch (IOException e) {
            System.out.println(e);
        }
    }


	@Override
	/**
     * Thread principal de reception de messages envoyés par le serveur
     */
    public void run() {
        try{
            //Premierement, on informe le serveur de notre connexion
            dataOutputStream.writeUTF("/conn@" + app.getNameText() + "#0");

            //Recevoir les messages du serveur, avec les différentes commandes
            String msgReceive, msgContent;
		    do {
                msgReceive = dataInputStream.readUTF();
                msgContent = msgReceive.substring(NB_CHAR_CMD);

                if(msgReceive.startsWith("/conn@")) {
                    String[] argv = msgContent.split("@");
                    app.connectUser(getIntFromString(argv[0]), msgContent.substring(argv[0].length()+1) );
                }
                else if(msgReceive.startsWith("/name@")) {
                    String[] argv = msgContent.split("@");
                    app.renameUser(getIntFromString(argv[0]), msgContent.substring(argv[0].length()+1));
                }
                else if(msgReceive.startsWith("/send@")) {
                    String[] argv = msgContent.split("@");
                    app.sendMessage(getIntFromString(argv[0]), msgContent.substring(argv[0].length()+1));
                }
                else if(msgReceive.startsWith("/priv@")) {
                    String[] argv = msgContent.split("@");
                    app.sendPrivateMessage(getIntFromString(argv[0]), getIntFromString(argv[1]), msgContent.substring(argv[0].length() + argv[1].length() + 2));
                }
                else if(msgReceive.startsWith("/disc@")) {
                    app.disconnectUser(getIntFromString(msgContent));
                }
                else if(msgReceive.startsWith("/user@")) {
                    String[] argv = msgContent.split("@");
                    app.connectedUser(getIntFromString(argv[0]), msgContent.substring(argv[0].length()+1), true);
                }
            }while (server.isConnected());
        }
        catch(Exception ex){
            System.out.println("Exception in running :" + ex);
            stop();
        }
	}
    

    /**
     * Arrête la connexion et le thread
     */
    public void stop(){
        try{
            System.out.println("Client interrupted by catch");
            dataOutputStream.writeUTF("/disc");
            app.deconnexionServer(true);
            server.close();
        }
        catch(IOException e){
            System.out.println("Fermeture session : " + e.getMessage());
        }
    }
}
