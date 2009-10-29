
#include <avr/io.h>
#include <avr/interrupt.h>

#include <Wire.h>
#include <inttypes.h>

// quadrature encoder

#include "QuadratureEncoder.h"

#define encLtA 3
#define encLtB 2
#define encRtA 11
#define encRtB 12

QuadratureEncoder encoder = QuadratureEncoder(encLtA, encLtB, encRtA, encRtB);

int32_t encLt, encRt;

// dual DC motor

#include "DualDCMotor.h"


#define mot1pwmPin  6
#define mot1diraPin 8
#define mot2pwmPin  5
#define mot2diraPin 7

DualDCMotor motor = DualDCMotor(mot1pwmPin, mot1diraPin, mot2pwmPin, mot2diraPin);

int actSpeedLt, actSpeedRt; 

// pcf8574 display

uint8_t rows = 3;
uint8_t cols = 16;

#include "LCDi2cDogm.h"                    
LCDi2cDogm lcd = LCDi2cDogm(rows,cols,0x27);             


void Circle(int degree, int spd)
{
  int  enc_count, enc;
  int  tot_count = 0;

  motor.setSpeed (0, 0);
  if (degree != 0)
  {
    enc_count = abs(degree);
    if (degree < 0)
    {
      motor.setDirection (1, 1);
      motor.setSpeed (0, spd);
    }
    else
    {
      motor.setDirection (1, 1);
      motor.setSpeed (spd, 0);
    }
    
  }  

  while (tot_count < enc_count)
  {
    if (degree < 0)
    {
      enc = encoder.readRt();
    }
    else
    {
      enc = encoder.readLt();
    }
    tot_count += abs(enc);
    delay(5);
  }
  motor.setSpeed (0, 0);
  lcd.setCursor(0,2);
  lcd.print(tot_count, DEC);
  lcd.print(" ");
  lcd.print(enc_count, DEC);
  lcd.print(" ");
  lcd.print(spd, DEC);
  lcd.print(" ");
  delay(500);
}


void GoTurn ( int distance, int degree, int spd)
{
  int  enc_count;
  int  tot_count = 0;
  int  diff = 0;
  int  l_speed = spd, r_speed = spd;
  int  enc_lt, enc_rt, enc;

  /* stop the motors until the direction is set */
  motor.setSpeed (0, 0);

  /* if distance is NOT zero, then take this value to go ... */
  if (distance != 0)
  {
    /* calculate tics from mm */
    //    enc_count  = abs (distance) * 10000L;
    //    enc_count /= MY_GO_ENC_COUNT_VALUE;
    enc_count = abs(distance);

    if (distance < 0)
      motor.setDirection (0, 0);
    else
      motor.setDirection (1, 1);
  }
  /* ... else take the value degree for a turn */
  else
  {
    /*  calculate tics from degree */
    //    enc_count  = abs (degree) * MY_TURN_ENC_COUNT_VALUE;
    //    enc_count /= 360L;
    enc_count = abs(degree);

    if (degree < 0)
      motor.setDirection (1, 0);
    else
      motor.setDirection (0, 1);
  }

  /* reset encoder */
  enc_lt = 0;
  enc_rt = 0;
  encLt = 0;
  encRt = 0;

  /* now start the machine */
  motor.setSpeed (l_speed, r_speed);

  while (tot_count < enc_count)
  {
    enc = encoder.readLt();
    enc_lt += abs(enc);			// read a single step encoder
    encLt += enc_lt;
    enc = encoder.readRt();
    enc_rt += abs(enc);			// read a single step encoder
    encRt += enc_rt;
    tot_count += enc_lt;
    diff = enc_lt - enc_rt;

    if (diff > 0)
    { /* Left faster than right */
      if ((l_speed > spd) || (r_speed > 244))
        l_speed -= abs(diff);
      else
        r_speed += abs(diff);
    }

    if (diff < 0)
    { /* Right faster than left */
      if ((r_speed > spd) || (l_speed > 244))
        r_speed -= abs(diff);
      else
        l_speed += abs(diff);
    }

    /* reset encoder */
    enc_lt = 0;
    enc_rt = 0;

    motor.setSpeed (l_speed, r_speed);
    delay(5);
  }
  motor.setSpeed (0, 0);
  lcd.setCursor(0,0);
  lcd.print(tot_count, DEC);
  lcd.print(" ");
  lcd.print(enc_count, DEC);
  lcd.print(" ");
  lcd.print(encLt, DEC);
  lcd.print(" ");
  lcd.print(encRt, DEC);
  lcd.print(" ");
  lcd.setCursor(0,1);
  lcd.print(spd, DEC);
  lcd.print(" ");
  lcd.print(l_speed, DEC);
  lcd.print(" ");
  lcd.print(r_speed, DEC);
  lcd.print(" ");
  delay(500);
}


#define DRAW_DIST  100
#define DRAW_SPEED 150
#define DRAW_90GRD 28
#define DRAW_45GRD 14
#define DRAW_135GRD 42


void drawLMR(void)
{
  //draw L
  GoTurn(DRAW_DIST,0,   DRAW_SPEED);
  GoTurn(0,-DRAW_90GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST/2,0, DRAW_SPEED);
  delay(1000);
  //draw M
  GoTurn(0,-DRAW_90GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST,0,   DRAW_SPEED);
  GoTurn(0,DRAW_135GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST/2,0, DRAW_SPEED);
  GoTurn(0,-DRAW_90GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST/2,0, DRAW_SPEED);
  GoTurn(0,DRAW_135GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST,0,   DRAW_SPEED);
  GoTurn(0,-DRAW_90GRD, DRAW_SPEED);
  delay(1000);
  //draw R
  GoTurn(DRAW_DIST/2,0, DRAW_SPEED);
  GoTurn(0,-DRAW_90GRD, DRAW_SPEED);
  GoTurn(DRAW_DIST,0,   DRAW_SPEED);
  GoTurn(0,DRAW_90GRD,  DRAW_SPEED);
  Circle(114,           DRAW_SPEED);
//  GoTurn(DRAW_DIST/4,0, DRAW_SPEED);
//  GoTurn(0,DRAW_45GRD,  DRAW_SPEED);
//  GoTurn(DRAW_DIST/4,0, DRAW_SPEED);
//  GoTurn(0,DRAW_45GRD,  DRAW_SPEED);
//  GoTurn(DRAW_DIST/4,0, DRAW_SPEED);
//  GoTurn(0,DRAW_45GRD,  DRAW_SPEED);
//  GoTurn(DRAW_DIST/4,0, DRAW_SPEED);
//  GoTurn(0,DRAW_45GRD,  DRAW_SPEED);
//  GoTurn(DRAW_DIST/4,0, DRAW_SPEED);
  GoTurn(0,-DRAW_135GRD,DRAW_SPEED);
  GoTurn(DRAW_DIST,0, DRAW_SPEED);
  delay(1000);
}



void setup() 
{ 
  int i;

  encoder.init();
  motor.init();
  Serial.begin(38400);
  lcd.init();                          // Init the display, clears the display

  lcd.setBacklight(0); 
  lcd.print(" Write LMR V03 ");       // Classic Hello World!
  Serial.println("Write LMR V03");
  delay(4000);
  lcd.setBacklight(1); 
}

int time = 0;

void loop()
{
  drawLMR();
  delay(4000);
}

