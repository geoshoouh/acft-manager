package com.acft.acft;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;


@SpringBootTest
public class AcftManagerServiceTest {

    @Autowired
    AcftManagerService acftManagerService;

    @Test
    void createNewTestGroupShouldReturnId(){
        Long testGroupId = acftManagerService.createNewTestGroup();
        Assert.notNull(testGroupId, "createNewTestGroup returned null");
    }

    @Test
    void createNewTestGroupWithPasscodeShouldReturnId(){
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        Assert.notNull(testGroupId, "createNewTestGroup returned null");
        Assert.isTrue(testGroup.getPasscode().equals(passcode), "createNewTestGroup w/passcode failed to create instance with expected passcode attribute");
    }

    @Test
    void getTestGroupShouldReturnTestGroup(){
        String passcode = "password";
        Long testGroupId = acftManagerService.createNewTestGroup(passcode);
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, passcode);
        Assert.isTrue(testGroup.getId() == testGroupId, "getTestGroup returned the incorrect group");
    }

    @Test
    void createNewSoldierShouldReturnId(){
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroup, "Tate", "Joshua", 26, true);
        Assert.notNull(soldierId, "createNewSoldier returned null ID");
    }

    @Test 
    void getSoldierByIdShouldReturnSoldier(){
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroup, "Tate", "Joshua", 26, true);
        Soldier soldier = acftManagerService.getSoldierById(soldierId);
        Assert.isTrue(soldier.getId() == soldierId, "getSoldierById returned the incorrect Soldier");
    }

    @Test 
    void getSoldiersByLastNameAndTestGroupIdShouldReturnListOfSoldiers(){
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
        List<Soldier> queryResult = acftManagerService.getSoldiersByLastNameAndTestGroupId("Smith", testGroupId);
        Assert.isTrue(queryResult.size() == 2, "error in retrieval of soldiers by lastName and groupId");
    }

    //Test like this pass when run along, but fail when all methods in the class are executed
    //Added initial query to establish reference
    //This is perhaps a lazy solution to mitigate a lack of familiarity with how Spring Boot tests execute
    @Test
    void getAllTestGroupsShouldReturnAllExistingTestGroupIds(){
        int reference = acftManagerService.getAllTestGroups().size();
        int n = 5;
        for (int i = 0; i < n; i++) acftManagerService.createNewTestGroup();
        List<Long> allExistingTestGroupIds = acftManagerService.getAllTestGroups();
        Assert.isTrue(allExistingTestGroupIds.size() == reference + n, "getAllTestGroups returned array of unexpected size");
    }

    @Test
    void getSoldiersByTestGroupIdShouldReturnListOfSoldiersWithPassedId(){
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
        List<Soldier> soldiersWithCertainGroupId = acftManagerService.getSoldiersByTestGroupId(testGroupId);
        Assert.isTrue(soldiersWithCertainGroupId.size() == n, "getSoldiersByTestGroupId returned array of unexpected size");
    }

    @Test
    void updateSoldierScoreShouldReturnCorrectScaledScore(){
        Long testGroupId = acftManagerService.createNewTestGroup();
        TestGroup testGroup = acftManagerService.getTestGroup(testGroupId, "");
        Long soldierId = acftManagerService.createNewSoldier(testGroup, "Tate" , "Joshua", 31, true);
        int convertedScore = acftManagerService.updateSoldierScore(soldierId, 1, 110);
        //Expected conversion for 31 year old male scoring 110 cm on the standing power throw is 90 points
        int expectedScore = 89;
        Assert.isTrue(convertedScore == expectedScore, "expected score was " + expectedScore + " and actual score was " + convertedScore);
    }

    @Test
    @Transactional
    void deleteTestGroupsOnScheduleDeletesExpiredTestGroups(){
        int reference = acftManagerService.getAllTestGroups().size();
        Long testGroupId1 = acftManagerService.createNewTestGroup();
        Long testGroupId2 = acftManagerService.createNewTestGroup();
        TestGroup testGroup1 = acftManagerService.getTestGroup(testGroupId1, "");
        TestGroup testGroup2 = acftManagerService.getTestGroup(testGroupId2, "");
        System.out.println("before changes: " + testGroup1);
        testGroup1.setExpirationDate(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
        
        testGroup2.setExpirationDate(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        //System.out.println("in test: " + testGroup1);
        //System.out.println("from repo: " + acftManagerService.getTestGroup(testGroupId1, ""));
        acftManagerService.deleteTestGroupsOnSchedule();
        Assert.isTrue(acftManagerService.getAllTestGroups().size() == reference + 1, "deleteTestGroupsOnSchedule did not produce expected results. Expected repo size " + 1 + ", size was actually " + acftManagerService.getAllTestGroups().size());
    }
}