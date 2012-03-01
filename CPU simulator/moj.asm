load #100 ; pocinje!!!
def loop1 0xbaad ;; test ove direktive...
store r3
loop:
	;;; aj' sad da napravim nekakvu gresku, da vidim da li radi sistem prijave...
loop0: asr r0[loop1]; hehehe...
load r3
sub #0x1
store r2
store r3
inte ; sta li mu se ne svidja u bezadresnim instrukcijama?
jnz #loop
jsr #subRoutine
org 0x200
subRoutine: load r0
store r1
not #0132
halt