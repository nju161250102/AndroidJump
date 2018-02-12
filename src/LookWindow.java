import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

public class LookWindow {

	private JFrame frame = new JFrame("监视窗口");
	private MyPanel panel = new MyPanel(); 
	private JTextArea label = new JTextArea();
	private JRadioButton autoButton = new JRadioButton("自动");
	private JRadioButton midAutoButton = new JRadioButton("半自动");
	private JRadioButton clickButton = new JRadioButton("手动");
	private JToggleButton button;
	private JScrollPane jsp;
	private ScreenShot pic;
	private MyThread task;
	private boolean isNeedClick = false;
	private int num = 0;
	
	public LookWindow() {
		frame.setSize(1280,768);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		
		panel.setBounds(0, 0, 1080, 760);
		button = new JToggleButton("开始");
		button.setBounds(1110, 25, 130, 50);
		label.setEditable(false);
		jsp = new JScrollPane(label);
		jsp.setBounds(1080, 180, 280, 350);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(autoButton);
		buttonGroup.add(midAutoButton);
		buttonGroup.add(clickButton);
		autoButton.setBounds(1110, 75, 100, 40);
		midAutoButton.setBounds(1110, 105, 100, 40);
		clickButton.setBounds(1110, 135, 100, 40);
		autoButton.setSelected(true);
		
		panel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				Runtime run = Runtime.getRuntime();
				if(clickButton.isSelected()) {
					try {
						if (pic==null) {
							Process process = run.exec("adb shell /system/bin/screencap -p /sdcard/p.png");
					        InputStream input = process.getInputStream();   
					        BufferedReader reader = new BufferedReader(new InputStreamReader(input));  
					        String szline;  
					        while ((szline = reader.readLine())!= null) {     
					            System.out.println(szline);
					        }
					        process = run.exec("adb pull /sdcard/p.png E:\\Programs\\AndroidJump\\pic\\0.png");
					         input = process.getInputStream();   
					         reader = new BufferedReader(new InputStreamReader(input));  
					        while ((szline = reader.readLine())!= null) {     
					            System.out.println(szline);
					        }
					        reader.close();
							pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\0.png");
							panel.repaint();
						} else {
							double length = Math.sqrt(Math.pow(pic.getPersonX() - e.getX(), 2) + Math.pow(pic.getPersonY() - e.getY()-700, 2));
							int time = calculateTime(length);
							run.exec("adb shell input swipe 500 500 501 501 "+time);
							pic = null;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (NoPersonException e1) {
						e1.printStackTrace();
					}
				}
				else if(isNeedClick) {
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
		
		button.addActionListener(e->{
			if(!button.isSelected()) {
				if (task != null) task.stop();
				button.setText("开始");
			}
			else {
				task = new MyThread();
				task.start();
				button.setText("结束");
			}
		});

		frame.add(autoButton);
		frame.add(midAutoButton);
		frame.add(clickButton);
		frame.add(button);
		frame.add(jsp);
		frame.add(panel);
		frame.setVisible(true);
		
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
		        	addText("序号: "+num);
					run.exec("adb shell /system/bin/screencap -p /sdcard/p.png");
					sleep(1500);
					run.exec("adb pull /sdcard/p.png E:\\Programs\\AndroidJump\\pic\\"+num+".png");
					sleep(400);
					pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\"+num+".png");
					pic.removeBackground();
					addText(pic.personPointInfo());
					panel.repaint();
					time = calculateTime(pic.getLength());
					pic.writeFile("E:\\Programs\\AndroidJump\\picout\\"+num+".png");
					panel.repaint();
					addText(pic.nextPointInfo());
					run.exec("adb shell input swipe 500 500 501 501 "+time);
					time += 4500;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} catch (NoPersonException e) {
					e.printStackTrace();
					break;
				} catch (NoNextException e) {
					e.printStackTrace();
					if(autoButton.isSelected()) {
						time = calculateTime(pic.getLengthByAngle());
						pic.writeFile("E:\\Programs\\AndroidJump\\picout\\"+num+".png");
						panel.repaint();
						addText(pic.nextPointInfo());
						try {
							run.exec("adb shell input swipe 500 500 501 501 "+time);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						time += 4500;
						continue;
					}
					JOptionPane.showMessageDialog(null, "请选择中心点");
					isNeedClick = true;
					num++;
					break;
				}
			}
			button.setSelected(false);
			button.setText("开始");
			System.out.println("线程结束");
		}
	}
	
	private void addText(String s) {
		label.setText(label.getText() + '\n' + s);
		JScrollBar jsb = jsp.getVerticalScrollBar();
		jsb.setValue(jsb.getMaximum());
	}
	
	private int calculateTime(double length) {
		double a = 151.44;
		double b = 609.45;
		return (int) ((-b+Math.sqrt(b*b+4*a*length))/(2*a)*1000);
	}
}
