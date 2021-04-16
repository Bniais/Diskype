import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Enumération représentant des emojis avec leurs symboles et leur image
 */
public enum Emoji {
    SMILE("Client/emoji/smile.png", ":)", ":-)"), 
    SAD("Client/emoji/sad.png", ":(", ":c", ":C", ":-("), 
    HEART("Client/emoji/heart.png","<3"),
    WINK("Client/emoji/wink.png",";)", ";-)"), 
    LAUGH("Client/emoji/laugh.png",":D",":d", ":-D"), 
    CRY("Client/emoji/cry.png",":'("), 
    OPEN_MOUTH("Client/emoji/open.png",":o", ":O");

    /** La taille de l'emoji */
    private static final int TAILLE_EMOJI = 18;
    
    /** Les symboles de l'emoji */
    private String[] symbols;

    /** Le style contenant l'image de l'emoji */
    private MutableAttributeSet style;

    /**
     * Constructeur d'un emoji, en spécifiant le chemin de l'image et les symboles associés
     * @param path Le chemin de l'image
     * @param argsSymbol Les symboles de l'emoji
     */
    private Emoji(String path, String... argsSymbol) {
        symbols = argsSymbol;

        //Créer le style contenant l'image
        style = new SimpleAttributeSet();
        ImageIcon emojiIcon = new ImageIcon(path);

        Image image = emojiIcon.getImage();
        Image newimg = image.getScaledInstance(TAILLE_EMOJI, TAILLE_EMOJI, java.awt.Image.SCALE_SMOOTH); //Scale l'image

        StyleConstants.setIcon(style, new ImageIcon(newimg));
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
    }

    /**
     * Acesseur du style
     * 
     * @return Le style de l'emoji
     */
    public MutableAttributeSet getStyle() {
        return style;
    }

    /**
     * Permet de rechercher et remplacer les symboles correspondant à un emoji par son image associée
     * @param doc Le document où chercher les symboles
     * @param offset Le caractère de début de recherche
     */
    public void remplaceSmiley(StyledDocument doc, int offset) {
        //On regarde chaque symbole de l'emoji et on les cherche dans le texte
        for(String s : symbols){
            int indexMin = offset;
            while (indexMin != -1) { //Tant qu'on trouve des simleys
                try {
                    indexMin = doc.getText(0, doc.getLength()).indexOf(s, indexMin);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
    
                if(indexMin != -1){
                    //On a trouvé le smiley, on le remplace par son image
                    doc.setCharacterAttributes(indexMin, s.length(), style, false);
                    indexMin += s.length();
                }   
            }
        }
    }
}
