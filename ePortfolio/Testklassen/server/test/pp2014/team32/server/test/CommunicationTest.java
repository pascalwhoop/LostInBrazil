package pp2014.team32.server.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.messages.AuthenticationResponse;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.messages.Ping;
import pp2014.team32.shared.messages.UserAuthentication;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Brokmeier, Pascal
 * @version 20.06.14
 */

@RunWith(JUnit4.class)
public class CommunicationTest {

    private static final int SERVER_PORT = 6032;
    private static String serverAddress = "localhost";
    private static int inputCounter = 0;
    private static StringBuffer outputBuffer = new StringBuffer();

    public static ObjectInputStream ois;
    public static ObjectOutputStream oos;


    @Test
    public void clientEndToEndConnectionTest() {
        try {
            if (initConnection()) {
                Thread outputThread = runOutput(1000);
                Thread inputThread = runInput();
                Thread.sleep(10000);
                outputThread.interrupt();
                inputThread.interrupt();
                System.out.print(outputBuffer.toString());
                System.out.println("Test ended");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void endToEndLoadTest(){
        try {
            if(initConnection()){
                Thread outputThread = runOutput(2);
                Thread inputThread = runInput();
                Thread.sleep(10000);
                outputThread.interrupt();
                inputThread.interrupt();
                System.out.print(outputBuffer.toString());
                System.out.println("Test ended");
            }
        }catch (Exception e){

        }
    }

    private boolean initConnection() throws IOException, ClassNotFoundException {
        initProperties();

        Socket socket = new Socket(serverAddress, SERVER_PORT);

        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject(new UserAuthentication("foo", "bar"));
        Message message = (Message) ois.readObject();
        if (message.MESSAGE_TYPE == MessageType.AUTHENTICATIONRESPONSE) {
            AuthenticationResponse ar = (AuthenticationResponse) message;
            if (ar.success) {
                return true;
            }
        }
        return false;

    }

    public Thread runOutput(final int timeout) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        CommunicationTest.oos.writeObject(new Ping());
                        Thread.sleep(timeout);
                    }
                } catch (InterruptedException e) {
                    System.out.println("output interrupted");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    public Thread runInput() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Message message = (Message) CommunicationTest.ois.readObject();
                        outputBuffer.append(inputCounter++).append(" received: ").append(message.MESSAGE_TYPE).append("\n");
                        Thread.sleep(0);
                    }
                } catch (InterruptedException e) {
                    System.out.println("input interrupted");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }


    public void initProperties() {
        String s = (new File("")).getAbsolutePath();
        String serverPrefs = "";
        String sharedPrefs = "";

        List<String> propertyPaths = new LinkedList<String>();
        String loggingPath;
        if (s.substring((s.length() - 3), (s.length())).equals("bin")) {
            propertyPaths.add("../../checkout/server/prefs/settings.properties");
            propertyPaths.add("../../checkout/shared/prefs/settings.properties");
            loggingPath = "../../checkout/server/prefs/logging.properties";
        } else {
            propertyPaths.add("prefs/settings.properties");
            propertyPaths.add("../Shared/prefs/settings.properties");
            loggingPath = "prefs/logging.properties";
        }

        // Initialize PropertyManager
        new PropertyManager(propertyPaths, loggingPath);

    }

}
