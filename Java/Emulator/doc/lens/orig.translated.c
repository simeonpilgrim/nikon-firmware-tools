/*-----------------------------------------------------
             Simulation of the AF- Nikon chip
                using a PIC 16F690 
   
   Fonctionnement :
   Function:   
    - Le boitier passe un signal RW1 de 1 vers 0 
	  The body gives a signal RW1 falling edge (1 to 0)
    - Il attend puis le repasse à 1 
	  Wait and returns 1
    - Il regarde ensuite l'état de ce signal. 
	  Sample the state of the signal
       - Si RW1 = 1, Rien il retente l'opération plus tard 
	     When RW1 = 1, nothing happens and will repeated
       - Si RW1 = 0, C'est que l'objectif est Ok pour recevoir 
	     When RW1 = 0, there is a lens which awaits data
          - Il envoie le caractère 0x44 
		    It send the character 0x44
          - L'objectif, lorsqu'il a reçu le 0x44 fait remonter le RW1 
		    When the lens 0x44 it it sets the RW1=1
          - L'objectif envoie ensuite un à un les 26 caracteres comme suit 
		    The lens will send 26 characters
               Passage du RW1 à 0, 
			   Set RW1 = 0
               Envoi du caractere, 
			   Send character
               Passage du RW1 à 1. 
			   Set RW1 = 1
   
   C'est le boitier qui genere le signal d'horloge.
   The body generates the clock   
   
   Cablage : 
   Pinout:

                        ---------- 
                    VDD !1     20! VSS 
   RA5/T1CKI/OSC1/CLKIN !2     19! RA0/AN0/C1IN+/ICSPDAT/ULPWU 
RA4/AN3/T1G/OSC2/CLKOUT !3     18! RA1/AN1/C12IN0-/VREF/ICSPCLK 
           RA3/MCLR/VPP !4     17! RA2/AN2/T0CKI/INT/C1OUT 
           RC5/CCP1/P1A !5     16! RC0/AN4/C2IN+ 
          RC4/C2OUT/P1B !6     15! RC1/AN5/C12IN1- 
    RC3/AN7/C12IN3-/P1C !7     14! RC2/AN6/C12IN2-/P1D 
             RC6/AN8/SS !8     13! RB4/AN10/SDI/SDA 
            RC7/AN9/SDO !9     12! RB5/AN11/RX/DT 
              RB7/TX/CK !10    11! RB6/SCK/SCL 
                        ---------- 

RW1 : Sur PIN 17 en lecture pour intercepter le front bas en interruption
                 Input which will generate an interrupt with falling edge 
      Sur PIN 16 en écriture pour générer le signal 
	             Output which generates the signal

CLK : Sur PIN 11 

SD  : Sur PIN 13 Pour recevoir 
                 To receive data.
      Sur PIN  9 Pour Transmettre 
	             To transmit data.

*/ 

#include <pic.h> 
#include <htc.h> 

// Le premier proto utilisait un transistor NPN en collecteur ouvert pour generer RW1 
// The first prototype uses a NPN transistor in open collector mode to generate the RW1 signal.
// d'où necessité d'inverser le signal. 
// Therefore it is neccesary to invert the signal.
// le proto actuel ne l'utilise plus 
// The current prototype does not need it anymore.
//#define avecnpn 

// definition de quelques LED cablées sur le proto pour tests. 
// Definition of LED outputs for prototype test.
#define LED1  RC5 
#define LED2  RA4 
#define LED3  RA5 

// constantes utilisées par le soft pour allumer ou eteindre les leds 
// Constant for switching the LEDs
#define LEDON  0 
#define LEDOFF 1 

// Un jour, le soft sera capable de simuler un type d'objo de fa?on dynamique en lisant les entr?es libres. 
// In future the SW will be able to simulate a lens dynamically.
// Irgendwann wird die SW in der Lage sein dynamisch neue Objektivtypen zu generieren
#define  SW1   RC4 
#define  SW2   RC1 
#define  SW3   RC3 
#define  SW4   RC2 
#define  SW5   RC6 
#define  SW6   RB5 
#define  SW7   RB7 

