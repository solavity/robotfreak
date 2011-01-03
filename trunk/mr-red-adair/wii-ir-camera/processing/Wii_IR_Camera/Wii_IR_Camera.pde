/**
 * Serial Duplex 
 * by Tom Igoe. 2
 * 
 * Sends a byte out the serial port when you type a key
 * listens for bytes received, and displays their value. 
 * This is just a quick application for testing serial data
 * in both directions. 
 */
import processing.serial.*;

DisplayItems di;  // backgroundcolor, grid, etc. are controlled in the DisplayItems object
// width and height should be set here
int xWidth = 600;
int yHeight = 600;

// set framerate
int fr = 24;
  
// set up the display items you want by choosing true
boolean bck = true;
boolean grid = true;
boolean g_vert = true;
boolean g_horiz = true;
boolean g_values = true;
boolean output = true; 
  

Serial myPort;      // The serial port
int whichKey = -1;  // Variable to hold keystoke values
int inByte = -1;    // Incoming serial data
String inString;  // Input string from serial port:
int lf = 10;      // ASCII linefeed
int VarN[] = {};
int xpos, ypos; 
int posx = -1;
int posy = -1; 

void setup() 
{
  size(xWidth, yHeight);
  frameRate(fr);

  di = new DisplayItems();   // create a font with the third font available to the system:
  PFont myFont = createFont(PFont.list()[2], 14);
  textFont(myFont);

  // List all the available serial ports:
  println(Serial.list());

  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // In Windows, this usually opens COM1.
  // Open whatever port is the one you're using.
  String portName = Serial.list()[2];
  myPort = new Serial(this, portName, 38400);
  myPort.bufferUntil(lf);

}
void drawCursor(int x, int y){
  stroke( (bck) ? (255) : (32) );
  strokeWeight(1);
  line(x-(width/128), y , x+(width/128), y );
  line(x, y-(width/128), x, y+(width/128) );
} 

void draw() 
{
  background(0);
  text("received: " + inString, 10,20);
  if (posx < 1023 && posy < 1023)
  {
//    xpos = constrain(  ((posx-100)*(width+100))/width , 0, width );
//    ypos = constrain(  ((posy-100)*(height+100))/height , 0, height ); 
    drawCursor(posx/2, posy/2);
  }
}

void serialEvent(Serial myPort) 
{
  inString = (myPort.readString());
  inByte = myPort.read();
  String StrVarN[] = split(inString, ",");
  if (StrVarN != null)
  {
    posx = Integer.parseInt(StrVarN[0]);
    posy = Integer.parseInt(StrVarN[1]);
    print(str(posx));
    print(",");
    println(str(posy));
  }
//  println(StrVarN[0]);
//  println(StrVarN.length);
}

void keyPressed() 
{
  // Send the keystroke out:
  myPort.write(key);
  whichKey = key;
}

