import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    private Socket s;
    private File file;

    public Client(Socket s) {
        this.s = s;
    }

    public Client(Socket s, File file) {
        this.s = s;
        this.file = file;
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {

        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = null;

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }
    private void upload(File file) {

        try {
            int length;
            byte[] buffer = new byte[4096];
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.write("UPLOAD "+file.getName());
            pw.println();
            pw.flush();

            while ((length = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, length);
               // System.out.println(buffer);
            }
            dos.flush();
            dos.close();
            fis.close();
            pw.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage() {

        try {

            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.write("NOTFOUND");
            pw.println();
            pw.flush();
            pw.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        //while (true)
            Scanner scanner = new Scanner(System.in);

            while (true)
            {


                //System.out.println("Enter file name: ");
                String fileName = scanner.nextLine();
                File file = new File(fileName);
                Socket s = new Socket("localhost",6789);
                Client client = new Client(s, file);

                Thread thread = new Thread(client);
                thread.start();
               // System.out.println("Connection established!");
            }

    }
    @Override
    public void run() {

        //System.out.println("Sending file to Server from client "+Thread.currentThread().getId());

        if(file.exists())
        {
            upload(file);
            System.out.println("File Uploaded!");
        }

        else
        {
            sendErrorMessage();
            System.out.println("File not found!");
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
