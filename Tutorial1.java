import org.graphstream.graph.*;
import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.implementations.*;
import java.util.*;



public class Tutorial1 {
	public static class Ant{
		private int antID;
		private String antName;
		private Node currNode;
		private List<String> path;
		private Node startVertex;
		private Node endIDVertex;
		private int cost;
		private List<String> bestpath;
		private int bestcost;

		Ant(int antID, String antName, Node startVertex, Node endIDVertex ){
			this.antID = antID;
			this.antName = antName;
			this.startVertex = startVertex;
			this.endIDVertex = endIDVertex;
			this.currNode = startVertex;
			this.path = new ArrayList<String>();
			this.bestpath = new ArrayList<String>();
			this.bestcost = 10000;
			currNode.setAttribute("visited");
			path.add(currNode.toString()); 
		}

		public int getAntID(){
			return antID;
		}
		
		public String getName(){
			return antName;
		}

		public Node getcurrentNode(){
			return currNode;
		}

		public List<String> getpath(){
			return path;
		}

		public void clear(Graph graph){
			this.currNode = startVertex;
			for(String nodename: path){
				Node node = graph.getNode(nodename);
				node.clearAttributes();
			}
			this.path = new ArrayList<String>();
			cost = 0;
		}

		public void addPhormone(){

		}

		public int getCost(){
			return cost;
		}

		public void addToPath(){

		}
		public String toString(){
			return "nope";
		}

		private void moveTo(Node target){
			if(!target.hasAttribute("visited")){
			//	System.out.println(currNode.toString());
				this.currNode = target;
				target.setAttribute("visited");
				path.add(target.toString());
			}
		}

		private void walk(Node node, Edge edge){
				moveTo(edge.getOpposite(node));
				//edge.setAttribute("visited");
				cost+=1;
		}

		private boolean halt(Edge edge){
			Random random = new Random();
			int n = random.nextInt(3);
			if(n==0){
				return true;
			}
			return false;
		}

		public boolean move(Graph graph){
			Node previous_node = currNode; int count=0;
			while(currNode == previous_node){
				for(Edge edge: currNode.getEachEdge()){
					System.out.println(edge.toString()+"----cur::::"+currNode.toString());
					if(!edge.getOpposite(currNode).hasAttribute("visited")){
						if(!edge.hasAttribute("visited")){
							if(halt(edge)){
								walk(currNode, edge);
								break;
							}
						}
					}
					++count;
					if(count > 100){
						clear(graph);
						break;
					}
				}
				if(count==0) return false;  
			}
			return true;
		}

		public void iterate(Graph graph){
			while(currNode!=endIDVertex){
				move(graph);
			}
			if(cost<bestcost){
				bestcost = cost;
				bestpath = path;
			}
		} 
	}
	public static void main(String args[]) {
		Graph graph = new SingleGraph("grid");
		Generator gen = new GridGenerator();
		gen.addSink(graph);
		gen.begin();

		for(int i=0; i<10; i++) {
			gen.nextEvents();
		}
		gen.end();
		//graph.display();
		Node a = graph.getNode(0);
		//System.out.println(a.toString());
		Node b = graph.getNode(10);
		//System.out.println(b.toString());
		Ant ant1;
		/*for(Edge edge: a.getEachEdge()){
			System.out.println(edge.toString());
			//System.out.println(edge.getOpposite(a).toString());
		}*/
		ant1 = new Ant(0, "aaa", a, b);
		ant1.iterate(graph);
		System.out.println(ant1.getpath().toString());
		//graph.display();
	}
}


