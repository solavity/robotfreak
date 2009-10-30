#include <Wire.h>
#include <SoftwareSerial.h>

#define DISTANCE_CLEAR      80
#define DISTANCE_FAR        60
#define DISTANCE_MIDDLE     40
#define DISTANCE_NEAR       20

#define ezsonarPin 0                 // EZ Sonar A/D channel

#define MOTOR_FULL_SPEED   0x40
#define MOTOR_HALF_SPEED   0x30
#define MOTOR_LOW_SPEED   0x20

#define rxPin 2                      // motor control RX pin
#define txPin 3                      // motor control TX pin
#define rstPin 4                     // motor control reset pin


#define LED_ON      0
#define LED_OFF     1

#define backledPin  5                // LED connected to digital pin 13
#define frontledPin 6                // LED connected to digital pin 13
#define blueled1Pin 7                // LED connected to digital pin 13
#define blueled2Pin 8                // LED connected to digital pin 13

#define BLOWER_ON   1
#define BLOWER_OFF  0

#define blowerPin  10

SoftwareSerial mySerial =  SoftwareSerial(rxPin, txPin);

int IRsensorAddress = 0xB0;
int slaveAddress;
byte data_buf[16];

int Ix[4];
int Iy[4];
int Is[4];

int value[5] = {1, 0, 0, 0, 2};                // previous value of the LED
long previousMillis = 0;        // will store last time LED was updated
long interval = 100;           // interval at which to blink (milliseconds)
int int_cnt = 0;               // interval counter

void Write_2bytes(byte d1, byte d2)
{
    Wire.beginTransmission(slaveAddress);
    Wire.send(d1); Wire.send(d2);
    Wire.endTransmission();
}

void MotorForward(uint8_t motor, uint8_t speed)
{
  mySerial.print(0x80, BYTE);
  mySerial.print(0x00, BYTE);
  mySerial.print((motor<<1)|0x01, BYTE);
  mySerial.print(speed, BYTE);
}

#define Motor0Forward(speed) MotorForward(0, speed)
#define Motor1Forward(speed) MotorForward(1, speed)

void MotorReverse(uint8_t motor, uint8_t speed)
{
  mySerial.print(0x80, BYTE);
  mySerial.print(0x00, BYTE);
  mySerial.print((motor<<1), BYTE);
  mySerial.print(speed, BYTE);
}

#define Motor0Reverse(speed) MotorReverse(0, speed)
#define Motor1Reverse(speed) MotorReverse(1, speed)

#define MotorStop(motor) MotorForward(motor, 0)
#define Motor0Stop() MotorForward(0, 0)
#define Motor1Stop() MotorForward(1, 0)

void MotorMode(uint8_t mode)
{
  mySerial.print(0x80, BYTE);
  mySerial.print(0x02, BYTE);
  mySerial.print(mode, BYTE);
}


void MotorInit(void)
{
  // define pin modes for tx, rx, led pins:
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  pinMode(rstPin, OUTPUT);

  // Reset the motor controller 
  digitalWrite(rstPin, LOW);  
  delay(100);
  digitalWrite(rstPin, HIGH); 
  pinMode(rstPin, INPUT);
  delay(1000);

  // start soft serial communication
  mySerial.begin(9600);
  
  // both motors stop
  Motor0Stop();
  Motor1Stop();
}

#define SPEED_STEPS  4
int act_speedl, act_speedr;
int new_speedl, new_speedr;

void SetMotorControl(int speedl, int speedr)
{
  new_speedl = speedl;
  new_speedr = speedr;  
}

