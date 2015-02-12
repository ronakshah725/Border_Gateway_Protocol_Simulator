import java.io.IOException;

public class irouter extends MyRouter {

	public static void main(String[] args) throws IOException {
		final irouter rt = new irouter();
		rt.myRID = args[0];
		for(int i=1;i<args.length;i+=2)
		{
			IP addobj = new IP(args[i],args[i+1]);
			rt.IPList.add(addobj);
		}
		rt.init();
		int i=1;
		while(i<100) 
		{
			if (i % 10 == 0)
			{
				rt.writeOUT();
			}
			if (i % 15 == 0)
			{
				rt.makeRTFile();
			}
			rt.readNNFiles();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Error from ir");
			}
			i++;
		}
	}
}