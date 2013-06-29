package nl.vu.cs.cn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;
import nl.vu.cs.nc.test.ClientThread;
import nl.vu.cs.nc.test.ServerThread;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MessagingActivity extends Activity {
	private TextView messageHistory1;
	private EditText myMessage1;
	private Button btn_send1;
	private StringBuilder received1;
	private String sent1;
	private String name;
	
	private TextView messageHistory2;
	private EditText myMessage2;
	private Button btn_send2;
	private Handler mHandler;
	private String sent2;
	private StringBuilder received2;
	
	Socket serverSocket;
	Socket clientSocket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);
		
		// server
		btn_send1 = (Button)findViewById(R.id.btn_send1);
		myMessage1 = (EditText)findViewById(R.id.txt_message1);
		messageHistory1 = (TextView)findViewById(R.id.textView_messages1);
		// client
		btn_send2 = (Button)findViewById(R.id.btn_send2);
		myMessage2 = (EditText)findViewById(R.id.txt_message2);
		messageHistory2 = (TextView)findViewById(R.id.textView_messages2);
		
		btn_send2.setOnClickListener(new OnClickListener() {
	         public void onClick(View v) {
	        	 sent2 = myMessage2.getText().toString()+'\0';
	        	 messageHistory2.append("\nME: "+sent2);
	        	 new Thread(new Runnable() {
	        		    public void run() {
	        		    	clientSocket.write(sent2.getBytes(), 0, sent2.length());
	        		    }
	        	  }).start();
	         }
		});
		
		btn_send1.setOnClickListener(new OnClickListener() {
	         public void onClick(View v) {
	        	 sent1 = myMessage1.getText().toString()+'\0';
	        	 messageHistory1.append("\nME: "+sent1);
	        	 new Thread(new Runnable() {
	        		    public void run() {
	        		    	serverSocket.write(sent1.getBytes(), 0, sent1.length());
	        		    }
	        	  }).start();
	         }
		});
		
	}
	
	@Override 
	protected void onStart() {
		super.onStart();
		// Handler gets created on the UI-thread
	    mHandler = new Handler();
	    
		// get params from previous intent
		name = getIntent().getStringExtra("name");
		
		// create server
		Toast.makeText(this, "Listening for connections ...", Toast.LENGTH_LONG).show();
		//ServerThread server = new ServerThread(1, 80, messageHistory1);
		//server.run();
		new Thread(new Runnable() {
		    public void run() {
		    	try {
					// create a new communication endpoint
					TCP tcpServer = new TCP(1);
					
					// bind (attach a local address to the socket)
					serverSocket = tcpServer.socket(80);
			    } catch (IOException e) {
					e.printStackTrace();
				} 
					
				//messageHistory1.append("\nListening for connections...");
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				mHandler.post(new Runnable() {
    	            @Override
    	            public void run() {
    	                // This gets executed on the UI thread so it can safely modify Views
    	            	Toast.makeText(MessagingActivity.this, "SERVER: client connected", Toast.LENGTH_LONG).show();
    	            }
    	        });
				
				
				
				int bytesRead = 0;
	    		while( bytesRead >= 0) {
	    			byte[] readBuffer = new byte[8152];
	    			received1 = new StringBuilder();
	    			bytesRead = serverSocket.read(readBuffer, 0, 8152);
	    			try {
						received1.append(new String(readBuffer, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	    			if (bytesRead < 1)
	    				continue;
					mHandler.post(new Runnable() {
	    	            @Override
	    	            public void run() {
	    	                // This gets executed on the UI thread so it can safely modify Views
	    	            	messageHistory1.append("\n"+name+": "+received1);
	    	            	//Toast.makeText(MessagingActivity.this, name+": "+received1, Toast.LENGTH_LONG).show();
	    	            }
	    	        });					
				}
				
		    }
		  }).start();
		// create client
		Toast.makeText(this, "Connecting to 192.168.0.1:80 ...", Toast.LENGTH_LONG).show();
		//ClientThread client = new ClientThread("192.168.0.1", 80, messageHistory2);
		//client.run();
		new Thread(new Runnable() {
		    public void run() {
		    	try {
		    		// create a new communication endpoint
		    		TCP tcpClient = new TCP(2);
		    		
		    		clientSocket = tcpClient.socket();
		    	} catch (IOException e) {
	    			e.printStackTrace();
	    		}
		    		
	    		// create server IP address and connect to server
	    		IpAddress serverAddress = IpAddress.getAddress("192.168.0.1");
	    		clientSocket.connect(serverAddress, 80);
	    		
	    		if (clientSocket.connect(serverAddress, 80) == false) {
	    			//messageHistory2.append("\nCould not connect to 192.168.0.1:80");
	    		}
	    		else {
	    			mHandler.post(new Runnable() {
	    	            @Override
	    	            public void run() {
	    	                // This gets executed on the UI thread so it can safely modify Views
	    	            	Toast.makeText(MessagingActivity.this, "CLIENT: Connection established", Toast.LENGTH_LONG).show();
	    	            }
	    	        });
	    			//messageHistory2.append("\nEnetered: "+clientSocket.getTcpControlBlock().getConnectionStateForTesting());
	    		}
	    		
	    		int bytesRead = 0;
	    		while( bytesRead >= 0) {
	    			byte[] readBuffer = new byte[8152];
	    			received2 = new StringBuilder();
	    			bytesRead = clientSocket.read(readBuffer, 0, 8152);
	    			try {
						received2.append(new String(readBuffer, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	    			if (bytesRead < 1)
	    				continue;
					mHandler.post(new Runnable() {
	    	            @Override
	    	            public void run() {
	    	                // This gets executed on the UI thread so it can safely modify Views
	    	            	messageHistory2.append("\nSERVER: "+received2);
	    	            	//Toast.makeText(MessagingActivity.this, name+": "+received2, Toast.LENGTH_LONG).show();
	    	            }
	    	        });					
				}
	    		
	    		//clientSocket.close();
	    		//messageHistory2.append("\nConnection closed");
	    		//Toast.makeText(MessagingActivity.this, "Connection closed", Toast.LENGTH_LONG).show();
	    		
		    		
		    }
		  }).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messaging, menu);
		return true;
	}

}
