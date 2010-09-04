//-------------------------------------------------------------------------------------

//              GHI Electronics, LLC

//               Copyright (c) 2009

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

using System.Threading;

using Microsoft.SPOT;

using Microsoft.SPOT.Hardware;



using GHIElectronics.NETMF.Hardware;



using GHIElectronics.NETMF.FEZ;



namespace GHIElectronics.NETMF.FEZ

{

    public static partial class FEZ_Components

    {

        public class DistanceDetector : IDisposable

        {

            AnalogIn adc;

            float Y0 = 0;

            float X0 = 0;

            float Y1 = 0;

            float X1 = 0;

            float C = 0;

            public enum SharpSensorType : byte

            {

                GP2Y0A21YK = 0,

                GP2D120 = 1,

            }



            public void Dispose()

            {

                adc.Dispose();

            }



            public DistanceDetector(FEZ_Pin.AnalogIn pin, SharpSensorType type)

            {

                adc = new AnalogIn((AnalogIn.Pin)pin);

                adc.SetLinearScale(0, 330);

                switch (type)

                {

                    case SharpSensorType.GP2Y0A21YK:

                        Y0 = 10;

                        X0 = 315;

                        Y1 = 80;

                        X1 = 30;

                        break;

                    case SharpSensorType.GP2D120:

                        Y0 = 3;

                        X0 = 315;

                        Y1 = 30;

                        X1 = 30;

                        break;

                }

                C = (Y1 - Y0) / (1 / X1 - 1 / X0);

            }



            public float GetDistance_cm()

            {

                return C / ((float)adc.Read() + (float).001) - (C / X0) + Y0;

            }

        }

    }

}



