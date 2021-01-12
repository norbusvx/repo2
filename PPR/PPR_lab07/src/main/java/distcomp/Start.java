/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distcomp;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Start {

public static void deleteFileOrFolder(final Path path) throws IOException {
  Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
    @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
      throws IOException {
      Files.delete(file);
      return CONTINUE;
    }

    @Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
      return handleException(e);
    }

    private FileVisitResult handleException(final IOException e) {
      e.printStackTrace(); // replace with more robust error handling
      return TERMINATE;
    }

    @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
      throws IOException {
      if(e!=null)return handleException(e);
      Files.delete(dir);
      return CONTINUE;
    }
  });
};

    public static void main (String[] args) throws Exception {
        String procesToStart = "0";
        File activemqData = new File("activemq-data");
        if(activemqData.exists())
        {
            System.out.println("Deleting ActiveMQ database...");
            deleteFileOrFolder(activemqData.toPath());
        }
        
        final ProcessE processE = new ProcessE();
        processE.start();
        
        final Process0 process0 = new Process0(false ,2);
        final Process1 process1 = new Process1(true ,2);
        final Process2 process2 = new Process2(false ,3);
        final Process3 process3 = new Process3(false ,3);
        final Process4 process4 = new Process4(false ,3);
        final Process5 process5 = new Process5(false ,1);
        process0.start();
        process1.start();
        process2.start();
        process3.start();
        process4.start();
        process5.start();


        System.out.println("Press enter to exit.");
        System.in.read();

        process0.destroy();
        process1.destroy();

    }
}
