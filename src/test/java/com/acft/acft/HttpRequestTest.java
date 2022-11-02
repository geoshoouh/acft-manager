package com.acft.acft;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


@SpringBootTest
@AutoConfigureMockMvc
public class HttpRequestTest {

    @Autowired
    AcftManagerService acftManagerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Gson gson;

    @Test
    void postNewTestGroupShouldReturnGroupId() throws Exception{
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
    void postNewTestGroupWithPasscodeShouldReturnGroupId() throws Exception{
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
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        Assert.isTrue(testGroup.getPasscode().equals(passcode), "in postNewTestGroup w/passcode, expected passcode was " + passcode + ",actual passcode was " + testGroup.getPasscode());
    }

    @Test
    void getTestGroupShouldReturnTestGroup() throws Exception{
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        TestGroup testGroup = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/{testGroupId}/{passcode}", testGroupId, passcode)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), 
            TestGroup.class
            );
        System.out.println(testGroup);
        Assert.isTrue(testGroup.getId() == testGroupId, "Response to /testGroup/get/{testGroupId} returned incorrect TestGroup");
    }

    @Test
    void createNewSoldierShouldReturnSoldierId() throws Exception{
        Long testGroupId = acftManagerService.createNewTestGroup();
        Long soldierId = Long.parseLong(
            mockMvc.perform(
                post("/testGroup/post/{testGroup}/{lastName}/{firstName}/{age}/{isMale}",
                testGroupId, "Tate", "Joshua", 26, true)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );
        Assert.notNull(soldierId, "Response to postNewSoldier http request was null");
    }

    @Test
    void getSoldierByIdShouldReturnSoldier() throws Exception{
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroup, "Tate", "Joshua", 26, true);
        Soldier soldier = gson.fromJson(
            mockMvc.perform(
                get("/soldier/get/{soldierId}", soldierId)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(),
             Soldier.class
            );
        Assert.isTrue(soldier.getId() == soldierId, "/soldier/get/{soldierId} responded with incorrect Soldier");
    }

    @Test
    void getSoldiersByLastNameAndTestGroupIdShouldReturnListOfSoldiers() throws Exception{
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        int n = 5;
        String[] lastNames = {"Smith", "Jones", "Samuels", "Smith", "Conway"};
        String[] firstNames = {"Jeff", "Timothy", "Darnell", "Fredrick", "Katherine"};
        int[] ages = {26, 18, 19, 30, 23};
        boolean[] genders = {true, true, true, true, false};
        for (int i = 0; i < n; i++){
            acftManagerService.createNewSoldier(testGroup, lastNames[i], firstNames[i], ages[i], genders[i]);
        }
        Type listOfSoldierObjects = new TypeToken<ArrayList<Soldier>>() {}.getType();
        List<Soldier> queryResult = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/get/byLastNameAndGroup/{lastName}/{testGroupId}",
                lastNames[0], testGroupId)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            , listOfSoldierObjects);
        Assert.isTrue(queryResult.size() == 2, "error in retrieval of soldiers by lastName and groupId");
    }

    @Test
    void getSoldiersByTestGroupIdShouldReturnListOfSoldiersWithPassedId() throws Exception{
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        int n = 5;
        String[] lastNames = {"Smith", "Jones", "Samuels", "Smith", "Conway"};
        String[] firstNames = {"Jeff", "Timothy", "Darnell", "Fredrick", "Katherine"};
        int[] ages = {26, 18, 19, 30, 23};
        boolean[] genders = {true, true, true, true, false};
        for (int i = 0; i < n; i++){
            acftManagerService.createNewSoldier(testGroup, lastNames[i], firstNames[i], ages[i], genders[i]);
        }
        Type listOfSoldierObjects = new TypeToken<ArrayList<Soldier>>() {}.getType();
        List<Soldier> queryResult = gson.fromJson(
            mockMvc.perform(
                get("/testGroup/getSoldiers/{testGroupId}",
                testGroupId)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            , listOfSoldierObjects);
            Assert.isTrue(queryResult.size() == n, "getSoldiersByTestGroupId returned array of unexpected size");
    }

    @Test
    void getAllTestGroupsShouldReturnAllExistingTestGroupIds() throws Exception{
        int reference = acftManagerService.getAllTestGroups().size();
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
    void updateSoldierScoreShouldReturnCorrectConvertedScore() throws Exception{
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroup, "Tate", "Joshua", 26, true);
        int eventId = 0;
        int rawScore = 205;
        int expectedConversion = 71;
        int requestResult = Integer.parseInt(
            mockMvc.perform(
                post("/soldier/updateScore/{soldierId}/{eventId}/{rawScore}",
                soldierId, eventId, rawScore)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            );
        Assert.isTrue(requestResult == expectedConversion, "For update score request: expected result was " + expectedConversion + ", actual result was " + requestResult);
    }
}