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

void modeAuto()
{
  duration = readHCSR04();
  
  cmMsec = microsecondsToCentimeters(duration);
  inMsec = microsecondsToInches(duration);
  
  Serial.print("CM: ");
  Serial.print(cmMsec);
  Serial.print(", IN: ");
  Serial.println(inMsec);
  if (cmMsec > 0 && cmMsec < 100)
  {
/*  
  if (cmMsec > 40)
    writeDrive(RCCMD_FORWARD);
  else if (cmMsec < 10)
    writeDrive(RCCMD_BACKWARD);
  else
    writeDrive(RCCMD_LEFT | RCCMD_FORWARD);
*/ 
  }
  delay(100);
}

void modeManual()
{
  rccmd = readRC();
  if (rccmd != 0)
  {
    Serial.print("Cmd: ");
    Serial.println(rccmd ,HEX);
  }  
  writeDrive(rccmd);
}

void modeSelect()
{
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
  
  rccmd = readRC();
  if (rccmd != 0)
  {
    Serial.print("Cmd: ");
    Serial.println(rccmd ,HEX);
  }  
  switch (rccmd)
  {
      case RCCMD_FORWARD:
        drive += 10;
        analogWrite(DriveFwPin, drive);
      break;
      case RCCMD_LEFT:
        steer += 10;
        analogWrite(DriveLtPin, steer);
      break;
      case RCCMD_RIGHT:
        steer += 10;
        analogWrite(DriveRtPin, steer);
      break;
      case RCCMD_BACKWARD:
        drive += 10;
        analogWrite(DriveBwPin, drive);
      break;
      default:
        drive = 0;
        steer = 0;
        writeDrive(0);
      break;  
  }
  delay(100);
}

void setup()
{
  
  Serial.begin(38400);
  Serial.println("Drifter v0.1");
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

