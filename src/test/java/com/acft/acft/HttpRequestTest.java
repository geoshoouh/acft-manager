package com.acft.acft;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;

import com.acft.acft.Entities.Soldier;
import com.acft.acft.Entities.TestGroup;
import com.acft.acft.Exceptions.InvalidPasscodeException;
import com.acft.acft.Services.AcftManagerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;

@SpringBootTest
@AutoConfigureMockMvc
public class HttpRequestTest {

    @Autowired
    AcftManagerService acftManagerService;

    BulkSoldierUploadTest bulkSoldierUploadTest = new BulkSoldierUploadTest();

    String testPath = "src/main/resources/data/bulkUploadTest.xlsx";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Gson gson;

    @Test
    void postNewTestGroupShouldReturnGroupIdAndPasswordNotIncludedInResponse() throws Exception {

        Long testGroupId = Long.parseLong(
            mockMvc.perform(
                post("/testGroup/new")
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                );
        
        Assert.notNull(testGroupId, "Response to /testGroup/new was null");
    }

    @Test
    void postNewTestGroupWithPasscodeShouldReturnGroupId() throws Exception {
        String passcode = "password";
        Long testGroupId = Long.parseLong(
            mockMvc.perform(
                post("/testGroup/new/{passcode}", passcode)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                );
        Assert.notNull(testGroupId, "Response to /testGroup/new was null");
        TestGroup testGroup = acftManagerService.getTestGroupByPseudoId(testGroupId, passcode);
        Assert.isTrue(testGroup.getPasscode().equals(passcode), "in postNewTestGroup w/passcode, expected passcode was " + passcode + ",actual passcode was " + testGroup.getPasscode());
    }

    //Ensures a test group's passcode cannot be fetched by making a getAllTestGroups request and inspecting the Json
    @Test
    void getTestGroupShouldReturnTestGroup() throws Exception {
        //testGroup instantiated w/passcode
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        TestGroup testGroupFromResponse = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/{testGroupId}/{passcode}", testGroup.getPseudoId(), passcode)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), 
            TestGroup.class
            );
        Assert.isTrue(testGroupFromResponse.getPseudoId() == testGroup.getPseudoId(), "Response to /testGroup/get/{testGroupId} returned incorrect TestGroup");

        //testGroup instantiated w/o passcode
        Long testGroupIdEmptyPasscode = acftManagerService.createNewTestGroup();
        TestGroup testGroupEmptyPasscode = acftManagerService.getTestGroup(testGroupIdEmptyPasscode, "");
        TestGroup testGroupFromResponseEmptyPasscode = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/{testGroupId}/randomText", testGroupEmptyPasscode.getPseudoId())
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), 
            TestGroup.class
            );
        Assert.isTrue(testGroupFromResponseEmptyPasscode.getPseudoId() == testGroupEmptyPasscode.getPseudoId(), "Response to /testGroup/get/{testGroupId} returned incorrect TestGroup");
    }

     //Ensures a test group's passcode cannot be fetched by making a getAllTestGroups request and inspecting the Json
     //Test group passcodes never reach the client side after instantiation
    @Test
    void testGroupPasscodeNotVisibleInJsonRepresentiation() throws Exception {
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        TestGroup testGroupFromResponse = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/{testGroupId}/{passcode}", testGroup.getPseudoId(), passcode)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), 
            TestGroup.class
        );
        boolean exceptionCaught = false;
        try {
            acftManagerService.getTestGroup(testGroupId, testGroupFromResponse.getPasscode());
        } catch (InvalidPasscodeException e) {
            exceptionCaught = true;
        }
        Assert.isTrue(exceptionCaught, "Passcode received from json conversion of testGroup should have been null and resulted in a thrown exception when attempting to get the same test group");
    }

    @Test
    void createNewSoldierShouldReturnSoldierId() throws Exception {
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = Long.parseLong(
            mockMvc.perform(
                post("/testGroup/post/{pseudoTestGroupId}/{passcode}/{lastName}/{firstName}/{age}/{isMale}",
                testGroup.getPseudoId(), "default", "Tate", "Joshua", 26, true)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );
        Assert.notNull(soldierId, "Response to postNewSoldier http request was null");
    }

    @Test
    void getSoldierByIdShouldReturnSoldier() throws Exception {
        Long testGroupId = acftManagerService.createNewTestGroup();
        Long soldierId = acftManagerService.createNewSoldier(testGroupId, "Tate", "Joshua", 26, true);
        Soldier soldier = gson.fromJson(
            mockMvc.perform(
                get("/soldier/get/{soldierId}/null", soldierId)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(),
             Soldier.class
            );
        Assert.isTrue(soldier.getId().equals(soldierId), "/soldier/get/{soldierId} responded with incorrect Soldier; expected " + soldier.getId() + ", was " + soldierId);
        String passcode = "password";
        testGroupId = acftManagerService.createNewTestGroup(passcode);
        soldierId = acftManagerService.createNewSoldier(testGroupId, passcode, "Tate", "Joshua", 26, true);
        soldier = gson.fromJson(
            mockMvc.perform(
                get("/soldier/get/{soldierId}/{passcode}", soldierId, passcode)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(),
             Soldier.class
            );
        Assert.isTrue(soldier.getId().equals(soldierId), "/soldier/get/{soldierId}/{passcode} responded with incorrect Soldier");
    }

    @Test
    void getSoldiersByTestGroupIdShouldReturnListOfSoldiersWithPassedId() throws Exception {
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        int n = 5;
        String[] lastNames = {"Smith", "Jones", "Samuels", "Smith", "Conway"};
        String[] firstNames = {"Jeff", "Timothy", "Darnell", "Fredrick", "Katherine"};
        int[] ages = {26, 18, 19, 30, 23};
        boolean[] genders = {true, true, true, true, false};
        for (int i = 0; i < n; i++){
            acftManagerService.createNewSoldier(testGroupId, lastNames[i], firstNames[i], ages[i], genders[i]);
        }
        Type listOfSoldierObjects = new TypeToken<ArrayList<Soldier>>() {}.getType();
        List<Soldier> queryResult = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/getSoldiers/{testGroupId}/randomText",
                testGroup.getPseudoId())
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            , listOfSoldierObjects);
            Assert.isTrue(queryResult.size() == n, "getSoldiersByTestGroupId returned array of unexpected size");
    }

    @Test
    void getAllTestGroupsShouldReturnAllExistingTestGroupIds() throws Exception {
        int reference = acftManagerService.getAllTestGroupPseudoIds().size();
        int n = 5;
        for (int i = 0; i < n; i++) acftManagerService.createNewTestGroup();
        Type listOfTestGroupIds = new TypeToken<ArrayList<Long>>() {}.getType();
        List<Long> requestResult = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/all")
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), listOfTestGroupIds);
        Assert.isTrue(requestResult.size() == reference + n, "getAllTestGroupsShouldReturnAllExistingTestGroupIds returned incorrectly sized array");
    }

    @Test
    void updateSoldierScoreShouldReturnCorrectConvertedScore() throws Exception {
        Long testGroupId = acftManagerService.createNewTestGroup();
        Long soldierId = acftManagerService.createNewSoldier(testGroupId, "Tate", "Joshua", 26, true);
        int eventId = 0;
        int rawScore = 205;
        int expectedConversion = 71;
        int requestResult = Integer.parseInt(
            mockMvc.perform(
                post("/soldier/updateScore/{soldierId}/{eventId}/{rawScore}/randomValue",
                soldierId, eventId, rawScore)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            );
        Assert.isTrue(requestResult == expectedConversion, "For update score request: expected result was " + expectedConversion + ", actual result was " + requestResult);
    }

    @Test
    void updateSoldierScoreOnProtectedTestGroupShouldReturnCorrectConvertedScore() throws Exception {
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        Long soldierId = acftManagerService.createNewSoldier(testGroupId, passcode, "Tate", "Joshua", 26, true);  
        int eventId = 5;
        int rawScore = 1080;
        int expectedScore = 74;
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        System.out.println("passcode from TG: " + testGroup.getPasscode() + " passed code: " + passcode);
        System.out.println(testGroup.getPasscode().equals(passcode));
        int requestResult = Integer.parseInt(
            mockMvc.perform(
                post("/soldier/updateScore/{soldierId}/{eventId}/{rawScore}/{passcode}",
                soldierId, eventId, rawScore, passcode)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            ); 
            Assert.isTrue(requestResult == expectedScore, "For update score request: expected result was " + expectedScore + ", actual result was " + requestResult);
    }

    // Test fails during image build; disabled for now.
    @Disabled
    @Test
    void exportXlsxFileForTestGroupShouldExportExpectedFile() throws Exception {
        int size = 5;
        //No passcode used in populateDatabase utility function
        Long testGroupId = acftManagerService.populateDatabase(size);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        HttpServletResponse response = mockMvc.perform(
            get("/testGroup/getXlsxFile/{testGroupId}", testGroup.getPseudoId())
        ).andExpect(status().isOk())
        .andReturn()
        .getResponse();
        Assert.isTrue(response.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), "In exportXlsxFileForTestGroupShouldExportExpectedFile: unexpected content type in servlet response");
        String path = "src/main/resources/data/testGroup_" + testGroupId + ".xlsx";
        Assert.isTrue(!new File(path).exists(), "In createXlsxFileCreatesXlsxFileWithExpectedSheets: file was not deleted after being served to client");
    }

    // Test fails during image build; disabled for now.
    @Disabled
    @Test
    void getBulkUploadTemplateReturnsFile() throws Exception {
        HttpServletResponse response = mockMvc.perform(
            get("/getBulkUploadTemplate")
        ).andExpect(status().isOk())
        .andReturn()
        .getResponse();
        Assert.isTrue(response.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), "In getBulkUploadTemplateReturnsFile: unexpected content type in servlet response");
    }

    @Test
    void flushDatabaseDeletesAllEntities() throws Exception {
        int size = 5;
        acftManagerService.populateDatabase(size);
        Assert.isTrue(acftManagerService.getSoldierRepositorySize() > 0 && acftManagerService.getTestGroupRepositorySize() > 0, "In flushDatabseDeletesAllEntities: database population failed");
        boolean response = Boolean.parseBoolean(
            mockMvc.perform(
                delete("/deleteAll")
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );
        Assert.isTrue(acftManagerService.getSoldierRepositorySize() == 0 && acftManagerService.getTestGroupRepositorySize() == 0, "In flushDatabseDeletesAllEntities: request to flushDatabase() failed");
        Assert.isTrue(response, "In flushDatabseDeletesAllEntities: unexpected boolean response");
    }

    @Test
    void deleteSoldierByIdPersistsDeletion() throws Exception {
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroupId, "Tate", "Joshua", 26, true);
        Assert.isTrue(acftManagerService.getSoldiersByTestGroupId(testGroupId).size() == 1, "In deleteSoldierByIdPersistsDeletion: testGroup had unexpected population size after soldier creation");
        boolean response = Boolean.parseBoolean(
            mockMvc.perform(
                delete("/soldier/delete/{testGroupId}/{soldierId}", testGroup.getPseudoId(), soldierId)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );
        Assert.isTrue(acftManagerService.getSoldiersByTestGroupId(testGroupId).size() == 0, "In deleteSoldierByIdPersistsDeletion: testGroup had unexpected population size after soldier deletion");
        Assert.isTrue(response, "In deleteSoldierByIdPersistsDeletion: unexpected boolean response");
    }

    @Test
    void getTestGroupDataReturnsExpectedData() throws Exception {
        int size = 5;
        Long testGroupId = acftManagerService.populateDatabase(size);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Type testGroupDataType = new TypeToken<ArrayList<ArrayList<Long>>>() {}.getType();
        List<List<Long>> testGroupData = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/{testGroupId}/get/scoreData/{raw}", testGroup.getPseudoId(), true)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            , testGroupDataType);
            Assert.isTrue(testGroupData.size() == size && testGroupData.get(0).size() == 8, "In getTestGroupDataReturnsExpectedData: data array had unexpected dimensions");

            System.out.println("=================== TestGroup Data (Http Test) ===================");
            testGroupData.forEach((row) -> {
                    row.forEach((element) -> {
                    System.out.print(element + " ");
                });
                System.out.println();
            });
    }

    @Test
    void populateDatePersistsData() throws Exception {
        int size = 11;
        Long testGroupPseudoId = Long.parseLong(
            mockMvc.perform(
                post("/populateDatabase/{size}", size)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
        TestGroup testGroup = acftManagerService.getTestGroupByPseudoId(testGroupPseudoId, "");
        Assert.isTrue(acftManagerService.getSoldiersByTestGroupId(testGroup.getId()).size() == size, "In populateDatePersistsData: unexpected testGroup population size after populate called");
    }

    @Test
    void instantiateBulkUploadDataInstantiatesSoldiers() throws Exception {
        int sz = 5;
        bulkSoldierUploadTest.generateBulkUploadTestFile(sz);
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        File file = new File(testPath);
        InputStream inputStream = new FileInputStream(file);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("bulkUpload.xlsx", inputStream);
        inputStream.close();
        boolean responseBodyBoolean = Boolean.parseBoolean(
            mockMvc.perform(
                post("/bulkUpload/{testGroupId}", testGroup.getPseudoId()).content(mockMultipartFile.getBytes())
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );
        Assert.isTrue(responseBodyBoolean, "In instantiateBulkUploadDataInstantiatesSoldiers for HTTP: unexpected response value, was " + responseBodyBoolean);
        Assert.isTrue(acftManagerService.getSoldiersByTestGroupId(testGroupId).size() == sz, "In instantiateBulkUploadDataInstantiatesSoldiers: unexpected test group size after soldier instantiation");
        file.delete();
        File reqFile = new File("src/main/resources/data/bulkUpload.xlsx");
        reqFile.delete();
    }
}
