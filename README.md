# ACFT Manager
**Description:** Eliminates the need for paper scorecards and manual score consolidation for the Army Combat Fitness Test. Enables decentralized on-site input of test data, automatic score conversion, data exportation, and data visualization<br/>

**Features:**<br/>
- Persist data via a UI [**Implemented**]<br/>
- Password authentication for test group access [**Implemented**]<br/>
- Automatic conversion of raw score data to scaled scores [**Implemented**]<br/>
- Automatic deletion of test groups after 48 hours [**Implemented**]<br/>
- Export testing group data to .xlsx [**Implemented**]<br/>
- Mobile-friendly UI [**Implemented**]<br/>
- UI Support for data visualization [**Implemented**]<br/>
- Add soldiers via excel spreadsheet [**Implemented**]<br/>

**Tech stack:**<br/>
   Postgres<br/>
   JPA/Hibernate<br/>
   Spring MVC<br/>
   
**Dependencies:**<br/>
   Gson<br/>
   Spring Data JPA<br/>
   H2 DB<br/>
   Apache POI<br/>
   Thymeleaf<br/>
   Spring Boot Test<br/>
   Spring Boot Dev Tools

**How to Run:**
1. Clone the repository
2. From the repository root run `docker-compose up`








