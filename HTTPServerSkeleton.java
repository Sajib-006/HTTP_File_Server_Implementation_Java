import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;

public class HTTPServerSkeleton implements Runnable{
    static final int PORT = 6789;
    private Socket socket;
    FileWriter fw;

    public HTTPServerSkeleton(Socket socket) throws IOException {
        this.fw =new FileWriter("log.txt",true);
        this.socket = socket;
    }

    public static String readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return String.valueOf(fileData);
    }

    private void download(File file) throws IOException {

        long fileSize = file.length();
        int length;
        byte[] buffer = new byte[4096];
        String mime = Files.probeContentType(file.toPath());
        try {
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            pr.write("HTTP/1.1 200 OK\r\n");
            pr.write("Server: Java HTTP Server: 1.1\r\n");
            pr.write("Date: " + new Date() + "\r\n");
            pr.write("Content-Length: " + fileSize + "\r\n");
            pr.write("Content-Type: " + mime + "\r\n");
            pr.write("Content-Transfer-Encoding: binary\r\n");
            pr.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
            pr.write("\r\n");
            pr.flush();



            fw.write("\r\n");
            fw.write(" HTTP/1.1 200 OK\r\n");
            fw.write("Server: Java HTTP Server: 1.1\r\n");
            fw.write("Date: " + new Date() + "\r\n");
            fw.write("Content-Length: " + fileSize + "\r\n");
            fw.write("Content-Type: " + mime + "\r\n");
            fw.write("Content-Transfer-Encoding: binary\r\n");
            fw.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
            fw.write("\r\n");






            while ((length = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, length);
            }
            dos.flush();
            pr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        
        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        serverConnect.accept();
        System.out.println("Accepted");



        while(true)
        {
            Socket s = serverConnect.accept();
            HTTPServerSkeleton server = new HTTPServerSkeleton(s);
            Thread thread = new Thread(server);
            thread.start();

        }
        
    }

    @Override
    public void run() {

        try {
            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataInputStream din=new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            String input = in.readLine();
            System.out.println("Request from client: "+input);



            if(input == null) System.out.println("Input file is null");
            if(input!=null) {
                if (input.length() > 0){
                    if (input.startsWith("GET")) {

                        String responseString = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                                "\t\t<link rel=\"icon\" href=\"data:,\"></head><br>\n";
                        responseString += "<body><ul>\n";


                        String[] str = input.split(" ");
                        //System.out.println("hi " + str[1]);
                        String str3 = str[1].substring(1);
                        //System.out.println("hiii  " + str3);
                        File directory = new File(str3);

                        if (directory.exists()) {

                            if (directory.isDirectory()) {
                                File[] files = directory.listFiles();
                                for (File f : files) {
                                    if (f.exists()) {

                                        if (f.isDirectory())
                                            responseString += "<li><b><a href=\" \\" + f.getPath() + "\">" + f.getName() + "</a></b></li><br>\n";
                                        else
                                            responseString += "<li><a href=\" \\" + f.getPath() + "\">" + f.getName() + "</a></li><br>\n";
                                    }
                                }

                            } else {
                                download(directory);
                            }


                            responseString += "</ul></body></html>";
                            pr.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Length: " + responseString.length() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            pr.write("\r\n");
                            pr.write(responseString);
                            pr.flush();

                            fw.write(input);
                            fw.write("\r\n");
                            fw.write(" HTTP/1.1 200 OK\r\n");
                            fw.write("Server: Java HTTP Server: 1.1\r\n");
                            fw.write("Date: " + new Date() + "\r\n");
                            fw.write("Content-Type: text/html\r\n");
                            fw.write("Content-Transfer-Encoding: binary\r\n");
                            fw.write("\r\n");

                        } else {
                            System.out.println("404: Page not found.");
                            responseString += "</ul><p>404: PAGE NOT FOUND.<br>Request with a valid directory.</p></body></html>";
                            pr.write("HTTP/1.1 404 PAGE NOT FOUND\r\n");
                            pr.write("Server: Java HTTP Server: 1.1\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Length: " + responseString.length() + "\r\n");
                            pr.write("Content-Type: null\r\n");
                            pr.write("\r\n");
                            pr.write(responseString);
                            pr.flush();

                            fw.write(input);
                            fw.write("\r\n");
                            fw.write(" HTTP/1.1 404  PAGE NOT FOUND\r\n");
                            fw.write("Server: Java HTTP Server: 1.1\r\n");
                            fw.write("Date: " + new Date() + "\r\n");
                            fw.write("Content length 0\n");
                            fw.write("Content-Type: null\r\n");
                            fw.write("Content-Transfer-Encoding: binary\r\n");
                            fw.write("\r\n");
                        }
                    } else if (input.startsWith("UPLOAD")) {
                        try {
                            int length;
                            String[] str = input.split(" ");
                            //System.out.println("hi " + str[1]);
                            byte[] buffer = new byte[4096];
                            File file = new File("root/" + str[1]);
                            FileOutputStream fos = new FileOutputStream(file);

                            while ((length = din.read(buffer, 0, 4096)) > 0) {
                                fos.write(buffer, 0, length);

                            }
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (input.startsWith("NOTFOUND")) {
                        System.out.println("File Not Found!");
                    }
                    else {
                        //not our work ,only for GET
                    }
            }
            }

            in.close();
            pr.close();
            fw.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
         }
    }
}
