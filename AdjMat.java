import java.util.ArrayList;
import java.util.HashMap;

public class AdjMat {

	private HashMap<Integer, ArrayList<Integer>> routerToLinksMap = null;
	HashMap<Integer, int[]> linksToRoutersArrMap = null;
	public AdjMat(HashMap<Integer, ArrayList<Integer>> routerToLinksMap){
		this.routerToLinksMap = routerToLinksMap;
	}

	public int[][] getAdjacencyMatrix(){

		HashMap<Integer, ArrayList<Integer>> linkToRoutersMap = new HashMap<Integer, ArrayList<Integer>>();

		for(int i : routerToLinksMap.keySet()){
			for(int j :  routerToLinksMap.get(i)){
				if(null != linkToRoutersMap && linkToRoutersMap.containsKey(j)){
					linkToRoutersMap.get(j).add(i);
				}
				else{
					ArrayList<Integer> routerList = new ArrayList<Integer>();
					routerList.add(i);
					linkToRoutersMap.put(j, routerList);
				}
			}
		}

		int noOfRouters = routerToLinksMap.size();

		// adjacency matrix of routers
		int[][] adjacencyMatrix = new int[noOfRouters][noOfRouters];

		// initialize the matrix
		for(int i= 0; i< noOfRouters ;i++){
			for(int j=0 ;j<noOfRouters ;j++){
				if(i == j)
					adjacencyMatrix[i][j] = 0;
				else
					adjacencyMatrix[i][j] = 9999;
			}
		}

		linksToRoutersArrMap = new HashMap<Integer, int[]>();
		for(int linkId : linkToRoutersMap.keySet()){
			int routersSize = linkToRoutersMap.get(linkId).size();
			int[] arrayRouters = new int[routersSize];
			int x =0;
			for(int routerId : linkToRoutersMap.get(linkId)){
				arrayRouters[x] = routerId;
				x++;
			}
			linksToRoutersArrMap.put(linkId, arrayRouters);
			for(int m =0 ; m < arrayRouters.length ; m++){
				for(int n = 0 ; n<arrayRouters.length; n++ ){
					if(m!=n){
						adjacencyMatrix[arrayRouters[m]-1][arrayRouters[n]-1] = linkId;
						adjacencyMatrix[arrayRouters[n]-1][arrayRouters[m]-1] = linkId;
					}
				}
			}
		}

//		for (int i = 0; i < 5; i++){
//			for (int j = 0; j < 5; j++){
//				System.out.println ("***********"+ adjacencyMatrix[i][j]);
//			}
//			System.out.println("\n");
//		}
		return adjacencyMatrix;
	}	
	
	public HashMap<Integer, int[]> getLinksToRouterArrMap(){
		return linksToRoutersArrMap;
	}

}


