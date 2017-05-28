import org.graphstream.graph.*;
import org.graphstream.algorithm.generator.*;
import org.graphstream.algorithm.*;
import org.graphstream.graph.implementations.*;
import java.util.*;
import org.graphstream.stream.*;



public class AntSystem {
	private static final double zo=0.7;
	public static class Ant{
		private int antID;
		private String antName;
		private Node currNode;
		private List<String> path;
		private Node startVertex;
		private Node endIDVertex;
		private double cost;
		private List<String> bestpath;
		private double bestcost;

		private final double alpha=2.0;
		private final double beta=3.0;

		private String deltaname;

		Ant(Graph graph, int antID, String antName, Node startVertex, Node endIDVertex ){
			this.antID = antID;
			this.antName = antName;
			this.startVertex = startVertex;
			this.endIDVertex = endIDVertex;
			this.currNode = startVertex;
			this.path = new ArrayList<String>();
			this.bestpath = new ArrayList<String>();
			this.bestcost = 10000;
			this.deltaname = "delta"+Integer.toString(antID);
			currNode.setAttribute("visited");
			path.add(currNode.toString());
			addDeltaToGraph(graph);
		}

		private void addDeltaToGraph(Graph graph){
			for(int i=0;i<100;i++){
			Node node = graph.getNode(i);
			for(Edge edge: graph.getEachEdge()){
				if(!edge.hasAttribute(deltaname)){
					edge.addAttribute(deltaname,0.0);
				}
			}
		}
	
		}

		public double getbestcost(){
			return bestcost;
		}

		public double getcost(){
			return cost;
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

		public List<String> getbestpath(){
			return bestpath;
		}

		public void clear(Graph graph){
			this.currNode = startVertex;
			for(String id: path){
				Node node = graph.getNode(id);
				node.removeAttribute("visited");
			}
			this.path = new ArrayList<String>();
			path.add(currNode.toString());
			cost = 0;
		}

		public double getCost(){
			return cost;
		}

		public void addToPath(){

		}
		public String toString(){
			return "nope";
		}

		private void moveTo(Node target){
			if(!target.hasAttribute("visited")){
				this.currNode = target;
				target.setAttribute("visited");
				path.add(target.toString());
			}
		}

		private void walk(Node node, Edge edge){
				moveTo(edge.getOpposite(node));
				double weight = edge.getAttribute("weight");
				cost+= weight;
				edge.setAttribute(deltaname, 1/cost);
		}

		private boolean halt(Graph graph, Edge edge){
			Random random = new Random();
			double  n = random.nextDouble();
			if(n < caculate_propability(edge, graph)){
				return true;
			}
			return false;
		}

		private double caculate_propability(Edge edge, Graph graph){
			double thetaAbove = edge.getAttribute("theta");
			double num1Above = Math.pow(thetaAbove, alpha);
			double nuyAbove = edge.getAttribute("nuy");
			double num2Above = Math.pow(nuyAbove, beta);
			double numberAbove = num1Above*num2Above;
			double sum = 0.0;
			for(Edge e: graph.getEachEdge()){
				if(!e.hasAttribute("caculate_propability")){
					e.addAttribute("caculate_propability");
					double theta = e.getAttribute("theta");
					double num1 = Math.pow(theta, alpha);
					double nuy = e.getAttribute("nuy");
					double num2 = Math.pow(nuy, beta);
					double number = num1*num2;
					sum+=number;
				}
			}
			if(sum==0){
				return 1.0;
			}else{
				return numberAbove/sum;
			}
		}

		public boolean move(Graph graph){
			Node previous_node = currNode; int count=0;
			while(currNode == previous_node){
				for(Edge edge: currNode.getEachEdge()){
					if(!edge.getOpposite(currNode).hasAttribute("visited")){						
						if(halt(graph, edge)){
							walk(currNode, edge);
							break;
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

	public static void addPhormone(Graph graph, int numberOfAnt){
		for(Edge edge: graph.getEachEdge()){
			double sum = calculateDeltaSum(edge, numberOfAnt);
			edge.setAttribute("theta", (1-zo)*sum);
		}
	}

	public static double calculateDeltaSum(Edge edge, int numberOfAnt){
		double sum=0;
		for(int i=0; i<numberOfAnt;i++){
			double num = edge.getAttribute("deltaname"+Integer.toString(i));
			sum+=num;
		}
		return sum;
	}

	public static void makeGraph(Graph graph){
		// Add weight for edges 
		for(Edge edge: graph.getEachEdge()){
			edge.setAttribute("weight", 1.0);
		}
		
		//Add nuy for edges
		for(Edge edge: graph.getEachEdge()){
			double weight = edge.getAttribute("weight");
			edge.setAttribute("nuy", 1.0/weight);
		}

		//Add theta for edges
		for(Edge edge: graph.getEachEdge()){
			edge.setAttribute("theta", 0.0);
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
		
		makeGraph(graph);
		//make dymanicity
		Random rand = new Random();

		Node a = graph.getNode(0);
		Node b = graph.getNode(11);
		
		Ant ant0 = new Ant(graph, 0, "ant0", a, b);

		for(int i=0; i<10; i++){
			double num = 2*rand.nextDouble();
			for(Edge edge: graph.getEachEdge()){
				int change = rand.nextInt(2);
				if(change==0){
					double weight = edge.getAttribute("weight");
					edge.setAttribute("weight", num*weight);
				}
			}
			ant0.iterate(graph);
			System.out.println(ant0.getbestpath().toString()+":::::::::::"+
			String.valueOf(ant0.getbestcost()));
			ant0.clear(graph);
			System.out.println("================================================");
		}
		
	}
}




