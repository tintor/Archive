package scrap;

import geometry.base.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Polyhedron {
	final List<Vertex> vertices = new ArrayList<Vertex>();
	final List<Edge> edges = new ArrayList<Edge>();
	final List<Face> faces = new ArrayList<Face>();
}

class Vertex {
	Vector3 position;
	final Map<Vertex, Edge> edges = new HashMap<Vertex, Edge>();
	final List<Face> faces = new ArrayList<Face>();
}

class Face {
	final List<Vertex> vertices = new ArrayList<Vertex>();
	final List<Edge> edges = new ArrayList<Edge>();
}

class Edge {
	Vertex vertexA, vertexB;
	Face faceA, faceB;
}