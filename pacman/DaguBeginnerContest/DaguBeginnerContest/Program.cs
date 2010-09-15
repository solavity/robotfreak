using System;
using System.Threading;

using Microsoft.SPOT;
using Microsoft.SPOT.Hardware;

using GHIElectronics.NETMF.FEZ;
using GHIElectronics.NETMF.Hardware;

namespace DaguBeginnerContest
{
    public class Program
    {
        // Sharp GP2D120
        static FEZ_Components.DistanceDetector side = new FEZ_Components.DistanceDetector(FEZ_Pin.AnalogIn.An0, FEZ_Components.DistanceDetector.SharpSensorType.GP2D120);
        //static FEZ_Components.DistanceDetector front = new FEZ_Components.DistanceDetector(FEZ_Pin.AnalogIn.An5, FEZ_Components.DistanceDetector.SharpSensorType.GP2D120);
        // Maxbotix EZ1
        static FEZ_Components.Maxbotix front = new FEZ_Components.Maxbotix(FEZ_Pin.AnalogIn.An5, FEZ_Components.Maxbotix.MaxbotixSensorType.LV_EZx_5V);
        // L298 Dual DC Motor Driver uses Di9,10,11,12,13
        static FEZ_Shields.DCMotorDriver motors = new FEZ_Shields.DCMotorDriver();
        // Servos
        static FEZ_Components.ServoMotor servo1 = new FEZ_Components.ServoMotor(FEZ_Pin.Digital.Di4);
        static FEZ_Components.ServoMotor servo2 = new FEZ_Components.ServoMotor(FEZ_Pin.Digital.Di5);
        // Create Piezo (speaker) object assigned to the a Peizo Component connected to Di8
        static FEZ_Components.Piezo piezo = new FEZ_Components.Piezo(FEZ_Pin.Digital.Di8);
        // LED
        static OutputPort LED = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.LED, true);
        // Line sensors
        static OutputPort ls_en = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.Di7, false);
        static FEZ_Components.ReflectiveSensor ls_r = new FEZ_Components.ReflectiveSensor(FEZ_Pin.AnalogIn.An2, 20);
        static FEZ_Components.ReflectiveSensor ls_l = new FEZ_Components.ReflectiveSensor(FEZ_Pin.AnalogIn.An3, 20);
        static FEZ_Components.ReflectiveSensor.DetectingState lsr;
        static FEZ_Components.ReflectiveSensor.DetectingState lsl;
        static Thread ServoThreadHandler;


        public static void Main()
        {
            Debug.Print("Pacman Dagu Challenge V0.3");

            // create thread handlers
            //            Thread SensorThreadHandler;
            Thread LedThreadHandler;
            // create new thread objects
            // and assing to my handlers
            //            SensorThreadHandler = new Thread(SensorThread);
            LedThreadHandler = new Thread(LedThread);
            ServoThreadHandler = new Thread(ServoThread);
            // start the new threads
            //            SensorThreadHandler.Start();
            LedThreadHandler.Start();

            /////////////////////////////////
            // Do anything else you like to do here
            PlayStartMelody();
            ServoThreadHandler.Start();


            while (true)
            {
                FollowLine();
                AvoidObstacle();
            }

            Thread.Sleep(Timeout.Infinite);
        }

        public static void SensorThread()
        {
            float distance;

            Debug.Print("Sensor Thread");

            while (true)
            {
                distance = front.GetDistance_cm();
                if (distance < 30.0)
                {
                    Debug.Print("Front Distance: " + distance);
                    piezo.Play(1000, (int)(distance * 10));
                }
                distance = side.GetDistance_cm();
                if (distance < 30.0)
                {
                    Debug.Print("Side Distance: " + distance);
                    piezo.Play(3000, (int)(distance * 10));
                }

                Thread.Sleep(100);
            }

        }

        public static void ServoThread()
        {
            while (true)
            {
                servo1.SetPosition(90);
                servo2.SetPosition(90);
                Thread.Sleep(100);
                servo1.SetPosition(50);
                servo2.SetPosition(130);
                Thread.Sleep(100);
            }
        }

        public static void LedThread()
        {
            Debug.Print("LED Thread");
            while (true)
            {
                LED.Write(!LED.Read());
                Thread.Sleep(250);
            }
        }

        public static void PlayStartMelody()
        {
            String PacMan = "PacMan:d=16,o=6,b=140:b5,b,f#,d#,8b,8d#,c,c7,g,f,8c7,8e,b5,b,f#,d#,8b,8d#,32d#,32e,f,32f,32f#,g,32g,32g#,a,8b";

            Debug.Print("Melody Thread");

            piezo.PlayRtttl(PacMan);
            Thread.Sleep(500);
        }

        public static void AvoidObstacle()
        {
            float distance;
            int line;
            sbyte speed_l = 0;
            sbyte speed_r = 0;
            byte delay;

            while (true)
            {
                // read front sensor
                distance = front.GetDistance_cm();
                if (distance < 20.0)    // object distance < 12cm?
                {
                    Debug.Print("Front Distance: " + distance);
                    // turn left
                    speed_l = 0;
                    speed_r = 40;
                    delay = 5;
                    motors.MoveRamp(speed_l, speed_r, delay);
                }
                else    // no front object
                {
                    // read line sensor
                    line = ReadLineSensors();
                    if (line != 0)    // line detected?
                    {
                        ServoThreadHandler.Resume();
                        break;       // abort process
                    }
                    // read side sensor
                    distance = side.GetDistance_cm();
                    if (distance < 8.0)    // object distance < 6cm?
                    {
                        speed_l = 0;   // move left
                        speed_r = 30;
                    }
                    else     // object distance > 6cm
                    {
                        speed_l = 30;    // move right
                        speed_r = 0;
                    }
                    delay = 5;
                    Debug.Print("Side Distance: " + distance);
                    Debug.Print("Speed: " + speed_l + " " + speed_r);
                    motors.MoveRamp(speed_l, speed_r, delay);
                }
                Thread.Sleep(20);
            }
        }

        public static void FollowLine()
        {
            float distance;
            byte line = 0;
            byte last_line = 0;
            sbyte speed_l = 0;
            sbyte speed_r = 0;

            while (true)
            {
                // read front sensor
                distance = front.GetDistance_cm();
                if (distance < 15.0)   // object distance < 15cm
                {
                    ServoThreadHandler.Suspend();
                    break;    // abort process
                }
                else   // no object, follow line
                {
                    // read line sensors
                    line = ReadLineSensors();
                    switch (line)
                    {
                        case 3:  // on line, go strait
                            speed_l = 40;
                            speed_r = 40;
                            break;
                        case 2:  // line left, go right
                            speed_l = 30;
                            speed_r = 40;
                            break;
                        case 1:  // line right, go left
                            speed_l = 40;
                            speed_r = 30;
                            break;
                        default:
                            if (last_line == 2)  // last detected line on left, sharp turn right
                            {
                                speed_l = 00;
                                speed_r = 40;
                            }
                            else if (last_line == 1)  // last detected line on right, sharp turn left
                            {
                                speed_l = 40;
                                speed_r = 00;
                            }
                            else                    // else, turn on place
                            {
                                speed_l = -20;
                                speed_r = 20;
                            }
                            break;
                    }
                    if (line != 0) last_line = line;
                    Debug.Print("Line: " + line + " " + last_line);
                    Debug.Print("Speed: " + speed_l + " " + speed_r);
                    motors.MoveRamp(speed_l, speed_r, 2);
                    Thread.Sleep(20);
                }
            }
        }

        public static byte ReadLineSensors()
        {
            byte result = 0;
            lsr = ls_r.GetState();
            lsl = ls_l.GetState();
            if (lsr == FEZ_Components.ReflectiveSensor.DetectingState.ReflectionDeteced)
            {
                result |= 1;
            }
            if (lsl == FEZ_Components.ReflectiveSensor.DetectingState.ReflectionDeteced)
            {
                result |= 2;
            }
            return result;
        }

    }
}
