This repository has been migrated to github
https://github.com/robotfreak/robotfreak

FezBridge is an [Android Debug Bridge (ADB)](http://developer.android.com/guide/developing/tools/adb.html) implementation in .NETMF for FEZ Devices.

# Introduction #

FezBridge is ported from the [MicroBridge](http://code.google.com/p/microbridge) for Arduino.
It is programmed in .NETMF and runs on FEZ devices from [GHI electronics](http://www.ghielectronics.com/), like the [FEZ Domino](http://www.ghielectronics.com/catalog/product/133). Other devices like [FEZ Panda](http://www.ghielectronics.com/catalog/product/135) needs a small [hardware modification](http://ghielectronics.blogspot.com/2011/03/usb-host-support-is-added-on-fez-panda.html) to support USB host mode.


# Details #

At the moment FezBridge is a work in progress. The basic communication and USB enuration has been done.

ADB works on almost every Android phone. That is important if you wan't to use [Android Open Accessory (ADK)](http://developer.android.com/guide/topics/usb/adk.html). ADK works only on Android phones with the newest firmware v2.3.4. But not all phones will support ADK, even with the newest firmware. This is where ADB can help. It has nearly the same functionality as ADK.

You can use ADB to forward TCP/IP ports via USB between phone and hardware. Simple data pipes can be used for communication.

A description of the ADB protocol can be found [here](http://lxr.e2g.org/source/system/core/adb/protocol.txt).

<a href='http://www.youtube.com/watch?feature=player_embedded&v=sDUndL7bEic' target='_blank'><img src='http://img.youtube.com/vi/sDUndL7bEic/0.jpg' width='425' height=344 /></a>


# To do list #

  * build seperate driver class
  * Demo programs
  * Demo apps (Android side)

# Weblinks #
  * [FezBridge Source repository](http://code.google.com/p/robotfreak/source/browse/#svn%2Ftrunk%2Ffez-bridge)
  * [FezBridge\_v02.zip](http://code.google.com/p/robotfreak/downloads/detail?name=FezBridge_v02.zip&can=2&q=)
  * [Android Debug Bridge (ADB)](http://developer.android.com/guide/developing/tools/adb.html)
  * [GHI electronics](http://www.ghielectronics.com/)
  * [MicroBridge](http://code.google.com/p/microbridge)