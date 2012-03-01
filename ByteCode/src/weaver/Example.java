package weaver;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import tintor.geometry.Vector3;

public class Example {
	public static Vector3 func(final Vector3 a, final Vector3 b, final Vector3 c) {
		final Vector3 r = new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
		return new Vector3(r.x - c.x, r.y - c.y, r.z - c.z);
	}

	public static void main(final String[] args) throws Exception {
		final JavaClass javaClass = new ClassParser("bin\\weaver\\Example.class").parse();

		final ClassGen cg = new ClassGen(javaClass);
		final ConstantPoolGen cpg = cg.getConstantPool();

		for (final Method method : cg.getMethods())
			if (method.getName().equals("func")) {
				final MethodGen mg = new MethodGen(method, cg.getClassName(), cpg);
				final InstructionList il = mg.getInstructionList();
				for (final InstructionHandle h : il.getInstructionHandles())
					if (h.getInstruction() instanceof INVOKESPECIAL) {
						final INVOKESPECIAL i = (INVOKESPECIAL) h.getInstruction();
						System.out.println("invokespecial # " + h.getPosition());
						if (i.getMethodName(cpg).equals("<init>")
								&& i.getReferenceType(cpg).toString().equals(Vector3.class.getName()))
							System.out.println(i.getMethodName(cpg));
						System.out.println(i.getReferenceType(cpg));

					}
			}
	}
}