/*
 * LCDi2cDogm.h - Library for DOGM LCD modules   
 * using a PFC8574 I2C port expander
 * see http://www.lcd-module/dogm
 * Created by RobotFreak 
 * http://www.letsmakerobots.com/user/1433
 *
 * Credits:
 * Based on the Original Arduino CrystalLibrary
 * and the LCD API from dale@wentztech.com 
 */


#ifndef LCDi2cDogm_h
#define LCDi2cDogm_h


#define _LCDEXPANDED				// If defined turn on advanced functions

#include <inttypes.h>

#include "Print.h"


class LCDi2cDogm : 
public Print {

public: 

  LCDi2cDogm(uint8_t num_lines, uint8_t num_col, uint8_t addr);

  void command(uint8_t value);

  void init();

  void setDelay(int,int);

  virtual void write(uint8_t);

  void clear();

  void home();

  void display();

  void noDisplay();

  void cursor();

  void noCursor();

  void blink();

  void noBlink();

  void setCursor(uint8_t Line, uint8_t Col );



  // [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
  // []
  // []	Extended Functions
  // []
  // [][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]


//#ifdef _LCDEXPANDED		


  uint8_t status();

  void load_custom_character(uint8_t char_num, uint8_t *rows);

  uint8_t keypad();

  void printstr(const char[]);

  void setBacklight(uint8_t new_val);

  void setContrast(uint8_t new_val);

  //4bit only, therefore ideally private but may be needed by user
  void commandNibble(uint8_t nibble);

//#endif

private:
  void pushNibble(uint8_t nibble);
  void pushByte(uint8_t value);
  void writePCF8574(uint8_t value);


};

#endif


