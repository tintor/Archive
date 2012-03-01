package weaver;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassLoader;
import org.apache.bcel.util.JavaWrapper;

public class Runner extends ClassLoader {
	@Override
	protected JavaClass modifyClass(final JavaClass clazz) {
		System.out.println("loading " + clazz.getClassName());
		ClassGen classGen = null;
		for (final Method method : clazz.getMethods())
			if (method.getName().startsWith("time")) {
				if (classGen == null) classGen = new ClassGen(clazz);
				addWrapper(classGen, method);
			}
		return classGen != null ? classGen.getJavaClass() : clazz;
	}

	static void inline(final ClassGen clazz, final ClassGen vector3) {
		for (final Method method : clazz.getMethods()) {
			final MethodGen mg = new MethodGen(method, clazz.getClassName(), clazz.getConstantPool());
			final InstructionList il = mg.getInstructionList();
			for (final InstructionHandle ih : il.getInstructionHandles()) {

			}
		}
	}

	public static void main(final String[] args) throws Exception {
		System.setProperty("bcel.classloader", Runner.class.getName());
		final String[] pargs = new String[args.length + 1];
		pargs[0] = Main.class.getName();
		System.arraycopy(args, 0, pargs, 1, args.length);
		JavaWrapper.main(pargs);
	}

	private static Object skeleton() {
		final long start = System.currentTimeMillis();
		Object result = null;
		try {
			result = skeleton();
		} finally {
			System.out.printf("Call to %s took %s ms.", "", System.currentTimeMillis() - start);
		}
		return result;
	}

	private static void addWrapper(final ClassGen cgen, final Method method) {
		// set up the construction tools
		final InstructionFactory ifact = new InstructionFactory(cgen);
		final InstructionList ilist = new InstructionList();
		final ConstantPoolGen pgen = cgen.getConstantPool();
		final String cname = cgen.getClassName();
		final MethodGen wrapgen = new MethodGen(method, cname, pgen);
		wrapgen.setInstructionList(ilist);

		// rename a copy of the original method
		final MethodGen methgen = new MethodGen(method, cname, pgen);
		cgen.removeMethod(method);
		final String iname = methgen.getName() + "$impl";
		methgen.setName(iname);
		cgen.addMethod(methgen.getMethod());
		final Type result = methgen.getReturnType();

		// compute the size of the calling parameters
		final Type[] types = methgen.getArgumentTypes();
		int slot = methgen.isStatic() ? 0 : 1;
		for (final Type type2 : types)
			slot += type2.getSize();

		// save time prior to invocation
		ilist.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
				Constants.INVOKESTATIC));
		ilist.append(InstructionFactory.createStore(Type.LONG, slot));

		// call the wrapped method
		int offset = 0;
		short invoke = Constants.INVOKESTATIC;
		if (!methgen.isStatic()) {
			ilist.append(InstructionFactory.createLoad(Type.OBJECT, 0));
			offset = 1;
			invoke = Constants.INVOKEVIRTUAL;
		}
		for (final Type type : types) {
			ilist.append(InstructionFactory.createLoad(type, offset));
			offset += type.getSize();
		}
		ilist.append(ifact.createInvoke(cname, iname, result, types, invoke));

		// store result for return later
		if (result != Type.VOID) ilist.append(InstructionFactory.createStore(result, slot + 2));

		// print time required for method call
		ilist.append(ifact.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"),
				Constants.GETSTATIC));
		ilist.append(InstructionConstants.DUP);
		ilist.append(InstructionConstants.DUP);
		final String text = "Call to method " + method.getName() + " took ";
		ilist.append(new PUSH(pgen, text));
		ilist.append(ifact.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		ilist.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
				Constants.INVOKESTATIC));
		ilist.append(InstructionFactory.createLoad(Type.LONG, slot));
		ilist.append(InstructionConstants.LSUB);
		ilist.append(ifact.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[] { Type.LONG },
				Constants.INVOKEVIRTUAL));
		ilist.append(new PUSH(pgen, " ms."));
		ilist.append(ifact.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));

		// return result from wrapped method call
		if (result != Type.VOID) ilist.append(InstructionFactory.createLoad(result, slot + 2));
		ilist.append(InstructionFactory.createReturn(result));

		// finalize the constructed method
		wrapgen.stripAttributes(true);
		wrapgen.setMaxStack();
		wrapgen.setMaxLocals();
		cgen.addMethod(wrapgen.getMethod());
		ilist.dispose();
	}
}