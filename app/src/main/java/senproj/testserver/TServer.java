package senproj.testserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static android.R.id.message;

/**
 * Created by Jay on 3/1/2017.
 */

public class TServer {
    MainActivity mainActivity;
    ServerSocket serverSocket;
    String msg = "";
    static final int socketServerPort = 8080;

    public TServer(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public int getPort(){
        return socketServerPort;
    }

    public void onDestroy() {
        if (serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {
        int connectionCount = 0;
        public void run(){
            try {
                serverSocket = new ServerSocket(socketServerPort);

                while(true){
                    //Line below blocks until a connection is made.
                    Socket acceptSocket = serverSocket.accept();
                    connectionCount++;
                    msg += "Connection # " + connectionCount +
                            " from "
                            + acceptSocket.getInetAddress() +
                            " : " + acceptSocket.getPort() + "\n";
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.msg.setText(msg);
                    }
                });
                    SocketServerReplyThread socketServerReplyThread =
                            new SocketServerReplyThread(acceptSocket, connectionCount);
                    socketServerReplyThread.run();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int connectionNumber;

        SocketServerReplyThread(Socket socket, int connectionNumber){
            hostThreadSocket = socket;
            this.connectionNumber = connectionNumber;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Server, you are #" + connectionNumber;

            try{
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                msg += "replayed: " + msgReply + "\n";

                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mainActivity.msg.setText(msg);
                    }
                });

            }catch(IOException e){
                e.printStackTrace();
            }

            mainActivity.runOnUiThread(new Runnable() {
                public void run(){
                    mainActivity.msg.setText(msg);
                }
            });
        }
    }
    public String getIpAddress(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}
