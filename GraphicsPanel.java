// Class: GraphicsPanel
// Written by: Mr. Swope
// Date: 1/27/2020
// Description: This class is the main class for this project.  It extends the Jpanel class and will be drawn on
// 				on the JPanel in the GraphicsMain class.  
//
// Since you will modify this class you should add comments that describe when and how you modified the class.  

import java.awt.Color;
import java.awt.Dimension;
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

public class GraphicsPanel extends JPanel implements KeyListener{

	private Timer timer;					// The timer is used to move objects at a consistent time interval.

	private Background background1;			// The background object will display a picture in the background.
	private Background background2;			// There has to be two background objects for scrolling.

	private Sprite sprite;					// create a Sprite object
	private Item item;						// This declares an Item object. You can make a Item display
	// pretty much any image that you would like by passing it
	// the path for the image.
	
	private boolean key_left;
	private boolean key_right;


	public GraphicsPanel(){
		background1 = new Background();	// You can set the background variable equal to an instance of any of  
		background2 = new Background(background1.getImage().getIconWidth());								

		item = new Item(500, 200, "images/objects/box.png", 4);  
		// The Item constructor has 4 parameters - the x coordinate, y coordinate
		// the path for the image, and the scale. The scale is used to make the
		// image smaller, so the bigger the scale, the smaller the image will be.


		sprite = new Sprite(50, 60, background1.getImage().getIconHeight());			
		// The Sprite constuctor has two parameter - - the x coordinate and y coordinate

		setPreferredSize(new Dimension(background1.getImage().getIconWidth(),
				background2.getImage().getIconHeight()));  
		// This line of code sets the dimension of the panel equal to the dimensions
		// of the background image.

		timer = new Timer(20, new ClockListener(this));   // This object will call the ClockListener's
		// action performed method every 5 milliseconds once the 
		// timer is started. You can change how frequently this
		// method is called by changing the first parameter.
		timer.start();
		this.setFocusable(true);					     // for keylistener
		this.addKeyListener(this);
		
		key_left = false;
		key_right = false;
	}

	// method: paintComponent
	// description: This method will paint the items onto the graphics panel.  This method is called when the panel is
	//   			first rendered.  It can also be called by this.repaint(). You'll want to draw each of your objects.
	//				This is the only place that you can draw objects.
	// parameters: Graphics g - This object is used to draw your images onto the graphics panel.
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;

		background1.draw(this, g);
		background2.draw(this, g);

		item.draw(g2, this);
		sprite.draw(g2, this);

		g2.setColor(Color.RED);
		Rectangle r = sprite.getBounds();
		g2.draw(r);
	}

	// method:clock
	// description: This method is called by the clocklistener every 5 milliseconds.  You should update the coordinates
	//				of one of your characters in this method so that it moves as time changes.  After you update the
	//				coordinates you should repaint the panel. 
	public void clock(){
		// You can move any of your objects by calling their move methods.
		sprite.move(this);
		
		System.out.println(sprite.getY());

		background1.move(sprite.getX(), sprite.getXDirection());
		background2.move(sprite.getX(), sprite.getXDirection());

		// You can also check to see if two objects intersect like this. In this case if the sprite collides with the
		// item, the item will get smaller. 
//		if(sprite.collision(item) && sprite.getY() < item.getY()) {
//			System.out.println("stop");
//			sprite.stop_Vertical();
//		}
		
		if((sprite.collision(item) && sprite.getY() < item.getY()) && sprite.getY() + sprite.imageResource.getImage().getIconHeight() - item.getY() <= 30) {
			sprite.y_coordinate = item.getY() - sprite.imageResource.getImage().getIconHeight() +1;
		}
		else if (sprite.getY() < background1.getImage().getIconHeight() && sprite.jumpCounter == -1)
			sprite.fall();

		this.repaint();
	}

	// method: keyPressed()
	// description: This method is called when a key is pressed. You can determine which key is pressed using the 
	//				KeyEvent object.  For example if(e.getKeyCode() == KeyEvent.VK_LEFT) would test to see if
	//				the left key was pressed.
	// parameters: KeyEvent e
	@Override
	public void keyPressed(KeyEvent e) {

//		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
//			sprite.walkRight();
//		else if(e.getKeyCode() == KeyEvent.VK_LEFT)
//			sprite.walkLeft();
		
		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			sprite.walkLeft();
			key_left = true;
			key_right = false;
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT && !(sprite.collision(item) && sprite.getY() < item.getY())) {
			sprite.walkRight();
			key_right = true;
			key_left = false;
		}
		
		else if(e.getKeyCode() == KeyEvent.VK_UP)
			sprite.moveUp();
		else if(e.getKeyCode() == KeyEvent.VK_DOWN && !(sprite.collision(item) && sprite.getY() < item.getY()))
			sprite.moveDown();

		else if(e.getKeyCode() == KeyEvent.VK_SPACE)
			sprite.run();
		else if(e.getKeyCode() == KeyEvent.VK_J && (sprite.getY() >= 204 || sprite.collision(item)))
			sprite.jump();
		else if(e.getKeyCode() == KeyEvent.VK_D) {
			playSound("src/sounds/bump.WAV");
			sprite.die();	
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

//		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT)
//			sprite.idle();
		
		if(e.getKeyCode() ==  KeyEvent.VK_LEFT && key_right == false) {
			sprite.idle();	
		}
		else if (e.getKeyCode() ==  KeyEvent.VK_RIGHT && key_left == false) {
			sprite.idle();
		}
		
		else if(e.getKeyCode() ==  KeyEvent.VK_UP || e.getKeyCode() ==  KeyEvent.VK_DOWN)
			sprite.stop_Vertical();


		else if(e.getKeyCode() ==  KeyEvent.VK_SPACE)
			sprite.slowDown();

	}

}
