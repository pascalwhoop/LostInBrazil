package pp2014.team32.server.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Brokmeier, Pascal
 * @version 17.06.14
 */

//@RunWith(JUnit4.class)
public class MultithreadedSocketTest {

    HashMap<String, String> objectToSend = new HashMap<>();
    Random generator = new Random();



   /*@Test
    public void performTest() {
        fillHashMap();
        runManipulationThread();
        runServerSocket();
        runClientSocket();

    }*/

    private void runServerSocket() {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(13234);
                    while (true) {
                        handleObjectReceivedServerSide(serverSocket.accept());
                    }
                } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    private void runClientSocket() {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("localhost", 13234);

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    while (true){
                        Object o = ois.readObject();
                        oos.writeObject(o);
                    }

                } catch (IOException | ClassNotFoundException e) {
                }
            }
        });
        clientThread.start();
    }

    private void runManipulationThread() {
        Thread manipulationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    manipulateArrayList();
                }catch (InterruptedException e){

                }
            }
        });
        manipulationThread.start();
    }

    private void handleObjectReceivedServerSide(Socket client) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        while (true) {
            oos.writeObject(objectToSend);
            System.out.println("SERVER: Object OUT!");
            Object o = ois.readObject();
            System.out.println("SERVER: Object IN!");
        }
    }

    private void fillHashMap() {
        for (int i = 0; i < 1000; i++) {
            objectToSend.put(String.valueOf(i), "foo bar");
        }
    }

    private void manipulateArrayList() throws InterruptedException{
        while (true) {
            if (generator.nextDouble() < 0.5) {
                objectToSend.put(String.valueOf(generator.nextInt(1000)), "added element no" + generator.nextInt());
                System.out.println("element added");
            } else {
                objectToSend.remove(String.valueOf(generator.nextInt(1000)));
                System.out.println("element removed");
            }
            Thread.sleep(5);
        }
    }
}
