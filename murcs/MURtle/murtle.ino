/* 
 * MiniMURCS - Multi Use Robot Construction System
 *
 * Libs used:  
 * microM - MicroMagician Lib, written by Russell Cameron
 * FastSPI - WS2811 RGB LEDs, written by 
 */

#include <microM.h>
#include <Servo.h>

#define DEBUG_LEVEL 2

#define penServoPin    3

#define encoderLtPin   3
#define encoderRtPin   6

/* global Variables */

// motor control variables
int leftSpeed,rightSpeed = 0;
byte leftBrake, rightBrake = 0;
int speedStep = 10;

// servo control variables
Servo penServo;
#define penUpPos 75
#define penDnPos 90

// IR remote control variables
int actIRCommand, lastIRCommand = 0; 

// Encoder  variables
#define ENC_LT 0
#define ENC_RT  1

int encoder[2];
int encRaw[2];
int encMin[2];
int encMax[2];
int encFlag[2];

int readIR(void)
{
  int irCmd = 0;
  if(microM.ircommand>0)                         // display command from IR receiver
  {
    irCmd = microM.ircommand;
#if (DEBUG_LEVEL >= 1)
    Serial.print("\tIR command:");
    Serial.println(microM.ircommand,DEC);        // use the DEC option to convert a byte to a decimal number
#endif
    microM.ircommand=0;                          // prevents display repeating the same values
  }
  return irCmd; 
}

void initEncoder()
{
  for(int i=0; i<2; i++)
  {
    encoder[i] = 0;
    encMin[i] = 10000;
    encMax[i] = 0;
    encFlag[i] = 0;
  }
}

void readEncoder()
{
 
  encRaw[ENC_LT] = analogRead(encoderLtPin);
  encRaw[ENC_RT] = analogRead(encoderRtPin);
  for(int i=0; i<2; i++)
  {
    if (encRaw[i] < 400 && encFlag[i] == 1)
    {
      encoder[i]++;
      encFlag[i] = 0;
#if (DEBUG_LEVEL >= 1)
      Serial.print("Enc: ");
      Serial.print(i, DEC);
      Serial.print(" ");
      Serial.println(encoder[i], DEC);
#endif
    }
    if (encRaw[i] > 450 && encFlag[i] == 0)
    {
      encoder[i]++;
      encFlag[i] = 1;
#if (DEBUG_LEVEL >= 1)
      Serial.print("Enc: ");
      Serial.print(i, DEC);
      Serial.print(" ");
      Serial.println(encoder[i], DEC);
#endif
    }
    if (encRaw[i] < encMin[i]) 
    {
      encMin[i] = encRaw[i];
#if (DEBUG_LEVEL >= 2)
      Serial.print("Min: ");
      Serial.print(i, DEC);
      Serial.print(" ");
      Serial.println(encMin[i], DEC);
#endif
    }
    if (encRaw[i] > encMax[i]) 
    {
      encMax[i] = encRaw[i];
#if (DEBUG_LEVEL >= 2)
      Serial.print("Max: ");
      Serial.print(i, DEC);
      Serial.print(" ");
      Serial.println(encMax[i], DEC);
#endif
    }
  }
}

void readSensors()
{
  readEncoder();
  
  actIRCommand = readIR();
  if (actIRCommand && actIRCommand != lastIRCommand)
  {
    // todo: update Mode of operation
    switch (actIRCommand)
    {
      case 49: // |<<
        penServo.write(penDnPos);
      break;
      case 50: // >>|
        penServo.write(penUpPos);
      break;
      case 57:  //stop
      case 5:
        leftSpeed = 0;
        rightSpeed = 0;
      break;
      case 2:
        leftSpeed = 150;
        rightSpeed = 150;
      break;
      case 8:
        leftSpeed = -150;
        rightSpeed = -150;
      break;
      case 4:
        leftSpeed = -150;
        rightSpeed = 150;
      break;
      case 6:
        leftSpeed = 150;
        rightSpeed = -150;
      break;
      case 1:
        leftSpeed = 0;
        rightSpeed = 150;
      break;
      case 3:
        leftSpeed = 150;
        rightSpeed = 0;
      break;
      case 7:
        leftSpeed = -150;
        rightSpeed = 0;
      break;
      case 9:
        leftSpeed = 0;
        rightSpeed = -150;
      break;
      
    }
    lastIRCommand = actIRCommand ;
  }
  else
    lastIRCommand = 0;
}

void updateActors()
{
  microM.Motors(leftSpeed,rightSpeed,leftBrake,rightBrake);    // update motor controller
}

void doProcessing()
{
}

void setup() 
{
  Serial.begin(38400);
  Serial.println("MiniMURCS v0.1");
  Serial.println("resetting!");

  microM.Setup(); 

  initEncoder();

  penServo.attach(penServoPin);
  penServo.write(penDnPos);
  delay(2000);
  penServo.write(penUpPos);
  delay(2000);
}


void loop()
{
  readSensors();
  doProcessing();
  updateActors();
}

