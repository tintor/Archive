package weaver;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class ExampleCreator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public ExampleCreator() {
    _cg = new ClassGen("weaver.Example", "java.lang.Object", "Example.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void create(OutputStream out) throws IOException {
    createMethod_0();
    createMethod_1();
    _cg.getJavaClass().dump(out);
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "weaver.Example", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, new ObjectType("tintor.geometry.Vector3"), new Type[] { new ObjectType("tintor.geometry.Vector3"), new ObjectType("tintor.geometry.Vector3"), new ObjectType("tintor.geometry.Vector3") }, new String[] { "arg0", "arg1", "arg2" }, "main", "weaver.Example", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createNew("tintor.geometry.Vector3"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FADD);
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FADD);
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FADD);
    il.append(_factory.createInvoke("tintor.geometry.Vector3", "<init>", Type.VOID, new Type[] { Type.FLOAT, Type.FLOAT, Type.FLOAT }, Constants.INVOKESPECIAL));
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_35 = il.append(_factory.createNew("tintor.geometry.Vector3"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "x", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FSUB);
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "y", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FSUB);
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess("tintor.geometry.Vector3", "z", Type.FLOAT, Constants.GETFIELD));
    il.append(InstructionConstants.FSUB);
    il.append(_factory.createInvoke("tintor.geometry.Vector3", "<init>", Type.VOID, new Type[] { Type.FLOAT, Type.FLOAT, Type.FLOAT }, Constants.INVOKESPECIAL));
    InstructionHandle ih_69 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    weaver.ExampleCreator creator = new weaver.ExampleCreator();
    creator.create(new FileOutputStream("weaver.Example.class"));
  }
}
