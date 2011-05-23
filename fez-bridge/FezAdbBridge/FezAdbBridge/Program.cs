using System;
using System.Threading;

using Microsoft.SPOT;
using Microsoft.SPOT.Hardware;

using GHIElectronics.NETMF.FEZ;
using GHIElectronics.NETMF.USBHost;
using GHIElectronics.NETMF.System;


namespace FezAdbBridge
{
  public class Program
  {
    static USBH_RawDevice usb;
    static USBH_RawDevice.Pipe adbInPipe;
    static USBH_RawDevice.Pipe adbOutPipe;
    static Thread adbInThread; // polls the adb for data

    public static void Main()
    {
      Debug.EnableGCMessages(false);

      // Subscribe to USBH event.
      USBHostController.DeviceConnectedEvent += DeviceConnectedEvent;

      // Sleep forever
      Thread.Sleep(Timeout.Infinite);
    }

    static void DeviceConnectedEvent(USBH_Device device)
    {
      usb = new USBH_RawDevice(device);
      USBH_Descriptors.Configuration cd = usb.GetConfigurationDescriptors(0);
      USBH_Descriptors.Endpoint adbEP = null;
      USBH_Descriptors.Interface adbIF = null;

      Debug.Print("[Device, Port " + usb.PORT_NUMBER + "]");
      Debug.Print("Interface: " + usb.INTERFACE_INDEX);
      Debug.Print("ID: " + usb.ID);
      Debug.Print("Type: " + usb.TYPE);
      Debug.Print("VID: " + usb.VENDOR_ID);
      Debug.Print("PID: " + usb.PRODUCT_ID);

      // look for HID class
      for (int i = 0; i < cd.bNumInterfaces; i++)
      {

        adbIF = cd.interfaces[i];

        Debug.Print("  === Interface ===");
        Debug.Print("  Class: " + cd.interfaces[i].bInterfaceClass);
        Debug.Print("  SubClass: " + cd.interfaces[i].bInterfaceSubclass);
        Debug.Print("  Number: " + cd.interfaces[i].bInterfaceNumber);
        Debug.Print("  Protocol: " + cd.interfaces[i].bInterfaceProtocol);
        Debug.Print("  Type: " + cd.interfaces[i].bDescriptorType);

        for (int ep = 0; ep < adbIF.bNumberEndpoints; ep++)
        {
          adbEP = adbIF.endpoints[ep];

          Debug.Print("   -- Endpoint --");
          Debug.Print("    Attributes: " + adbIF.endpoints[ep].bmAttributes);
          Debug.Print("    Address: " + adbIF.endpoints[ep].bEndpointAddress);
          Debug.Print("    Type: " + adbIF.endpoints[ep].bDescriptorType);
          Debug.Print("    Interval: " + adbIF.endpoints[ep].bInterval);
          Debug.Print(" ");
          switch (adbEP.bEndpointAddress)
          {
            case 132: // ADB data in
              adbInPipe = usb.OpenPipe(adbEP);
              adbInPipe.TransferTimeout = 0;              // recommended for interrupt transfers
              adbInThread = new Thread(AdbInThread);      // create the polling thread
              break;
            case 3:   // ADB data out
              adbOutPipe = usb.OpenPipe(adbEP);
              break;
          }

        }

        Debug.Print(" ");
        Debug.Print(" ");
      }

      // set configuration
      usb.SendSetupTransfer(0x00, 0x09, cd.bConfigurationValue, 0x0000);
      adbInThread.Start();

    }

    static void AdbInThread()
    {
      int count, i;
      string datastring = "";

      // Maximum data is wMaxPacketSize
      byte[] adbData = new byte[adbInPipe.PipeEndpoint.wMaxPacketSize];

      // Read every bInterval
      while (true)
      {
        Thread.Sleep(adbInPipe.PipeEndpoint.bInterval);

        count = adbInPipe.TransferData(adbData, 0, adbData.Length);

        datastring = "Data = ";

        for (i = 0; i < adbData.Length; i++)
        {
          datastring = datastring + ((sbyte)(adbData[i]) + " ");
        }
        Debug.Print(datastring);
      }
    }


  }
}
