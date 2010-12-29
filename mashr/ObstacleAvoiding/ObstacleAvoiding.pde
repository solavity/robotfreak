/*
	DistanceBySoftwareSerial.pde - URM 37 Control Library Version 2.0.0
 	Author: Miles Burton, miles@mnetcs.com
 	Copyright (c) 2009 Miles Burton All Rights Reserved
 
 	This library is free software; you can redistribute it and/or
 	modify it under the terms of the GNU Lesser General Public
 	License as published by the Free Software Foundation; either
 	version 2.1 of the License, or (at your option) any later version.
 
 	This library is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 	Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public
 	License along with this library; if not, write to the Free Software
 	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 
 */
#include <inttypes.h> 
#include "URMSerial.h"


// The measurement we're taking
#define DISTANCE 1
#define TEMPERATURE 2
#define ERROR 3
#define NOTREADY 4
#define TIMEOUT 5

int PWM1=5; //M1 Speed Control
int PWM2=6; //M2 Speed Control
int DIR1=4; //M1 Direction Control
int DIR2=7; //M1 Direction Control

static int last_speed1, last_speed2;
int speed_l;
int speed_r;
int dly;
int distance;

void motorInit(void)
{
  pinMode(DIR1, OUTPUT);
  pinMode(DIR2, OUTPUT);
}

void motorMove(int speed1, int speed2)
{

  last_speed1 = speed1;
  last_speed2 = speed2;

  if (speed1 < 0)
  {
    digitalWrite(DIR1, LOW);
    analogWrite(PWM1, abs(speed1));
  }
  else
  {
    digitalWrite(DIR1, HIGH);
    analogWrite(PWM1, abs(speed1));
  }
  
  if (speed2 < 0)
  {
    digitalWrite(DIR2, LOW);
    analogWrite(PWM2, abs(speed2));
  }
  else
  {
    digitalWrite(DIR2, HIGH);
    analogWrite(PWM2, abs(speed2));
  }
  
}

void motorMoveRamp(int speed1, int speed2, int dly)
{
  int temp_speed1 = last_speed1;
  int temp_speed2 = last_speed2;

  Serial.print("MoveRamp: ");
  Serial.print(speed1, DEC);
  Serial.print(", ");
  Serial.print(speed2, DEC);
  Serial.print(", ");
  Serial.println(dly, DEC);
  while ((speed1 != temp_speed1) ||
         (speed2 != temp_speed2))
   {
      if (temp_speed1 > speed1)
        temp_speed1--;
      else if (temp_speed1 < speed1)
        temp_speed1++;

      if (temp_speed2 > speed2)
        temp_speed2--;
      else if (temp_speed2 < speed2)
        temp_speed2++;
      motorMove(temp_speed1, temp_speed2);
      delay(dly);
   }
}

URMSerial urm;

void setup() {

  Serial.begin(38400);                 // Sets the baud rate to 38400
  motorInit();
  speed_l = 0;
  speed_r = 0;
  urm.begin(2,3,9600);                 // RX Pin, TX Pin, Baud Rate
  Serial.println("MASHR obstacle Avoiding Version 1.0");   
}

  
void loop()
{

  distance = getDistance();  // get distance
  Serial.print("Distance: ");
  Serial.println(distance, DEC);
  if (distance > 0)
  {
  if (distance < 20)
  {
    motorMove(0,0);
    speed_l = -150;
    speed_r = 150;
    dly = 5;
    do {
      distance = getDistance();  // get distance
      motorMove(speed_l, speed_r);
//      motorMoveRamp(speed_l, speed_r, dly);
  } while(distance < 40);  
  }
  else if (distance < 30)
  {
    speed_l = 150;
    speed_r = 150;
  }
  else
  {
    speed_l = 200;
    speed_r = 200;
  }
  dly = 10;
  motorMove(speed_l, speed_r);
//  motorMoveRamp(speed_l, speed_r, dly);
  }
  delay(40);
}


int value; // This value will be populated
int getDistance()
{
  
  // Request a distance reading from the URM37
  switch(urm.requestMeasurementOrTimeout(DISTANCE, value)) // Find out the type of request
  {
  case DISTANCE: // Double check the reading we receive is of DISTANCE type
    //    Serial.println(value); // Fetch the distance in centimeters from the URM37
    return value;
    break;
  case TEMPERATURE:
    return value;
    break;
  case ERROR:
    Serial.println("Error");
    break;
  case NOTREADY:
    Serial.println("Not Ready");
    break;
  case TIMEOUT:
    Serial.println("Timeout");
    break;
  } 

  return -1;
}







