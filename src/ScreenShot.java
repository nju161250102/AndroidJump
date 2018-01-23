import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class ScreenShot {

	private BufferedImage image;
	private int personX, personY;
	private int nextX, nextY;
	
	public ScreenShot(String filePath) throws NoPersonException {
		try {
			image = ImageIO.read(new File(filePath));
			removeBackground();
			getPersonPoint();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No file...");
		}
	}
	
	public void writeFile(String filePath) {
		try {
			System.out.println("人物坐标:X "+personX+"   Y "+personY);
			drawLine(personX, personY);
			System.out.println("下一坐标:X "+nextX+"   Y "+nextY);
			drawLine(nextX, nextY);
			ImageIO.write(image, "png", new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Save Failed...");
		}
	}
	
	private void getPersonPoint() throws NoPersonException {
		int p = 0;
		for(int j = 0; j < image.getHeight(); j++) {
        	for(int i = 0; i < image.getWidth(); i++) {
				int color = image.getRGB(i, j);
				if (inArrange(40, 80, getRed(color)) && 
						inArrange(40, 60, getGreen(color)) && 
						inArrange(70, 100, getBlue(color))) {
					p++;
				}
				else {
					if (p >= 75) {
						personX = i - p/2 - 1;
						personY = j + 6;
						return;
					} else p = 0;
				}
			}
		}
		throw new NoPersonException();
	}
	
	private void getNextPoint() throws NoNextException {
		BufferedImage bf = null;
		if (personX < 540) bf = image.getSubimage(personX+40, 290, 1040-personX, personY-290);
		else bf = image.getSubimage(0, 290, personX-40, personY-290);
		
		int color = 0;
		outer:
		for (int i = 0; i < bf.getHeight(); i++) {
			for (int j = 0; j < bf.getWidth(); j++) {
				if (bf.getRGB(j, i) != 0xffffffff) {
					nextX = j; nextY = i; break outer;
				} //else bimage.setRGB(j, i, 0xffff0000);
			}
		}
		for (int j = bf.getWidth()-1; j >= 0; j--) {
			if (bf.getRGB(j, nextY) != 0xffffffff && bf.getRGB(j, nextY) != 0xffff0000) {
				nextX = (nextX + j) / 2;
				color = bf.getRGB(nextX, nextY);
				break;
			} 
		}
		for (int i = bf.getHeight() - 1; i > 0; i--) {
			if (bf.getRGB(nextX, i) == color) {
				if (i - nextY < 25) throw new NoNextException();
				nextY = (nextY + i) / 2 + 290;
				nextX = personX < 540 ? nextX+personX+40 : nextX;
				break;
			}
		}
	}
	
	private void removeBackground() {
		int colorA = image.getRGB(0, 0);
		//获取底部背景色
		TreeMap<Integer, Integer> colorMap = new TreeMap<Integer,Integer>();
		for(int i = 0; i < image.getWidth(); i += (image.getWidth()/10)) {
			int color = image.getRGB(i, image.getHeight()-1);
			if (colorMap.containsKey(color)) 
				colorMap.put(color, colorMap.get(color)+1);
			else colorMap.put(color, 1);
		}
		
		List<Map.Entry<Integer,Integer>> list = new ArrayList<Map.Entry<Integer,Integer>>(colorMap.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<Integer,Integer>>() {
            //降序排序
            @Override
            public int compare(Entry<Integer, Integer> o1,
                    Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        int colorB = list.iterator().next().getKey();
        
        for(int i = 0; i < image.getWidth(); i++) {
        	for(int j = 0; j < image.getHeight(); j++) {
        		if (j < 290 || j > 1780) {
					image.setRGB(i, j, 0xffffffff);
					continue;
				}
        		int color = image.getRGB(i, j);
        		if (inArrange(getRed(colorA), getRed(colorB), getRed(color)) &&
        			inArrange(getGreen(colorA), getGreen(colorB), getGreen(color)) &&
        			inArrange(getBlue(colorA), getBlue(colorB), getBlue(color)))
        			image.setRGB(i, j, 0xffffffff);
        	}
        }
        
	}
	
	public double getLength() throws NoNextException {
		getNextPoint();
		return Math.sqrt(Math.pow(personX - nextX, 2) + Math.pow(personY - nextY, 2));
	}
	
	public double getLength(int x, int y) {
		return Math.sqrt(Math.pow(personX - x, 2) + Math.pow(personY - y, 2));
	}
	
	private int getRed(int bits) {
		return (bits & 0x00ff0000) >> 16;
	}
	private int getGreen(int bits) {
		return (bits & 0x0000ff00) >> 8;
	}
	private int getBlue(int bits) {
		return bits & 0x000000ff;
	}
	private boolean inArrange(int a, int b, int c) {
		return (a-c)*(b-c) <= 0;
	}
	private void drawLine(int x,int y) {
		for (int i = 0; i < image.getHeight(); i++) {
			image.setRGB(x, i, 0xffff0000);
		}
		for (int i = 0; i < image.getWidth(); i++) {
			image.setRGB(i, y, 0xffff0000);
		}
	}
}
