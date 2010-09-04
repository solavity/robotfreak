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
        static FEZ_Components.DistanceDetector gp2 = new FEZ_Components.DistanceDetector(FEZ_Pin.AnalogIn.An0, FEZ_Components.DistanceDetector.SharpSensorType.GP2D120);
        // Maxbotix EZ1
        static FEZ_Components.Maxbotix ez1 = new FEZ_Components.Maxbotix(FEZ_Pin.AnalogIn.An5, FEZ_Components.Maxbotix.MaxbotixSensorType.LV_EZx_5V);
        // L298 Dual DC Motor Driver uses Di9,10,11,12,13
        static FEZ_Shields.DCMotorDriver motors = new FEZ_Shields.DCMotorDriver();
        // Create Piezo (speaker) object assigned to the a Peizo Component connected to Di8
        static FEZ_Components.Piezo piezo = new FEZ_Components.Piezo(FEZ_Pin.Digital.Di8);
        // LED
        static OutputPort LED = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.LED, true);
        // Line sensors
        static OutputPort ls_en = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.Di4, false);
        static FEZ_Components.ReflectiveSensor ls_r = new FEZ_Components.ReflectiveSensor(FEZ_Pin.AnalogIn.An2, 20);
        static FEZ_Components.ReflectiveSensor ls_l = new FEZ_Components.ReflectiveSensor(FEZ_Pin.AnalogIn.An3, 20);
        static FEZ_Components.ReflectiveSensor.DetectingState lsr;
        static FEZ_Components.ReflectiveSensor.DetectingState lsl;


        static byte[] linedata;

        public static void Main()
        {
            Debug.Print("FEZ Domino Thread Test V0.2");

            linedata = new byte[64];


            // create thread handlers
            //            Thread SensorThreadHandler;
            Thread LedThreadHandler;
            // create new thread objects
            // and assing to my handlers
            //            SensorThreadHandler = new Thread(SensorThread);
            LedThreadHandler = new Thread(LedThread);
            // start the new threads
            //            SensorThreadHandler.Start();
            LedThreadHandler.Start();

            /////////////////////////////////
            // Do anything else you like to do here
            PlayStartMelody();
            /*
                        Debug.Print("Forward");
                        motors.MoveRamp(50, 50, 100);
                        motors.Move(0, 0);
                        Thread.Sleep(2000);
                        Debug.Print("Backward");
                        motors.MoveRamp(-50, -50, 100);
                        motors.Move(0, 0);
                        Thread.Sleep(2000);
                        Debug.Print("Turn Left");
                        motors.MoveRamp(50, -50, 100);
                        motors.Move(0, 0);
                        Thread.Sleep(2000);
                        Debug.Print("Turn Right");
                        motors.MoveRamp(-50, 50, 100);
                        motors.Move(0, 0);
                        Thread.Sleep(2000);
            */

            FollowLine();

            Thread.Sleep(Timeout.Infinite);
        }

        public static void SensorThread()
        {
            Debug.Print("Sensor Thread");

            while (true)
            {
                float ir_distance = gp2.GetDistance_cm();
                if (ir_distance < 30.0)
                {
                    Debug.Print("IR Distance: " + ir_distance);
                    if (ir_distance < 10.0)
                    {
                        piezo.Play(1000, 500);
                    }
                }
                float us_distance = ez1.GetDistance_cm();
                if (us_distance < 30)
                {
                    Debug.Print("US Distance: " + us_distance);
                    if (us_distance < 15.0)
                    {
                        piezo.Play(3000, 250);
                    }
                }

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

        public static void FollowLine()
        {
            byte line = 0;
            byte last_line = 0;
            sbyte speed_l = 0;
            sbyte speed_r = 0;

            while (true)
            {
                line = ReadLineSensors();
                switch (line)
                {
                    case 3:  // on line, go strait
                        speed_l = 40;
                        speed_r = 40;
                        break;
                    case 1:  // line left, go right
                        speed_l = 30;
                        speed_r = 40;
                        break;
                    case 2:  // line right, go left
                        speed_l = 40;
                        speed_r = 30;
                        break;
                    default:
                        if (last_line == 1)
                        {
                            speed_l = 00;
                            speed_r = 30;
                        }
                        else if (last_line == 2)
                        {
                            speed_l = 30;
                            speed_r = 00;
                        }
                        else
                        {
                            speed_l = -20;
                            speed_r = 20;
                        }
                        break;
                }
                if (line != 0) last_line = line;
                Debug.Print("Line: " + line + " " + last_line);
                Debug.Print("Speed: " + speed_l + " " + speed_r);
                motors.MoveRamp(speed_l, speed_r, 10);
                Thread.Sleep(20);
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
