/****************************************************************************/
/*!
  \file     ultrasonic.c

  \brief    Funktionen zur Ultraschall Erweiterung.

  \version  V001 - 10.01.2005 - Robin Gruber - DLR\n
            Original Version aus dem Asuro Buch Band 1\n
  \version  V002 - 28.07.2005 - Lutz Mager (luma)\n
            Umbau auf InitUltrasonics, RestoreAsuro und Chirp Funktion  
  \version  V003 - 17.04.2006 - RN-User dopez/madman2k\n
            Patch Version fuer neue Asuro Lib
  \version  V004 - 22.01.2008 - m.a.r.v.i.n\n
            Magic Numbers ersetzt durch IO Register Defines\n
            Anpassung an ATmega168

*****************************************************************************/
/*****************************************************************************
*                                                                            *
*   This program is free software; you can redistribute it and/or modify     *
*   it under the terms of the GNU General Public License as published by     *
*   the Free Software Foundation; either version 2 of the License, or        *
*   any later version.                                                       *
*                                                                            *
*****************************************************************************/

#include <avr/io.h>
#include <avr/interrupt.h>
#include "ultrasonic.h"

volatile unsigned char count36kHz;
volatile unsigned long timebase;

#if defined(__AVR_ATmega8__)
#else
#define OCR2 OCR2A
#endif

SIGNAL (SIG_OVERFLOW2)
{
  TCNT2 += 0x25;
  count36kHz ++;
  if (!count36kHz)
    timebase ++;
}


/**
 * being used insted TIMER2_OVF_vect during ultrasonic polling
 */
#if defined(__AVR_ATmega8__)
SIGNAL(SIG_OUTPUT_COMPARE2)
#else
SIGNAL(SIG_OUTPUT_COMPARE2A) 
#endif
{
  count36kHz++;
  if (!count36kHz)
    timebase ++;
}

void Sleep (
  unsigned char time36kHz)
{
  unsigned char ziel = (time36kHz + count36kHz) & 0x00FF;

  while (count36kHz != ziel)
    ;
}

/**
 * initialises the Ultrasonic module
 * this function is automaticly called by Chirp
 */
void InitUltrasonics(void)
{
	// Change Oscillator-frequency of Timer 2
	// to 40kHz, no toggling of IO-pin:
  cli();
#if defined(__AVR_ATmega8__)
  TCCR2  = _BV(WGM21) | _BV(CS20);
  TIMSK &= ~_BV(TOIE2); 
  TIMSK |= _BV(OCIE2);       // OCIE2:  Timer/Counter2 Output Compare Match Interrupt Enable
#else
  // fast PWM, set OC2A on compare match, clear OC2A at bottom, clk/1
  TCCR2A = _BV(WGM21);
  TCCR2B = _BV(CS20);
  // interrupt on timer overflow
  TIMSK2 &= ~_BV(TOIE2); 
  TIMSK2 |= _BV(OCIE2A); 
#endif	
  // 40kHz carrier/timer
  OCR2 = 100;

  ADCSRA = 0;      // deactivate ADC
  ACSR  |= _BV(ACIS1);       // Comparator Interrupt on Falling Output Edge

  ADMUX  = 0x03;             // connect ADC3-input with comparator
#if defined(__AVR_ATmega8__)
  SFIOR |= _BV(ACME);        // connect ADC multiplexer to comparator
#else
  ADCSRB |= _BV(ACME);        // connect ADC multiplexer to comparator
#endif
  DDRD  &= ~_BV(6);          // use Port D Pin 6 as input (AIN0)
  DDRB |= _BV(3);            // use Port B Pin3 as output (OC2A)
  sei();
}

/**
 * restores the hardware after using the Ultrasonic module
 * this function is called automaticly after a Chirp
 */
void RestoreAsuro(void)
{
  cli();
#if defined(__AVR_ATmega8__)
  TCCR2 = _BV(WGM20) | _BV(WGM21) | _BV(COM20) | _BV(COM21) | _BV(CS20);
  TIMSK &= ~_BV(OCIE2);     // OCIE2:  Timer/Counter2 Output Compare Match Interrupt Disable
  // interrupt on timer overflow
  TIMSK |= _BV(TOIE2); 
#else
  // fast PWM, set OC2A on compare match, clear OC2A at bottom, clk/1
  TCCR2A = _BV(WGM20) | _BV(WGM21) | _BV(COM2A0) | _BV(COM2A1);
  TCCR2B = _BV(CS20);
  TIMSK2 &= ~_BV(OCIE2A); 
  // interrupt on timer overflow
  TIMSK2 |= _BV(TOIE2); 
#endif
  // 36kHz carrier/timer
  OCR2  = 0x91;              // duty cycle for 36kHz

  ADCSRA = _BV(ADEN) | _BV(ADPS2) | _BV(ADPS1); // clk/64
  ACSR  &= ~_BV(ACIS1);

  sei();
  Sleep(1);
}

/**
 * @return distance in cm
 */
int Chirp(void)
{
  unsigned int sleeptime = 0, dist = 0;

//  InitUltrasonics();

  // chirpen:
  count36kHz = 0;

  while(count36kHz != 20) {
    OCR2 = 100 + 20 / 2 - count36kHz;
  }

#if defined(__AVR_ATmega8__)
  TCCR2	= _BV(WGM21) | _BV(CS20);
#else
  TCCR2A = _BV(WGM21);
  TCCR2B = _BV(CS20);
#endif
  OCR2	= 100;

  // analyse echoes:
  while(1) {
    Sleep(1);
    sleeptime++;
    if((ACSR & _BV(ACI))) {
      dist = (unsigned int) ((long) ((344L * ((sleeptime * 1000L) / 72L) / 10000L) / 2L));
      ACSR |= _BV(ACI);
      break;
    }
		
    ACSR |= _BV(ACI);
		
    if(sleeptime > 3500) {
      RestoreAsuro();
      return -1;
    }
  }
	
//  RestoreAsuro();
  return dist;
}

