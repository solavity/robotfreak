

//    BoB action playback
//      This program was generated by BoB Poser to playback a specific action.
//    Use it as the starting point for your projects!
//
//    Copyright (C) 2012  Jonathan Dowdall, Project Biped (www.projectbiped.com)
//                  ported for BoB Biped by RobotFreak
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.


#include <Servo.h>


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Servo Pin definitions
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#define RAservoPin 6                       // right ankle servo pin
#define RHservoPin 7                       // right hip servo pin
#define LAservoPin 8                       // left ankle servo pin
#define LHservoPin 9                       // left hip servo pin

#define RAservoIdx 0                       // right ankle servo index in comm buffer
#define RHservoIdx 1                       // right hip servo index in comm buffer
#define LAservoIdx 2                       // left ankle servo index in comm buffer
#define LHservoIdx 3                       // left hip servo index in comm buffer

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Variables
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
Servo RAservo;                              // right ankleservo object
Servo RHservo;                              // right hip servo object
Servo LAservo;                              // left ankleservo object
Servo LHservo;                              // left hip servo object 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
const int maximumServoPosition = 2000;   // the maximum pulse duration for the servo shield (2ms pulse)
const int minimumServoPosition = 1000;   // the minimum pulse duration for the servo shield (1ms pulse)
const int centerServoPosition  = 1000;   // the center pulse duration for the servo shield  (1.5ms pulse)
const int numberOfServos             = 4;      // the communication with the control program is expecting 8 servos 

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class Action
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  The Action class collects information necessary to perform a sequence of movements.  It allows for intermediate 
  //  frames (robot positions) to be generated between key frames on the fly (keeping memory usage low).   
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //Members
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  public:
    int numberOfFrames;      //total number of frames in the action
    int numberOfKeyFrames;   //number of key frames (this must match the first dimension of the frames array)
    int frameDelay;          //number of milliseconds to wait inbetween each frame during playback
    int frameNumber;         //current frame number during playback
    int* frames;             //pointer to a two dimensional array containing the individual frames

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //Methods
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  Action(int setNumberOfKeyFrames, int setNumberOfFrames, int setFrameDelay, void* setFrames)
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  {
    frames            = (int*)setFrames;
    numberOfKeyFrames = setNumberOfKeyFrames;
    numberOfFrames    = setNumberOfFrames;
    frameDelay        = setFrameDelay;
    frameNumber       = 0;
  }  
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  void NextFrame()
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  {
    frameNumber++;
    if(frameNumber >= numberOfFrames)
      frameNumber = 0;
      
    delay(frameDelay);    
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  void GetCurrentFrame(int* frame)
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  {
     GetFrame(frame, frameNumber);
  }
   
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  void GetFrame(int* frame, int targetFrameNumber)
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  {
    //Compute the frame based on the target frame number.  Remember that this can either be a key frame or an intermediate
    //frame between two key frames.
    
    //compute the number of intermediate frames between each key frame
    int numberOfIntermediateFrames = numberOfFrames/numberOfKeyFrames;
    
    //compute the closest key frame before the target frame number
    int sourceKeyFrame = targetFrameNumber/numberOfIntermediateFrames;
    
    //get the key frame after the source key frame 
    int destinationKeyFrame = sourceKeyFrame + 1;
    //wrap around if this is the last frame
    if(destinationKeyFrame >= numberOfKeyFrames)
      destinationKeyFrame = 0;
      
    //compute mixing percentage between the source and destination key frame
    float percent = (float)(targetFrameNumber - sourceKeyFrame*numberOfIntermediateFrames)/(float)numberOfIntermediateFrames;
    
    //mix the source and destination key frame to produce the target frame
    GetIntermediateFrame(frames + sourceKeyFrame*numberOfServos, frames + destinationKeyFrame*numberOfServos, percent, frame);    
  }
  
  private:
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  void GetIntermediateFrame(int* fromState, int* toState, float percent, int* outState)
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  {
      //update each servo
      for (int servo = 0; servo < numberOfServos; servo++)
      {   
        //compute the angles from both the from state and the to state for this servo.
        //remember that the states are stored as 100*angle ... so they need to be converted back into angles
        int source = ((float)fromState[servo]);      
        int target = ((float)toState[servo]);    
        
        //the servo angle used is the linear interpolation between the two 
        outState[servo] = percent*(target - source) + source;
      }
  }
  
};
 
 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Action Frames
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Joints positions are in degrees * 100 (home position is 60 degrees)
/////////////////////////////////////////////
//JOINT INDEXES
// 0 right ankle
// 1 right hip
// 2 left ankle
// 3 left hip
/////////////////////////////////////////////
int frames[8][4] = {
                      { 6000,  6000,  6000,  6000 },
                      { 9442,  6000, 10740,  6000 },
                      { 9542,  3580, 10740,  3679 },
                      { 6274,  3704,  6362,  3679 },
                      { 6274,  3704,  6362,  3679 },
                      {  960,  3704,  2718,  3679 },
                      { 3006,  6349,  2718,  6997 },
                      { 5476,  6698,  5961,  6523 } 
};

int calibration[4] = {1646, 49, -598, 0 };

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Actions
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
Action myAction(4, 40, 50, frames); //the action
Action* currentAction;                //pointer to the current action

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void setup()
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  //set the initial action
  currentAction = &myAction;
  
  //wait for a second to begin (keeps the communication line open in case a new program is being downloaded)
  delay(1000);    
  
  //start up the communication
  Serial.begin(9600);  
  
  //initialize the servos
  initializeServos();
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void loop()
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  //this is the main update loop for the microcontroller
  
  //update the current action
  UpdateAction();
  
  //get the frame from the current action  
  int frame[numberOfServos];
  currentAction->GetCurrentFrame(frame);
  
  //set the servo positions for this frame number
  SetServoPositions(frame);                    
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void UpdateAction()
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  //check to see if the robot is walking
  if(currentAction == &myAction)
  {
    // TODO (you) : add some logic for your action here!
    
    // move to the next frame of the action
    currentAction->NextFrame();    
  }  
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void initializeServos()
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{   
  LHservo.attach(LHservoPin);
  RHservo.attach(RHservoPin);
  LAservo.attach(LAservoPin);
  RAservo.attach(RAservoPin);

  ServosSetPosition(LHservoIdx, centerServoPosition);
  ServosSetPosition(RHservoIdx, centerServoPosition);
  ServosSetPosition(LAservoIdx, centerServoPosition);
  ServosSetPosition(RAservoIdx, centerServoPosition); 
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void  ServosSetPosition(int servo, int pwm)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  switch(servo)
  {
    case LHservoIdx:
      LHservo.write(pwm);
    break;
    case RHservoIdx:
      RHservo.write(pwm);
    break;
    case LAservoIdx:
      LAservo.write(pwm);
    break;
    case RAservoIdx:
      RAservo.write(pwm);
    break;
  }
}        

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void SetServoPositions(int* frame)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{
  //update each servo
  for (int servo = 0; servo < numberOfServos; servo++)
  {   
    //the servo angle used is the linear interpolation between the two 
    float servoAngle = (frame[servo] + calibration[servo])/100.0f;
     
    // the servo control shield commands the servos via pulse width modulations, not angles
    // the PWMs range from 1000 (equal to 0 degrees) to 2000 (equal to 120 degrees)
    // so the servo angle needs to be converted to the corresponding PWM range.
    int pwm = (int)(servoAngle/120.0* (float)(maximumServoPosition - minimumServoPosition)) + minimumServoPosition;
      
    //Set PWM for the servo shield to send out to the servo
    ServosSetPosition(servo, pwm);       
  }


}
