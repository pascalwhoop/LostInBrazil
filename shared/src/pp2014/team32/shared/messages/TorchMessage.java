package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Der Server weist den Client mit dieser Message darauf hin, dass ein
 * GameCharacter im selben Level wie der Client eine Fackel an- oder abgelegt
 * hat.
 * 
 * @author Christian Hovestadt
 * @version 5.7.14
 */
public class TorchMessage extends Message {

	private static final long	serialVersionUID	= 6004342349061835109L;

	public final int			GAMECHARACTER_ID;
	public final boolean		TORCH_IN_USE;

	/**
	 * @param GAMECHARACTER_ID ID des GameCharacters, den die Aenderung betrifft
	 * @param TORCH_IN_USE Fackel angelegt (true) oder abegelegt (false)
	 * @author Christian Hovestadt
	 */
	public TorchMessage(int GAMECHARACTER_ID, boolean TORCH_IN_USE) {
		super(MessageType.TORCHMESSAGE);
		this.GAMECHARACTER_ID = GAMECHARACTER_ID;
		this.TORCH_IN_USE = TORCH_IN_USE;
	}
}
