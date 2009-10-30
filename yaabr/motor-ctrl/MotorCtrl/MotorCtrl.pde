// YAABR Motor Controller 
// acts as I2C Slave

#include <Wire.h>
#include <OrangutanMotors.h>
#include <md23.h>

OrangutanMotors motors;

int new_speed1, new_speed2;
int cur_speed1, cur_speed2;
char speed1, speed2;
char ver, bat, cur1, cur2; 
char acc, mode, cmd;
char new_cmd;
long enc1, enc2;
int timeout, timeout_enable;

void setup()
{
  Wire.begin(0x58);                // join i2c bus with address 0x58
  Wire.onReceive(receiveEvent);    // register event
  Wire.onRequest(requestEvent);    // register event
  Serial.begin(38400);             // start serial for output
  speed1=speed2=0;
  new_speed1=new_speed2=0;
  cur_speed1=cur_speed2=0;
  enc1=enc2=0;
  bat=0;
  cur1=cur2=0;
  mode=0;
  cmd=0;
  ver = 3;
  acc=5;
  timeout=2000;
  timeout_enable=true;
  Serial.println("YAABR Motor-Ctrl V01");
}

void loop()
{
  // increase/decrease speed in acc steps
  if (new_speed1 > cur_speed1-acc)
    cur_speed1 -= acc;
  else if (new_speed1 < cur_speed1+acc)
    cur_speed1 -= acc;
  else 
    cur_speed1 = new_speed1;
  if (new_speed2 > cur_speed2-acc)
    cur_speed2 -= acc;
  else if (new_speed2 < cur_speed2+acc)
    cur_speed2 -= acc;
  else 
    cur_speed2 = new_speed2;
  
  // check Timeout enable
  if (timeout_enable)
  {
    // Timeout occured?
    if (timeout>10)
      timeout-=10;
    else
      cur_speed1 = cur_speed2 = 0;   // yes, stop motors
  }

  motors.setSpeeds(cur_speed1, cur_speed2);   
  delay(10);
}

// function that executes whenever data is received from master
// this function is registered as an event, see setup()
void receiveEvent(int howMany)
{
  char buf[4];
  char i;
  Serial.print("R: ");
  i=0;
  while(Wire.available()) // loop through all but the last
  {
    buf[i] = Wire.receive();     // receive byte as a character
    Serial.print(buf[i],HEX);    // print the character
    i++;
  }
  if (i==2)
  {
    switch(buf[0])
    {
      case MD23_RegSpeed1:
        speed1 = buf[1];
        switch(mode)
        { 
          case 0:
            new_speed1 = ((buf[1]-128)<<1);
          break;
          case 1:
            new_speed1 = buf[1]<<1;
          break;
        }
      break;
      case MD23_RegSpeed2:
        speed2 = buf[1];
        switch(mode)
        { 
          case 0:
            new_speed2 = ((buf[1]-128)<<1);
          break;
          case 1:
            new_speed2 = buf[1]<<1;
          break;
          case 2:
            new_speed1 = ((speed1-128)<<1)+speed2;
            new_speed2 = ((speed1-128)<<1)-speed2;
          break;
          case 3:
            new_speed1 = (speed1<<1)+speed2;
            new_speed2 = (speed1<<1)-speed2;
          break;
        }
      break;
      case MD23_RegAccel:
        if (buf[1] > 0 && buf[1] <= 10) 
          acc = buf[1];
      break;
      case MD23_RegMode:
        if (buf[1] >= 0 && buf[1] < 4)
          mode = buf[1];
      break;
      case MD23_RegCommand:
        new_cmd = buf[1];
      break;
      default:
      break;
    } 
  }
  Serial.print("\r\n");
  timeout=2000;
}

// function that executes whenever data is requested by master
// this function is registered as an event, see setup()
void requestEvent()
{
  int c, i, j;
  char buf[4];
  Serial.print("R: ");
  c = 0;
  if(Wire.available()) // loop through all but the last
  {
    c = Wire.receive(); // receive byte as a character
    Serial.print(c,HEX);         // print the character
    switch(c)
    {
      case MD23_RegSpeed1:
        i = 1;
        buf[0] = speed1;
      break;
      case MD23_RegSpeed2:
        i = 1;
        buf[0] = speed2;
      break;
      case MD23_RegEncoder1:
        i = 4;
        buf[0] = (char) ((enc1 & 0xff000000) >> 24);
        buf[1] = (char) ((enc1 & 0x00ff0000) >> 16);
        buf[2] = (char) ((enc1 & 0x0000ff00) >> 8);
        buf[3] = (char) (enc1 & 0x000000ff);
      break;
      case MD23_RegEncoder2:
        i = 4;
        buf[0] = (char) ((enc2 & 0xff000000) >> 24);
        buf[1] = (char) ((enc2 & 0x00ff0000) >> 16);
        buf[2] = (char) ((enc2 & 0x0000ff00) >> 8);
        buf[3] = (char) (enc2 & 0x000000ff);
      break;
      case MD23_RegRevision:   // Sofware Revision
        i = 1;
        buf[0] = ver;
      break;
      case MD23_RegCurrent1:
        i = 1;
        buf[0] = cur1;
      break;
      case MD23_RegCurrent2:
        i = 1;
        buf[0] = cur2;
      break;
      case MD23_RegAccel:
        i = 1;
        buf[0] = acc;
      break;
      case MD23_RegMode:
        i = 1;
        buf[0] = mode;
      break;
      case MD23_RegCommand:
        i = 1;
        buf[0] = cmd;
      break;
      default:
        i=1;
        buf[0] = 0;        
      break;
    } 
    j = 0; 
    while (i>0)
    {
      Wire.send(buf[j]);
      Serial.print(buf[j],HEX);         // print the character
      i--;
      j++;
    }
      
  }
  Serial.print("\r\n");
  timeout=2000;
} 
