package com.origin.storage.service;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.origin.storage.exception.MyFileNotFoundException;
import com.origin.storage.model.FileDataAsJson;

import net.sf.json.JSONObject;

public class ExcelToJsonCoverter {

    
    public static FileDataAsJson creteJSONAndTextFileFromExcel(String filePath)
    {
        try{
            FileInputStream fInputStream = new FileInputStream(filePath.trim());
   
         /* Create the workbook object to access excel file. */
            //Workbook excelWookBook = new XSSFWorkbook(fInputStream)
         /* Because this example use .xls excel file format, so it should use HSSFWorkbook class. For .xlsx format excel file use XSSFWorkbook class.*/;
            Workbook excelWorkBook = new HSSFWorkbook(fInputStream);

            // Get all excel sheet count.
            int totalSheetNumber = excelWorkBook.getNumberOfSheets();

            // Loop in all excel sheet.
            for(int i=0;i<totalSheetNumber;i++)
            {
                // Get current sheet.
                Sheet sheet = excelWorkBook.getSheetAt(i);

                // Get sheet name.
                String sheetName = sheet.getSheetName();

                if(sheetName != null && sheetName.length() > 0)
                {
                    // Get current sheet data in a list table.
                    List<List<String>> sheetDataTable = getSheetDataList(sheet);
                    

                    // Generate JSON format of above sheet data and write to a JSON file.
                    return getJSONStringFromList(sheetDataTable);
                    
                }
            }
            // Close excel work book object. 
            excelWorkBook.close();
            throw new MyFileNotFoundException("There was a problem in looking for file");
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
		return null;
    }


    private static List<List<String>> getSheetDataList(Sheet sheet)
    {
        List<List<String>> ret = new ArrayList<List<String>>();

        // Get the first and last sheet row number.
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        if(lastRowNum > 0)
        {
            // Loop in sheet rows.
            for(int i=firstRowNum; i<lastRowNum + 1; i++)
            {
                // Get current row object.
                Row row = sheet.getRow(i);

                // Get first and last cell number.
                int firstCellNum = row.getFirstCellNum();
                int lastCellNum = row.getLastCellNum();

                // Create a String list to save column data in a row.
                List<String> rowDataList = new ArrayList<String>();

                // Loop in the row cells.
                for(int j = firstCellNum; j < lastCellNum; j++)
                {
                    // Get current cell.
                    Cell cell = row.getCell(j);

                    // Get cell type.
                    int cellType = cell.getCellType();

                    if(cellType == CellType.NUMERIC.getCode())
                    {
                        long numberValue = (long) cell.getNumericCellValue();

                        String stringCellValue = BigInteger.valueOf(numberValue).toString();

                        rowDataList.add(stringCellValue);

                    }else if(cellType == cell.getCellTypeEnum().STRING.getCode())
                    {
                        String cellValue = cell.getStringCellValue();
                        rowDataList.add(cellValue);
                        
                    }else if(cellType == CellType.BOOLEAN.getCode())
                    {
                        boolean numberValue = cell.getBooleanCellValue();

                        String stringCellValue = String.valueOf(numberValue);

                        rowDataList.add(stringCellValue);

                    }else if(cellType == CellType.BLANK.getCode())
                    {
                        rowDataList.add("");
                    }
                }

                // Add current row data list in the return list.
                ret.add(rowDataList);
            }
        }
        return ret;
    }

    /* Return a JSON string from the string list. */
    private static FileDataAsJson getJSONStringFromList(List<List<String>> dataTable)
    {
    	FileDataAsJson returnObj = new FileDataAsJson();//
        String ret = "";

        if(dataTable != null)
        {
            int rowCount = dataTable.size();

            if(rowCount > 1)
            	if(rowCount == 1){
            		
            	}
            {
                // Create a JSONObject to store table data.
                JSONObject tableJsonObject = new JSONObject();

                // The first row is the header row, store each column name.
                List<String> headerRow = dataTable.get(0);

                int columnCount = headerRow.size();
                returnObj.setHeaders(headerRow);
                
                // Loop in the row data list.
                for(int i=1; i<rowCount; i++)
                {
                    // Get current row data.
                    List<String> dataRow = dataTable.get(i);

                    // Create a JSONObject object to store row data.
                    JSONObject rowJsonObject = new JSONObject();

                    for(int j=0;j<columnCount;j++)
                    {
                        String columnName = headerRow.get(j);
                        String columnValue = dataRow.get(j);

                        rowJsonObject.put(columnName, columnValue);
                    }

                    tableJsonObject.put(i, rowJsonObject);
                }

                returnObj.setData(tableJsonObject.toString());

            }
        }
        return returnObj;
    }

}