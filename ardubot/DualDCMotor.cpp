/*
 * DualDCMotor.cpp - Library for robotics motor control
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 */


#include "WConstants.h" 
#include "DualDCMotor.h"

extern "C" {  
#include <avr/io.h>
#include <inttypes.h>
}

static uint8_t _mot1pwmPin, _mot1diraPin, _mot2pwmPin, _mot2diraPin;

DualDCMotor::DualDCMotor(uint8_t mot1pwmPin, uint8_t mot1diraPin, uint8_t mot2pwmPin, uint8_t mot2diraPin)
{
  _mot1pwmPin = mot1pwmPin;
  _mot1diraPin = mot1diraPin;
  _mot2pwmPin = mot2pwmPin;
  _mot2diraPin = mot2diraPin;
}

void DualDCMotor::init(void)
{
  pinMode(_mot1diraPin, OUTPUT);
  pinMode(_mot2diraPin, OUTPUT);
  analogWrite(_mot1pwmPin, 0);
  digitalWrite(_mot1diraPin, 0);
  analogWrite(_mot2pwmPin, 0);
  digitalWrite(_mot2diraPin, 0);
}

void DualDCMotor::setSpeed(int speedLt, int speedRt)
{
  analogWrite(_mot1pwmPin, speedLt);
  analogWrite(_mot2pwmPin, speedRt);   
}

void DualDCMotor::setDirection(int dirLt, int dirRt)
{
  digitalWrite(_mot1diraPin, dirLt);
  digitalWrite(_mot2diraPin, dirRt);   
}

    

