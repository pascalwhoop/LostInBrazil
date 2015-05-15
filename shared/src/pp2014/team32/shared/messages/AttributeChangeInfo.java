package pp2014.team32.shared.messages;

import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Sie informiert ueber die Aenderung der Attribute einer Creature.
 * 
 * @author Christian Hovestadt
 * @version 31.05.14
 */
public class AttributeChangeInfo extends Message {
    private static final long serialVersionUID = -4012554032395077075L;
    public final int CREATURE_ID;
    public final Attributes newAttributes;

    /**
     * @param CREATURE_ID ID des GameCharacters, dessen Attributes aktualisiert werden soll
     * @param newAttributes neues Attributes-Objekt
     * @author Christian Hovestadt
     */
    public AttributeChangeInfo(int CREATURE_ID, Attributes newAttributes){
        super(MessageType.ATTRIBUTECHANGEINFO);

        this.CREATURE_ID = CREATURE_ID;
        this.newAttributes = newAttributes;
    }
}
