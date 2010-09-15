#ifndef ULTRASONIC_H
#define ULTRASONIC_H


#ifdef __cplusplus
extern "C"{
#endif 
void InitUltrasonics(void);
void RestoreAsuro(void);
int Chirp(void);
#ifdef __cplusplus
} // extern "C"
#endif 
#endif /* ULTRASONIC_H */

