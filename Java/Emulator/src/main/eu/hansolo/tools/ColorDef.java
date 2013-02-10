package eu.hansolo.tools;


/**
 * Definition of colors that will be used in gradients etc.
 * This is useful to assure that you use the same color combinations
 * in all the different components. Each color is defined in three
 * brightness levels.
 * @author hansolo
 */
public enum ColorDef
{
    RED(new java.awt.Color(162, 0, 0, 255), new java.awt.Color(214, 62, 50, 255), new java.awt.Color(252, 29, 0, 255)),
    ORANGE(new java.awt.Color(150, 53, 26, 255), new java.awt.Color(252, 81, 0, 255), new java.awt.Color(253, 136, 0, 255)),
    YELLOW(new java.awt.Color(162, 162, 0, 255), new java.awt.Color(214, 214, 50, 255), new java.awt.Color(252, 252, 29, 255)),
    GREEN(new java.awt.Color(0, 162, 0, 255), new java.awt.Color(62, 214, 50, 255), new java.awt.Color(29, 252, 0, 255)),
    BLUE(new java.awt.Color(0, 0, 162, 255), new java.awt.Color(50, 62, 214, 255), new java.awt.Color(0, 29, 252, 255)),
    GRAY(new java.awt.Color(106, 106, 106, 255), new java.awt.Color(156, 156, 156, 255), new java.awt.Color(205, 205, 205, 255)),
    CYAN(new java.awt.Color(15, 109, 108, 255), new java.awt.Color(0, 255, 255, 255), new java.awt.Color(179, 255, 255, 255)),
    MAGENTA(new java.awt.Color(98, 0, 114, 255), new java.awt.Color(255, 0, 255, 255), new java.awt.Color(255, 179, 255, 255)),
    RAITH(new java.awt.Color(0, 65, 125, 255), new java.awt.Color(0, 106, 172, 255), new java.awt.Color(130, 180, 214, 255)),
    GREEN_LCD(new java.awt.Color(15, 109, 93, 255), new java.awt.Color(0, 185, 165, 255), new java.awt.Color(48, 255, 204,255)),
    JUG_GREEN(new java.awt.Color(0x204524), new java.awt.Color(0x32A100), new java.awt.Color(0x81CE00)),
    WHITE(new java.awt.Color(220, 220, 220, 255), new java.awt.Color(235, 235, 235, 255), java.awt.Color.WHITE);

    public final java.awt.Color DARK;
    public final java.awt.Color MEDIUM;
    public final java.awt.Color LIGHT;
  
    ColorDef(final java.awt.Color DARK_COLOR, final java.awt.Color MEDIUM_COLOR, final java.awt.Color LIGHT_COLOR)
    {
        this.DARK = DARK_COLOR;
        this.MEDIUM = MEDIUM_COLOR;
        this.LIGHT = LIGHT_COLOR;
    }
}