// Le signal RW1 du boitier 
// The signal RW1 from the body
#define  RWIN    RA2 
#define  RWOUT   RC0 

// constantes que l'on enverra au boitier pour lui indiquer l'ouverture de l'objo que l'on simule. 
// Constants which we will be send to the body to indicate the aperture
#define lensaperture_f10 1 
#define lensaperture_f11 2 
#define lensaperture_f12 8 
#define lensaperture_f13 10 
#define lensaperture_f16 16 
#define lensaperture_f17 18 
#define lensaperture_f18 20 
#define lensaperture_f20 25 
#define lensaperture_f24 30 
#define lensaperture_f25 32 
#define lensaperture_f27 34 
#define lensaperture_f28 36 
#define lensaperture_f30 38 
#define lensaperture_f32 40 
#define lensaperture_f33 42 
#define lensaperture_f35 44 
#define lensaperture_f38 46 
#define lensaperture_f40 48 
#define lensaperture_f45 52 
#define lensaperture_f48 54 
#define lensaperture_f50 56 
#define lensaperture_f53 58 
#define lensaperture_f56 60 

// Constante ? modifier en fonction de l'ouverture max de l'objo ? simuler 
// pour l'instant, la focale simul?e est fixe ? 50 mm 
// Constant to modify depending on selected objects currently fixed at 50mmm
#define lensaperture lensaperture_f18 


// quelques variables 
// Some variables
bit inwaitingstate; 
bit waitforstart; 
unsigned char i; 
unsigned char inbyte; 
unsigned char numbytetosend,bytetosend; 

// definition de la tramme ? envoyer 
// definition of the data to be send
// 26 caracteres 
// 26 characters
#define nbbytetosend 26 
// La table en ROM 
// The rom table
unsigned char tblref[nbbytetosend] = {37,20,80,0,96,248,17,12,6,0,26,20,0,32,0,0,80,20,15,88,80,80,20,20,5,20 }; 

// copie de la table en ROM vers RAM. C'est celle ci qui sera envoy?e 
// apres avoir ?t? modifi?e en focntion des caracteristique de l'objo 
// Copy of the rom table in ram. This will be send after having been modified
// depending on the selected lens
unsigned char tblout[nbbytetosend]; 

// les "Fuses" du PIC 
// The fuses of the PIC
__CONFIG (0x33D4); 

// Recopie de la table ROM vers la RAM et insertion des values de l'objectif ? simuler. 
// Copy the rom table into ram and insert the simulated lens values
// Ouverture courante en : 1 ; 17 ; 25 
// Actual aperture is in 1; 17; 25
// ouverture mini en 22 
// Minimum aperture is in 22
// ouverture maxi en 23 
// Maximum aperture is in 23
void filltblout (void) 
{ 
   unsigned char i; 
   for (i=0 ; i< nbbytetosend ; i++ )  
   { 
      tblout[i]  = tblref[i]; 
   } 
   tblout[1]  = lensaperture; 
   tblout[17] = lensaperture; 
   tblout[25] = lensaperture; 
} 


// Bon, la, une fonction qui renverse un octet. 
// Histoire de MSB qui part premier. 
// je sais : Y'avait juste ? reverser les octets de la table 
// pour eviter cette heresie. 
// Reverse the octet. 
unsigned char inverseoctet ( unsigned char value ) 
{ 
   unsigned char result; 
   result = 0; 
   if ( value & 128 ) result = result | 1; 
   if ( value & 64  ) result = result | 2; 
   if ( value & 32  ) result = result | 4; 
   if ( value & 16  ) result = result | 8; 
   if ( value & 8   ) result = result | 16; 
   if ( value & 4   ) result = result | 32; 
   if ( value & 2   ) result = result | 64; 
   if ( value & 1   ) result = result | 128; 
   return (result); 
} 


// Configuer la transmission SPI 
// Configure transmission of SPI
void initreception (void) 
{ 
   CKP = 1;    // Evenements sur front montant de CLK 
               // Events on rising edge of the CLK
   CKE = 0;     
   SSPEN  = 0; 
   SSPCON = 0b00010101;  // Transmission en SPI, 
                         // Transmition at SPI
   SSPEN  = 1; 
   #ifdef avecnpn 
      TRISC  = 0b11011110; // SDO en entree 
	                       // SDO as an input
   #else 
      TRISC  = 0b11011111; // SDO en entree, RWout en entr?e 
	                       // SDO as an input; RWout as an input
   #endif 
} 

