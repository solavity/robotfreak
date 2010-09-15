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
        public class LineSensor : IDisposable
        {
            static OutputPort _ls_enable;
            static long[] ls_time = new long[6];
            static long startTime;

            static void MakePinOutput(TristatePort port)
            {
                if (port.Active == false)
                    port.Active = true;
            }
            
            static void MakePinInput(TristatePort port)
            {
                if (port.Active == true)
                    port.Active = false;
            }
            
            static void ls1_OnInterrupt(uint port, uint state, DateTime time)
            {

            }

            static void ls2_OnInterrupt(uint port, uint state, DateTime time)
            {

            }
            static void ls3_OnInterrupt(uint port, uint state, DateTime time)
            {

            }
            static void ls4_OnInterrupt(uint port, uint state, DateTime time)
            {

            }
            static void ls5_OnInterrupt(uint port, uint state, DateTime time)
            {

            }
            static void ls6_OnInterrupt(uint port, uint state, DateTime time)
            {

            }
            public void Dispose()
            {
                _ls_enable.Dispose();
            }

            public LineSensor()
            {
                Initialize();
            }

            static public void Initialize()
            {
                _ls_enable = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT3, true);

            }

            public void Burst()
            {
                OutputPort ls1 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT4, true);
                OutputPort ls2 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT5, true);
                OutputPort ls3 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT6, true);
                OutputPort ls4 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT7, true);
                OutputPort ls5 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT8, true);
                OutputPort ls6 = new OutputPort((Cpu.Pin)FEZ_Pin.Digital.UEXT9, true);

                ls1.Write(true);
                ls2.Write(true);
                ls3.Write(true);
                ls4.Write(true);
                ls5.Write(true);
                ls6.Write(true);

                ls1.Dispose();
                ls2.Dispose();
                ls3.Dispose();
                ls4.Dispose();
                ls5.Dispose();
                ls6.Dispose();

            }

            public void TimeMeasure()
            {
                InterruptPort ls1_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT4, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);
                InterruptPort ls2_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT5, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);
                InterruptPort ls3_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT6, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);
                InterruptPort ls4_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT7, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);
                InterruptPort ls5_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT8, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);
                InterruptPort ls6_int = new InterruptPort((Cpu.Pin)FEZ_Pin.Digital.UEXT9, true, Port.ResistorMode.Disabled, Port.InterruptMode.InterruptEdgeLow);

                startTime = DateTime.Now.Ticks;

                ls1_int.OnInterrupt += new NativeEventHandler(ls1_OnInterrupt);
                ls2_int.OnInterrupt += new NativeEventHandler(ls2_OnInterrupt);
                ls3_int.OnInterrupt += new NativeEventHandler(ls3_OnInterrupt);
                ls4_int.OnInterrupt += new NativeEventHandler(ls4_OnInterrupt);
                ls5_int.OnInterrupt += new NativeEventHandler(ls5_OnInterrupt);
                ls6_int.OnInterrupt += new NativeEventHandler(ls6_OnInterrupt);

            }

            private void ls1_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[0] = time.Ticks - startTime;
            }
            private void ls2_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[1] = time.Ticks - startTime;
            }
            private void ls3_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[2] = time.Ticks - startTime;
            }
            private void ls4_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[3] = time.Ticks - startTime;
            }
            private void ls5_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[4] = time.Ticks - startTime;
            }
            private void ls6_onInterrupt(uint port, uint state, DateTime time)
            {
                ls_time[5] = time.Ticks - startTime;
            }

            public void ReadRaw()
            {
                _ls_enable.Write(true);
                Burst();
                TimeMeasure();
                Thread.Sleep(4);
            }

        }
    }
}
