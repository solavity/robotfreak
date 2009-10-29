/*
 * DualDCMotor.h - Library for robotics motor control
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 */

 
#ifndef _Motor_h
#define _Motor_h

extern "C" {  
#include <avr/io.h>
#include <avr/interrupt.h>
#include <inttypes.h>
}

class DualDCMotor {

public:
  DualDCMotor(uint8_t mot1pwmPin, uint8_t mot1diraPin, uint8_t mot2pwmPin, uint8_t mot2diraPin);
  void init(void);
 
  void setSpeed(int speedLt, int speedRt);

  void setDirection(int dirLt, int dirRt);

};

#endif    

