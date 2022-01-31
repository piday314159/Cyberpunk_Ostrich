// Class: GraphicsPanel
// Written by: Mr. Swope
// Date: 1/27/2020
// Description: This class is the main class for this project.  It extends the Jpanel class and will be drawn on
// 				on the JPanel in the GraphicsMain class.  
//
// Modified the jump and movement system, and added a universal fall method. 

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Rectangle;
import java.util.ArrayList;

public class GraphicsPanel extends JPanel implements KeyListener{

	private Timer timer;					// The timer is used to move objects at a consistent time interval.

	private ArrayList<Background> background;
	private Background background1;			// The background object will display a picture in the background.
	private Background background2;	// There has to be two background objects for scrolling.
	private Background background3;

	private Sprite sprite;					// create a Sprite object
	private ArrayList<Item> item;						// This declares an Item object. You can make a Item display
	// pretty much any image that you would like by passing it
	// the path for the image.

	private Boolean collide;

	private Boolean key_left;
	private Boolean key_right;

	private int platformCounter;
	
	private double score;
	
	private double highScore;


	public GraphicsPanel(){
		background = new ArrayList<>();

		background1 = new Background();	// initialize 3 backgrounds used to scroll the screens 
		background2 = new Background(background1.getImage().getIconWidth());
		background3 = new Background(background2.getImage().getIconWidth() + background1.getImage().getIconWidth());

		background.add(background1);
		background.add(background2);
		background.add(background3);

		item = new ArrayList<>(); // array  list containing platforms
		item.add(new Item(500, 200, "images/objects/building5 purple.png", 1));  

		// The Item constructor has 4 parameters - the x coordinate, y coordinate
		// the path for the image, and the scale. The scale is used to make the
		// image smaller, so the bigger the scale, the smaller the image will be.


		sprite = new Sprite(400, 60, background.get(0).getImage().getIconHeight());			
		// The Sprite constuctor has two parameter - - the x coordinate and y coordinate

		setPreferredSize(new Dimension(background.get(0).getImage().getIconWidth(),
				background.get(1).getImage().getIconHeight()));  
		// This line of code sets the dimension of the panel equal to the dimensions
		// of the background image.

		timer = new Timer(12, new ClockListener(this));   // This object will call the ClockListener's
		// action performed method every 5 milliseconds once the 
		// timer is started. You can change how frequently this
		// method is called by changing the first parameter.
		timer.start();
		this.setFocusable(true);					     // for keylistener
		this.addKeyListener(this);

		key_left = false; // variables used to prevent you from stopping if you switch your direction of movement
		key_right = false;

		collide = false; // boolean for collision check

		platformCounter = 0;
		
		score = 0;
		
		highScore = 0;
	}

	// method: paintComponent
	// description: This method will paint the items onto the graphics panel.  This method is called when the panel is
	//   			first rendered.  It can also be called by this.repaint(). You'll want to draw each of your objects.
	//				This is the only place that you can draw objects.
	// parameters: Graphics g - This object is used to draw your images onto the graphics panel.
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;

		for(Background b : background)
			b.draw(this, g);

		for (Item i: item) {
			i.draw(g2, this);
		}

		sprite.draw(g2, this);

		g2.setColor(Color.lightGray);
		
//		Rectangle r = sprite.getBounds();
//		g2.draw(r);
		
		Font font = new Font ("Serif", Font.PLAIN, 50);
		
		g2.setFont(font);
		
