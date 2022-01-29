import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server
{
    static Vector<Client_Handler> sockets=new Vector<Client_Handler>(); //Vector to hold the an each instance of class client_handler(using vectors instead of lists because vectors are thread safe)
    public static void main(String args[]) throws IOException
    {
        ServerSocket socket=new ServerSocket(2020); //Server Socket running at port 2020
        int i=0;
        System.out.println("Server is waiting for Connections");
        while(true){
            System.out.println("Current Client Count- "+ sockets.size());
            Socket connection =socket.accept();
            String client_name="Client-"+i;
            PrintWriter output=new PrintWriter(connection.getOutputStream(),true);//OUTPUT STREAM
            BufferedReader input=new BufferedReader(new InputStreamReader(connection.getInputStream()));//INPUT STREAM
            Client_Handler c=new Client_Handler(client_name,connection,output,input);
            sockets.add(c);
            Thread t =new Thread(c);
            t.start();
            ++i;
        }
    }
}


class Client_Handler implements Runnable{
    Socket connection;
    PrintWriter output;
    BufferedReader input;
    String client_name;


    Client_Handler(String client_name,Socket connection,PrintWriter output,BufferedReader input) {
        this.connection=connection;
        this.output=output;
        this.input=input;
        this.client_name=client_name;
    }


    String Receive() throws IOException {
        return input.readLine();
    }


    void Send(String msg) {
        output.println(msg);
    }


    void CloseStreams() throws IOException {
        output.close();
        input.close();
        connection.close();
        Server.sockets.remove(this);
        System.out.println("Current Client Count- "+ Server.sockets.size());
    }


    public void run() {
        try{
            while(true) {
                String msg=Receive();

                if(msg.endsWith("Client_wants_to_end_the_connection")) {
                    msg=msg.substring(0,msg.indexOf('-'))+" has left the server";
                    for(Client_Handler c:Server.sockets) {
                        if(!c.client_name.equals(client_name)) c.Send(msg);
                    }
                    CloseStreams();
                    break;
                }

                for(Client_Handler c:Server.sockets) {
                    if(!c.client_name.equals(client_name))  c.Send(msg);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}