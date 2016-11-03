package com.bftcom;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomSaxHandler extends DefaultHandler {
  private boolean isFieldFound = false;
  private boolean isColNameFound = false;
  private int currentColIndex = 0;
  private int colIndex = -1;//-1 means no information yet available about the column index position in xml file
  private int currentFieldIndex = 0;
  private String colToFind;

  public CustomSaxHandler(String colToFind) {
    this.colToFind = colToFind;
  }

  public BigDecimal getResult() {
    return result;
  }

  private BigDecimal result = new BigDecimal(0.);
  private List<BigDecimal> resultList = new ArrayList<>();

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    //    System.out.println("Start Element :" + qName);
    if (qName.equalsIgnoreCase("row")) {
      currentFieldIndex = 0;//reset the count for each new row
    }

    if (qName.equalsIgnoreCase("field")) {
      currentFieldIndex++;
      if (currentFieldIndex == colIndex) {
        isFieldFound = true;
      }
    } else if (qName.equalsIgnoreCase("column_name")) {
      currentColIndex++;
      isColNameFound = true;//now we have the index, let's flag this event. consumer is the characters method
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {

    //    System.out.println("End Element :" + qName);

  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (isFieldFound) {
      try {
        BigDecimal value = new BigDecimal(new String(ch, start, length));
        result = result.add(value);
//        System.out.println(result.toPlainString());
        resultList.add(value);
      } catch (NumberFormatException e) {
        //do nothing. wrong format. probably empty
      }
      isFieldFound = false;
    }

    if (isColNameFound) {
      String colName = new String(ch, start, length);
      if (colToFind.equals(colName)) {
        colIndex = currentColIndex;//this is the index we need to use for further parsing
      }
      isColNameFound = false;//the event has been consumed
    }
  }

  public List<BigDecimal> getResultList() {
    return resultList;
  }
}