void UpdateMotorControl(void)
{
  if (new_speedl != act_speedl)
  {
    if (new_speedl > act_speedl)
    {
      if (new_speedl - act_speedl > SPEED_STEPS)
        act_speedl += SPEED_STEPS;
      else
        act_speedl = new_speedl;
     }
     else if (act_speedl - new_speedl > SPEED_STEPS)
       act_speedl -= SPEED_STEPS;
     else
       act_speedl = new_speedl;
       
    if (act_speedl >= 0)   
      Motor0Forward(act_speedl);
    else  
      Motor0Reverse(act_speedl);
  }

  if (new_speedr != act_speedr)
  {
    if (new_speedr > act_speedr)
    {
      if (new_speedr - act_speedr > SPEED_STEPS)
        act_speedr += SPEED_STEPS;
      else
        act_speedr = new_speedr;
     }
     else if (act_speedr - new_speedr > SPEED_STEPS)
       act_speedr -= SPEED_STEPS;
     else
       act_speedr = new_speedl;

    if (act_speedr >= 0)   
      Motor0Forward(act_speedr);
    else  
      Motor0Reverse(act_speedr);
  
  }
  
}

void BlowerInit(void)
{
  pinMode(blowerPin, OUTPUT);
  BlowerMode(BLOWER_OFF);
}

void BlowerMode(int mode)
{
  digitalWrite(blowerPin, mode);
}

void PrintEzSonar(int distance)
{
  Serial.print(distance);
  Serial.println(" cm");
}

int ReadEzSonar(void)
{
  int val, inches, cm;

  val = analogRead(ezsonarPin);

  // convert the analog value into a distance
  inches = val/2; 
  cm = inches * 2.54;
  
  PrintEzSonar(cm);
  return cm;
}


void LedInit(void)
{
  pinMode(backledPin, OUTPUT);      // sets the digital pin as output
  pinMode(frontledPin, OUTPUT);      // sets the digital pin as output
  pinMode(blueled1Pin, OUTPUT);      // sets the digital pin as output
  pinMode(blueled2Pin, OUTPUT);      // sets the digital pin as output
  
  digitalWrite(backledPin, LED_OFF);
  digitalWrite(frontledPin, LED_OFF);
  digitalWrite(blueled1Pin, LED_OFF);
  digitalWrite(blueled2Pin, LED_OFF);
  
}

void FlashBlueLEDs(boolean result)
{
  int val;
  if(result)
  {
    if (millis() - previousMillis > interval) 
    {
      previousMillis = millis();   // remember the last time we blinked the LED
      val = value[int_cnt];
      if (int_cnt <5)
        int_cnt++;
      else
        int_cnt = 0;
   
      if (val == 1) 
        digitalWrite(blueled1Pin, LED_ON);
      else if (val == 2)  
        digitalWrite(blueled2Pin, LED_ON);
      else
      {
        digitalWrite(blueled1Pin, LED_OFF);
        digitalWrite(blueled2Pin, LED_OFF);
      }
      
    }
  }
  else
  {
    digitalWrite(blueled1Pin, LED_OFF);
    digitalWrite(blueled2Pin, LED_OFF);
  }
}

void WiiSensorInit(void)
{
    slaveAddress = IRsensorAddress >> 1;   // This results in 0x21 as the address to pass to TWI
    Serial.begin(38400);
    Wire.begin();
    // IR sensor initialize
    Write_2bytes(0x30,0x01); delay(10);
    Write_2bytes(0x30,0x08); delay(10);
    Write_2bytes(0x06,0x90); delay(10);
    Write_2bytes(0x08,0xC0); delay(10);
    Write_2bytes(0x1A,0x40); delay(10);
    Write_2bytes(0x33,0x33); delay(10);
    delay(100);
}

void ReadWiiSensor(void)
{
  int i, j, s;
  
  //IR sensor read
  Wire.beginTransmission(slaveAddress);
  Wire.send(0x36);
  Wire.endTransmission();

  Wire.requestFrom(slaveAddress, 16);        // Request the 2 byte heading (MSB comes first)
  for (i=0;i<16;i++) { data_buf[i]=0; }
  i=0;
  while(Wire.available() && i < 16) { 
      data_buf[i] = Wire.receive();
      i++;
  }
  
  j=0;
  for(i=0; i<4; i++) 
  {
    Ix[i] = data_buf[j+1];
    Iy[i] = data_buf[j+2];
    s   = data_buf[j+3];
    Ix[i] += (s & 0x30) <<4;
    Iy[i] += (s & 0xC0) <<2;
    Is[i] = s & 0x0F;
    j+=3;
  }
}

