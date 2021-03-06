package comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/* **************************************************************
 * Prototype for client/server communication.
 * On creation attempts to connect to a client.
 * Takes console input and sends it to the client.
 * Receives and prints input sent from the client to the console.
 */
public class ServerPrototype {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
    private Scanner reader;
	
	public ServerPrototype(){
		try{
			serverSocket = new ServerSocket(1234);
			boolean attempting = true;
			int input;
			while(attempting){
				serverSocket.setSoTimeout(5000);
				try{
					socket = serverSocket.accept();
					System.out.println("server connected successfully");
					attempting = false;
				}
				catch (SocketTimeoutException e){
					System.out.println("Server timed out. Press r to retry, anything else to quit");
					input = System.in.read();
					if(input != 'r'){
						System.out.println("quitting");
						attempting = false;
					}
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void initComms(){
		try{
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
            reader = new Scanner(System.in);
			
			Thread inputThread = new Thread(new Runnable(){
				public void run(){
                    while (!socket.isClosed()){
                        readInput();
                        try{
                            Thread.sleep(50);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
				}
			});
			
			Thread outputThread = new Thread(new Runnable(){
				public void run(){
                    while (!socket.isClosed()){
                        writeOutput();
                        try{
                            Thread.sleep(50);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
				}
			});
			inputThread.start();
			outputThread.start();
			System.out.println("You can now send and receive text from the client. Send q to shut down.");
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void readInput(){
		try {
            String i = in.readUTF();
            if (i.equals("q")){
                shutdown();
            }
            else{
                System.out.println("CLIENT: " + i);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeOutput(){
		try {
            if (reader.hasNext()){
                String next = reader.next();
                out.writeUTF(next);
                out.flush();
                if (next.equals("q")){
                    shutdown();
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void shutdown(){
	    try{
            System.out.println("Shutting Down");
            socket.close();
            serverSocket.close();
	    } catch (IOException e){
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {
		ServerPrototype s = new ServerPrototype();
		s.initComms();
	}
}