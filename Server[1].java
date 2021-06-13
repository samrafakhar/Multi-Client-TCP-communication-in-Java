import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{
    static int active_count = 0;
    static int serverPort = 1234;
    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(serverPort);
        System.out.println("Waiting for clients on port "+serverPort);
        while (true)
        {
            Socket s = null;
            try
            {
                s = ss.accept();
                InetAddress addr = s.getInetAddress();
                int port = s.getPort();
                active_count++;
                System.out.println("Got connection from "+addr.toString().substring(1)+":"+port);
                System.out.println("Active Connections = "+active_count);
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                Thread t = new ClientHandler(s, in, out);
                t.start();
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}
class ClientHandler extends Thread
{
    final DataInputStream in;
    final DataOutputStream out;
    final Socket s;
    public ClientHandler(Socket s, DataInputStream in, DataOutputStream out)
    {
        this.s = s;
        this.in = in;
        this.out = out;
    }
    public void save(String data) throws IOException {
        InetAddress addr = this.s.getInetAddress();
        String filename = addr+".txt";
        filename = filename.substring(1);
        File file = new File(filename);
        file.createNewFile();
        System.out.println("Information saved for client "+addr.toString().substring(1));
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(data);
        fileWriter.close();
    }
    public String read() throws FileNotFoundException {
        String data = "\0";
        InetAddress addr = this.s.getInetAddress();
        String filename = addr+".txt";
        filename = filename.substring(1);
        String ch;
        File file = new File(filename);
        if(file.exists() ) {
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                data = data + "\n" + reader.nextLine();
            }
            reader.close();
            System.out.println("Information for client "+addr.toString().substring(1));
            System.out.println(data);
        }
        else{
            System.out.println("No Information found for client "+addr.toString().substring(1));
        }
        return data;
    }

    @Override
    public void run()
    {
        String received;
        String toreturn;
        while (true)
        {
            try {
                out.writeUTF("Welcome to the server "+InetAddress.getLocalHost()+":"+Server.serverPort+"\n" +
                        "\ta) Type \"save\" to save the data to the server.\n" +
                        "\tb) Type \"read\" to read the data from the server.\n" +
                        "\tc) Type \"exit\" to terminate connection.\n" +
                        "Enter choice: ");

                received = in.readUTF();
                if(received.equals("exit"))
                {
                    System.out.println("Client " + this.s.getInetAddress().toString().substring(1) + " closed connection");
                    this.s.close();
                    Server.active_count--;
                    System.out.println("Connection closed");
                    System.out.println("Active Connections = "+Server.active_count);
                    break;
                }
                switch (received) {
                    case "save" :
                        String data = "\0";
                        String line = "\0";
                        while (!data.equals("eof"))
                        {
                            try
                            {
                                data = in.readUTF();
                                line = line +"\n"+data;
                            }
                            catch(IOException i)
                            {
                                System.out.println(i);
                            }
                        }
                        line = line.substring(0, line.length() - 3);
                        save(line);
                        break;

                    case "read" :
                        String read_data = "\0";
                        read_data = read();
                        out.writeUTF(read_data);
                        break;

                    default:
                        out.writeUTF("Invalid input");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            this.in.close();
            this.out.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}