void PrintWiiSensor(void)
{
  int i;
  
  for(i=0; i<4; i++)
  {
    if (Ix[i] < 1000)
      Serial.print(" ");
    if (Ix[i] < 100)  
      Serial.print(" ");
    if (Ix[i] < 10)  
      Serial.print(" ");
    Serial.print( int(Ix[i]) );
    Serial.print(",");
    if (Iy[i] < 1000)
      Serial.print(" ");
    if (Iy[i] < 100)  
      Serial.print(" ");
    if (Iy[i] < 10)  
      Serial.print(" ");
    Serial.print( int(Iy[i]) );
    Serial.print(",");
    if (Is[i] < 1000)
      Serial.print(" ");
    if (Is[i] < 100)  
      Serial.print(" ");
    if (Is[i] < 10)  
      Serial.print(" ");
    Serial.print( int(Is[i]) );
    if (i<3)
      Serial.print(",");
  }
  Serial.println("");
}

boolean CheckWiiSensor(void)
{
  int i;
  boolean result;
  
  result = false;
  for(i=0; i<4; i++)
  {
    if (Ix[i] < 1023 && Iy[i] <1023)
    {
//      if (Ix[i] >400 && Ix[i] <500) 
      {
      }
      result = true;
    }
  }
  return result;
}

/////////////////////////////////////////////////////////////////////

void setup()
{
  Serial.begin(38400);
  // init motor controller
  MotorInit();
  // init LED pins
  LedInit();
  // init blower motor  
  BlowerInit();
  // Init Wii sensor
  WiiSensorInit();

  Serial.println("Complete-Test V01");
}

/////////////////////////////////////////////////////////////////////

void loop() 
{
  uint8_t speedl, speedr;
  int distance, i, d;
  boolean result;
  
  digitalWrite(frontledPin, LED_ON);
  distance = DISTANCE_FAR+1;
  Motor0Stop();
  Motor1Stop(); 
  d = ReadEzSonar();
//  distance = ReadEzSonar();
  if (distance <= DISTANCE_NEAR)
  {
    digitalWrite(backledPin, LED_ON);
    while (distance <= DISTANCE_MIDDLE)
    {
      Motor0Reverse(MOTOR_FULL_SPEED);
      Motor1Forward(MOTOR_FULL_SPEED);
      delay(500);
      distance = ReadEzSonar();
    }
    
  }
  else if (distance <= DISTANCE_MIDDLE)
  {
    digitalWrite(backledPin, LED_ON);
    Motor0Forward(MOTOR_LOW_SPEED);
    Motor1Forward(MOTOR_LOW_SPEED);
  }
  else if (distance <= DISTANCE_FAR)
  {
    digitalWrite(backledPin, LED_OFF);
    Motor0Forward(MOTOR_HALF_SPEED);
    Motor1Forward(MOTOR_HALF_SPEED);
  }
  else
  {
    digitalWrite(backledPin, LED_OFF);
//    Motor0Forward(MOTOR_FULL_SPEED);
//    Motor1Forward(MOTOR_FULL_SPEED);
  }
  
  ReadWiiSensor();
  PrintWiiSensor();
  result = CheckWiiSensor();
  FlashBlueLEDs(result);
 
/*  
  for(i=0; i<4; i++)
  {
    if (Ix[i] < 1023 && Iy[i] <1023)
    {
//      if (Ix[i] >400 && Ix[i] <500) 
      {
        Motor0Stop();
        Motor1Stop(); 
        BlowerMode(BLOWER_ON);
        delay(2000);
      }
      
    }
  }
  BlowerMode(BLOWER_OFF);
*/  
  delay(40);
  
} 
