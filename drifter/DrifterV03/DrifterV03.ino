// Drifter V0.1
// RC Car hack by RobotFreak

// I/O definitions
#define RCinFwPin 3
#define RCinBwPin 4
#define RCinLtPin 7
#define RCinRtPin 8

#define DriveFwPin 5
#define DriveBwPin 6
#define DriveLtPin 9
#define DriveRtPin 10

#define EchoPin    11
#define TriggerPin 12

#define gyroPin    0

#define RCCMD_FORWARD  1
#define RCCMD_BACKWARD 2
#define RCCMD_LEFT     4
#define RCCMD_RIGHT    8

#define MODE_UNKNOWN   0
#define MODE_MANUAL    1
#define MODE_DRIFT_LT  2
#define MODE_DRIFT_RT  3
#define MODE_STOP      4
#define MODE_TEST      5

int mode, rccmd;
long duration;
long cmMsec, inMsec;

int readRC(void)
{
  int val = 0;
  val |= (digitalRead(RCinFwPin)) ? RCCMD_FORWARD : 0;
  val |= (digitalRead(RCinBwPin)) ? RCCMD_BACKWARD : 0;
  val |= (digitalRead(RCinLtPin)) ? RCCMD_LEFT : 0;
  val |= (digitalRead(RCinRtPin)) ? RCCMD_RIGHT : 0;
  return val;  
}

void writeDrive(int cmd)
{
  digitalWrite(DriveFwPin, (cmd & RCCMD_FORWARD));
  digitalWrite(DriveBwPin, (cmd & RCCMD_BACKWARD));
  digitalWrite(DriveLtPin, (cmd & RCCMD_LEFT));
  digitalWrite(DriveRtPin, (cmd & RCCMD_RIGHT));
}

int readIDG300(void)
{
  int Raw;
  const int count = 5;

  for(int i = 0; i < count; i++)
  {
    Raw += analogRead(gyroPin);
  }

  Raw = Raw / count;

  return Raw; 
}

