<h1> Quanomous Quickstart </h1>

Quanomous is a configurable, QR code based, autonomous generation and execution system designed for FTC robots. It allows for teams to generate autonomous routines to compliment their allinace partners, allowing each team to create an auto that enables your alliance to do the best. It also allows teams to generate autonomous routines visually using the Quanomous web tool, then run those routines directly on their robot.

<img src="https://i.postimg.cc/76YzLQ5m/Quanomous_Blocks.png" alt="Quanomous Blocks" width="250">

The system works by exporting autonomous scripts as compressed strings that are decoded on the robot and executed sequentially.

Project created by 3DRoboticsDuluth.

Using the Quanomous Generator

  Generate autos using: https://3droboticsduluth.github.io/Quanomous/

The generator exports autonomous routines as: Base64 + GZIP compressed JSON scripts

These are processed on the robot using: Quanomous.process(encodedAutoString);

This will:

  1. Decode the script

  2. Parse the JSON
  
  3. Save it to robot storage

  4. Return the filename for execution

Installation

Clone the repository and open it in Android Studio.

Copy these core files into your TeamCode module:

Quanomous.java
QuanomousCommand.java

Then implement QuanomousCommand in your robot code.

Running Autonomous

Load and execute autos like this:
  Robot side
   
    Scan the QR code on the robots camera
  
  Software side
    
    quanomous.execute()

Storage Location

Autos are stored on the robot at:

  /sdcard/FIRST/quanomous/

License

BSD-3-Clause. You can use, modify, and distribute this code as long as attribution is kept.
