import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


public class controller {
	static HashMap<String,Integer> pointerMap = new HashMap<String,Integer>();
	public static void main(String[] args) throws IOException {
		int i=1;
		while(i<100)
		{
			try {
				Thread.sleep(1000);
				loopOut();
				} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
        
        public static void loopOut(){
		File dir = new File (".");
		File[] files = dir.listFiles();  
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("out")) {
                    try {
                        readOut(fileName);
                    } catch (IOException e) {
                        System.out.println("");
                    }
                }
            }  

	}


	public static void readOut(String fileName) throws IOException {
		FileReader fr = new FileReader(fileName); 
		BufferedReader br = new BufferedReader(fr); 
		String s; 
		int temp=0;
		while((s = br.readLine())!= null && pointerMap.get(fileName)!=null){
			if(!(s.trim().equals("")))
			{
				++temp;
				if(temp > pointerMap.get(fileName))
				{
				String delims = "[ ,()]+";
				String[] tokens = s.split(delims);
				writetofile(tokens[3],s);
				}
			}
		} 
		pointerMap.put(fileName,temp);
		fr.close(); 
		}
	 

	public static void writetofile(String routerid,String message) {
		// TODO Auto-generated method stub
		try
		{
			String filename = "NET"+routerid+".txt";
			File file = new File(filename);

			if(!file.exists()){
				file.createNewFile();
			}
			
			BufferedWriter bufferWritter = new BufferedWriter(new FileWriter(file,true));
			bufferWritter.append(message);
			bufferWritter.append("\n");
			bufferWritter.close();
		}
		catch (IOException ex) {
			// report
		} 

	}

	
}
