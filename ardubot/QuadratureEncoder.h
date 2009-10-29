/*
 * QuadratureEncoder.h - Library for quadrature encoders
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 *
 * Credits:
 * based on code from Peter Dannegger
 * http://www.mikrocontroller.net/articles/Drehgeber
 */


#ifndef Encoder_h
#define Encoder_h 

extern "C" { 
#include <avr/io.h>
#include <inttypes.h>
}

class QuadratureEncoder {

public:  
  int32_t encLt, encRt;

  QuadratureEncoder(uint8_t encLtA, uint8_t encRtA, uint8_t encLtB, uint8_t encRtB);   //constructor

  void init( void );
  
  void reset( void );
  
  int8_t readLt( void );
  
  int8_t readRt( void );

};

#endif

