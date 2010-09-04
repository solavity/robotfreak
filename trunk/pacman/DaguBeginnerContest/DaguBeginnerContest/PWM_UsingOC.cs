using System;
using Microsoft.SPOT;
using Microsoft.SPOT.Hardware;
using GHIElectronics.NETMF.Hardware;
using GHIElectronics.NETMF.FEZ;

namespace GHIElectronics.NETMF.FEZ
{
    public static partial class FEZ_Components
    {
        public class PWM_UsingOC : IDisposable  
        {
            OutputCompare _oc;
            uint[] timings = new uint[2];
            public PWM_UsingOC(FEZ_Pin.Digital dp)
            {
                _oc = new OutputCompare((Cpu.Pin)dp, false, timings.Length);
            }
            
            public void Dispose()
            {
                _oc.Dispose();
            } 

            public void SetPulse(uint period_nanosecond, uint highTime_nanosecond)
            {
                uint period = period_nanosecond / 1000;
                uint highTime = highTime_nanosecond / 1000;

                timings[0] = highTime;
                timings[1] = period - highTime;
                _oc.Set(true, timings, 0, 2, true);
            }

            public void Set(int frequency, byte dutyCycle)
            {
                // in nano seconds
                uint period = 0;
                uint highTime = 0;

                period = (uint)(1000000000 / frequency);

                highTime = (uint)((ulong)(period) * dutyCycle / 100);
                SetPulse(period, highTime);
            }
            public void Stop()
            {
                _oc.Set(false, timings, 0, 2, false);
            }
        }
    }
}
