package assembler.instruction;

public class InstructionSet {
    private static Instruction ii[] = {
        new NonAddressInstruction("RTS", 0xf0),
        new NonAddressInstruction("RTI", 0xf1), 
        new NonAddressInstruction("INTE", 0xf2),
        new NonAddressInstruction("INTD", 0xf3),
        new NonAddressInstruction("TRPE", 0xf4),
        new NonAddressInstruction("TRPD", 0xf5), 
        new NonAddressInstruction("STIVTP", 0xf6),
        new NonAddressInstruction("STIMR", 0xf7),
        new NonAddressInstruction("STSP", 0xf8),
        new NonAddressInstruction("HALT", 0xf9), 
        new IntInstruction("INT", 0x00),
        new JumpInstruction("JSR", 0x01),
        new JumpInstruction("JZ", 0x02),
        new JumpInstruction("JNZ", 0x03),
        new JumpInstruction("JV", 0x04),
        new JumpInstruction("JC", 0x05),
        new JumpInstruction("JNEG", 0x06),
        new JumpInstruction("JMP", 0x07),
        new AddressInstruction("LOAD", 0x10), 
        new AddressInstruction("STORE", 0x20),
        new AddressInstruction("NOT", 0x30),
        new AddressInstruction("OR", 0x40),
        new AddressInstruction("XOR", 0x50), 
        new AddressInstruction("AND", 0x60),
        new AddressInstruction("ASL", 0x70),
        new AddressInstruction("ASR", 0x80),
        new AddressInstruction("LSR", 0x90), 
        new AddressInstruction("ADD", 0xA0),
        new AddressInstruction("SUB", 0xB0),
        new AddressInstruction("CMP", 0xC0),
        new AddressInstruction("INC", 0xD0), 
        new AddressInstruction("DEC", 0xE0),
        new DefineInstruction("DB", 1),
        new DefineInstruction("DW", 2)
    };

    public static Instruction GetInstruction(String name) { 
        for(int i = 0; i < ii.length; i++) {
            if(ii[i].equals(name)) return ii[i];
        }
        return null;
    }
} 