/*
* YAABR main controller
* Arduino code for MD23 compatible motor controller
*/

#include <Wire.h>

#define md23Address 0x58                                    // Address of the MD22
#define softwareReg 0x0D                                    // Byte to read the software version
#define speed1 0x00                                         // Byte to send speed to first motor
#define speed2 0x01                                         // Byte to send speed to second motor
#define cmdByte 0x10                                        // Command byte
#define encoderOne 0x02                                     // Byte to read motor encoder 1
#define encoderTwo 0x06                                     // Byte to read motor encoder 2
#define voltRead 0x0A                                       // Byte to read battery volts
#define rxPin 2                                             // software serial rx pin
#define txPin 3                                             // software serial tx pin

int x=0;                                                    // int x stores a value to be used as speed data for the motors

void setup(){
  Wire.begin();
  Serial.begin(38400);
  delay(100);                                               // Wait for everything to power up
  Serial.println("YAABR Main Ctrl V01");
  int softVer = getSoft();                                  // Gets the software version of MD23
  Serial.print("YAABR Motor Ctrl V");
  Serial.println(softVer, DEC);                               // Print software version to the screen

  encodeReset();                                            // Cals a function that resets the encoder values to 0
}

void loop(){
 x = 255;                                                   // Put a value of 255 in x, this will dive motors forward at full speed
 
 do{                                                        // Start loop to drive motors forward
    Wire.beginTransmission(md23Address);                    // Drive motor 2 at speed value stored in x
    Wire.send(speed2);
    Wire.send(x);                                           
    Wire.endTransmission();
  
    Wire.beginTransmission(md23Address);                    // Drive motor 1 at speed value stored in x
    Wire.send(speed1);
    Wire.send(x);
    Wire.endTransmission();
 
    encoder1();                                             // Calls a function that reads and displays value of encoder 1 to LCD03
    encoder2();                                             // Calls a function that reads and displays value of encoder 2 to LCD03
    volts();                                                // Calls function that reads and displays battery volts
    if(encoder1() > 4500)                                   // When value of encoder 1 is greater than 4500 put 138 in x to slow motor down
      x = 138;
 }while(encoder1() < 5000);                                 // Loop untill encoder 1 is 5000 or more
 
 stopMotor();                                               // Calls function to stop motors
 delay(1000);
 
 x = 0;                                                     // Put a value of 0 in x, this will drive motors at full reverse speed
 
 do{                                                        // Start loop to drive motors reverse
    Wire.beginTransmission(md23Address);                    // Drive motor 2 at speed value stored in x
    Wire.send(speed2);
    Wire.send(x);
    Wire.endTransmission();
  
    Wire.beginTransmission(md23Address);                    // Drive motor 1 at speed value stored in x
    Wire.send(speed1);
    Wire.send(x);
    Wire.endTransmission();
  
    encoder1();                                             // Calls a function that reads and displays value of encoder 1 to LCD03
    encoder2();                                             // Calls a function that reads and displays value of encoder 2 to LCD03
    volts();                                                // Calls function that reads and displays battery volts
    if(encoder1() < 500)                                    // When value of encoder 1 is less than 500 put 118 in x to slow motor down
      x = 118;   
  }while(encoder1() > 0);                                   // Loop untill encoder 1 is 0 or less
  
  stopMotor();                                              // Calls function to stop motors
  delay(1000);
}

int getSoft(){                                              // Function that gets the software version
  Wire.beginTransmission(md23Address);                      // Send byte to read software version as a single byte
  Wire.send(softwareReg);
  Wire.endTransmission();
  
  Wire.requestFrom(md23Address, 1);                         // request 1 byte form MD23
  while(Wire.available() < 1);                              // Wait for it to arrive
  int software = Wire.receive();                            // Read it in
  
  return(software);
}

void encodeReset(){                                         // This function resets the encoder values to 0
  Wire.beginTransmission(md23Address);
  Wire.send(cmdByte);
  Wire.send(0x20);                                          // Putting the value 0x20 to reset encoders
  Wire.endTransmission(); 
}

long encoder1(){                                            // Function to read and display value of encoder 1 as a long
  Wire.beginTransmission(md23Address);                      // Send byte to get a reading from encoder 1
  Wire.send(encoderOne);
  Wire.endTransmission();
  
  Wire.requestFrom(md23Address, 4);                         // Request 4 bytes from MD23
  while(Wire.available() < 4);                              // Wait for 4 bytes to arrive
  long firstByte = Wire.receive();                          // Recieve 4 bytes highest byte first
  long secondByte = Wire.receive();
  long thirdByte = Wire.receive();
  long fourthByte = Wire.receive();
  
  long poss1 = (firstByte << 24) + (secondByte << 16) + (thirdByte << 8) + fourthByte;  // Put them together
  
  Serial.print("Encoder 1 ");
  Serial.println(poss1, DEC);                                 // Print encoder 1 value to LCD03
  delay(50);                                                // Wait for everything to make sure everything is sent
  
  return(poss1);                                            // returns value of encoder 1 position as a long
}

long encoder2(){                                            // Function to read and display velue of encoder 2 as a long
  Wire.beginTransmission(md23Address);                      // Send byte to get a reading from encoder 2                   
  Wire.send(encoderTwo);
  Wire.endTransmission();
  
  Wire.requestFrom(md23Address, 4);                         // Request 4 bytes from MD23
  while(Wire.available() < 4);                              // Wait for 4 bytes to become available
  long firstByte = Wire.receive();                          // Get four bytes highest byte first
  long secondByte = Wire.receive();
  long thirdByte = Wire.receive();
  long fourthByte = Wire.receive();
  
  long poss2 = (firstByte << 24) + (secondByte << 16) + (thirdByte << 8) + fourthByte;  // Put them together
  
  Serial.print("Encoder 2 ");
  Serial.println(poss2, DEC);                                 // Print value of encoder 2 position
  delay(50);                                                // Wait for everything to make sure everything is sent
   
  return(poss2);                                            // Returns encoder 2 position as a long
}

void volts(){                                               // Function to read and display battery volts as a single byte
  Wire.beginTransmission(md23Address);                      // Send byte to read volts
  Wire.send(voltRead);
  Wire.endTransmission();
  
  Wire.requestFrom(md23Address, 1);                         // Request 1 byte form MD23
  while(Wire.available() < 1);                              // Wait for byte to arrive 
  int batteryVolts = Wire.receive();                        // Get byte
  
  Serial.print("Battery v = ");
  Serial.print(batteryVolts/10, DEC);                       // Print batery volts / 10 to give you the number of whole volts
  Serial.print(".");                                        // As arduino has truncates floating point numbers we print a . here to act as a decimal point
  Serial.println(batteryVolts%10, DEC);                       // Prints Battery volts % 10 to give us the 
  
  delay(50);                                                // Wait to make sure everything is sent
}

void stopMotor(){                                           // Function to stop motors
  Wire.beginTransmission(md23Address);
  Wire.send(speed2);
  Wire.send(128);                                           // Sends a value of 128 to motor 2 this value stops the motor
  Wire.endTransmission();
  
  Wire.beginTransmission(md23Address);
  Wire.send(speed1);
  Wire.send(128);                                           // Sends a value of 128 to motor 1 this value stops the motor
  Wire.endTransmission();
}  
