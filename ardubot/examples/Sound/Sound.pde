#include <inttypes.h>
#include "DualDCMotor.h"

#define ledPin 13

#define mot1pwmPin  6
#define mot1diraPin 8
#define mot2pwmPin  5
#define mot2diraPin 7

DualDCMotor motor = DualDCMotor(mot1pwmPin, mot1diraPin, mot2pwmPin, mot2diraPin);

#define DIR_FORWARD 1
#define DIR_REVERSE 0

// tone table: thanks to IVO

//T�ne definieren - tiefere T�ne brummen und scheppern
#define G4              392 
#define A4		440
#define B4		466
#define H4		493
#define C5		523
#define DES5	554
#define D5		587
#define ES5		622
#define E5		659
#define F5		698
#define GES5	740
#define G5		783
#define AS5		830
#define A5		880
#define B5		932
#define H5		987
#define C6		1046
#define DES6	1108
#define D6		1174
#define ES6 	1244
#define E6		1318
#define F6		1396
#define GES6	1479
#define G6		1567
#define AS6		1661
#define A6		1760
#define B6		1864
#define H6		1975
#define C7		2093
#define DES7	2217
#define D7		2349
#define ES7		2489
#define E7		2637
#define F7		2793
#define GES7	2959
#define G7		3135
#define AS7		3322
#define A7		3520
#define H7		3729
#define B7		3951

#define QUAD 250
#define HALF 500
#define FULL 1000
#define _OFF 0
#define STOP 0


uint16_t duck_melody[]={
	C5,HALF,
	D5,HALF,
	E5,HALF,
	F5,HALF,
	G5,FULL,
	_OFF,HALF,
	G5,FULL,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	G5,FULL,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	A5,HALF,
	_OFF,HALF,
	G5,FULL,
	_OFF,HALF,

	F5,HALF,
	_OFF,HALF,
	F5,HALF,
	_OFF,HALF,
	F5,HALF,
	_OFF,HALF,
	F5,HALF,
	_OFF,HALF,
	E5,FULL,
	_OFF,HALF,
	E5,FULL,
	_OFF,HALF,

	D5,HALF,
	_OFF,HALF,
	D5,HALF,
	_OFF,HALF,
	D5,HALF,
	_OFF,HALF,
	D5,HALF,
	_OFF,HALF,

	C5,FULL,
	_OFF,HALF,

	STOP,STOP
};

uint16_t unknown_melody[]={
	G5,FULL,
	G5,FULL,
	A5,HALF,
	G5,HALF,
	C6,HALF,
	H5,HALF/2,
	G5,FULL,
	G5,FULL,
	A5,HALF,
	G5,HALF,
	D6,HALF,
	C6,HALF/2,
	G5,FULL,
	G5,FULL,
	G6,HALF,
	E6,HALF,
	C6,FULL,
	C6,FULL,
	H5,HALF,
	A5,HALF,
	F6,FULL,
	F6,FULL,
	E6,HALF,
	C6,HALF,
	D6,HALF,
	C6,HALF/2,

	STOP,STOP
};

uint16_t my_melody[] = {
  C5,HALF, 
  G4,FULL,
  G4,FULL, 
  A4,HALF, 
  G4,HALF,
  0,HALF, 
  B4,HALF, 
  C5,HALF,
  STOP,STOP
}; 

uint16_t schwebung1[]={
	C5,1000,
	C5,1000,
	C5,1000,
	C5,2000,
	STOP,STOP
};
uint16_t schwebung2[]={
	D5,1000,
	E5,1000,
	F5,1000,
	G5,2000,
	STOP,STOP
};

uint16_t frere_jaques[] = {
	D5,HALF,
	E5,HALF,
	F5,HALF,
	D5,HALF,
	D5,HALF,
	E5,HALF,
	F5,HALF,
	D5,HALF,

	F5,HALF,
	G5,HALF,
	A5,FULL,
	F5,HALF,
	G5,HALF,
	A5,FULL,

	A5,QUAD,
	H5,QUAD,
	A5,QUAD,
	G5,QUAD,
	F5,HALF,
	D5,HALF,
	A5,QUAD,
	H5,QUAD,
	A5,QUAD,
	G5,QUAD,
	F5,HALF,
	D5,HALF,

	D5,HALF,
	A4,HALF,
	D5,FULL,
	D5,HALF,
	A4,HALF,
	D5,FULL,
	STOP,STOP
};  


void playMelody(uint16_t *notes)
{
  int index = 0;
  uint16_t note;
  uint16_t dur;
  
  do
  {
    note = notes[index++];
    dur = notes[index++];
    playSound(note, dur, 255);
  } while (dur != 0);

}

void playSound(uint16_t freq, uint16_t duration_msec, uint8_t  amplitude)
{
  uint32_t n,k,period_usec,dauer_usec;

  period_usec = 1000000L / freq;
  dauer_usec = 1000 * (uint32_t) duration_msec;
  k = dauer_usec / period_usec;

  motor.setSpeed (amplitude, amplitude);

  for (n = 0; n < k; n++)
  {
    motor.setDirection (DIR_FORWARD, DIR_FORWARD);
    delayMicroseconds(period_usec/2);
    motor.setDirection (DIR_REVERSE, DIR_REVERSE);
    delayMicroseconds(period_usec/2);
  }
  motor.setSpeed (0, 0);
}

#define BEEP playSound(1000, 100, 255)

void blinkLed(int cnt, int duration)
{
  int i;
  for(i=0; i<cnt; i++)
  {
    digitalWrite(ledPin, HIGH);
    delay(duration/2);
    digitalWrite(ledPin, LOW);
    delay(duration/2);
  }
}

void setup() 
{ 
  pinMode(ledPin, OUTPUT);
  motor.init();
  Serial.begin(38400);
  Serial.println("L293D 3-pin");
  blinkLed(3, 1000);
  BEEP;
  delay(1000);
  BEEP;
  delay(1000);
  BEEP;
  delay(1000);
}


void loop()
{
  // One voice on two notors
  playMelody(frere_jaques);
  delay(2000);
}

