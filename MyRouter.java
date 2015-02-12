
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MyRouter {

    String myRID;
    int broadcastid = 01;
    ArrayList<IP> IPList = new ArrayList<IP>();

    //hashmap for discovered routers
    HashMap<String, MyRouter> RTMap = new HashMap<String, MyRouter>();
    static HashMap<String, ArrayList<String>> topMap = new HashMap<String, ArrayList<String>>();
    HashMap<String, Integer> filePointerMap = new HashMap<String, Integer>();

    public StringBuffer getNN() {
        StringBuffer networks = new StringBuffer();
        for (IP ip : IPList) {
            networks.append(ip.nn).append(" ");
        }
        return networks;
    }

    public void init() {
        String network = this.getNN().toString();
        String[] token = network.split("[ ]+");
        String[] Netfiles = new String[token.length];
        for (int i = 0; i < token.length; i++) {
            Netfiles[i] = "NET" + token[i] + ".txt";
        }
        for (String s : Netfiles) {
            filePointerMap.put(s, 0);
        }
    }
    public void readNNWriteOut(String fileName, String[] ind) throws IOException {
        HashMap<String, Integer> ridbid = new HashMap<>();
        FileReader fr = new FileReader(fileName);
        try (BufferedReader br = new BufferedReader(fr)) {
            String line;
            int currLine = 0;
            
            
            
            
            while ((line = br.readLine()) != null && filePointerMap.get(fileName) != null) {
                
                
                
                if (!(line.trim().equals(""))) {
                    ++currLine;
                    
                    
                    if (currLine > filePointerMap.get(fileName)) {
                        
                        
                        String[] elem = line.split("[ ,()]+");
                        String eachRT = elem[6];
                        
                        MyRouter rt;
                        
                        if (!RTMap.keySet().contains(eachRT)) {
                            
                            
                            rt = new MyRouter();
                            RTMap.put(eachRT, rt);
                            
                            
                        }
                        rt = RTMap.get(eachRT);
                        IP address = new IP(elem[1], elem[2]);
                        rt.IPList.add(address);
                        
                        int currentbroadcastid = Integer.parseInt(elem[7]);
                        if (line.contains("OPTIONS")) {
                            continue;
                        }
                        if (eachRT.equals(this.myRID)) {
                            continue;
                        }
                        int startBID = 0;
                        if (ridbid.containsKey(eachRT)) {
                            startBID = ridbid.get(eachRT);
                            if (currentbroadcastid > startBID) {
                                ridbid.put(eachRT, currentbroadcastid);
                            } else {
                                continue;
                            }
                        } else {
                            ridbid.put(eachRT, currentbroadcastid);
                            if (!(topMap.containsKey(eachRT))) {
                                String networklist = line.substring(33, line.length());
                                String deli = " ";
                                String[] list = networklist.split(deli);
                                ArrayList<String> net = new ArrayList<String>();
                                for (String e : list) {
                                    net.add(e);
                                }
                                String filene = "topology" + this.myRID + ".txt";
                                File filese = new File(filene);
                                //if file doesn't exists, then create it
                                if (!filese.exists()) {
                                    filese.createNewFile();
                                }
                                //true = append file
                                FileWriter fW = new FileWriter(filese.getName(), true);
                                BufferedWriter bWr = new BufferedWriter(fW);
                                topMap.put(eachRT, net);
                                bWr.append(eachRT);
                                bWr.append(net.toString());
                                bWr.write("\n");
                                bWr.close();
                            }
                        }
                        for (String s1 : ind) {
                            if (!s1.equals(elem[3])) {
                                String lsa = line.substring(0, 9) + s1 + line.substring(11, line.length());
                                String filename = "out" + this.myRID + ".txt";
                                File file = new File(filename);
                                //if file doesn't exists, then create it
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                //true = append file
                                FileWriter fileWritter = new FileWriter(file.getName(), true);
                                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                                bufferWritter.append(lsa);
                                bufferWritter.append("\n");
                                bufferWritter.close();
                            }
                        }
                    }
                }
            }
            filePointerMap.put(fileName, currLine);
        }
    }

    public void writeOUT() {
        try {
            String filename = "out" + this.myRID + ".txt";
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bufferWritter = new BufferedWriter(new FileWriter(file, true))) {

                String BIDString = String.format("%02d", broadcastid);
                for (IP s : IPList) {
                    String LSA = "(" + s.nn + "," + s.hh + ")" + " "
                            + "(" + s.nn + ",99)" + " " + "LSA" + " " + this.myRID + " "
                            + BIDString + " " + "NETWKS" + " " + getNN().toString();
                    bufferWritter.write(LSA);
                    bufferWritter.write("\n");
                }
                broadcastid++;
            }
        } catch (IOException ex) {
            // report
        }

    }

    public void readNNFiles() throws IOException {
        String[] ind = getNN().toString().split("[ ]+");
        String[] Netfiles = new String[ind.length];
        for (int i = 0; i < ind.length; i++) {
            Netfiles[i] = "NET" + ind[i] + ".txt";
        }
        File dir = new File(".");
        File[] files = dir.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            for (String s : Netfiles) {
                if (fileName.equals(s)) {
                    readNNWriteOut(fileName, ind);
                }
            }
        }
    }

    

    public void makeRTFile() throws IOException {
        ArrayList<String> foundNN = new ArrayList<String>();
        String filename = "RT" + this.myRID + ".txt";
        File file = new File(filename);
        //if file doesn't exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

 
        try (BufferedWriter bufferWritter = new BufferedWriter(new FileWriter(file, true))) {
            String rid = this.myRID;
            String network = this.getNN().toString();

            String[] ind = network.split("[ ]+");
            for (String s : ind) {
                foundNN.add(s);
            }   topMap.put(rid, foundNN);
        HashMap<Integer, ArrayList<Integer>> rtMapper = new HashMap<>();
            HashMap<String, ArrayList<String>> rtMapperString = MyRouter.topMap;
            for (String key : rtMapperString.keySet()) {
                ArrayList<Integer> toPut = new ArrayList<>();
                for (String s : rtMapperString.get(key)) {
                    toPut.add(Integer.valueOf(s));
                }
                rtMapper.put(Integer.valueOf(key), toPut);
            }   bufferWritter.write("\n" + "Network" + "\t\t" + "NextHop_Router" + "\t\t" + "TimeStamp" + "\n");
        AdjMat matGen = new AdjMat(rtMapper);
            FormMap topology = new FormMap(matGen.getAdjacencyMatrix(), matGen.getLinksToRouterArrMap());
            topology.calculateShortestPath();
            String nn1 = null, hh1 = null;
            int[][] nextHops = topology.shortestPathNextHops;
            int[][] networksToUse = topology.networksToUse;
            for (int j = 0; j < matGen.getLinksToRouterArrMap().size(); j++) {
                String targetLanId = Utilities.convertToPaddedString(j + 1);
                int x = Integer.parseInt(rid) - 1;
                
                String nn = Utilities.convertToPaddedString(networksToUse[x][j]); //network to use to reach next hop router
                String nextHopRouterId = Utilities.convertToPaddedString(nextHops[x][j] + 1); //id of next hop routers to go to targetLanId
                
                MyRouter nextHopRouter = RTMap.get(nextHopRouterId);
                
                for (IP a : nextHopRouter.IPList) {
                    if (a.nn.equals(nn)) {
                        nn1 = a.nn;
                        hh1 = a.hh;
                    }
                }
                
                if (nn1 != null) {
                    String my_ip = "(" + nn1 + "," + hh1 + ")";
                    java.util.Date date= new java.util.Date();
                    bufferWritter.write("\n" + targetLanId + "\t\t\t" + my_ip + "\t\t\t" + new Timestamp(date.getTime()));
                }
                
            }
        }
    }

}
