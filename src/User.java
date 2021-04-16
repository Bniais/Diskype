import java.awt.Color;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Classe représentant un utilisateur avec son nom, sa couleur et ses styles
 */
public class User {
    /** La couleur utilisée pour l'utilisateur */
    private Color color;

    /** Le nom de l'utilisateur */
    private String name;
    
    /** Les styles de l'utilisateur */
    private MutableAttributeSet style, meStyle;

    /** Determine si c'est l'utilisateur principal (celui qui utilise l'instance de App) */
    private boolean isSelf;

    /**
     * Constructeur de l'utilisateur, en spécifiant le nom, la couleur, si c'est l'utilisateur principal
     * @param name Le nom de l'utilisateur
     * @param color La couleur de l'utilisateur
     * @param isSelf Est l'utilisateur principal ?
     */
    public User(String name, Color color, boolean isSelf){
        this.name = name;
        this.color = color;
        this.isSelf = isSelf;

        /* Crée le style d'envoi de message standard */
        style = new SimpleAttributeSet();
        StyleConstants.setBold(style, true);
        StyleConstants.setForeground(style, color);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);

        /* Crée le style d'envoi de message en tant qu'utilisateur principal */
        if(isSelf){
            meStyle = new SimpleAttributeSet();
            StyleConstants.setBold(meStyle, true);
            StyleConstants.setForeground(meStyle, color);
            StyleConstants.setAlignment(meStyle, StyleConstants.ALIGN_RIGHT);
            
        }
        else meStyle = style;
    }

    /**
     * Accesseur en écriture de name
     * @param name
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Accesseur en lecture de name, qui renvois "Moi" si c'est l'utilisateur principal
     * @return Le nom de l'utilisateur, ou "Moi" si c'est l'utilisateur principal
     */
    public String getName() {
        if(!isSelf)
            return name;
        else
            return "Moi";
    }

    /**
     * Accesseur direct en lecture de name
     * @return Le nom de l'utilisateur
     */
    public String getRealName() {
            return name;
    }

    /**
     * Accesseur en lecture de color
     * @return La couleur
     */
    public Color getColor() {
        return color;
    }

    /**
     * Accesseur en lecture du style de base
     * @return Le style de base
     */
    public MutableAttributeSet getStyle() {
        return style;
    }

    /**
     * Accesseur en lecture du style d'utilisateur principal
     * @return Le style d'utilisateur principal
     */
    public MutableAttributeSet getMeStyle() {
        return meStyle;
    }
}
