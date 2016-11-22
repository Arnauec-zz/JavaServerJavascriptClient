/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
/**
 *
 * @author Epicur
 */

public class ChatServer extends WebSocketServer {

        private ConcurrentHashMap<WebSocket, String> connexions = new ConcurrentHashMap<>();
        private Iterator<String> iterator;
	public ChatServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public ChatServer( InetSocketAddress address ) {
		super( address );
	}
        
        public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		iterator = connexions.values().iterator();
                while(iterator.hasNext()){
                    conn.send("/newuser " + iterator.next());
                }
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
                String str = connexions.get(conn);
                connexions.remove(conn);
		this.sendToAll("/deleteuser " + str);
		System.out.println( conn + " ha sortit de la sala!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
                if(message.contains("/newuser ")){
                    String[] str = message.split(" ");
                    connexions.put(conn, str[1]);
                    this.sendToAll(message);
                } else {
                    this.sendToAll( message );                    
                }
		System.out.println( conn + ": " + message );
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		System.out.println( "fragment rebut: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 50000; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ChatServer s = new ChatServer( port );
		s.start();
		System.out.println( "Hem iniciat el servidor en el port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			}
		}
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
}

