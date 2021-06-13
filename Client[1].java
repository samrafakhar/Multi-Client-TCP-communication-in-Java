import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    public static void write(String data, InetAddress ip) throws IOException {
        InetAddress sip = InetAddress.getLocalHost();
        String server = sip.toString().replace(sip.getHostName(),"");
        String filename = ip.toString().substring(10)+"_"+server.substring(1)+".txt";
        File file = new File(filename);
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(data);
        fileWriter.close();
        System.out.println("Information retrieved from server has been written to "+filename);
    }
    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner scn = new Scanner(System.in);
            InetAddress ip = InetAddress.getByName("localhost");
            Socket s = new Socket(ip, Server.serverPort);
            Server.active_count++;
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            System.out.println("Client "+ip+" is active.");
            while (true)
            {
                System.out.println(in.readUTF());
                String tosend = scn.nextLine();
                out.writeUTF(tosend);
                if(tosend.equals("save"))
                {
                    System.out.println("Enter data that you want to save to server. Add 'eof' to indicate the end of input");
                    String line = "\0";
                    while (!line.equals("eof"))
                    {
                        try
                        {
                            line = scn.nextLine();
                            out.writeUTF(line);
                        }
                        catch(IOException i)
                        {
                            System.out.println(i);
                        }
                    }
                    System.out.println("Data saved to server successfully");
                }
                if(tosend.equals("read"))
                {
                    String data = in.readUTF();
                    if(data.equals(""))
                        System.out.println("No information found for client "+ip);
                    else {
                        System.out.println("Information for client "+ip);
                        System.out.println(data);
                        write(data, ip);
                    }
                }
                if(tosend.equals("exit"))
                {
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }
            }
            scn.close();
            in.close();
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}