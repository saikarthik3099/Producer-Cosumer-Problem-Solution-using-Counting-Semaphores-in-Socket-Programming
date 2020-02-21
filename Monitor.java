// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
import java.util.concurrent.Semaphore;

class Q {
    static final int BUFFERSIZE = 3;
    int[] buffer = new int[BUFFERSIZE];
    int in = 0;
    int out = 0;
  
    Semaphore full = new Semaphore(0); 
  
    Semaphore empty = new Semaphore(BUFFERSIZE);
    
    Semaphore mutex = new Semaphore(1);
  
    // to get an item from buffer 
    int get() 
    { 
        int item;      
        item = buffer[out];
        out = (out + 1)% BUFFERSIZE;
        return item;
       
    } 
  
    // to put an item in buffer 
    void put(int item) 
    { 
             
        buffer[in] = item;
        in = (in + 1)% BUFFERSIZE;
     
    } 
}
// Server class 
public class Monitor
{ 
	public static void main(String[] args) throws IOException 
	{ 
                Q q = new Q();
		// server is listening on port 5056 
		ServerSocket ss = new ServerSocket(5056); 
		
		// running infinite loop for getting 
		// client request 
		while (true) 
		{ 
			Socket s = null; 
			
			try
			{ 
				// socket object to receive incoming client requests 
				s = ss.accept(); 
				
				System.out.println("A new client is connected : " + s); 
				
				// obtaining input and out streams 
				DataInputStream dis = new DataInputStream(s.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
				
				System.out.println("Assigning new thread for this client"); 

				// create a new thread object 
				
                                if(dis.readUTF().equals("consumer"))
                                {
                                    Thread t = new ConsumerHandler(q,s,dis,dos);
                                    t.start();
                                }
                                else
                                {
                                    Thread t = new ProducerHandler(q,s,dis,dos);
                                    t.start();
                                }    
                                 
				
			} 
			catch (Exception e){ 
				
			} 
		} 
	} 
} 

class ProducerHandler extends Thread 
{
	final DataInputStream dis; 
	final DataOutputStream dos; 
	final Socket s;
        Q q;
	

	// Constructor 
	public ProducerHandler(Q q,Socket s, DataInputStream dis, DataOutputStream dos) 
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;
                this.q = q;
	} 
   
	@Override
	public void run() 
	{ 
		String received; 
		String toreturn; 
		while (true) 
		{
			try { 
                                
				// Ask user what he wants 
				dos.writeUTF("Enter the item to Produce..\n"+ 
							"Type Exit to terminate connection."); 
				
				// receive the answer from client 
				received = dis.readUTF(); 
				
				if(received.equals("Exit")) 
				{ 
					System.out.println("Client " + this.s + " sends exit..."); 
					System.out.println("Closing this connection."); 
					this.s.close(); 
					System.out.println("Connection closed"); 
					break; 
				} 
				
                                try
                                {
                                    q.empty.acquire();
                                    q.mutex.acquire();
                                }
                                catch(Exception e)
                                {

                                }
				
                                q.put(Integer.parseInt(received));
                                System.out.println("Producer " + s + " produced " + Integer.parseInt(received));
                                
                                q.mutex.release();
                                q.full.release();
                                
                                dos.writeUTF("Item entered Successfully");
				
				 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
		} 
		
		try
		{ 
			// closing resources 
			this.dis.close(); 
			this.dos.close(); 
			
		}catch(IOException e){ 
			e.printStackTrace(); 
		} 
	} 
}
// ClientHandler class 
class ConsumerHandler extends Thread 
{
	final DataInputStream dis; 
	final DataOutputStream dos; 
	final Socket s;
        Q q;
	

	// Constructor 
	public ConsumerHandler(Q q,Socket s, DataInputStream dis, DataOutputStream dos) 
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;
                this.q = q;
	} 

	@Override
	public void run() 
	{ 
		String received; 
		String toreturn; 
		while (true) 
		{ 
			try { 
				// Ask user what he wants 
				dos.writeUTF("Do you want to consume an item..? Type Yes\n"+ 
							"Type Exit to terminate connection."); 
				
				// receive the answer from client 
				received = dis.readUTF(); 
				
				if(received.equals("Exit")) 
				{ 
					System.out.println("Client " + this.s + " sends exit..."); 
					System.out.println("Closing this connection."); 
					this.s.close(); 
					System.out.println("Connection closed"); 
					break; 
				}
                                
                                try
                                {
                                    q.full.acquire();
                                    q.mutex.acquire();
                                }
                                catch(Exception e)
                                {

                                }
                                
                                int item = q.get();
                                System.out.println("Consumer " + s + " consumed " + item);
                                
                                q.mutex.release();
                                q.empty.release();
                                dos.writeUTF("Item consumed Successfully !!");
				
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
		} 
		
		try
		{ 
			// closing resources 
			this.dis.close(); 
			this.dos.close(); 
			
		}catch(IOException e){ 
			e.printStackTrace(); 
		} 
	} 
} 
