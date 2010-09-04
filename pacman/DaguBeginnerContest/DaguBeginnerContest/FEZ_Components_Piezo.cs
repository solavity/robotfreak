//-------------------------------------------------------------------------------------
//              GHI Electronics, LLC
//               Copyright (c) 2010
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
using System.Threading;
using Microsoft.SPOT;
using GHIElectronics.NETMF.Hardware;
using GHIElectronics.NETMF.FEZ;

namespace GHIElectronics.NETMF.FEZ
{
    public static partial class FEZ_Components
    {
        public class Piezo : IDisposable
        {
            public int[] notes =
            {
                0,
                /* NOTE_C4 = */ 262,
                /* NOTE_CS4 = */ 277,
                /* NOTE_D4 = */ 294,
                /* NOTE_DS4 = */ 311,
                /* NOTE_E4 = */ 330,
                /* NOTE_F4 = */ 349,
                /* NOTE_FS4 = */ 370,
                /* NOTE_G4 = */ 392,
                /* NOTE_GS4 = */ 415,
                /* NOTE_A4 = */ 440,
                /* NOTE_AS4 = */ 466,
                /* NOTE_B4 = */ 494,
                /* NOTE_C5 = */ 523,
                /* NOTE_CS5 = */ 554,
                /* NOTE_D5 = */ 587,
                /* NOTE_DS5 = */ 622,
                /* NOTE_E5 = */ 659,
                /* NOTE_F5 = */ 698,
                /* NOTE_FS5 = */ 740,
                /* NOTE_G5 = */ 784,
                /* NOTE_GS5 = */ 831,
                /* NOTE_A5 = */ 880,
                /* NOTE_AS5 = */ 932,
                /* NOTE_B5 = */ 988,
                /* NOTE_C6 = */ 1047,
                /* NOTE_CS6 = */ 1109,
                /* NOTE_D6 = */ 1175,
                /* NOTE_DS6 = */ 1245,
                /* NOTE_E6 = */ 1319,
                /* NOTE_F6 = */ 1397,
                /* NOTE_FS6 = */ 1480,
                /* NOTE_G6 = */ 1568,
                /* NOTE_GS6 = */ 1661,
                /* NOTE_A6 = */ 1760,
                /* NOTE_AS6 = */ 1865,
                /* NOTE_B6 = */ 1976,
                /* NOTE_C7 = */ 2093,
                /* NOTE_CS7 = */ 2217,
                /* NOTE_D7 = */ 2349,
                /* NOTE_DS7 = */ 2489,
                /* NOTE_E7 = */ 2637,
                /* NOTE_F7 = */ 2794,
                /* NOTE_FS7 = */ 2960,
                /* NOTE_G7 = */ 3136,
                /* NOTE_GS7 = */ 3322,
                /* NOTE_A7 = */ 3520,
                /* NOTE_AS7 = */ 3729,
                /* NOTE_B7 = */ 3951,
                /* NOTE_C8 = */ 4186,
                /* NOTE_CS8 = */ 4435,
                /* NOTE_D8 = */ 4699,
                /* NOTE_DS8 = */ 4978
            };

            FEZ_Components.PWM_UsingOC pwm;

            public Piezo(FEZ_Pin.Digital pin)
            {
                pwm = new FEZ_Components.PWM_UsingOC(pin);
            }

            public void Dispose()
            {
                pwm.Dispose();
            }

            public void Play(int freq_Hz, int duration_mSec)
            {
                pwm.Set(freq_Hz, 50);
                Thread.Sleep(duration_mSec);
                pwm.Stop();
            }

            public void Play(int[] freqs, int[] durations_mSec)
            {
                if (freqs.Length != durations_mSec.Length)
                    throw new ArgumentException("Arrays must be of the same length.");

                for (int i = 0; i < freqs.Length; i++)
                {
                    Play(freqs[i], durations_mSec[0]);
                }
            }

            public void PlayRtttl(String p)
            {
                int default_dur = 4;
                int default_oct = 6;
                int bpm = 63;
                int num;
                int wholenote;
                int duration;
                int note;
                int scale;
                int i = 0;
                string[] result;
                string[] defaults;
                string[] nodes;
                
                // format: d=N,o=N,b=NNN:
                // find the start (skip name, etc)
                result = p.Split(':');
                defaults = result[1].Split(',');
                nodes = result[2].Split(',');

                // get default duration
                string[] dur = defaults[0].Split('=');
                if(dur[0] == "d")
                {
                default_dur = Convert.ToInt32(dur[1]);
                Debug.Print("ddur: " + default_dur);
                }

                // get default octave
                string[] oct = defaults[1].Split('=');
                if(oct[0] == "o")
                {
                default_oct = Convert.ToInt32(oct[1]);
                Debug.Print("doct: " + default_oct);
                }

                string[] bpmn = defaults[2].Split('=');
                if(bpmn[0] == "b")
                {
                bpm = Convert.ToInt32(bpmn[1]);
                Debug.Print("bpm: " + bpm);
                }

                // BPM usually expresses the number of quarter notes per minute
                wholenote = (60 * 1000 / bpm) * 4;  // this is the time for whole note (in milliseconds)
                Debug.Print("wn: " + wholenote);

                // now begin note loop
                foreach (string n in nodes)
                {
                    scale = default_oct;
                    i = 0;
                    // first, get note duration, if available
                    num = 0;
                    while (n[i] >= '0' && n[i] <= '9')
                    {
                        num = (num * 10) + (n[i++] - '0');
                    }

                    if (num != 0) duration = wholenote / num;
                    else duration = wholenote / default_dur;  // we will need to check if we are a dotted note after

                    // now get the note
                    note = 0;

                    switch (n[i])
                    {
                        case 'c':
                            note = 1;
                            break;
                        case 'd':
                            note = 3;
                            break;
                        case 'e':
                            note = 5;
                            break;
                        case 'f':
                            note = 6;
                            break;
                        case 'g':
                            note = 8;
                            break;
                        case 'a':
                            note = 10;
                            break;
                        case 'b':
                            note = 12;
                            break;
                        case 'p':
                            note = 0;
                            break;
                        default:
                            note = 0;
                            break;
                    }
                    i++;
                    if (i < n.Length)
                    {
                        // now, get optional '#' sharp
                        if (n[i] == '#')
                        {
                            note++;
                            i++;
                        }
                        if (i < n.Length)
                        {
                            // now, get optional '.' dotted note
                            if (n[i] == '.')
                            {
                                duration += duration / 2;
                                i++;
                            }
                            if (i < n.Length)
                            {

                                // now, get scale
                                if (n[i] >= '0' && n[i] <= '9')
                                {
                                    scale = n[i] - '0';
                                    i++;
                                }
                            }
                        }
                    }

                    // now play the note

                    if (note != 0)
                    {
                        int m = (scale - 4) * 12;
                        Debug.Print("Playing: " + scale + " " + note + " " + notes[m+note] + " " + duration);
                        Play(notes[m + note], duration);
                    }
                    else
                    {
                        Debug.Print("Pausing: " + duration);
                        pwm.Stop();
                        Thread.Sleep(duration);
                    }
                }
                pwm.Stop();
            }
        }
    }
}
