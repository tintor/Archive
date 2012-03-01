ORG 0x100

load #1000h
stsp ; sp<-1000h

load #0 
store r0     ; r0=0

load #0200h  ;  na lokaciju 0006h (ulaz 3 u IVT) se stavlja
store r0[6]  ;  broj 200h , a to je adresa prekidne rutine

load #ffh    ;  na lokaciju 300h se stavlja
store r0[300h]  ;  broj FFh, a to je nepostojeci kod operacije


jsr #300h ; 


load #0007h  
store r1     ; r0=7;
inc r1	     ; r0=8;		

jsr #loop
halt

loop:  	dec r1
	load r1
	cmp #1
	jnz #loop 	
	ASL r1
	Load r1
	add r1
	rts



ORG 0x200 ; prekidna rutina na lokaciju 300h stavlja nov legalan kod
 	  ; operacije RTS	

load #f0h 
store r0[300h]
rti


; na kraju treba da je ACC=4 i R1=2