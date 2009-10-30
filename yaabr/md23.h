// MD23 Definitionen
#define MD23_Address  0x58       // I2C Address of the MD23

#define MD23_RegSpeed1   0x00       // Motor 1 Speed register     rw
#define MD23_RegSpeed2   0x01       // Motor 2 Speed register     rw
#define MD23_RegEncoder1 0x02       // Motor 1 Encoder register   ro
#define MD23_RegEncoder2 0x06       // Motor 2 Encoder register   ro
#define MD23_RegBattery  0x0A       // Battery volts register     ro
#define MD23_RegCurrent1 0x0B       // Motor 1 Current register   ro
#define MD23_RegCurrent2 0x0C       // Motor 2 Current register   ro
#define MD23_RegRevision 0x0D       // Software version register  ro
#define MD23_RegAccel    0x0E       // Acceleration register      rw
#define MD23_RegMode     0x0F       // Mode of operation register rw
#define MD23_RegCommand  0x10       // Command register           rw

#define MD23_CmdResetEncoder     0x20 // Resets the encoder registers to zero
#define MD23_CmdAutoSpeedDisable 0x30 // Disables automatic speed regulation
#define MD23_CmdAutoSpeedEnable  0x31 // Enables automatic speed regulation
#define MD23_CmdTimeoutDisable   0x32 // Disables 2 second timeout of motors
#define MD23_CmdTimeoutEnable    0x33 // Enables 2 second timeout of motors
#define MD23_CmdI2CAddrChange1   0xA0 // 1st in sequence to change I2C address
#define MD23_CmdI2CAddrChange2   0xA4 // 2st in sequence to change I2C address
#define MD23_CmdI2CAddrChange3   0xA5 // 3st in sequence to change I2C address

