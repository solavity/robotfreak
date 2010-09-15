//-------------------------------------------------------------------------------------
//               GHI Electronics, LLC - Pin changes made by Foekie
//               Copyright (c) 2010
//               All rights reserved
//-------------------------------------------------------------------------------------

/*
 * You can use this file if you agree to the following:
 *
 * 1. This header can't be changed under any condition.
 *    
 * 2. This is a free software and therefore is provided with NO warranty.
 * 
 * 3. Feel free to modify the code but we ask you to provide us with
 *	  any bugs reports so we can keep the code up to date.
 *
 * 4. This code may ONLY be used with GHI Electronics, LLC products.
 *
 * THIS SOFTWARE IS PROVIDED BY GHI ELECTRONICS, LLC ``AS IS'' AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL 
 * GHI ELECTRONICS, LLC BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR ORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  
 *
 *	Specs are subject to change without any notice
 */

using System;
//using System.IO.Ports;
//using System.Runtime.CompilerServices;
using System.Threading;
using Microsoft.SPOT;
using Microsoft.SPOT.Hardware;
using GHIElectronics.NETMF.Hardware;

namespace GHIElectronics.NETMF.FEZ
{
    public static partial class FEZ_Shields
    {
        public class DCMotorDriver : IDisposable
        {
            static PWM _pwm1;
            static OutputPort _dir1;
           
            static PWM _pwm2;
            static OutputPort _dir2;

            static sbyte _last_speed1, _last_speed2;

            public void Dispose()
            {
                _pwm1.Dispose();
                _dir1.Dispose();
                _pwm2.Dispose();
                _dir2.Dispose();
            } 

            public DCMotorDriver()
            {
                Initialize();
            }

            static public void Initialize()
            {
                _pwm1 = new PWM((PWM.Pin)FEZ_Pin.PWM.Di10);
                _dir1 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.Di12, false);

                /*
                 * 
                 * Modification to make it work
                 * 
                 * 
                 * We set pin 9 for PWM here. You will need to route pin 9 to pin 11.
                 * 
                 * You can do this the following ways:
                 *     * Use a breadbord wire and stick it in the headers of the shield from pin 9 to pin 11.  
                 *                  ==>(you cannot use pin 11 anymore!!) <==
                 *                  
                 *     * Solder a wire from pin 9 to pin 11 on the shield and cut the connection line from pin 11 to the IC on the shield
                 */

                _pwm2 = new PWM((PWM.Pin)FEZ_Pin.PWM.Di9); //set pin 9 for PWM
                _dir2 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.Di13, false);

                //end of modification
            }

            public void Move(sbyte speed1, sbyte speed2)
            {
                if (speed1 > 100 || speed1 < -100 || speed2 > 100 || speed2 < -100)
                    new ArgumentException();

                _last_speed1 = speed1;
                _last_speed2 = speed2;

                if (speed1 < 0)
                {
                    _dir1.Write(true);
                    _pwm1.Set(100, (byte)(100 - System.Math.Abs(speed1)));
                }
                else
                {
                    _dir1.Write(false);
                    _pwm1.Set(100, (byte)(speed1));
                }
               
                ////////////////////////////
                if (speed2 < 0)
                {
                    _dir2.Write(true);
                    _pwm2.Set(100, (byte)(100 - System.Math.Abs(speed2)));
                }
                else
                {
                    _dir2.Write(false);
                    _pwm2.Set(100, (byte)(speed2));
                }
            }

            public void MoveRamp(sbyte speed1, sbyte speed2, byte ramping_delay)
            {
                sbyte temp_speed1 = _last_speed1;
                sbyte temp_speed2 = _last_speed2;

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
//                    Debug.Print("Speed; " + temp_speed1 + " " + temp_speed2);
                    Move(temp_speed1, temp_speed2);
                    Thread.Sleep(ramping_delay);
                }
            }
        }
    }
}

