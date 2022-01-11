import java.net.*;
import java.io.*;
import java.util.*;
public class GroupChat
{
	private static final String TERMINATE = "Exit";
	static String nome;
	static volatile boolean finished = false;
	public static void main(String[] args)
	{
		if (args.length != 2)
			System.out.println("Dois argumentos necessários: <ip-multicast> <numero-da-porta>");
		else
		{
			try
			{
				InetAddress group = InetAddress.getByName(args[0]);
				int port = Integer.parseInt(args[1]);
				Scanner sc = new Scanner(System.in);
				System.out.print("Coloque seu nome: ");
				nome = sc.nextLine();
				MulticastSocket socket = new MulticastSocket(port);
			
				// Since we are deploying
				socket.setTimeToLive(0);
				//this on localhost only (For a subnet set it as 1)
				
				socket.joinGroup(group);
				Thread t = new Thread(new
				ReadThread(socket,group,port));
			
				// Spawn a thread for reading messages
				t.start();
				
				// sent to the current group
				System.out.println("Digite a mensagem...\n");
				while(true)
				{
					String mensagem;
					mensagem = sc.nextLine();
					if(mensagem.equalsIgnoreCase(GroupChat.TERMINATE))
					{
						finished = true;
						socket.leaveGroup(group);
						socket.close();
						break;
					}
					mensagem = nome + ": " + mensagem;
					byte[] buffer = mensagem.getBytes();
					DatagramPacket datagram = new
					DatagramPacket(buffer,buffer.length,group,port);
					socket.send(datagram);
				}
			}
			catch(SocketException se)
			{
				System.out.println("Erro ao criar o socket");
				se.printStackTrace();
			}
			catch(IOException ie)
			{
				System.out.println("Erro ao ler/escrever do/para o socket");
				ie.printStackTrace();
			}
		}
	}
}
class ReadThread implements Runnable
{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private static final int MAX_LEN = 1000;
	ReadThread(MulticastSocket socket,InetAddress group,int port)
	{
		this.socket = socket;
		this.group = group;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		while(!GroupChat.finished)
		{
				byte[] buffer = new byte[ReadThread.MAX_LEN];
				DatagramPacket datagram = new
				DatagramPacket(buffer,buffer.length,group,port);
				String message;
			try
			{
				socket.receive(datagram);
				message = new
				String(buffer,0,datagram.getLength(),"UTF-8");
				if(!message.startsWith(GroupChat.nome))
					System.out.println(message);
			}
			catch(IOException e)
			{
				System.out.println("Socket fechado!");
			}
		}
	}
}
