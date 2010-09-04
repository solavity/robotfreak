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
        public class Maxbotix : IDisposable
        {
            AnalogIn adc;
            float div = 1;

            public void Dispose()
            {
                adc.Dispose();
            }

            public enum MaxbotixSensorType : byte
            {
                LV_EZx_3V3,
                LV_EZx_5V,
                XL_EZx_3V3,
                XL_EZx_5V,
            }


            public Maxbotix(FEZ_Pin.AnalogIn pin, MaxbotixSensorType type)
            {
                adc = new AnalogIn((AnalogIn.Pin)pin);
                adc.SetLinearScale(0, 3300);
                switch (type)
                {
                    case MaxbotixSensorType.LV_EZx_3V3:
                        div = (float)(6.4 / 2.54);
                        break;
                    case MaxbotixSensorType.LV_EZx_5V:
                        div = (float)(9.8 / 2.54);
                        break;
                    case MaxbotixSensorType.XL_EZx_3V3:
                        div = (float)(3.2);
                        break;
                    case MaxbotixSensorType.XL_EZx_5V:
                        div = (float)(4.9);
                        break;
                }
            }

            public float GetDistance_cm()
            {
                return (float) ((float)adc.Read() / div);
            }
        }
    }
}
