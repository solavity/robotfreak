// DAGU Beginners Robot chassis Challenge
// First Test: Drive each motor in both directions
// http://letsmakerobots.com/user/1433 (RobotFreak)

#define mot1pwmPin  6
#define mot1diraPin 7
#define mot2pwmPin  5
#define mot2diraPin 4

int actSpeedLt, actSpeedRt; 

void motorInit(void)
{
  pinMode(mot1diraPin, OUTPUT);
  pinMode(mot2diraPin, OUTPUT);
  analogWrite(mot1pwmPin, 0);
  digitalWrite(mot1diraPin, 0);
  analogWrite(mot2pwmPin, 0);
  digitalWrite(mot2diraPin, 0);
}

void motorSetSpeed(int speedLt, int speedRt)
{
  analogWrite(mot1pwmPin, speedLt);
  analogWrite(mot2pwmPin, speedRt);   
}

void motorSetDirection(int dirLt, int dirRt)
{
  digitalWrite(mot1diraPin, dirLt);
  digitalWrite(mot2diraPin, dirRt);   
}

void setup() 
{ 
  int i;

  motorInit();
  Serial.begin(38400);

  Serial.println("Motor Test V03");
}


void motorSweep(int ml, int mr, int dr, int dl)
{
  int i;
  
  for (i=0; i<256; i++)
  {
    motorSetSpeed(i*ml,i*mr);
    motorSetDirection(dr, dl);
    delay(10);
  }
} 
  

void loop()
{
  Serial.println("Left motor, forward");
  motorSweep(1, 0, HIGH, HIGH);
  delay(4000);
  Serial.println("Left motor, backward");
  motorSweep(1, 0, LOW, LOW);
  delay(4000);
  Serial.println("Right motor, forward");
  motorSweep(0, 1, HIGH, HIGH);
  delay(4000);
  Serial.println("Right motor, backward");
  motorSweep(0, 1, LOW, LOW);
  delay(4000);
}

