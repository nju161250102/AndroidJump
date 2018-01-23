import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class LookWindow {

	private JFrame frame = new JFrame("监视窗口");
	private JLabel label = new JLabel();
	private ScreenShot pic;
	private boolean isNeedClick = false;
	
	public LookWindow() {
		frame.setSize(1080,768);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane jsp = new JScrollPane(label);
		frame.add(jsp);
		
		Thread task = new Thread(){
			@Override
			public void run() {
				Runtime run = Runtime.getRuntime();
				for (int i = 0;; i++) {
			        try {
			        	System.out.println("序号: "+i);
						run.exec("adb shell /system/bin/screencap -p /sdcard/p.png");
						sleep(1200);
						run.exec("adb pull /sdcard/p.png E:\\Programs\\AndroidJump\\pic\\"+i+".png");
						sleep(300);
						label.setIcon(new ImageIcon("E:\\Programs\\AndroidJump\\pic\\"+i+".png"));
						pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\"+i+".png");
						int time = (int) (pic.getLength()/737*1000);
						pic.writeFile("E:\\Programs\\AndroidJump\\picout\\"+i+".png");
						run.exec("adb shell input swipe 500 500 501 501 "+time);
						sleep(4500 + time);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					} catch (NoPersonException e) {
						e.printStackTrace();
						break;
					} catch (NoNextException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "请选择中心点");
						isNeedClick = true;
						break;
					}
				}
			}
		};
		
		label.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(isNeedClick) {
					Runtime run = Runtime.getRuntime();
					int time = (int) (pic.getLength(e.getX(), e.getY())/737*1000);
					try {
						run.exec("adb shell input swipe 500 500 501 501 "+time);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					isNeedClick = false;
				}
			}
		});
		
		frame.setVisible(true);
		task.start();
	}
}
