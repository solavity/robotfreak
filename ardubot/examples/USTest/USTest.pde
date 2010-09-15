/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   any later version.                                                    *
 ***************************************************************************/

#include "ultrasonic.h"
#include "DualDCMotor.h"


#define mot1pwmPin  9
#define mot1diraPin 8
#define mot2pwmPin  10
#define mot2diraPin 12

// ultrasonic sensor uses digital pins 6, 11 and analog pin 3 (could only be changed directly in code)
#define usAnPin   3
#define usAin0Pin 6
#define usPwmPin  11

#define HALF_SPEED  80
#define FULL_SPEED 160

DualDCMotor motor = DualDCMotor(mot1pwmPin, mot1diraPin, mot2pwmPin, mot2diraPin);

void setup(void)
{
  Serial.begin(9600);
  Serial.println("Ultrasonic test");
  motor.init();
  pinMode(usPwmPin, OUTPUT);
  pinMode(usAin0Pin, INPUT);
  InitUltrasonics();
  Serial.println("Init complete");
  delay(1000);
}

void loop(void) 
{
  int distance, distance2;

  distance = Chirp();
  Serial.print("distance: ");
  Serial.println(distance, DEC);
  if (distance >= 0)
  {
    if (distance <20)
    {
      motor.setSpeed (0, 0);
      
      delay(1000);
      motor.setDirection(0,0);
      motor.setSpeed(HALF_SPEED, HALF_SPEED);
      delay(1000);
      distance2=0;
      while (distance2 >= 0)
      {
        motor.setDirection(0,1);
        motor.setSpeed(HALF_SPEED, HALF_SPEED);
        delay(1000);
        distance2 = Chirp();
      }
    }
    else if (distance <30)
    {
      motor.setDirection(1,1);
      motor.setSpeed(HALF_SPEED, HALF_SPEED);
    }
  } 
  else
  {
    motor.setDirection(1,1);
    motor.setSpeed(FULL_SPEED, FULL_SPEED);
  }
  delay(100);
} 


