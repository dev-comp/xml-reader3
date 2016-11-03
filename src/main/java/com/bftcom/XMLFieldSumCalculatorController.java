package com.bftcom;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.Preferences;

public class XMLFieldSumCalculatorController {
  private static final Logger log = LoggerFactory.getLogger(XMLFieldSumCalculatorController.class);
  public static final String LAST_INPUT_DIR = "LAST_INPUT_DIR";
  public static final String LAST_COL_NAME = "LAST_COL_DIR";

  @FXML
  private TextField directoryField;
  @FXML
  private TextField resultField;

  @FXML
  private TextField colNameField;

  private DirectoryChooser dirFileChooser = new DirectoryChooser();

  private Preferences prefs;

  @FXML
  void initialize() {
    System.out.println("");
    prefs = Preferences.userNodeForPackage(this.getClass());
    File file = new File(prefs.get(LAST_INPUT_DIR, ""));
    colNameField.setText(prefs.get(LAST_COL_NAME, ""));
    directoryField.setText(prefs.get(LAST_INPUT_DIR, ""));

    if (file.exists()) {
      dirFileChooser.setInitialDirectory(file);
    }
  }

  public void selectDir() {
    dirFileChooser.setTitle("Open Resource File");
    Stage mainStage = (Stage) directoryField.getScene().getWindow();

    File selectedFile = dirFileChooser.showDialog(mainStage);

    StringBuilder sb = new StringBuilder();
    if (selectedFile != null) {
      prefs.put(LAST_INPUT_DIR, selectedFile.getAbsolutePath());
      dirFileChooser.setInitialDirectory(selectedFile);//take the first file
      sb.append(selectedFile.getAbsolutePath());
    }
    directoryField.setText(sb.toString());
  }

  public void calculateSum() {
    String colName = colNameField.getText().trim();
    prefs.put(LAST_COL_NAME, colName);
    calculateSumsFromXmlDumpFiles(colName, directoryField.getText());


  }

  private BigDecimal calculateSumsFromXmlDumpFiles(String searchAttrValue, String dirToProcess) {
    resultField.setText("Идет подсчет суммы ...");

    Runnable runnable = () -> {
      BigDecimal sum = new BigDecimal(0);
      File _dirToProcess = new File(dirToProcess);
      StringBuilder sb = new StringBuilder();
      XmlDataDumpFileVisitor fileVisitor = new XmlDataDumpFileVisitor(null, _dirToProcess);
      try {

        List<Path> list = fileVisitor.collectFiles();

        if (list.size() == 0) {
          list = fileVisitor.getList();
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        for (Path path : list) {
          try {

            CustomSaxHandler handler = new CustomSaxHandler(searchAttrValue);
            saxParser.parse(path.toFile(), handler);
            BigDecimal result = handler.getResult();
            sb.append(path.getFileName()).append(":").append(result.toPlainString()).append("\n");
            sum = sum.add(result);
            //              resultField.setText(sum.toPlainString());

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        resultField.setText(sum.toPlainString());
        System.out.println(sb.toString() + "Total sum: " + sum.toPlainString());
        sb.append("Total sum: " + sum.toPlainString());

        FileOutputStream fop = null;
        File file = new File("sum.csv");
        fop = new FileOutputStream(file);

        // if file doesnt exists, then create it
        if (!file.exists()) {
          file.createNewFile();
        }

        // get the content in bytes
        byte[] contentInBytes = sb.toString().getBytes();

        fop.write(contentInBytes);
        fop.flush();
        fop.close();

        System.out.println("Done");

      } catch (IOException | SAXException | ParserConfigurationException e) {
        e.printStackTrace();
      }

    };
    new Thread(runnable).start();
    return null;
  }

}
