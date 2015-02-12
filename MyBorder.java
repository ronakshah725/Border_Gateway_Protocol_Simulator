
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBorder {

    String myRID;
    String myAS;
    static String genPathAdv;
    int myBID = 01;
    static int sec = 1;
    ArrayList<IP> IPList = new ArrayList<>();
    HashMap<String, ArrayList<String>> top = new HashMap<>();
    HashMap<String, Integer> filePointerMap = new HashMap<>();

    HashSet<String> iBGP_peers = new HashSet<>();
    HashSet<String> eBGP_peers = new HashSet<>();
    List<String> discovered_EXT_NN_List = new ArrayList<>();
    static HashSet<String> dumpNN = new HashSet<>();

    
    
    
    public void init() {
        String network = this.getNN().toString();
        String delims = "[ ]+";
        String[] token = network.split(delims);
        String[] Netfiles = new String[token.length];
        for (int i = 0; i < token.length; i++) {
            Netfiles[i] = "NET" + token[i] + ".txt";
        }
        for (String s : Netfiles) {
            filePointerMap.put(s, 0);
        }
        for (int j = 0; j < token.length; j++) {
            dumpNN.add(token[j]);
        }
    }

    public StringBuffer getNN() {
        StringBuffer networks = new StringBuffer();
        for (IP s : IPList) {
            networks.append(s.nn);
            networks.append(" ");
        }
        return networks;
    }

    public void populate_ALL_NN_Internally_Discov(String s) {
        Pattern pattern = Pattern.compile("(?<=NETWKS).*.(?=OPTIONS)");
        Matcher matcher = pattern.matcher(s);
        String fetchedNN = null;
        while (matcher.find()) {
            fetchedNN = matcher.group().toString();
        }
        fetchedNN = fetchedNN.trim();
        String[] ind = fetchedNN.split("[ ]+");
        for (String st : ind) {
            dumpNN.add(st);
        }
    }

    public void readNN_each() throws IOException {
        String network = this.getNN().toString();
        String[] ind = network.split("[ ]+");
        String[] NNFiles = new String[ind.length];
        for (int i = 0; i < ind.length; i++) {
            NNFiles[i] = "NET" + ind[i] + ".txt";
        }
        File dir = new File(".");
        File[] files = dir.listFiles();
        for (int t = 0; t < files.length; t++) {
            String fileName = files[t].getName();
            for (String s : NNFiles) {
                if (fileName.equals(s)) {
                    readandupdate(fileName, ind);
                }
            }
        }

    }

        public void writeOUT() {
        try {
            String filename = "out" + this.myRID + ".txt";
            File file = new File(filename);
      
            if (!file.exists()) {
                file.createNewFile();
            }
 
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            StringBuffer networks = getNN();
            String bid = String.format("%02d", myBID);
            for (IP s : IPList) {
                String LSA = "(" + s.nn + "," + s.hh + ")" + " " + "(" + s.nn + ",99)" 
                        + " " + "LSA" + " " + this.myRID + " " + bid + " " + "NETWKS" 
                        + " " + networks.toString() + " " + "OPTIONS" + " " + "BORDER" + " " + this.myAS;
                bufferWritter.write(LSA);
                bufferWritter.write("\n");
            }
            myBID++;
            bufferWritter.close();
        } catch (IOException ex) {
            // report
        }

    }

    public void readandupdate(String fileName, String[] token) throws IOException {
        HashMap<String, Integer> ridbid = new HashMap<String, Integer>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName.toString()))) {
            String line;
            int currLine = 0;
            while ((line = br.readLine()) != null && filePointerMap.get(fileName) != null) {
                if (!(line.trim().equals(""))) {
                    ++currLine;
                    if (currLine > filePointerMap.get(fileName)) {
                        String m;
                        String thisNN;
                        String[] elem = line.split("[ ,()]+");
                        String rtID = elem[6];
                        String src_ip = "(" + elem[1] + "," + elem[2] + ")";
                        int thisBID;
                        thisBID = Integer.parseInt(elem[7]);
                        int ind = line.lastIndexOf(" ");
                        thisNN = elem[3];
                        String as_no = line.substring(ind + 1, line.length());
                        if (!line.contains("PATHADV")) 
                        {
                            if (rtID.equals(this.myRID)) {
                                continue;
                            }
                            // To check if it is LSA from internal router
                            if (!line.contains("OPTIONS")) {
                                if (ridbid.containsKey(rtID)) {
                                    int startBID = 0;
                                    startBID = ridbid.get(rtID);
                                    if (thisBID > startBID) {
                                        ridbid.put(rtID, thisBID);
                                        m = line + " " + "OPTIONS" + " " + this.myAS;
                                        writetoAdjNN(token, m, elem[3]);
                                        for (int i = 9; i < elem.length; i++) {
                                            dumpNN.add(elem[i]);
                                        }
                                    } else {
                                        continue;
                                    }
                                } else {
                                    ridbid.put(rtID, thisBID);
                                    m = line + " " + "OPTIONS" + " " + this.myAS;
                                    writetoAdjNN(token, m, elem[3]);
                                }
                            } // If the LSA has OPTIONS, then
                            else {
                                // If it only has the AS# option, then
                                if (!line.contains("BORDER")) {
                                    // If the AS is the same myAS mine, it comes from an internal router,
                                    // send LSA on all LANs
                                    if (as_no.equals(this.myAS)) {
                                        if (ridbid.containsKey(rtID)) {
                                            int startBID = 0;
                                            startBID = (Integer) ridbid.get(rtID);
                                            if (thisBID > startBID) {
                                                ridbid.put(rtID, thisBID);
                                                writetoAdjNN(token, line, elem[3]);
                                                populate_ALL_NN_Internally_Discov(line);// Adds networks to hashset dumpNN
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            ridbid.put(rtID, thisBID);
                                            writetoAdjNN(token, line, elem[3]);
                                        }
                                    } else // If the AS is different, it is from an internal router in a different AS,
                                        // so the message is discarded.
                                    {
                                        continue;
                                    }
                                } else {
             
                                    if (as_no.equals(this.myAS)) {
                                       
                                        iBGP_peers.add(src_ip);
                                        if (!discovered_EXT_NN_List.isEmpty()) {
                                            String filename = "out" + this.myRID + ".txt";
                                            File file = new File(filename);
                                            //if file doesn't exists, then create it
                                            if (!file.exists()) {
                                                file.createNewFile();
                                            }
                                            //true = append file
                               
                                            try (BufferedWriter bufferWritter = new BufferedWriter(new FileWriter(file.getName(), true))) {
                                                for (String j : discovered_EXT_NN_List) {
                                                    bufferWritter.append(j);
                                                    bufferWritter.append("\n");
                                                }
                                            }
                                        }
                                        populate_ALL_NN_Internally_Discov(line);
                                        //
                                    } else if (!as_no.equals(this.myAS)) {
                            
                                        StringBuffer myNN = this.getNN();
                                        eBGP_peers.add(src_ip);
                                        if (sec == 1) {
                                            sendBGPinfo(this.IPList, this.myAS, src_ip, this.myRID, thisNN);
                                            sec = 0;
                                        }
                                        if (myNN.toString().contains(thisNN)) {
                                            if (!discovered_EXT_NN_List.isEmpty()) {
                                                String filename = "out" + this.myRID + ".txt";
                                                File file = new File(filename);
                                                //if file doesn't exists, then create it
                                                if (!file.exists()) {
                                                    file.createNewFile();
                                                }
                                                //true = append file
                                                FileWriter fileWritter = new FileWriter(file.getName(), true);
                                                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                                                for (String j : discovered_EXT_NN_List) {
                                                    bufferWritter.append(j);
                                                    bufferWritter.append("\n");
                                                }
                                                bufferWritter.close();
                                            }
                                        } else {
                                            continue;
                                        }
                                    }
                                    
                                }

                            }
                        } else if (line.contains("PATHADV")) {
                            String pathadv = line;
                            Pattern pattern = Pattern.compile("(?<=PATHADV).*");
                            Matcher matcher = pattern.matcher(pathadv);
                            String totalnetworks = null;
                            while (matcher.find()) {
                                totalnetworks = matcher.group().toString();
                            }
                            String nn = null, hh = null;
                            for (IP e : this.IPList) {
                                if (e.nn.equals(thisNN)) {
                                    nn = e.nn;
                                    hh = e.hh;
                                }
                            }
                            String my_ip = "(" + nn + "," + hh + ")";
                            for (String st : eBGP_peers) {
                                String pathadvertisement = my_ip + " " + st + " " + this.myRID + " " + "PATHADV" + " " + this.myAS + " " + totalnetworks;
                                discovered_EXT_NN_List.add(pathadvertisement);
                            }
                            for (String st : iBGP_peers) {
                                String pathadvertisement = "(" + nn + "," + hh + ")" + " " + st + " " + this.myRID + " " + "PATHADV" + " " + this.myAS + " " + totalnetworks;
                                dumpNN.add(pathadvertisement);
                            }
                        }
                    }
                }
            }
            filePointerMap.put(fileName, currLine);
        }
    }

    public void sendBGPinfo(ArrayList<IP> interfaces2, String my_as, String source_ip, String routerid2, String currentnn) throws IOException {
		// Generate path advertisement
        // Write to outXX file
        // Send path advertisement to your peers 
        // Then they will add external networks to LSA 
        String nn = null, hh = null;

        for (IP e : interfaces2) {
            if (e.nn.equals(currentnn)) {
                nn = e.nn;
                hh = e.hh;
            }
        }
        String my_ip = "(" + nn + "," + hh + ")";
        String allnetworks = dumpNN.toString().replace("[", "").replace("]", "").replace(",", "");
        genPathAdv = my_ip + " " + source_ip + " " + routerid2 + " " + "PATHADV" + " " + my_as + " " + "NETWKS" + " " + allnetworks;
        String filename = "out" + this.myRID + ".txt";
        File file = new File(filename);
        //if file doesn't exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        //true = append file
        FileWriter fileWritter = new FileWriter(file.getName(), true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        bufferWritter.append(genPathAdv);
        bufferWritter.append("\n");
        bufferWritter.close();

    }

    public void writetoAdjNN(String[] token, String message, String incominglan) throws IOException {
        for (String s1 : token) {
            if (!s1.equals(incominglan)) {
                String filename = "out" + this.myRID + ".txt";
                File file = new File(filename);
                //if file doesn't exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                //true = append file
                FileWriter fileWritter = new FileWriter(file.getName(), true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.append(message);
                bufferWritter.append("\n");
                bufferWritter.close();
            }
        }

    }
}
