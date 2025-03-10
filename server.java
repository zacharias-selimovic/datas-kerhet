import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//My comment: Max
// morgan
public class server implements Runnable {
  private ServerSocket serverSocket = null;
  private static int numConnectedClients = 0;
  private List<Record> records;
  private Logger logger;
  
  public server(ServerSocket ss) throws IOException {
    serverSocket = ss;
    newListener();
    this.records = new ArrayList<>();
    this.logger = new Logger();
    records.add(new Record("doctor1", "nurse1", "patient1")); // try to add a record to "database"
    records.add(new Record("doctor2", "nurse1", "patient2")); // try to add a record to "database"
    records.add(new Record("doctor2", "nurse1", "patient1")); // try to add a record to "database"
  }

  private String extractField(String dn, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "=([^,]+)");
        Matcher matcher = pattern.matcher(dn);
        return matcher.find() ? matcher.group(1) : null;
    }
  
  private List<Record> subjectRecords(String CN, String OU){
    if(OU.equals("Authority")){
      return this.records;
    } else {
      ArrayList<Record> partialList = new ArrayList<>();
      switch(OU){
        case "Doctor" : 
          for (Record r : this.records){
            if(r.doctorCN.equals(CN)){
              partialList.add(r);
            }
          }
        case "Nurse" :
          for (Record r : this.records){
            if(r.nurseCN.equals(CN)){
              partialList.add(r);
            }
          }
        case "Patient" : 
          for (Record r : this.records){
            if(r.patientCN.equals(CN)){
              partialList.add(r);
            }
        }
        default :
          return partialList;
      }
    }

  }

  public void run() {
    try {
      SSLSocket socket=(SSLSocket)serverSocket.accept();
      newListener();
      SSLSession session = socket.getSession();
      Certificate[] cert = session.getPeerCertificates();
      String subject = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
      String issuer = ((X509Certificate) cert[0]).getIssuerX500Principal().getName();
      String serialNumber = ((X509Certificate) cert[0]).getSerialNumber().toString();
      numConnectedClients++;
      logger.writeLog(extractField(subject, "CN") + " Logged in");
      System.out.println("client connected");
      System.out.println("Issuer: " + issuer);
      System.out.println("Serial number " + serialNumber);
      System.out.println("client name (cert subject DN field): " + subject);
      System.out.println(numConnectedClients + " concurrent connection(s)\n");
      //System.out.println("Will try to read the records in database that client can access!");

      //System.out.println("Record content: " + records.get(0).read(extractField(subject, "OU"), extractField(subject, "CN")));
      //IT WORKS
      
      PrintWriter out = null;
      BufferedReader in = null;
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String clientMsg = null;
      List<Record> clientRecords = subjectRecords(extractField(subject, "CN"), extractField(subject, "OU"));
      welcomeMsg(out, extractField(subject, "CN"), extractField(subject, "OU"));
      //Client MSG LOOP
      while ((clientMsg = in.readLine()) != null) {
        String toSend = "";
        String[] commandParts = clientMsg.split(" ", 2);
        String command = commandParts[0].toLowerCase();
    
        switch (command) {
            case "read" :
                if (clientMsg.matches("read (\\d+)")) {
                    int recordNumber = Integer.parseInt(clientMsg.replaceAll("read (\\d+)", "$1"));
                    if (recordNumber > 0 && recordNumber <= clientRecords.size()) {
                        String content = clientRecords.get(recordNumber - 1).read(
                            extractField(subject, "OU"),
                            extractField(subject, "CN")
                        );
                        System.out.println("Record content: " + content);
                        toSend = content;
                        logger.writeLog(extractField(subject, "CN") + " read " + clientRecords.get(recordNumber - 1).patientCN + "'s record");
                    } else {
                        toSend = "Invalid record number!";
                    }
                }
                break;

            case "edit" :
                if (clientMsg.matches("edit (\\d+) (.+)")) {
                    Pattern pattern = Pattern.compile("edit (\\d+) (.+)");
                    Matcher matcher = pattern.matcher(clientMsg);
                    if (matcher.matches()) {
                        int recordNumber = Integer.parseInt(matcher.group(1));
                        String editMsg = matcher.group(2);
                        if (recordNumber > 0 && recordNumber <= clientRecords.size()) {
                            String content = clientRecords.get(recordNumber - 1).write(
                                extractField(subject, "OU"),
                                extractField(subject, "CN"),
                                editMsg
                            );
                            System.out.println("Record content: " + content);
                            logger.writeLog(extractField(subject, "CN") + " edited " + clientRecords.get(recordNumber - 1).patientCN + "'s record");
                            toSend = content;
                        } else {
                            toSend = "Invalid record number!";
                        }
                    }
                }
                break;

            case "add" :
                if (clientMsg.matches("add \"([^\"]+)\" \"([^\"]+)\"")) {
                    Pattern pattern = Pattern.compile("add \"([^\"]+)\" \"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(clientMsg);
                    if (matcher.matches()) {
                        String nurseCN = matcher.group(1);
                        String patientCN = matcher.group(2);
                        clientRecords.add(new Record(extractField(subject, "CN"),nurseCN, patientCN));
                        logger.writeLog(extractField(subject, "CN") + " Added a new record to " + patientCN + "with " + nurseCN);
                        toSend = "Record added successfully. You know have " + (records.size()) + " records to operate on";
                    }
                } else {
                    toSend = "Invalid add command! Use: add \"nurseCN\" \"patientCN\"";
                }
                break;

            case "remove" :
                if (clientMsg.matches("remove (\\d+)")) {
                    int recordNumber = Integer.parseInt(clientMsg.replaceAll("remove (\\d+)", "$1"));
                    if(!extractField(subject, "OU").equals("Authority")){
                      toSend = "You cant do that";
                      break;
                    }
                    if (recordNumber > 0 && recordNumber <= clientRecords.size()) {
                        String removedRecordPatientCN = clientRecords.get(recordNumber - 1).patientCN;
                        clientRecords.remove(recordNumber - 1);
                        toSend = "Record removed successfully.";
                        logger.writeLog(extractField(subject, "CN") + "Removed " + removedRecordPatientCN + "'s record");

                    } else {
                        toSend = "Invalid record number!";
                    }
                } else {
                    toSend = "Invalid remove command! Use: remove (number)";
                }
                break;
    
            default : if(toSend.equals("")){
              toSend = "Invalid command!";
            }
            break;
        }
    
        System.out.println("Received '" + clientMsg + "' from client");
        System.out.print("Sending '" + toSend + "' to client...");
        out.println(toSend);
        out.flush();
        System.out.println("done\n");
    }
      in.close();
      out.close();
      socket.close();
      numConnectedClients--;
      System.out.println("client disconnected");
      System.out.println(numConnectedClients + " concurrent connection(s)\n");
    } catch (IOException e) {
      System.out.println("Client died: " + e.getMessage());
      e.printStackTrace();
      return;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void welcomeMsg(PrintWriter out, String CN, String OU){
    System.out.println("Sending welcome message to client");
    List<Record> records = subjectRecords(CN, OU);
    if(OU.equals("Doctor")){
      out.println("Welcome " + CN + " type 'read 1-"+ records.size() + "' to read records, edit 1-" + records.size() +  " 'edit message' to edit records, type 'add 'nurseCN' 'patientCN' to add new record, q to quit" + " You have " + records.size() +  " Records to operate on");
    } else if(OU.equals("Nurse")){
      out.println("Welcome " + CN + " type 'read 1-"+ records.size() + "' to read records, edit 1-" + records.size() + " 'edit message' to edit records, q to quit" + " You have " + records.size() +  " Records to operate on");
    } else if(OU.equals("Authority")){
      out.println("Welcome " + CN + " type 'read 1-"+ records.size() + "' to read records, remove 1-" + records.size() + " to remove records, q to quit" + " You have " + records.size() +  " Records to operate on");
    } else {
      out.println("Welcome " + CN + " type 'read 1-" + records.size() +  "' to read records, q to quit :: " + " You have " + records.size() + " Records to read");
    }
    out.flush();
  }
  
  private void newListener() { (new Thread(this)).start(); } // calls run()
  public static void main(String args[]) {
    System.out.println("\nServer Started\n");
    int port = -1;
    if (args.length >= 1) {
      port = Integer.parseInt(args[0]);
    }
    String type = "TLSv1.2";
    try {
      ServerSocketFactory ssf = getServerSocketFactory(type);
      ServerSocket ss = ssf.createServerSocket(port);
      ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
      new server(ss);
    } catch (IOException e) {
      System.out.println("Unable to start Server: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static ServerSocketFactory getServerSocketFactory(String type) {
    if (type.equals("TLSv1.2")) {
      SSLServerSocketFactory ssf = null;
      try { // set up key manager to perform server authentication
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] password = "serverpassword".toCharArray();
        // keystore password (storepass)
        ks.load(new FileInputStream("server.keystore"), password);  
        // truststore password (storepass)
        ts.load(new FileInputStream("server-truststore"), password); 
        kmf.init(ks, password); // certificate password (keypass)
        tmf.init(ts);  // possible to use keystore as truststore here
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        ssf = ctx.getServerSocketFactory();
        return ssf;
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      return ServerSocketFactory.getDefault();
    }
    return null;
  }
}
//Aya