package weaver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public class ExampleCreator implements Constants {
	private final InstructionFactory _factory;
	private final ConstantPoolGen _cp;
	private final ClassGen _cg;

	public ExampleCreator() {
		_cg = new ClassGen("weaver.Example", "java.lang.Object", "Example.java", ACC_PUBLIC | ACC_SUPER, new String[] {});

		_cp = _cg.getConstantPool();
		_factory = new InstructionFactory(_cg, _cp);
	}

	public void create(final OutputStream out) throws IOException {
		createMethod_1();
		_cg.getJavaClass().dump(out);
	}

	private void createMethod_1() {
		final InstructionList il = new InstructionList();
		final MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, new ObjectType("tintor.geometry.Vector3"),
				new Type[] { new ObjectType("tintor.geometry.Vector3"), new ObjectType("tintor.geometry.Vector3"),
						new ObjectType("tintor.geometry.Vector3") }, new String[] { "arg0", "arg1", "arg2" },
				"main", "weaver.Example", il, _cp);

		il.append(_factory.createNew("tintor.geometry.Vector3"));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FADD);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FADD);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FADD);
		il.append(_factory.createInvoke("tintor.geometry.Vector3", "<init>", Type.VOID, new Type[] { Type.FLOAT,
				Type.FLOAT, Type.FLOAT }, Constants.INVOKESPECIAL));
		il.append(InstructionFactory.createStore(Type.OBJECT, 3));
		il.append(_factory.createNew("tintor.geometry.Vector3"));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FSUB);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FSUB);
		il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
		il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
		il.append(InstructionConstants.FSUB);
		il.append(_factory.createInvoke("tintor.geometry.Vector3", "<init>", Type.VOID, new Type[] { Type.FLOAT,
				Type.FLOAT, Type.FLOAT }, Constants.INVOKESPECIAL));
		il.append(InstructionFactory.createReturn(Type.OBJECT));
		method.setMaxStack();
		method.setMaxLocals();
		_cg.addMethod(method.getMethod());
		il.dispose();
	}

	public static void main(final String[] args) throws Exception {
		final weaver.ExampleCreator creator = new weaver.ExampleCreator();
		creator.create(new FileOutputStream("weaver.Example.class"));
	}
}