// quelques fonction g?n?rales 
// Some general functions
void enable_INT(void) 
{ 
   INTE = 1; 
   INTF = 0; 
} 

void disable_INT(void) 
{ 
   INTE = 0; 
   INTF = 0; 
} 

void enable_T0 (void) 
{ 
   T0IE = 1; 
   T0IF = 0; 
   TMR0 = 0; 
} 
 
 void disable_T0 (void) 
{ 
   T0IE = 0; 
   T0IF = 0; 
   TMR0 = 0; 
} 
 
void enable_interrupts(void) 
{ 
   GIE = 1; 
} 
 
void disable_interrupts(void) 
{ 
   GIE = 0; 
} 
 
 
 // Se mettre en etat d'attente du front descenfdant du RW1 
 // Waiting for the falling edge of RW1
 void do_idle(void) 
 { 
   inwaitingstate = 1; 
   waitforstart = 0; 
   initreception(); 
   disable_T0();     // Pas besoin de timer; 
                     // We do not need the timer
   #ifdef avecnpn 
      RWOUT = 0;      // Ligne RW = 1 
	                  // Set RW = 1
   #else 
      TRISC = TRISC | 0b00000001; // RWout en entr?e 
	                              // RWout as input
      RWOUT = 1;      // Ligne RW = 1 
	                  // Set RW = 1
   #endif 
   SSPIE = 0;     // pas d'irq en reception 
                  // Disable interupt on reception
   enable_INT();  // Irq sur descente du RW 
                  // Interrupt on falling edge  of RW1
 } 
 
 
 void interrupt tc_int(void) 
 { 
    // recu int du timer0 : c'est grave (timeout) 
    // On se remet en attente 
	// Interrupt received from timer0; This is serious! Resetting to waiting state
    if (T0IE && T0IF) 
	{ 
       T0IF=0; 
       LED1 = LEDOFF; 
       LED2 = LEDOFF; 
       LED3 = LEDOFF; 
       if ((inwaitingstate==0) && (waitforstart)) 
	   { 
          #ifdef avecnpn 
             RWOUT = 1; // pour indiquer au boitier que l'on a un truc a lui dire
                        // To indicate the body that something has arisen			 
          #else 
             TRISC = TRISC & 0b11111110; 
             RWOUT = 0; // pour indiquer au boitier que l'on a un truc a lui dire 
                        // To indicate the body that something has arisen			 
          #endif 
          waitforstart = 0; 
       } 
       else 
	   {
	      do_idle(); 
	   }
    } 
    
    // recu front descendant sur RW : le boitier veut 
    // savoir ? qui il a ? faire 
	// Falling edge received from RW1; The Body wants know what it is connected to
    if (INTE && INTF) 
	{ 
       INTF  = 0; 
       #ifdef avecnpn 
          RWOUT = 1 ;   // on met la ligne rw à 0 
		                // Set RW1 = 0
       #else 
          TRISC = TRISC & 0b11111110; 
          RWOUT = 0 ;   // on met la ligne rw à 0 
		                // Set RW1 = 0
       #endif 
       
       inbyte = SSPBUF; 
       
       INTE  = 0;    // 
       SSPIF = 0;    // on autorise les irqs sur port s?rie;
                     // Enable interrupts at the serial port	   
       SSPIE = 1; 
       
       LED2 = LEDON; 
       enable_T0(); 
    } 
    
    // nous venons de recevoir un caractere sur le port s?rie 
	// Now we have received a character from the serial port
    if (SSPIF && SSPIE) 
    { 
       SSPIF = 0; 
       TMR0  = 0; 
       inbyte = SSPBUF; 
       if (inwaitingstate==1) 
	   { 
         if (inbyte == 0x44) 
	     {
            // caractere d'initialisation venant du boitier 
            // Initialsation character from body
            numbytetosend = 0; 
            bytetosend = inverseoctet(tblout[numbytetosend]); // preparation du premier caractere ? envoyer
                                                              // Preparing first character to be send			
            SSPBUF = bytetosend; // on envoie le caractere 
			                     // Send character
            
            // passer en transmission 
			// Switch to transmission
            inwaitingstate = 0; 
            INTE = 0;  // Pas d'irq sur front du RW 
			           // No interrupt from RW1
            INTF = 0;  // par s?curit? 
			           // for security
            
            TRISC = TRISC & 0b01111111; 
            
            LED1 = LEDON; 
            #ifdef avecnpn 
               RWOUT = 0; // Liberation du RW 
			              // Release RW1
            #else 
               RWOUT = 1; // Liberation du RW 
			              // Release RW1
               TRISC = TRISC | 0b00000001; 
            #endif 
            enable_T0(); // initialisation du timeout 
			             // initialise the time out
            TMR0 = 252; 
            waitforstart = 1; 
         } 
         else
		 {
		   do_idle(); 
		 }
       } 
       else 
	   { 
          // Recu interruption alors que l'on est en emision 
		  // Interruption received in transmission
          LED3 = LEDON; 
          #ifdef avecnpn 
             RWOUT=0; // RW au niveau 1 
	                  // Set RW = 1
          #else 
             TRISC = TRISC | 0b00000001; 
             RWOUT=1; // RW au niveau 1 
 	                  // Set RW = 1
          #endif 
          numbytetosend++; 
          if (numbytetosend<nbbytetosend) 
          { 
             bytetosend = inverseoctet(tblout[numbytetosend]); // caractere ? envoyer 
			                                                   // charater to send....
             SSPBUF = bytetosend; // on envoie le caractere
                                  // Send buffer			 
             waitforstart = 1; 
             enable_T0(); // initialisation du timeout
                          // initialisation of the timeout			 
             TMR0 = 252; 
          } 
          else 
		  {
		     do_idle(); // on a plus rien à transmettre;
                        // Nothing left to transmit.			 
	      }
       } 
    } 
 } 
 
 
 void main (void) 
{ 
   filltblout(); 
   // Pas d'entr?e analogique 
   // No analogue inputs
   ANSEL = 0; 
   ANSELH = 0; 
   
   // Configuration des Entr?es/Sorties 
   // Input / Output configuration
   TRISA = 0x0C; 
   TRISB = 0b11111111;     // 0xFF; 
   #ifdef avecnpn 
      TRISC = 0b11011110;  // 5E 
   #else 
      TRISC = 0b11011111;  // 5E 
   #endif 
   
   // Initialisation du Timer0 qui servira de timeout 
   // Initialise Timer0 to deliver a timeout
   // OPTION_REG : 
   // Bit 7 = 1 -- Pull-up disabled 
   // Bit 6 = 0 -- Interruption sur front descendant de Int 
   //              Interrupt on falling edge
   // Bit 5 = 0 -- Timer sur clock Interne 
   //              Timer delivered from internal clock
   // Bit 4 = 0 -- N/A 
   // Bit 3 = 0 -- Prescaler sur timer 0 
   //              Prescaler for Timer0
   // Bits 2-0 = 011 -- Prescaler ? 16 
   //                   Prescaler is 16
   // Soit un timer egal ? 16*255us = 4ms 
   //                                 4ms
   OPTION = 0b00000011;  //Bit7 = 1 pull-ups disabled 
   
   WPUA  = 0;  // Pas de pull-ups sur port A
               // No pull up at port A   
   WPUB  = 0;  // Pas de pull-ups sur port B 
               // No pull up at port B
   WPUA2 = 1;  // Pull-up sur RA2 (RWIN) 
               // Pull up for RA2 (RWIN)
   
   enable_interrupts(); 
   PEIE  = 1;  // autoriser l'autorisation du SPI; 
               // Enable SPI
   
   LED1 = LEDOFF; 
   LED2 = LEDOFF; 
   LED3 = LEDOFF; 
   
   do_idle();   // se mettre en position d'attente du boitier 
                // Wait for body
   while (1) 
   { 
   
   
   } 
} 