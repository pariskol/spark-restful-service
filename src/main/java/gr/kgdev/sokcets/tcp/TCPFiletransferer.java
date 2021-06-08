package gr.kgdev.sokcets.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TCPFiletransferer {

    private static final int BUFFER_SIZE = 4*1024;
    public static final String FILE_TRANSFER = "FILE_TRANSFER";

    public static boolean isFileTransfer(String message) {
    	return message.equals(TCPFiletransferer.FILE_TRANSFER);
    }
    
	public static void sendFile(String path, DataOutputStream out) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        // send file name
        out.writeUTF(file.getName());
        // send file size
        out.writeLong(file.length());  
        // break file into chunks
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytes=fileInputStream.read(buffer))!=-1){
        	out.write(buffer,0,bytes);
        	out.flush();
        }
        fileInputStream.close();
    }
    
    public static void receiveFile(DataInputStream in) throws Exception{
    	
    	// read file name
        String fileName = in.readUTF();

        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        // read file size
        long size = in.readLong();
        
        byte[] buffer = new byte[BUFFER_SIZE];
        while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }
    
//    public void readBytes() throws IOException {
// 	   byte[] buffer = new byte[1024];
// 	    int read;
// 	    while((read = in.read(buffer)) != -1) {
// 	        String output = new String(buffer, 0, read);
// 	        System.out.print(output);
// 	        System.out.flush();
// 	    };
// }
}
