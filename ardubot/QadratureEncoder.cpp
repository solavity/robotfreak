/*
 * QuadratureEncoder.cpp - Library for quadrature encoders
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 *
 * Credits:
 * based on code from Peter Dannegger
 * http://www.mikrocontroller.net/articles/Drehgeber
 */
 

#include "WConstants.h" 
#include "QuadratureEncoder.h"

extern "C" { 
#include <avr/io.h>
#include <avr/interrupt.h>
#include <inttypes.h>
}

#define LT_PHASE_A		digitalRead(_encLtA)
#define LT_PHASE_B		digitalRead(_encLtB)
#define RT_PHASE_A		digitalRead(_encRtA)
#define RT_PHASE_B		digitalRead(_encRtB)

static volatile int8_t encDeltaLt, encDeltaRt;
static int8_t lastLt, lastRt;
static uint8_t _encLtA, _encRtA, _encLtB, _encRtB;


ISR( TIMER2_COMPA_vect )
{
  int8_t val, diff;

//  digitalWrite(ledPin, HIGH);
  val = 0;
  if( LT_PHASE_A )
    val = 3;
  if( LT_PHASE_B )
    val ^= 1;					// convert gray to binary
  diff = lastLt - val;				// difference last - new
  if( diff & 1 ){				// bit 0 = value (1)
    lastLt = val;				// store new as next last
    encDeltaLt += (diff & 2) - 1;		// bit 1 = direction (+/-)
  }

  val = 0;
  if( RT_PHASE_A )
    val = 3;
  if( RT_PHASE_B )
    val ^= 1;					// convert gray to binary
  diff = lastRt - val;				// difference last - new
  if( diff & 1 ){				// bit 0 = value (1)
    lastRt = val;				// store new as next last
    encDeltaRt += (diff & 2) - 1;		// bit 1 = direction (+/-)
  }
//  digitalWrite(ledPin, LOW);
}

QuadratureEncoder::QuadratureEncoder(uint8_t encLtA, uint8_t encLtB, uint8_t encRtA, uint8_t encRtB)
{
  _encLtA = encLtA; 
  _encLtB = encLtB;
  _encRtA = encRtA;
  _encRtB = encRtB; 

}	

void QuadratureEncoder::init(void)
{
  int8_t val;

  cli();
  TIMSK2 |= (1<<OCIE2A);
  sei();
  pinMode(_encLtA, INPUT);
  pinMode(_encRtA, INPUT);
  pinMode(_encLtB, INPUT);
  pinMode(_encRtB, INPUT);

  val=0;
  if (LT_PHASE_A)
    val = 3;
  if (LT_PHASE_B)
    val ^= 1;
  lastLt = val;
  encDeltaLt = 0;

  val=0;
  if (RT_PHASE_A)
    val = 3;
  if (RT_PHASE_B)
    val ^= 1;
  lastRt = val;
  encDeltaRt = 0;

  encLt = 0;
  encRt = 0;
}

void QuadratureEncoder::reset( void )
{
  encLt = 0;
  encRt = 0;
}

int8_t QuadratureEncoder::readLt( void )			// read single step encoders
{
  int8_t val;

  cli();
  val = encDeltaLt;
  encDeltaLt = 0;
  sei();
  return val;					// counts since last call
}

int8_t QuadratureEncoder::readRt( void )			// read single step encoders
{
  int8_t val;

  cli();
  val = encDeltaRt;
  encDeltaRt = 0;
  sei();
  return val;					// counts since last call
}

