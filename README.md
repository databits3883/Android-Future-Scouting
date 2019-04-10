# Databits 3883 FRC Scouting App

This app was built for the Lenovo Tab 4 8, but has general ui support for all tablets and most "normal" sized phones. Smaller phones might have some issues because the UI was built to have everything always on the screen without scrolling.

Everything needed to generate the apk is in the repo, I have included a fork AwesomeQRCode as well because i need to do a minor fix on it to allow it to work properly in a java app. You can see my changes [here](https://github.com/SumiMakito/AwesomeQRCode/pull/37)

## Setup required to use this app efficiently
* Configuration Google Sheet - Format explained in the [Settings](#settings) Section.
* Match list Google Sheet - Format explained in the [Settings](#settings) Section.
* 6 Devices (Any combination of Phones/Tablets Wifi-Only/LTE, but having them all the same or almost all the same is a plus.
* 1 Device having an LTE connection as well as an Google Account on the device to allow data to be uploaded to Google Sheets.

## Explanation of each section and how to use it

### Welcome

This is the first screen you will see when you launch the app. It tells you how to open up the navigation drawer as well as a drop-down menu to select the position the device will be scouting or to set the device into practice mode.

* Selecting a Red/Blue position will enable the Team auto-fill function in Crowd Scouting and the Missing-scan detector in Master Device.
* Selecting Practice mode will require you to manually fill in the team number in Crowd Scouting.

<img src="https://github.com/databits3883/Android-Future-Scouting/raw/master/Screenshots/Welcome.png" width="320" height="512"><img src="https://github.com/databits3883/Android-Future-Scouting/raw/master/Screenshots/Welcome_clicked.png" width="320" height="512">

### Crowd Scouting

Practice Mode Scouting
No auto-fill team number
Auto-Match Incrementing after export

Qualification Scouting
Auto-fill team number based on position and match number
Auto-Match Incrementing after export

QR Scanned by master device (If you click the screen before it is scanned press the Display Previous QR Code button to get it again)
All data gathered by each device is stored locally in FRC/crowd_data.csv as well as a copy of each match QR in /FRC/QR/ (The CSV is just for backup purpuses, QR codes are saved to make it easier for the previous qr button to work)

### Pit Scouting

All data gathered by the device is stored locally in FRC/pit_data.csv so you can either take the file and manually import the data into your sheet or optionally upload it if your pit scouting devices have the ability via LTE or Tethering.

### Master

Scans QR codes from Crowd Scouting and stores them all in stats.csv and upload.csv. stats contains all the data for backup purposes, upload only contains what hasn't been uploaded yet.

Configuration Google Sheet
Match list Google Sheet

### Settings

Remove all data

Set Configuration Google Sheet

Download latest team info from Configuration Google Sheet