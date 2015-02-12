
public class Utilities {
	public static String convertToPaddedString(int i){
		if((i>=100) || (i<0)){
			throw new IllegalArgumentException();
		}
		if(i<10){
			return "0"+i;
		} else {
			return ""+i;
		}
	}
}
