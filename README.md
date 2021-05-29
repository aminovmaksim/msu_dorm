## Web app to show your dormitory room and your neighbours

To run the app:
* create _credentials.json_ at _src/main/resources_
* Build project with ```gradle build```
* Run project: ```java -jar build/libs/msu_dorm-1.0.0.jar```
* Follow the link provided in output to authenticate
* After auth _StoredCredential_ file will be created at _/tokens_ and with the next runs you do not need to auth anymore