import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LookWindow {

	private JFrame frame = new JFrame("监视窗口");
	private MyPanel panel = new MyPanel(); 
	private ScreenShot pic;
	private MyThread task;
	private boolean isNeedClick = false;
	private int num = 0;
	
	public LookWindow() {
		frame.setSize(1080,768);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		
		panel.setBounds(0, 0, 1080, 760);
		frame.add(panel);
		task = new MyThread();
		
		panel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(isNeedClick) {
					Runtime run = Runtime.getRuntime();
					int time = (int) (pic.getLength(e.getX(), e.getY()+700)/737*1000);
					try {
						run.exec("adb shell input swipe 500 500 501 501 "+time);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					isNeedClick = false;
					task = new MyThread();
					task.start();
				}
			}
		});
		
		frame.setVisible(true);
		task.start();
	}
	
	class MyPanel extends JPanel {
		public void paintComponent(Graphics g) {
			if(pic != null) g.drawImage(pic.getImage().getSubimage(0, 700, 1080, 768), 0, 0, null);
		}
	}
	
	class MyThread extends Thread {
		private int time = 4500;
		@Override
		public void run() {
			Runtime run = Runtime.getRuntime();
			for (;; num++) {
		        try {
		        	sleep(time);
		        	System.out.println("序号: "+num);
					run.exec("adb shell /system/bin/screencap -p /sdcard/p.png");
					sleep(1500);
					run.exec("adb pull /sdcard/p.png E:\\Programs\\AndroidJump\\pic\\"+num+".png");
					sleep(300);
					pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\"+num+".png");
					panel.repaint();
					time = (int) (pic.getLength()/737*1000);
					pic.writeFile("E:\\Programs\\AndroidJump\\picout\\"+num+".png");
					panel.repaint();
					run.exec("adb shell input swipe 500 500 501 501 "+time);
					time += 4500;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} catch (NoPersonException e) {
					e.printStackTrace();
					break;
				} catch (NoNextException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "请选择中心点");
					isNeedClick = true;
					num++;
					break;
				}
			}
			System.out.println("线程结束");
		}
	}
}