		g2.drawString("Score: " + (int)score, 1450, 75);
		
	}

	// method:clock
	// description: This method is called by the clocklistener every 5 milliseconds.  You should update the coordinates
	//				of one of your characters in this method so that it moves as time changes.  After you update the
	//				coordinates you should repaint the panel. 
	public void clock(){
		
		// collision check for the array list of platforms
		for(int k = 0; k < item.size(); k++) {
			if((sprite.collision(item.get(k)) && sprite.getY() + sprite.imageResource.getImage().getIconHeight() - item.get(k).getY() <= 35)) {
				sprite.y_coordinate = item.get(k).getY() - sprite.imageResource.getImage().getIconHeight()+5;
				collide = true;
			}
		}
		
		// You can move any of your objects by calling their move methods.
		sprite.move(this);

		for(Background b : background)
			b.move(/*sprite.getX(),*/ sprite.getXDirection());

		for (Item i: item) {
			i.move(sprite.getXDirection());
		}

		int itemSize = item.size() - 1;

		// adds platforms if there are less than 15 of them
		if (platformCounter < 15) {
			item.add(new Item(item.get(itemSize).getX() + 200 + (int)(Math.random() * 300), 150 + (int)(Math.random()* 130) , "images/objects/building5 purple.png", 1));
			platformCounter++;
		}

		// removes platforms that move too far off screen
		for (int i = item.size() - 1; i >= 0; i--) {
			if (item.get(i).getX() < -1000) {
				item.remove(i);
				platformCounter --;
			}
		}

		// adds and removes backgrounds as the screen scrolls
		for (int b = background.size() - 1; b >= 0; b--) {
			if (background.get(b).getX() < -1800) {
				background.add(new Background(background.get(background.size() - 1).getX() + background.get(background.size() - 1).getImage().getIconWidth()));
				background.remove(b);
			}
		}
		
		// causes the sprite to fall if they are not jumping and not standing on anything
		if (sprite.getY() < background1.getImage().getIconHeight() && sprite.jumpCounter == -1 && !collide)
			sprite.fall();
		
		if (sprite.getY() > background1.getImage().getIconHeight()) {
			sprite.die();
		}

		collide = false;
		
		if(sprite.scoreCount > highScore)
			highScore = sprite.scoreCount;
		
		score = highScore;
		
		this.repaint();
	}

	// method: keyPressed()
	// description: This method is called when a key is pressed. You can determine which key is pressed using the 
	//				KeyEvent object.  For example if(e.getKeyCode() == KeyEvent.VK_LEFT) would test to see if
	//				the left key was pressed.
	// parameters: KeyEvent e
	@Override
	public void keyPressed(KeyEvent e) {

		if(e.getKeyCode() == KeyEvent.VK_RIGHT && !sprite.isDead) {
			sprite.walkRight();
			key_right = true;
			key_left = false;
		}

		if(e.getKeyCode() == KeyEvent.VK_LEFT && !sprite.isDead) {
			sprite.walkLeft();
			key_right = false;
			key_left = true;
		}

//		if(e.getKeyCode() == KeyEvent.VK_SPACE)
//			sprite.run();

		else if(e.getKeyCode() == KeyEvent.VK_D) {
			playSound("src/sounds/bump.WAV");
			sprite.die();	
		}

		// causes the sprite to jump
		for(Item i: item) {
			if(e.getKeyCode() == KeyEvent.VK_UP && (sprite.getY() >= 204 || (sprite.collision(i)
					&& sprite.getY() + sprite.imageResource.getImage().getIconHeight() - i.getY() <= 30) ))
				sprite.jump();
		}
	}

	// This function will play the sound "fileName".
	public static void playSound(String fileName) {
		try {
			File url = new File(fileName);
			Clip clip = AudioSystem.getClip();

			AudioInputStream ais = AudioSystem.getAudioInputStream(url);
			clip.open(ais);
			clip.start();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// method: keyTyped()
	// description: This method is called when a key is pressed and released. It basically combines the keyPressed and
	//              keyReleased functions.  You can determine which key is typed using the KeyEvent object.  
	//				For example if(e.getKeyCode() == KeyEvent.VK_LEFT) would test to see if the left key was typed.
	//				You probably don't want to do much in this method, but instead want to implement the keyPresses and keyReleased methods.
	// parameters: KeyEvent e
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	// method: keyReleased()
	// description: This method is called when a key is released. You can determine which key is released using the 
	//				KeyEvent object.  For example if(e.getKeyCode() == KeyEvent.VK_LEFT) would test to see if
	//				the left key was pressed.
	// parameters: KeyEvent e
	@Override
	public void keyReleased(KeyEvent e) {
		
		// sprite only stop moving if neither key is held, won't stop if direction changes 
		if((e.getKeyCode() == KeyEvent.VK_RIGHT && key_left == false) || (e.getKeyCode() == KeyEvent.VK_LEFT && key_right == false))
			sprite.idle();
		else if(e.getKeyCode() ==  KeyEvent.VK_UP || e.getKeyCode() ==  KeyEvent.VK_DOWN)
			sprite.stop_Vertical();
		else if(e.getKeyCode() ==  KeyEvent.VK_SPACE)
			sprite.slowDown();

	}

}
