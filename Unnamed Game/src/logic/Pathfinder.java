package logic;

import java.util.ArrayList;

import graphics.Renderer;
import graphics.primitives.Primitive.Primitives;
import settings.CompiledSettings;
import graphics.primitives.VertexData;
import utility.Logger;
import utility.vec2;
import utility.vec4;

public class Pathfinder {
	
	private static Renderer debugRenderer = null;
	
	public boolean path_found;
	public boolean failed;
	
	private class Node {
		public boolean visited = false;
		public boolean open = false;
		public boolean walkable;

		public float g_cost;
		public float h_cost;
		
		public int x;
		public int y;

		ArrayList<Node> neighbours;
		Node parent;

		public float f_cost() { 
			return g_cost + h_cost; 
		}

		public Node(int x, int y, boolean walkable) {
			this.x = x;
			this.y = y;
			this.walkable = walkable;
			neighbours = new ArrayList<Node>();
		}
	};
	private Node[][] nodemap;

	private ArrayList<Node> openSet;
	//private ArrayList<Node> closedSet;
	
	private Node startnode;
	private Node endnode;
	private Node curnode;

	
	private float distance(Node nodeA, Node nodeB) {
		float dx = Math.abs(nodeA.x - nodeB.x);
		float dy = Math.abs(nodeA.y - nodeB.y);

		if (dx > dy) return 1.41f * dy + dx - dy;
		return 1.41f * dx + dy - dx;
	}

	public Pathfinder(int startnode_x, int startnode_y, int endnode_x, int endnode_y, int grid_scale) {
		path_found = false;
		failed = false;
		int gridWidth = Math.abs(startnode_x - endnode_x) + 2 * grid_scale;
		int gridHeight = Math.abs(startnode_y - endnode_y) + 2 * grid_scale;
		int gridTopLeftX = Math.min(startnode_x, endnode_x) - grid_scale;
		int gridTopLeftY = Math.min(startnode_y, endnode_y) - grid_scale;
		nodemap = new Node[gridWidth][gridHeight];
		openSet = new ArrayList<Node>();
		
		//Setup nodemap
		for (int x = 0; x < gridWidth; x++)
		{
			for (int y = 0; y < gridHeight; y++)
			{
				int mapX = x + gridTopLeftX;
				int mapY = y + gridTopLeftY;
				
				nodemap[x][y] = new Node(mapX, mapY, !Database.getObject(Main.game.getMap().getTile(mapX, mapY).object).wall);

				//Startnode
				if (mapX == startnode_x && mapY == startnode_y) startnode = nodemap[x][y];
				
				//Endnode
				if (mapX == endnode_x && mapY == endnode_y) endnode = nodemap[x][y];
			}
		}

		for (int x = 0; x < nodemap.length; x++)
		{
			for (int y = 0; y < nodemap[x].length; y++)
			{
				
				for (int xd = -1; xd < 2; xd++) {
					for (int yd = -1; yd < 2; yd++) {
						if (x + xd >= 0 && x + xd < nodemap.length && y + yd >= 0 && y + yd < nodemap[x].length && !(xd == 0 && yd == 0)) {
							Node neighbour = nodemap[x + xd][y + yd];
							nodemap[x][y].neighbours.add(neighbour);
						}
					}
				}
				
			}
		}

		if (startnode == null || endnode == null) {
			Logger.error("Startnode or endnode lost");
			failed = true;
		}

		//Sets
		openSet.add(startnode);

	}

	public int step() {
		if (openSet.size() <= 0) return -1;

		int curnodeindex = 0;
		curnode = openSet.get(0);
		for (int i = 1; i < openSet.size(); i++)
		{
			if (openSet.get(i).f_cost() < curnode.f_cost() || (openSet.get(i).f_cost() == curnode.f_cost() && openSet.get(i).h_cost < curnode.h_cost)) {
				curnodeindex = i;
				curnode = openSet.get(i);
			}
		}

		curnode.visited = true;
		openSet.remove(curnodeindex);

		if (curnode == endnode) {
			path_found = true;
			return 1;
		}

		for (int i = 0; i < curnode.neighbours.size(); i++)
		{
			if (!curnode.neighbours.get(i).walkable || curnode.neighbours.get(i).visited) {
				continue;
			}

			float neighbourNewCost = curnode.g_cost + distance(curnode, curnode.neighbours.get(i));
			if (neighbourNewCost < curnode.neighbours.get(i).g_cost || !curnode.neighbours.get(i).open) {
				curnode.neighbours.get(i).g_cost = neighbourNewCost;
				curnode.neighbours.get(i).h_cost = distance(curnode.neighbours.get(i), endnode);
				curnode.neighbours.get(i).parent = curnode;

				if (!curnode.neighbours.get(i).open) {
					curnode.neighbours.get(i).open = true;
					openSet.add(curnode.neighbours.get(i));
				}
			}
		}
		return 0;
	}

	public int runNSteps(int n) {
		for (int i = 0; i < n; i++)
		{
			int result = step();

			if (result == 1) return 1;
			if (result == 0) continue;
			if (result == -1) return -1;
		}
		return 0;
	}

	public int runUnitlEnd() {
		int result = 0;
		while (openSet.size() > 0 && path_found == false)
		{
			result = step();
		}
		return result;
	}

	public void debugRender(float offx, float offy) {
		if (debugRenderer == null) debugRenderer = new Renderer(Primitives.Line);
		
		Node curNode = curnode;
		vec4 lineColor = new vec4(1.0f, 0.5f, 0.0f, 1.0f);

		vec2 pos = new vec2((curNode.x + offx + 0.5f), (curNode.y + offy + 0.5f)).mul(CompiledSettings.TILE_SIZE);
		debugRenderer.submit(new VertexData(pos, new vec2(0.0f), 0, lineColor));
		
		while (curNode != startnode) {
			pos = new vec2((curNode.x + offx + 0.5f), (curNode.y + offy + 0.5f)).mul(CompiledSettings.TILE_SIZE);

			debugRenderer.submit(new VertexData(pos, new vec2(0.0f), 0, lineColor));
			debugRenderer.submit(new VertexData(pos, new vec2(0.0f), 0, lineColor));

			curNode = curNode.parent;
		}

		pos = new vec2((startnode.x + offx + 0.5f), (startnode.y + offy + 0.5f)).mul(CompiledSettings.TILE_SIZE);
		debugRenderer.submit(new VertexData(pos, new vec2(0.0f), 0, lineColor));
	}

	public ArrayList<vec2> retrace() {
		ArrayList<vec2> path = new ArrayList<vec2>();

		Node curNode = curnode;
		while (curNode != startnode) {
			path.add(new vec2(curNode.x, curNode.y));
			curNode = curNode.parent;
		}
		return path;
	}
	
}
