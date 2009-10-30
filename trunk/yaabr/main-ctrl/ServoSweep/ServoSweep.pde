#include <Wire.h>
#include <Servo.h>

#define irPin 0
#define servoPin 9

int irVal[20];
int pos = 0;

Servo myservo;  // create servo object to control a servo  

void setup()
{
  myservo.attach(servoPin);  // attaches the servo on pin 9 to the servo object  
  pos = 90;
  myservo.write(pos);
  
  Serial.begin(38400);    // for Arduino Diecemila
//  Serial.begin(115200);  // for Arduino BT
  Serial.println("YAABR Test V0.1");
  delay(100);
}

void loop()
{
  int ir, i;
  irVal[pos/10] = analogRead(irPin);
  ir = irVal[pos/10];
  Serial.println(ir, DEC);
  if (ir > 400)
  {
    Serial.println("stop");
  }
  else if (ir > 200)
  {
    Serial.println("slow");
  }
  else
  {
    Serial.println("go on");
  }

  for(pos=90; pos <=160; pos+=10)
  {
    irVal[pos/10] = analogRead(irPin);
    myservo.write(pos);
    delay(100);
  }
  for(pos=160; pos >=90; pos-=10)
  {
    irVal[pos/10] = analogRead(irPin);
    myservo.write(pos);
    delay(100);
  }
  delay(1000);
  for(pos=90; pos >=20; pos-=10)
  {
    irVal[pos/10] = analogRead(irPin);
    myservo.write(pos);
    delay(100);
  }
  for(pos=20; pos <=90; pos+=10)
  {
    irVal[pos/10] = analogRead(irPin);
    myservo.write(pos);
    delay(100);
  }
  delay(1000);
  for(i=0; i<20; i++)
  {
    Serial.print(irVal[i], HEX);
    Serial.print(" ");  
  }
  Serial.println(" ");
  delay(40);
}

void IRServoSweep(int from, int to, int steps, int del)
{
  if (steps && del && (to > from))
  {
    for(pos=from; pos <=to; pos+=steps)
    {
      irVal[pos/10] = analogRead(irPin);
      myservo.write(pos);
      delay(del);
    }
    for(pos=to; pos >=from; pos-=steps)
    {
      irVal[pos/10] = analogRead(irPin);
      myservo.write(pos);
      delay(del);
    }
