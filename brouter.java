import java.io.IOException;


public class brouter extends MyBorder{
	public static void main(String[] args) throws IOException {
	final brouter routerobj = new brouter();
	routerobj.myRID = args[0];
	routerobj.myAS = args[1];
	for(int i=2;i<args.length;i+=2)
	{
		IP addobj = new IP(args[i],args[i+1]);
		routerobj.IPList.add(addobj);
	}
	routerobj.init();
	int i = 1;
	  while (i < 100) {
	    if (i % 10 == 0) 
	    { 
	         routerobj.writeOUT();
	    }
	    routerobj.readNN_each();
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("In brouter main");
		} /* sleep one sec */
	    i++;
	  }
	}

	
}
