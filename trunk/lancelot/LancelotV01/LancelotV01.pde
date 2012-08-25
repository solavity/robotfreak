/* 
 * Sir Lancelot
 */
#include <Servo.h> 
 

#define PWM1 5 //M1 Speed Control
#define PWM2 6 //M2 Speed Control
#define DIR1 4 //M1 Direction Control
#define DIR2 7 //M1 Direction Control
#define servoPin 8
#define echoPin 2
#define triggerPin 3

static int last_speed1, last_speed2;
int speed_l;
int speed_r;
int dly;
int distance;

Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 
int pos = 0;    // variable to store the servo position 

void motorInit(void)
{
  pinMode(DIR1, OUTPUT);
  pinMode(DIR2, OUTPUT);
}

void motorMove(int speed1, int speed2)
{

  last_speed1 = speed1;
  last_speed2 = speed2;

  if (speed1 < 0)
  {
    digitalWrite(DIR1, LOW);
    analogWrite(PWM1, abs(speed1));
  }
  else
  {
    digitalWrite(DIR1, HIGH);
    analogWrite(PWM1, abs(speed1));
  }
  
  if (speed2 < 0)
  {
    digitalWrite(DIR2, LOW);
    analogWrite(PWM2, abs(speed2));
  }
  else
  {
    digitalWrite(DIR2, HIGH);
    analogWrite(PWM2, abs(speed2));
  }
  
}

void motorMoveRamp(int speed1, int speed2, int dly)
{
  int temp_speed1 = last_speed1;
  int temp_speed2 = last_speed2;

  Serial.print("MoveRamp: ");
  Serial.print(speed1, DEC);
  Serial.print(", ");
  Serial.print(speed2, DEC);
  Serial.print(", ");
  Serial.println(dly, DEC);
  while ((speed1 != temp_speed1) ||
         (speed2 != temp_speed2))
   {
      if (temp_speed1 > speed1)
        temp_speed1--;
      else if (temp_speed1 < speed1)
        temp_speed1++;

      if (temp_speed2 > speed2)
        temp_speed2--;
      else if (temp_speed2 < speed2)
        temp_speed2++;
      motorMove(temp_speed1, temp_speed2);
      delay(dly);
   }
}

void setup() {

  Serial.begin(38400);                 // Sets the baud rate to 38400
  motorInit();
  ultrasonicInit();
  myservo.attach(servoPin);  // attaches the servo on pin 9 to the servo object 
  speed_l = 0;
  speed_r = 0;
  Serial.println("Lancelot obstacle Avoiding Version 1.0");   
}

  
void loop()
{
  distance = getDistance();  // get distance
  Serial.print("Distance: ");
  Serial.println(distance, DEC);
  if (distance > 4)
  {
  if (distance < 15)
  {
    motorMove(0,0);
    speed_l = -150;
    speed_r = 150;
    dly = 5;
    do {
      distance = getDistance();  // get distance
      motorMove(speed_l, speed_r);
//      motorMoveRamp(speed_l, speed_r, dly);
  } while(distance < 30);  
  }
  else if (distance < 30)
  {
    pos = 70;
    speed_l = 150;
    speed_r = 150;
  }
  else
  {
    pos = 0;
    speed_l = 200;
    speed_r = 200;
  }
  dly = 10;
  motorMove(speed_l, speed_r);
  myservo.write(pos);
//  motorMoveRamp(speed_l, speed_r, dly);
  }
  delay(40);
}

void ultrasonicInit(void)
{
    pinMode(triggerPin, OUTPUT);
    pinMode(echoPin, INPUT);

}
int value; // This value will be populated
int getDistance()
{
  long pulseLen; 
  digitalWrite(triggerPin, LOW);
  delayMicroseconds(2);
  digitalWrite(triggerPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(triggerPin, LOW);
  pulseLen = pulseIn(echoPin, HIGH);
  value = (int) (pulseLen /29 / 2);
  return value;
}
