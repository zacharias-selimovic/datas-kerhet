import java.net.*;
import java.io.*;
import javax.net.ssl.*;

import java.security.KeyStore;
import java.security.cert.*;
import java.security.spec.ECFieldF2m;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */

public class client {
  public static void main(String[] args) throws Exception {
    String host = null;
    int port = -1;
    for (int i = 0; i < args.length; i++) {
      System.out.println("args[" + i + "] = " + args[i]);
    }
    if (args.length < 2) {
      System.out.println("USAGE: java client host port");
      System.exit(-1);
    }
    try { /* get input parameters */
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (IllegalArgumentException e) {
      System.out.println("USAGE: java client host port");
      System.exit(-1);
    }

    try {
      SSLSocketFactory factory = null;
      try {
        System.out.println("--Hospital login--");
        System.out.println("Enter username: ");
        String name = System.console().readLine();
        System.out.println("Enter password: ");
        char[] password = System.console().readPassword();


        //char[] password = "clientpassword".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        // keystore password (storepass)
        try{
          ks.load(new FileInputStream(name + ".keystore"), password);  
          // truststore password (storepass);
          ts.load(new FileInputStream(name + ".truststore"), password); 
          kmf.init(ks, password); // user password (keypass)
          tmf.init(ts); // keystore can be used as truststore here
          ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e){
          System.out.println("Wrong pass or username");
        }
        factory = ctx.getSocketFactory();
      } catch (Exception e) {
        throw new IOException(e.getMessage());
      }
      SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
      System.out.println("\nsocket before handshake:\n" + socket + "\n");

      /*
       * send http request
       *
       * See SSLSocketClient.java for more information about why
       * there is a forced handshake here when using PrintWriters.
       */

      socket.startHandshake();
      SSLSession session = socket.getSession();
      Certificate[] cert = session.getPeerCertificates();
      String subject = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
      String issuer = ((X509Certificate) cert[0]).getIssuerX500Principal().getName();
      String serialNumber = ((X509Certificate) cert[0]).getSerialNumber().toString();

      System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
      System.out.println("Issuer: "+ issuer);
      System.out.println("Serial number " + serialNumber);
      System.out.println("socket after handshake:\n" + socket + "\n");
      System.out.println("secure connection established\n\n");
      //System.out.println("Creating records from terminal not finished. type read to read this doctors records");
      
      BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      //read welcome msg
      System.out.println(in.readLine());

      String msg;
      for (;;) {
        System.out.print(">");
        msg = read.readLine();
        if (msg.equalsIgnoreCase("q")) {
          break;
        }
        System.out.print("sending '" + msg + "' to server...");
        out.println(msg);
        out.flush();
        System.out.println("done");
        System.out.println( in.readLine());
      }
      in.close();
      out.close();
      read.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
