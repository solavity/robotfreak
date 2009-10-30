#include <SoftwareSerial.h>

#define rxPin 2
#define txPin 3
#define rstPin 4

#define MAX_SPEED   0x40

SoftwareSerial mySerial =  SoftwareSerial(rxPin, txPin);

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



void setup()
{
  // define pin modes for tx, rx, led pins:
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  pinMode(rstPin, OUTPUT);
  
  mySerial.begin(9600);
  Serial.begin(38400);
  // reset motor controller
  digitalWrite(rstPin, LOW);  
  delay(100);
  digitalWrite(rstPin, HIGH); 
  pinMode(rstPin, INPUT);
  delay(1000);
//  MotorMode(2);
  delay(1000);

  
  Motor0Stop();
  Motor1Stop();
  
  Serial.println("Motor-Test V01");
}

void loop() 
{
  uint8_t speedl, speedr;
/*  
  speedl=0x40;
  speedr=0x40;
  Motor0Forward(speedl);
  Motor1Forward(speedl);
  delay(2000);
  Motor0Stop();
  Motor1Stop();
  delay(2000);
  Motor0Reverse(speedl);
  Motor1Reverse(speedl);
  delay(2000);
  Motor0Stop();
  Motor1Stop();
  delay(2000);
  Motor0Forward(speedl);
  Motor1Reverse(speedl);
  delay(2000);
  Motor0Stop();
  Motor1Stop();
  delay(2000);
  Motor0Reverse(speedl);
  Motor1Forward(speedl);
  delay(2000);
  Motor0Stop();
  Motor1Stop();
  delay(2000);
*/  
  
  
  for(speedl=0; speedl<MAX_SPEED; speedl++) 
  {
    Motor0Forward(speedl);
    Motor1Forward(speedl);
    delay(100);
  }
  Serial.print("Forward: ");
  Serial.println(speedl, DEC);
  delay(1000);
  for(speedl=MAX_SPEED; speedl>0; speedl--) 
  {
    Motor0Forward(speedl);
    Motor1Forward(speedl);
    delay(100);
  }
  Serial.print("Forward: ");
  Serial.println(speedl, DEC);
  delay(1000);
  
  for(speedr=0; speedr<MAX_SPEED; speedr++) 
  {
    Motor0Reverse(speedr);
    Motor1Reverse(speedr);
    delay(100);
  }
  Serial.print("Reverse: ");
  Serial.println(speedr, DEC);
  delay(1000);
  for(speedr=MAX_SPEED; speedr>0; speedr--  ) 
  {
    Motor0Reverse(speedr);
    Motor1Reverse(speedr);
    delay(100);
  }
  Serial.print("Reverse: ");
  Serial.println(speedr, DEC);
  delay(1000);
} 
