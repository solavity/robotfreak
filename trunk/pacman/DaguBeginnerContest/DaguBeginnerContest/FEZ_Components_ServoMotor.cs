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

using Microsoft.SPOT;

using Microsoft.SPOT.Hardware;



using GHIElectronics.NETMF.Hardware;



namespace GHIElectronics.NETMF.FEZ

{

    public static partial class FEZ_Components

    {

        public class ServoMotor : IDisposable

        {

            OutputCompare oc;

            uint[] timings = new uint[5];

            public void Dispose()

            {

                oc.Dispose();

            }

            public ServoMotor(FEZ_Pin.Digital pin)

            {

                oc = new OutputCompare((Cpu.Pin)pin, true, 5);

            }

            public void SetPosition(byte angle_degree)

            {



                uint pos = (uint)(((float)((2500-400) / 180) * (angle_degree)) + 400);

                timings[0] = pos;

                timings[1] = 50000;

                oc.Set(true, timings, 0, 2, true);

            }



        }

    }

}

