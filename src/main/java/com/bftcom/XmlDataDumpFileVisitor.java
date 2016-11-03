package com.bftcom;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.*;

public class XmlDataDumpFileVisitor extends SimpleFileVisitor<Path> {
  private final String fileToProcess;
  private final File executionDirOrJar;
  private List<Path> list = new ArrayList<>();

  public XmlDataDumpFileVisitor (String fileToProcess, File executionDirOrJar) {
    this.fileToProcess = fileToProcess;
    this.executionDirOrJar = executionDirOrJar;
  }

  public List<Path> getList() {
    return list;
  }

  public List<Path> collectFiles() throws IOException {
    list = new ArrayList<>();
    if (fileToProcess == null) {
      if (executionDirOrJar != null) {
        if (executionDirOrJar.getAbsolutePath().endsWith(".jar")) {
          Files.walkFileTree(Paths.get(executionDirOrJar.getParentFile().getAbsolutePath()), this);
        } else {
          Files.walkFileTree(Paths.get(executionDirOrJar.getAbsolutePath()), this);
        }
      }
    } else {
      if (executionDirOrJar != null) {
        list.add(Paths.get(executionDirOrJar.getParentFile().getAbsolutePath() + "/" + fileToProcess));
      }
    }
    return list;
  }


  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
    if (attr.isRegularFile() && file.getFileName().toString().endsWith("xml")) {
      list.add(file);//collect file
//      System.out.format("Regular file: %s ", file);
//      System.out.println();
    }
//    System.out.println("(" + attr.size() + "bytes)");
    return CONTINUE;
  }

  // Print each directory visited.
  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
//    System.out.format("Directory: %s%n", dir);
    return CONTINUE;
  }

  // If there is some error accessing
  // the file, let the user know.
  // If you don't override this method
  // and an error occurs, an IOException 
  // is thrown.
  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) {
    System.err.println(exc);
    return CONTINUE;
  }
}
