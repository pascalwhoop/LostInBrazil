package pp2014.team32.client.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.client.test.utils.ObjectSerializationHelper;
import pp2014.team32.shared.enums.ChatMessageType;
import pp2014.team32.shared.messages.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Brokmeier, Pascal
 * @version 27.05.14
 */
@RunWith(JUnit4.class)
public class ServerConnectionTest {

    /**
     * Eine Methode um zu testen, ob die ObjectInputStreams und ObjectOutputStreams richtig genutzt wurden. Dafuer wird
     * der Server "weggemockt" und stattdessen dummy werte zurueck gegeben.
     *
     * @author Brokmeier, Pascal
     */
    @Test
    public void testInit() throws IOException {

        //response from server
        byte[] serverMockResponse = ObjectSerializationHelper.serialize(new ChatMessage("testUser", "testMessage", ChatMessageType.CHAT));
        //using mockito
        final Socket socket = mock(Socket.class);
        final ObjectInputStream oos = mock(ObjectInputStream.class);
        final OutputStream os = mock(OutputStream.class);
        when(socket.getOutputStream()).thenReturn(os);

    }
}
