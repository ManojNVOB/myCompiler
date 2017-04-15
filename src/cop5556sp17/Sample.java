package cop5556sp17;


import java.io.File;
import java.net.URL;

public class Sample implements Runnable{
	//public  int i;
	//public boolean b;
	URL url;
	public File file;
	
	Sample(String[] args){
		//i = Integer.parseInt(args[0]);
		//b= Boolean.valueOf(args[1]);
		file = new File(args[0]);
		url = PLPRuntimeImageIO.getURL(args, 1);
	}
/*	public static void main(String[] args) {
		new Sample(args).run();
		
	}
	@Override
	public void run() {
		int a=0;
		i=a;
		while(i != 100){
			b = true;
		}
	}*/
	public static void main(String[] args){
		
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}