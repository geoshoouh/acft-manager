
export async function createNewTestGroup(){
    let response = await fetch('http://localhost:8080/testGroup/new', {
        method: 'POST'
      }).then((response) => response).catch((error) => console.log(error));
    if (!response.ok) throw Error(`Response to testGroup/new was ${response.status}`);
    return response.json();
}

export async function createNewTestGroupWithPasscode(passcode){
  let response = await fetch(`http://localhost:8080/testGroup/new/${passcode}`, {
        method: 'POST'
      }).then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to testGroup/new/${passcode} was ${response.status}`);
  return response.json();
}

export async function createNewSoldier(testGroupId, lastName, firstName, age, isMale){
  let response = await fetch(
    `http://localhost:8080/testGroup/post/${testGroupId}/${lastName}/${firstName}/${age}/${isMale}`,
    {method: 'POST'})
    .then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to /testGroup/post/${testGroupId}/${lastName}/${firstName}/${age}/${isMale} was ${response.status}`);
  return response.json();
}

export async function getAllTestGroups(){
  let response = await fetch('http://localhost:8080/testGroup/get/all')
    .then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to /testGroup/get/all was ${response.status}`);
  return response.json();
}

export async function getEditSoldierDataView(){
  location.replace('http://localhost:8080/editSoldierData');
}

export async function getHomePageView(){
  location.replace('http://localhost:8080');
}

export async function getSoldiersByTestGroupId(testGroupId){
  if (typeof testGroupId != "number"){
    console.log(`getSoldiersByTestGroupId in ACFTManagerAPI expected Number; ${typeof soldierId} passed`);
    return;
  }
  let response = await fetch(
    `http://localhost:8080/testGroup/getSoldiers/${testGroupId}`
  ).then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to /testGroup/getSoldiers/${testGroupId} was ${response.status}`);
  return response.json();
}

export async function getSoldierById(soldierId){
  let response = await fetch(
    `http://localhost:8080/soldier/get/${soldierId}`
  ).then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to /get/${soldierId} was ${response.status}`);
  return response.json();
}

export async function getTestGroupById(testGroupId, passcode){
  let response = await fetch(
    `http://localhost:8080/testGroup/get/${testGroupId}/${passcode}`
  ).then((response) => response).catch((error) => console.log(error));
  if (!response.ok) throw Error(`Response to /testGroup/get/${testGroupId}/${passcode} was ${response.status}`);
  return response.json();
}

export async function updateSoldierScore(soldierId, eventId, rawScore){
  let response = await fetch(
    `http://localhost:8080/soldier/updateScore/${soldierId}/${eventId-1}/${rawScore}`,
    {method: 'POST'})
    .then((response) => response).catch((error) => console.log(error));
    if (!response.ok) throw Error(`Response to /soldier/updateScore/${soldierId}/${eventId-1}/${rawScore} was ${response.status}`);
    return response.json();
}



