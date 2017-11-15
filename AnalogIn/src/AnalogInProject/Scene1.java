package AnalogInProject;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Scene1 extends SceneManager {
	
	/************************************************
	   Resource
	*************************************************/
	// Music
//	private Audio introMusic;
	
	// Image
	private Image background = ImageManager.background;
	
	/************************************************
	   Component/object
	*************************************************/	
	private SceneManager thisInstance = this; // thisInstance
	
	private JButton testButton = new JButton(
			new ImageIcon(ImageManager.testButtonImage_1.getScaledInstance(400, 100, Image.SCALE_SMOOTH)));
	
	public Scene1() {
		GIM.currentScene = this;
		//Input 등록
		GIM.keyInputBuffer = new KeyInputBuffer();
		GIM.keyInputBuffer.start();
		
		// Icon normal
		testButton.setBounds(500, 500, 400, 100);
		testButton.setBorderPainted(false);
		testButton.setContentAreaFilled(false); // 채우지마
		testButton.setFocusPainted(false);
		testButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				// button.setIcon으로 아이콘변경
				testButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// button.setIcon으로 아이콘변경
				testButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// 눌렀을때 처리 화면을 바꿔보자.
				GIM.GameObject.changeScene(thisInstance, "CreateGame");
			}
		});
		GIM.GameObject.add(testButton); // JFrame에 버튼을 추가.
		systemObject.add(testButton);

		blockObject.add(new Block(new BlockInformation(400,300,300,300,ImageManager.testButtonImage_1)));
		blockObject.add(new Block(new BlockInformation(500,100,300,300,ImageManager.testButtonImage_2)));

		for(Block b : blockObject){
			GIM.GameObject.add(b);	
		}
		
//		introMusic = new Audio("testMusic.mp3", true);
//		introMusic.start();
		
		GIM.GameObject.repaint();
	}
	@Override
	public void removeScene()
	{
		for(Component c : systemObject){
			GIM.GameObject.remove(c);			
		}
		for(Block b : blockObject){
			GIM.GameObject.remove(b);			
		}
//		introMusic.close();
	}
	public void screenDraw(Graphics g) {
		g.drawImage(background, 0, 0, 1250, 1080, null);
		GIM.GameObject.paintComponents(g);
		GIM.GameObject.repaint();
	}
}