long readHCSR04()
{
  long pulsein;
  // The HC-SR04 is triggered by a HIGH pulse of 10 or more microseconds.
  digitalWrite(TriggerPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(TriggerPin, LOW);
  // The same pin is used to read the signal from the HC-SR04 a HIGH
  // pulse whose duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  pulsein = pulseIn(EchoPin, HIGH);
  return pulsein;
}

long microsecondsToInches(long microseconds)
{
  // According to Parallax's datasheet for the PING))), there are
  // 73.746 microseconds per inch (i.e. sound travels at 1130 feet per
  // second).  This gives the distance travelled by the ping, outbound
  // and return, so we divide by 2 to get the distance of the obstacle.
  return microseconds / 74 / 2;
}
 
long microsecondsToCentimeters(long microseconds)
{
  // The speed of sound is 340 m/s or 29 microseconds per centimeter.
  // The ping travels out and back, so to find the distance of the
  // object we take half of the distance travelled.
  return microseconds / 29 / 2;
}

void rampSpeed(int newSpeed, int steps)
{
  static int actSpeed = 0;
  int diff;
  if (newSpeed > actSpeed) // forward accelerate
  {
    if ((steps > 0) && (newSpeed < (actSpeed + steps)))
      actSpeed += steps;
    else
      actSpeed = newSpeed;
  }
  else if (newSpeed < actSpeed) // forward decelerate
  {
    if ((steps > 0) && (newSpeed < (actSpeed + steps)))
      actSpeed += steps;
    else
      actSpeed = newSpeed;
  }
  else
    actSpeed = newSpeed;
  analogWrite(DriveFwPin, actSpeed);
}

void rampDirection(int newDirection, int steps)
{
  static int actDirection = 0;
  int diff;
  if (newDirection > actDirection) // direction accelerate
  {
    if ((steps > 0) && (newDirection < (actDirection + steps)))
      actDirection += steps;
    else
      actDirection = newDirection;
  }
  else if (newDirection < actDirection) // direction deccelerate
  {
    if ((steps > 0) && (newDirection < (actDirection + steps)))
      actDirection += steps;
    else
      actDirection = newDirection;
  }
  else
    actDirection = newDirection;
  analogWrite(DriveLtPin, actDirection);
}

void modeAuto()
{
  int newSpeed, newDirection;
  duration = readHCSR04();
  
  cmMsec = microsecondsToCentimeters(duration);
  inMsec = microsecondsToInches(duration);
  
  Serial.print("CM: ");
  Serial.print(cmMsec);
  Serial.print(", IN: ");
  Serial.println(inMsec);
  if (cmMsec > 0 && cmMsec < 100)
  {
    if (cmMsec > 40)
    {
      newDirection = 0;
      newSpeed = 200;
    }
    else if (cmMsec < 10)
    {
      newDirection = 0;
      newSpeed = 0;
      rampSpeed(newSpeed, 0);
    }
    else
    {
//      if (mode == MODE_DRIFT_LT)
        newDirection = 200;
//      else if  (mode == MODE_DRIFT_RT)
//        newDirection = -200;
      newSpeed = 100;
     }
    rampSpeed(newSpeed, 10);
    rampDirection(newDirection, 20);
    delay(40);
  }
}

void modeManual()
{
  int val;
 
  val = readIDG300();
  Serial.write( 0xff );                                                         
  Serial.write( (val >> 8) & 0xff );                                            
  Serial.write( val & 0xff );
  
  rccmd = readRC();
/*
  if (rccmd != 0)
  {
    Serial.print("Cmd: ");
    Serial.println(rccmd ,HEX);
  }  
*/
  writeDrive(rccmd);
}

void modeSelect()
{
  int val;
  
  val = readIDG300();
  Serial.print("Gyro: ");
  Serial.println(val, DEC);
/*  Serial.write( 0xff );                                                         
  Serial.write( (val >> 8) & 0xff );                                            
  Serial.write( val & 0xff ); */

  rccmd = readRC();
  if (rccmd != 0)
  {
    switch(rccmd)
    {
      case RCCMD_FORWARD:
        mode = MODE_MANUAL;
        Serial.println("Mode: Manu");
      break;
      case RCCMD_LEFT:
        mode = MODE_DRIFT_LT;
        Serial.println("Mode: Drift Left");
      break;
      case RCCMD_RIGHT:
        mode = MODE_DRIFT_RT;
        Serial.println("Mode: Drift Right");
      break;
      case RCCMD_BACKWARD:
        mode = MODE_TEST;
        Serial.println("Mode: Test");
      break;
    }
  }  
}

void modeStop()
{
  writeDrive(0);
  mode = MODE_UNKNOWN;
}

void modeTest()
{
  static int drive = 0;
  static int steer = 0;
  int val;
  
  rccmd = readRC();
  val = readIDG300();
  Serial.print("Gyro: ");
  Serial.println(val, DEC);
  if (val > 480)
  {
    rccmd |= RCCMD_RIGHT;
    rccmd &= ~RCCMD_LEFT;
  }
  else if (val < 460)
  {
    rccmd |= RCCMD_LEFT;
    rccmd &= ~RCCMD_RIGHT;
  }
/*  Serial.write( 0xff );                                                         
  Serial.write( (val >> 8) & 0xff );                                            
  Serial.write( val & 0xff ); */
  
  if (rccmd != 0)
  {
//    Serial.print("Cmd: ");
//    Serial.println(rccmd ,HEX);
  if (rccmd & RCCMD_FORWARD)
  {
    drive++;
    analogWrite(DriveFwPin, drive);
  }
  if (rccmd & RCCMD_LEFT)
  {
    if (steer < 240)
      steer+=10;
    analogWrite(DriveLtPin, steer);
  }  
  if (rccmd & RCCMD_RIGHT)
  {
    if (steer < 240)
      steer+=10;
    analogWrite(DriveRtPin, steer);
  }
  if (rccmd & RCCMD_BACKWARD)
  {
    drive++;
    analogWrite(DriveBwPin, drive);
  }
  }
  else
  {
    drive = 0;
    steer = 0;
    writeDrive(0);
  }
  delay(10);
}

void setup()
{
  
  Serial.begin(115200);
  Serial.println("Drifter v0.3");
  pinMode(RCinFwPin, INPUT_PULLUP);  
  pinMode(RCinBwPin, INPUT_PULLUP);  
  pinMode(RCinLtPin, INPUT_PULLUP);  
  pinMode(RCinRtPin, INPUT_PULLUP);  

  pinMode(DriveFwPin, OUTPUT);  
  pinMode(DriveBwPin, OUTPUT);  
  pinMode(DriveLtPin, OUTPUT);  
  pinMode(DriveRtPin, OUTPUT);  
  
  pinMode(EchoPin, INPUT_PULLUP);
  pinMode(TriggerPin, OUTPUT);
  digitalWrite(TriggerPin, LOW);
  
  mode = MODE_UNKNOWN;
}

void loop()
{
  switch(mode)
  {
    case MODE_UNKNOWN:
      modeSelect();
    break;
    case MODE_MANUAL:
      modeManual();
    break;
    case MODE_DRIFT_LT:
      modeAuto();
    break;
    case MODE_DRIFT_RT:
     modeAuto();
    break;
    case MODE_STOP:
      modeStop();
    break;   
    case MODE_TEST:
      modeTest();
    break;   
  }
}

