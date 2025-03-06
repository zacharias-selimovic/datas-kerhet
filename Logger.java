import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private File logFile;

    public Logger(){
        this.logFile = new File("log.txt");
    }

    public void writeLog(String message){
        LocalDateTime logTime = LocalDateTime.now();
        DateTimeFormatter logFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
            writer.append(logTime.format(logFormatter) + "  ::  " + message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
