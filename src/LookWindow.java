import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

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
		jsp.setBounds(1080, 150, 280, 350);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(autoButton);
		buttonGroup.add(midAutoButton);
		autoButton.setBounds(1110, 75, 100, 40);
		midAutoButton.setBounds(1110, 105, 100, 40);
		autoButton.setSelected(true);
		
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
					sleep(300);
					pic = new ScreenShot("E:\\Programs\\AndroidJump\\pic\\"+num+".png");
					addText(pic.personPointInfo());
					panel.repaint();
					time = (int) (pic.getLength()/737*1000);
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
						time = (int) (pic.getLengthByAngle()/737*1000);
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
}
