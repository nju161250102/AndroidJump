import java.io.File;
import java.io.IOException;

public class AutoJump extends Thread{

	public static void main(String[] args) {
		//test();
		//autoRun();
		new LookWindow();
	}

	public static void test() {
		File file = new File("/pic/");
		File[] fileList = file.listFiles();
		for (File f : fileList) {
			System.out.println(f.getName());
			try {
				ScreenShot pic = new ScreenShot(f.getPath());
				pic.writeFile("/picout/"+f.getName());
			} catch (NoPersonException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	public static void autoRun() {
		Runtime run = Runtime.getRuntime();
		for (int i = 0;; i++) {
	        try {
	        	System.out.println("ÐòºÅ: "+i);
				run.exec("adb shell /system/bin/screencap -p /sdcard/p.png");
				AutoJump.sleep(1200);
				run.exec("adb pull /sdcard/p.png E:\\Programs\\AndroidJump\\pic\\"+i+".png");
				AutoJump.sleep(300);
				ScreenShot pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\"+i+".png");
				pic.writeFile("E:\\Programs\\AndroidJump\\picout\\"+i+".png");
				int time = (int) (pic.getLength()/737*1000);
				run.exec("adb shell input swipe 500 500 501 501 "+time);
				AutoJump.sleep(4500);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} catch (NoPersonException e) {
				e.printStackTrace();
				break;
			} catch (NoNextException e) {
				e.printStackTrace();
				continue;
			}
        }
	}
}
