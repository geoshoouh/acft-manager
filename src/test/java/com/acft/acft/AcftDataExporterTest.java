package com.acft.acft;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.acft.acft.Entities.Soldier;
import com.acft.acft.Services.AcftDataConversion;
import com.acft.acft.Services.AcftDataExporter;
import com.acft.acft.Services.AcftManagerService;
import org.springframework.util.Assert;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
public class AcftDataExporterTest {

    @Autowired
    AcftManagerService acftManagerService;
    
    @Autowired
    AcftDataExporter acftDataExporter;

    //Using this or file IO utility methods
    @Autowired
    AcftDataConversion acftDataConversion;

    @Test
    void createXlsxWorkbookCreatesWorkbookWithExpectedData() {
        int size = 5;
        Long testGroupId = acftManagerService.populateDatabase(size);
        List<Soldier> soldiers = acftManagerService.getSoldiersByTestGroupId(testGroupId);
        int n = soldiers.size();
        XSSFWorkbook workbook = acftDataExporter.createXlsxWorkbook(soldiers);
        Assert.isTrue(workbook.getNumberOfSheets() == 2, "In createXlsxWorkbookCreatesWorkbookWithExpectedData: workbook had unexpected number of sheets");
        String[] headerNames = {"ID", "Last", "First", "Age", "Gender", "MDL", "SPT", "HRP", "SDC", "PLK", "2MR", "Total"};
        for (int i = 0; i < workbook.getNumberOfSheets(); i++){
            XSSFSheet sheet = workbook.getSheetAt(i);
            int rowEnd = (i == 0) ? headerNames.length : headerNames.length - 1;
            for (int j = 0; j < rowEnd; j++){
                String cellValue = sheet.getRow(0).getCell(j).getStringCellValue();
                Assert.isTrue(cellValue == headerNames[j], "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected header value");
            }
            for (int j = 0; j < n; j++){
                //offset due to header row having index 0
                Row row = sheet.getRow(j + 1);
                //when running all tests at once; there's some leftover persistence from some tests
                //So, must use id's of soldiers retrieved by the query in the top portion of this function
                Soldier soldier = soldiers.get(j);
                Assert.isTrue(Long.valueOf((long)row.getCell(0).getNumericCellValue()) == soldier.getPseudoId(), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: expected cell value " + soldier.getPseudoId() + ", was  " + Long.valueOf((long)row.getCell(0).getNumericCellValue()));
                Assert.isTrue(row.getCell(1).getStringCellValue().equals(soldier.getLastName()), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected cell value");
                Assert.isTrue(row.getCell(2).getStringCellValue().equals(soldier.getFirstName()), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected cell value");
                Assert.isTrue(Integer.valueOf((int)row.getCell(3).getNumericCellValue()) == soldier.getAge(), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected cell value");
                Assert.isTrue(row.getCell(4).getStringCellValue().equals((soldier.isMale()) ? "M" : "F"), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected cell value");
                for (int k = 5; k < rowEnd; k++){
                    Cell cell = row.getCell(k);
                    String cellValue = AcftDataExporter.getCellValueAsString(cell);
                    String compareValue;
                    if (i == 0){
                        compareValue = (k == rowEnd - 1) ? Integer.toString(soldier.getTotalScore()) : Integer.toString(soldier.getScoreByEventId(k - 5, false));
                    } else compareValue = soldier.getRawScoreAsString(k - 5);
                    Assert.isTrue(cellValue.equals(compareValue), "In createXlsxWorkbookCreatesWorkbookWithExpectedData: unexpected cell value");
                }
            }
        }
    }

    @Test
    void createXlsxFileCreatesXlsxFileWithExpectedSheets(){
        int size = 5;
        Long testGroupId = acftManagerService.populateDatabase(size);
        String path = "src/main/resources/data/testGroup_" + testGroupId + ".xlsx";
        List<Soldier> soldiers = acftManagerService.getSoldiersByTestGroupId(testGroupId, "");
        XSSFWorkbook workbook = acftDataExporter.createXlsxWorkbook(soldiers);
        acftDataExporter.createXlsxFile(workbook, testGroupId);
        InputStream file = acftDataConversion.getFile(path);
        Assert.isTrue(file != null, "in createXlsxFileCreatesFileWithXlsxWorkbook: file was null");
        Workbook derivedWorkbook = AcftDataConversion.getWorkbook(file);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++){
            Assert.isTrue(derivedWorkbook.getSheetAt(i).getSheetName().equals(workbook.getSheetAt(i).getSheetName()), "in createXlsxFileCreatesFileWithXlsxWorkbook: sheet mismatch in derived workbook");
        }
        new File(path).delete();
    }
}
