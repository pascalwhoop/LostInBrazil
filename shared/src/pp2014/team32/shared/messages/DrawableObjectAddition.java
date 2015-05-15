package pp2014.team32.shared.messages;

import pp2014.team32.shared.entities.DrawableObject;
import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Ein DrawableObject wird zur aktuellen LevelMap des Clients hinzugefuegt
 * 
 * @author Christian Hovestadt
 * @version 14.6.14
 */
public class DrawableObjectAddition extends Message{
    private static final long serialVersionUID = 9168624530608860380L;

    public final DrawableObject DRAWABLE_OBJECT;

    /**
     * @param DRAWABLE_OBJECT Neues DrawableObject
     * @author Christian Hovestadt
     */
    public DrawableObjectAddition(DrawableObject DRAWABLE_OBJECT) {
        super(MessageType.DRAWABLEOBJECTADDITION);
        this.DRAWABLE_OBJECT = DRAWABLE_OBJECT;
    }
}
