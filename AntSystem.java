import org.graphstream.graph.*;
import org.graphstream.algorithm.generator.*;
import org.graphstream.algorithm.*;
import org.graphstream.graph.implementations.*;
import java.util.*;
import org.graphstream.stream.*;
import org.graphstream.algorithm.Dijkstra;




public class AntSystem {
	private static final double maxcost=1000000;
	private static final double zo=0.7;
	public static class Ant{
		private int antID;
		private String antName;
		private Node currNode;
		private List<String> path;
		private List<Edge> edgePath;
		private Node startVertex;
		private Node endIDVertex;
		private double cost;
		private List<String> bestpath;
		private double bestcost;

		private final double alpha=2.0;
		private final double beta=3.0;

		private String deltaname;
// Init
		Ant(Graph graph, int antID, String antName, Node startVertex, Node endIDVertex ){
			this.antID = antID;
			this.antName = antName;
			this.startVertex = startVertex;
			this.endIDVertex = endIDVertex;
			this.currNode = startVertex;
			this.path = new ArrayList<String>();
			this.bestpath = new ArrayList<String>();
			this.bestcost = maxcost;
			this.deltaname = "delta"+Integer.toString(antID);
			this.edgePath = new ArrayList<Edge>();
			currNode.setAttribute("visited");
			path.add(currNode.toString());
			addDeltaToGraph(graph);
		}
// Them delta cua kien cho graph
		private void addDeltaToGraph(Graph graph){
			for(Edge edge: graph.getEachEdge()){
				if(!edge.hasAttribute(deltaname)){
					if(cost==0) edge.addAttribute(deltaname,0.0);
					else edge.setAttribute(deltaname, 1.0/getcost());
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
			double sum = 0.0;
			for(Edge edge: edgePath){
				double weight = edge.getAttribute("weight");
				sum+=weight;
			}
			cost = sum;
			return cost;
		}

		public void addToPath(){

		}
		public String toString(){
			return "nope";
		}

		private void moveTo(Node target){//Di den node, ham phu cho walk
			if(!target.hasAttribute("visited")){
				this.currNode = target;
				target.setAttribute("visited");
				path.add(target.toString());
			}
		}

		private void walk(Node node, Edge edge){// Thuc hien di sau khi da quyet dinh
				moveTo(edge.getOpposite(node));
				double weight = edge.getAttribute("weight");
			//Cap nhat moi cho Kien
				edgePath.add(edge);//Them canh vao path
				cost= getCost();// Them chi phi
				edge.setAttribute(deltaname, 1/cost);//Cap nhat delta cua kien
		}

		private boolean halt(Graph graph, Edge edge){//Quyet dinh xem co di ko theo xac suat dc tinh
			Random random = new Random();
			double  n = random.nextDouble();
			if(n < caculate_propability(edge, graph)){
				return true;
			}
			return false;
		}

		private double caculate_propability(Edge edge, Graph graph){//Tinh xac suat theo cong thuc o paper
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
			Random rand = new Random();
			while(currNode == previous_node){
				int r1 = rand.nextInt(10);
				if(r1<5){//Xac suat 50% di bua
					for(Edge edge: currNode.getEachEdge()){
						int r2 = rand.nextInt(50);
						if(r2<10){
							walk(currNode, edge);
							break;
						}
					}
				}
				for(Edge edge: currNode.getEachEdge()){
					if(!edge.getOpposite(currNode).hasAttribute("visited")){						
						if(halt(graph, edge)){//Neu ham halt tra ve true thi di qua canh nay
							walk(currNode, edge);
							break;
						}
					}
					++count;
					if(count > 100){//Lap qua nhieu ma khong duoc thi di duong khac, do thi to thi cho to ra
						//Vidu count>1000
						clear(graph);//Cac thong so tro lai luc dau
						break;
					}
				}
				if(count==0) return false;  
			}
			
			return true;
		}
//lap lien tuc cho den khi den diem cuoi (endIDVertex)
		public void iterate(Graph graph){
			while(currNode!=endIDVertex){
				addDeltaToGraph(graph);//Them delta
				move(graph);//Kien di chuyen trong graph
				addPhormone(graph, 1);
			}
			if(cost<bestcost){
				bestcost = cost;
				bestpath = path;
			}
		} 
	}
//cap nhat theta cho ca graph voi tat ca kien
	public static void addPhormone(Graph graph, int numberOfAnt){
		for(Edge edge: graph.getEachEdge()){
			double theta = edge.getAttribute("theta");
			double sum = calculateDeltaSum(edge, numberOfAnt);
			edge.setAttribute("theta", theta+(1-zo)*sum);
		}
	}
//tinh tong cac delta
	public static double calculateDeltaSum(Edge edge, int numberOfAnt){
		double sum=0;
		for(int i=0; i<numberOfAnt;i++){
			double num = edge.getAttribute("delta"+Integer.toString(i));
			sum+=num;
		}
		return sum;
	}
//Khoi tao graph voi cac thong so weight, nuy, theta
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
		Graph graph = new SingleGraph("Random");
		    Generator gen = new RandomGenerator(50);
		    gen.addSink(graph);
		    gen.begin();
		    for(int i=0; i<100; i++)
		        gen.nextEvents();
		    gen.end();
		
		makeGraph(graph);
		//make dymanicity
		Random rand = new Random();


		Node a = graph.getNode(0);
		Node b = graph.getNode(1);
		
		Ant ant0 = new Ant(graph, 0, "ant0", a, b);

		for(int i=0; i<20; i++){
			ant0.iterate(graph);
			//dynamicity co the bo de test graph tinh
			double num = rand.nextDouble();
			for(Edge edge: graph.getEachEdge()){
				int change = rand.nextInt(200);
				if(change==0){
					double weight = edge.getAttribute("weight");
					edge.setAttribute("weight", weight+num);
				}else if(change==1){
					double weight = edge.getAttribute("weight");
					if(weight-num>=0){
						edge.setAttribute("weight", weight-num);
					}
				}
				double weight = edge.getAttribute("weight");
				edge.setAttribute("nuy",1.0/weight);
				//edge.setAttribute("delta0", 0.0);
			}
			//dynamicity

			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
			dijkstra.init(graph);
			dijkstra.setSource(a);
			dijkstra.compute();
			System.out.println("dijkstra:::::"+dijkstra.getPath(b));
			System.out.printf("dijkstra::::::: %s->%s:%10.2f%n", dijkstra.getSource(), b,
								dijkstra.getPathLength(b));

			System.out.println(ant0.getbestpath().toString()+":::::::::::"+
			String.valueOf(ant0.getbestcost()));
			ant0.clear(graph);
			System.out.println("================================================");
		}
		
	}
}
//



