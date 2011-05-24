﻿using System;
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

    const UInt32 A_SYNC = 0x434e5953;
    const UInt32 A_CNXN = 0x4e584e43;
    const UInt32 A_OPEN = 0x4e45504f;
    const UInt32 A_OKAY = 0x59414b4f;
    const UInt32 A_CLSE = 0x45534c43;
    const UInt32 A_WRTE = 0x45545257;

    private const ushort VALID_VID = 0x18D1;
    private const ushort VALID_PID = 0x4E12;
    private static bool initPhaseOneComplete;
    private static bool initPhaseTwoComplete;
    private static bool isReady;

    private static string hostName = "host::fezbridge";

    private UInt32[] AdbData = new UInt32[6];

    private struct sAdbMessage
    {
      public UInt32 command;
      public UInt32 arg0;
      public UInt32 arg1;
      public UInt32 dataLength;
      public UInt32 dataCheck;
      public UInt32 magic;

      public int ByteSize
      {
        get { return 24; }
      }
      public byte[] ToArray()
      {
        byte[] data = new byte[ByteSize];
        int offset = 0;

        data[offset++] = (byte)(command); data[offset++] = (byte)(command >> 8); data[offset++] = (byte)(command >> 16); data[offset++] = (byte)(command >> 24);
        data[offset++] = (byte)(arg0); data[offset++] = (byte)(arg0 >> 8); data[offset++] = (byte)(arg0 >> 16); data[offset++] = (byte)(arg0 >> 24);
        data[offset++] = (byte)(arg1); data[offset++] = (byte)(arg1 >> 8); data[offset++] = (byte)(arg1 >> 16); data[offset++] = (byte)(arg1 >> 24);
        data[offset++] = (byte)(dataLength); data[offset++] = (byte)(dataLength >> 8); data[offset++] = (byte)(dataLength >> 16); data[offset++] = (byte)(dataLength >> 24);
        data[offset++] = (byte)(dataCheck); data[offset++] = (byte)(dataCheck >> 8); data[offset++] = (byte)(dataCheck >> 16); data[offset++] = (byte)(dataCheck >> 24);
        data[offset++] = (byte)(magic); data[offset++] = (byte)(magic >> 8); data[offset++] = (byte)(magic >> 16); data[offset++] = (byte)(magic >> 24); 
        string datastring = "Out>> ";

        for (int i = 0; i < data.Length; i++)
        {
          datastring = datastring + ((sbyte)(data[i]) + " ");
        }
        Debug.Print(datastring);
        return data;
      }
    };

    static sAdbMessage adbMsg = new sAdbMessage();

    public static void Main()
    {
      Debug.EnableGCMessages(false);

      // Subscribe to USBH event.
      USBHostController.DeviceConnectedEvent += DeviceConnectedEvent;
      USBHostController.DeviceDisconnectedEvent += DeviceDisconnectedEvent;
      initPhaseOneComplete = false;
      initPhaseTwoComplete = false;
      isReady = false;

      // Sleep forever
      Thread.Sleep(Timeout.Infinite);
    }

    static void DeviceConnectedEvent(USBH_Device device)
    {
      if (!initPhaseOneComplete)
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
          if (adbIF.bInterfaceClass == 255)
          {

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
                case 133: // ADB data in
                  adbInPipe = usb.OpenPipe(adbEP);
                  //adbInPipe.TransferTimeout = 0;              // recommended for interrupt transfers
                  break;
                case 5:   // ADB data out
                  adbOutPipe = usb.OpenPipe(adbEP);
                  break;
              }

            }
            initPhaseOneComplete = true;
          }
        }

      }
      else
      {
          initPhaseTwoComplete = true;
      }

      if (initPhaseTwoComplete)
      {
        initPhaseOneComplete = false;
        initPhaseTwoComplete = false;
        isReady = true;
      }

      if (isReady)
      {
        usb.SendSetupTransfer(0x00, 0x09, 0x0001, 0x0000);
        adbInThread = new Thread(AdbListening);      // create the polling thread
        adbInThread.Priority = ThreadPriority.Highest;
        adbInThread.Start();

        SendAdbMessage(A_CNXN, 16777216, 4096, StringToByteArray(hostName));
      }

    }

    static void DeviceDisconnectedEvent(USBH_Device device)
    {
      isReady = false;

    }

    static void AdbListening()
    {
      int count, i;
      string datastring = "";

      // Maximum data is wMaxPacketSize
      byte[] adbData = new byte[adbInPipe.PipeEndpoint.wMaxPacketSize];

      // Read every bInterval
      while (true)
      {
        Thread.Sleep(adbInPipe.PipeEndpoint.bInterval);

        try
        {
          count = adbInPipe.TransferData(adbData, 0, adbData.Length);
        }
        catch (Exception ex)
        {
          Debug.Print(ex.ToString());
        }


        datastring = "In << ";

        for (i = 0; i < adbData.Length; i++)
        {
          datastring = datastring + ((sbyte)(adbData[i]) + " ");
        }
        Debug.Print(datastring);
      }
    }

    public static void SendAdbMessage(UInt32 st, UInt32 arg0, UInt32 arg1, byte[] data)
    {
      UInt32 crc = 0;
      UInt32 cnt = 0;

      if (data != null)
      {
        while (cnt < data.Length)
        {
          crc += data[cnt++];
        }
      }
      byte[] zdata = new byte[data.Length + 1];
      data.CopyTo(zdata, 0);


      adbMsg.command = st;
      adbMsg.arg0 = arg0;
      adbMsg.arg1 = arg1;
      adbMsg.dataLength = (UInt32)zdata.Length;
      adbMsg.dataCheck = crc;
      adbMsg.magic = st ^ 0xffffffff;
      try
      {
        adbOutPipe.TransferData(adbMsg.ToArray(), 0, adbMsg.ByteSize);
        if (data != null)
          adbOutPipe.TransferData(zdata, 0, zdata.Length);
      }
      catch (Exception ex)
      {
        Debug.Print(ex.ToString());
      }

    }
    private static byte[] StringToByteArray(string str)
    {
      System.Text.UTF8Encoding enc = new System.Text.UTF8Encoding();
      return enc.GetBytes(str);
    }


  }
}