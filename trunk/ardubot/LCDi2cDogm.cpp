/*
 * LCDi2cDogm.cpp - Library for DOGM LCD modules   
 * using a PFC8574 I2C port expander
 * see http://www.lcd-module/dogm
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 *
 * Credits:
 * Based on the Original Arduino CrystalLibrary
 * and the LCD API from dale@wentztech.com 
 */


#include <Wire.h>
#include "LCDi2cDogm.h"
extern "C" {
#include <string.h>			//needed for strlen()
#include <inttypes.h>
#include "WConstants.h"		//all things wiring / arduino
}  

// commands
#define LCD_CLEARDISPLAY 0x01
#define LCD_RETURNHOME 0x02
#define LCD_ENTRYMODESET 0x04
#define LCD_DISPLAYCONTROL 0x08
#define LCD_CURSORSHIFT 0x10
#define LCD_FUNCTIONSET 0x20
#define LCD_SETCGRAMADDR 0x40
#define LCD_SETDDRAMADDR 0x80

// flags for display entry mode
#define LCD_ENTRYRIGHT 0x00
#define LCD_ENTRYLEFT 0x02
#define LCD_ENTRYSHIFTINCREMENT 0x01
#define LCD_ENTRYSHIFTDECREMENT 0x00

// flags for display on/off control
#define LCD_DISPLAYON 0x04
#define LCD_DISPLAYOFF 0x00
#define LCD_CURSORON 0x02
#define LCD_CURSOROFF 0x00
#define LCD_BLINKON 0x01
#define LCD_BLINKOFF 0x00

// flags for display/cursor shift
#define LCD_DISPLAYMOVE 0x08
#define LCD_CURSORMOVE 0x00
#define LCD_MOVERIGHT 0x04
#define LCD_MOVELEFT 0x00

// flags for function set
#define LCD_8BITMODE 0x10
#define LCD_4BITMODE 0x00
#define LCD_2LINE 0x08
#define LCD_1LINE 0x00
#define LCD_5x10DOTS 0x04
#define LCD_5x7DOTS 0x00 

//defines for PCF8574 port pins
#define EN_PIN  (1<<7)
#define BL_PIN  (1<<6)
#define RW_PIN  (1<<5)
#define RS_PIN  (1<<4)

#define _LCDEXPANDED				// If defined turn on advanced functions

//const uint8_t lcd_init[] = {(LCD_4BITMODE | LCD_2LINE | LCD_5x7DOTS), 0};
const uint8_t lcd_init[] = {0x29, 0x1C, 0x52, 0x69, 0x74, 0x28, 0x0F, 0};             // DOGM162 Init, See Datasheet

// Global Vars 

uint8_t g_num_lines = 2;
uint8_t g_num_col = 16;
uint8_t g_addr = 0x20;
uint8_t g_ctrl = 0;

int g_cmdDelay = 0;
int g_charDelay = 0;

uint8_t _displayfunction;
uint8_t _displaycontrol;
uint8_t _displaymode;



// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Class Constructor 
// []
// []	num_lines = 1-4
// []   num_col = 1-80
// []  
// []   
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]




LCDi2cDogm::LCDi2cDogm (uint8_t num_lines,uint8_t num_col,uint8_t addr)
{

  g_num_lines = num_lines;
  g_num_col = num_col;
  g_addr = addr;

}

// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	initiatize lcd after a short pause
// []
// []	
// []   Put the display in some kind of known mode
// []   Put the cursor at 0,0
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::init () 
{
  int i;
  Wire.begin();
  writePCF8574(0);
  delay(40);
  //The first 4 nibbles and timings are not in my DEM16217 SYH datasheet, but apparently are HD44780 standard...
  commandNibble(0x03);
  delayMicroseconds(2000);
  commandNibble(0x03);
  delayMicroseconds(30);
  commandNibble(0x03);
  delayMicroseconds(30);

  // needed by the LCDs controller
  //this being 2 sets up 4-bit mode.
  commandNibble(0x02);
  delayMicroseconds(30);
//  commandNibble(0x02);
//  delayMicroseconds(30);
  //todo: make configurable by the user of this library.
  //NFXX where
  //N = num lines (0=1 line or 1=2 lines).
  //F= format (number of dots (0=5x7 or 1=5x10)).
  //X=don't care

  int num_lines_ptn = g_num_lines - 1 << 3;
  int dot_format_ptn = 0x00;      //5x7 dots.  0x04 is 5x10

//    commandNibble(num_lines_ptn | dot_format_ptn);
  i = 0;
  while(lcd_init[i] != 0)
  {
    command(lcd_init[i]);
    delayMicroseconds(30);
    i++;
  }
  delayMicroseconds(60);

  //The rest of the init is not specific to 4-bit mode.
  //NOTE: we're writing full bytes now, not nibbles.

  // display control:
  // turn display on, cursor off, no blinking
  // turn the display on with no cursor or blinking default
  _displaycontrol = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;  
  display();

  // clear it off
  clear();

  // Initialize to default text direction (for romance languages)
  _displaymode = LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
  // set the entry mode
  command(LCD_ENTRYMODESET | _displaymode);

}

// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Over ride the default delays used to send commands to the display
// []
// []	The default values are set by the library
// []   this allows the programer to take into account code delays
// []   and speed things up.
// []   
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::setDelay (int cmdDelay,int charDelay) 
{
  g_cmdDelay = cmdDelay;
  g_charDelay = charDelay;

}

void LCDi2cDogm::writePCF8574(uint8_t value)
{
  Wire.beginTransmission(g_addr);
  Wire.send(value);
  Wire.endTransmission();
}  

//push a nibble of data through the the LCD's DB4~7 pins, clocking with the Enable pin.
//We don't care what RS and RW are, here.
void LCDi2cDogm::pushNibble(uint8_t value)
{
  value |= g_ctrl;
  Wire.beginTransmission(g_addr);
  Wire.send(value);
  Wire.send(value | EN_PIN);
  Wire.send(value);
  Wire.endTransmission();
  delayMicroseconds(100);
}

//push a byte of data through the LCD's DB4~7 pins, in two steps, clocking each with the enable pin.
void LCDi2cDogm::pushByte(uint8_t value)
{
  uint8_t val_lower = value & 0x0F;
  uint8_t val_upper = value >> 4;
  pushNibble(val_upper);
  pushNibble(val_lower);
}

void LCDi2cDogm::commandNibble(uint8_t nibble) 
{
  g_ctrl &= ~RS_PIN;
  g_ctrl &= ~RW_PIN;
  pushNibble(nibble);
}
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []   Send a command to the display. 
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::command(uint8_t value) 
{
  g_ctrl &= ~RS_PIN;
  g_ctrl &= ~RW_PIN;
  pushByte(value);
  delay(g_cmdDelay);
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []   Send a command to the display. 
// []
// []	This is also used by the print, and println
// []	
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]



void LCDi2cDogm::write(uint8_t value) 
{
  g_ctrl |= RS_PIN;
  g_ctrl &= ~RW_PIN;
  pushByte(value);
  delay(g_charDelay);

}



// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Clear the display, and put cursor at 0,0 
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::clear()
{
  command(LCD_CLEARDISPLAY);  // clear display, set cursor position to zero
  delayMicroseconds(2000);  // this command takes a long time! 
}

// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Home to custor to 0,0
// []
// []	Do not disturb data on the displayClear the display
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::home()
{
  command(LCD_RETURNHOME);  // set cursor position to zero
  delayMicroseconds(2000);  // this command takes a long time! }
}

// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn on the display
// []
// []	Depending on the display, might just turn backlighting on
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]

void LCDi2cDogm::display()
{
  g_ctrl |= BL_PIN;
  _displaycontrol |= LCD_DISPLAYON; 
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn off the display
// []
// []	Depending on the display, might just turn backlighting off
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]

void LCDi2cDogm::noDisplay()
{
  g_ctrl &= ~BL_PIN;
  _displaycontrol &= ~LCD_DISPLAYON; 
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn on the underline cursor
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]

void LCDi2cDogm::cursor()
{
  _displaycontrol |= LCD_CURSORON;
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn off the underline cursor
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]

void LCDi2cDogm::noCursor()
{
  _displaycontrol &= ~LCD_CURSORON;
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn on the blinking block cursor
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::blink()
{
  _displaycontrol |= LCD_BLINKON;
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Turn off the blinking block cursor
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]

void LCDi2cDogm::noBlink()
{
  _displaycontrol &= ~LCD_BLINKON;
  command(LCD_DISPLAYCONTROL | _displaycontrol); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Position the cursor to position line,column
// []
// []	line is 0 - Max Display lines
// []	column 0 - Max Display Width
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::setCursor(uint8_t col, uint8_t row)
{
  int row_offsets[] = { 
    0x00, 0x10, 0x20  };
  if ( row > g_num_lines ) 
  {
    row = g_num_lines-1;    // we count rows starting w/0
  }

  command(LCD_SETDDRAMADDR | (col + row_offsets[row])); 
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Return the status of the display
// []
// []	Does nothing on this display
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]	

uint8_t LCDi2cDogm::status()
{

  return 0;
}


// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Read data from keypad
// []
// []	
// []	
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


uint8_t LCDi2cDogm::keypad ()
{


}



// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
// []
// []	Load data for a custom character
// []
// []	Char = custom character number 0-7
// []	Row is array of chars containing bytes 0-7
// []
// []
// [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


void LCDi2cDogm::load_custom_character(uint8_t char_num, uint8_t *rows)
{


}




void LCDi2cDogm::setBacklight(uint8_t new_val)
{
  if (new_val) 
    g_ctrl |= BL_PIN; 
  else 
    g_ctrl &= ~BL_PIN; 
}


void LCDi2cDogm::setContrast(uint8_t new_val)
{

}





