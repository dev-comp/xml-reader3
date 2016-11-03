package com.bftcom;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class App {
  public static void main(String[] args) {

    String searchAttrValue = null;
    String fileToProcess = null;

    if (args.length == 1) {
      searchAttrValue = args[0];
    } else if (args.length == 2) {
      searchAttrValue = args[1];
      fileToProcess = args[0];
    } else {
      System.out.println("Аргументы программы: наименование колонки или наименование файла и колонки");
      System.exit(0);
    }
    calculateSumsFromXmlDumpFiles(searchAttrValue, fileToProcess);
  }

  private static void calculateSumsFromXmlDumpFiles(String searchAttrValue, String fileToProcess) {
    File executionDirOrJar = null;
    try {
      executionDirOrJar = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//      System.out.println("Curdir: " + file1);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }


    XmlDataDumpFileVisitor fileVisitor = new XmlDataDumpFileVisitor(fileToProcess, executionDirOrJar);
    try {

      List<Path> list = fileVisitor.collectFiles();

      if (list.size() == 0) {
        list = fileVisitor.getList();
      }
      BigDecimal sum = new BigDecimal(0);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      System.out.println("Идет подсчет суммы ...");
      for (Path path : list) {
        try {

          CustomSaxHandler handler = new CustomSaxHandler(searchAttrValue);
          saxParser.parse(path.toFile(), handler);
          BigDecimal result = handler.getResult();
          sum = sum.add(result);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      System.out.println(sum);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }
  }
}
