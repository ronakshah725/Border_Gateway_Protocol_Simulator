import java.util.HashMap;

public class FormMap {

	int[][] adjacencyMatrix = null;
	HashMap<Integer, int[]> linkToRoutersConn = null;
	static int min= 0;
	int matrixLength = 0;

	int[][] shortestPathRrouterLinks;
	int[][] shortestPathNextHops;
	int[][] networksToUse;


	public FormMap(int[][] adjacencyMatrix, HashMap<Integer, int[]> linkToRoutersConn){

		this.adjacencyMatrix = adjacencyMatrix;
		this.linkToRoutersConn = linkToRoutersConn;
		matrixLength = adjacencyMatrix.length;
	}

	// calculates shortest path between each node
	public int[][] calculateShortestPath(){

		int[][] shortestPathNodes = new int[matrixLength][matrixLength];
		int[][] nextHops = new int[matrixLength][matrixLength];


		int i, j, k;

		for (i=0; i<matrixLength; i++){
			for (j=0; j<matrixLength; j++){
				if((adjacencyMatrix[i][j]!=0) && (adjacencyMatrix[i][j]!=9999)){
					shortestPathNodes[i][j] = 1;
				} else {
					shortestPathNodes[i][j] = adjacencyMatrix[i][j];
				}

				if(adjacencyMatrix[i][j]!=9999){
					nextHops[i][j] = j;
				} else {
					nextHops[i][j] = -1;
				}
			}
		}



		for (k=0; k<matrixLength; k++){
			for (i=0; i<matrixLength; i++){
				for (j=0; j<matrixLength; j++){
					if (shortestPathNodes[i][k] + shortestPathNodes[k][j] < shortestPathNodes[i][j]){
						shortestPathNodes[i][j] = shortestPathNodes[i][k] + shortestPathNodes[k][j];
						nextHops[i][j] = nextHops[i][k];
					}
				}
			}
		}
		//	    printNextHops(nextHops);

		return (calculateShortestPathNodesLinks(shortestPathNodes, nextHops, linkToRoutersConn));
	}

	// calculates shortest path between a router and a link
	private int[][] calculateShortestPathNodesLinks(int[][] shortestPathNodes, int[][] nextHops, HashMap<Integer, int[]> linkToRoutersConn){
		shortestPathRrouterLinks = new int[shortestPathNodes.length][linkToRoutersConn.size()];
		shortestPathNextHops = new int[shortestPathNodes.length][linkToRoutersConn.size()];
		networksToUse = new int[shortestPathNodes.length][linkToRoutersConn.size()];


		for(int i=0 ; i<shortestPathNodes.length ; i++){
			for(int j=0 ; j<linkToRoutersConn.keySet().size()  ; j++){
				int linkId = j+1;
				min = 99;
				int nextHop = -1;
				for(int k=0 ; k<linkToRoutersConn.get(linkId).length ; k++){
					int routerId = this.linkToRoutersConn.get(linkId)[k];
					if(min> shortestPathNodes[i][routerId-1]){
						nextHop = nextHops[i][routerId-1];
						min = shortestPathNodes[i][routerId-1];
					} 
					shortestPathRrouterLinks[i][j] = min;
					shortestPathNextHops[i][j] = nextHop;
					if(nextHop!=-1){
						networksToUse[i][j] = adjacencyMatrix[i][nextHop];
					}
				}

			}
		}

		return shortestPathNodes;

	}

	private int compare(int i, int j) {
		if(i > j)
			return j;
		else
			return i;
	}

}


