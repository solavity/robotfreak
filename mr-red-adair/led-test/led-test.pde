/* Blink without Delay
 *
 * Turns on and off a light emitting diode(LED) connected to a digital  
 * pin, without using the delay() function.  This means that other code
 * can run at the same time without being interrupted by the LED code.
 *
 * http://www.arduino.cc/en/Tutorial/BlinkWithoutDelay
 */

int backledPin = 5;                // LED connected to digital pin 13
int frontledPin = 6;                // LED connected to digital pin 13
int blueled1Pin = 7;                // LED connected to digital pin 13
int blueled2Pin = 8;                // LED connected to digital pin 13
int value = LOW;                // previous value of the LED
long previousMillis = 0;        // will store last time LED was updated
long interval = 1000;           // interval at which to blink (milliseconds)

void setup()
{
  pinMode(backledPin, OUTPUT);      // sets the digital pin as output
  pinMode(frontledPin, OUTPUT);      // sets the digital pin as output
  pinMode(blueled1Pin, OUTPUT);      // sets the digital pin as output
  pinMode(blueled2Pin, OUTPUT);      // sets the digital pin as output
 
  digitalWrite(backledPin, value);
  digitalWrite(frontledPin, value);
}

void loop()
{
  // here is where you'd put code that needs to be running all the time.

  // check to see if it's time to blink the LED; that is, is the difference
  // between the current time and last time we blinked the LED bigger than
  // the interval at which we want to blink the LED.
  if (millis() - previousMillis > interval) {
    previousMillis = millis();   // remember the last time we blinked the LED

    // if the LED is off turn it on and vice-versa.
    if (value == LOW)
      value = HIGH;
    else
      value = LOW;

    digitalWrite(blueled2Pin, value);
    digitalWrite(blueled1Pin, value);
  }
}
