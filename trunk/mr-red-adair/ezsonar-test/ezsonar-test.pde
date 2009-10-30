int ezsonarPin = 0;

void setup()
{
  Serial.begin(38400);
}

void loop()
{
  int val, inches, cm;

  val = analogRead(ezsonarPin);

  // convert the analog value into a distance
  inches = val/2; 
  cm = inches * 2.54;
  
  Serial.print(inches);
  Serial.print("in, ");
  Serial.print(cm);
  Serial.print("cm");
  Serial.println();
  
  delay(100);
}


