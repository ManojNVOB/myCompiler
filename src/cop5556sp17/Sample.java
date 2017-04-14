package cop5556sp17;

public class Sample implements Runnable{
	public  int i;
	public boolean b;
	
	Sample(String[] args){
		i = Integer.parseInt(args[0]);
		//b= Boolean.valueOf(args[1]);
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
		
		int i=0;
		i=0;
		if(i!=0){
			//int j=0;
			//int k=1;
			i=i+1;
		}
		
		i=3;
		//int j=5;
		if(i!=0){
			i=i+1;
		}
		//int l=1;
		//int m=2;
	}
		
/*	while(i<3){
		i=i+1;
	}*/

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}