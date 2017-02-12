package spaceRace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;


public class SpaceShips extends JPanel implements ActionListener, KeyListener{
	private static final long serialVersionUID = 1L;
	private ImageIcon ss1[] = new ImageIcon[16]; // array of images ss1
	private ImageIcon ss2[] = new ImageIcon[16]; // array of images ss2
	private int currentImage1 = 0; // current image index
	private int currentImage2 = 0; // current image index
	private ImageIcon asteroidPic;
	private Timer tm = new Timer(50, this);
	
	private final double deltaAngle= 22.5;//angle of each turn
	private double a1, v1;//declare up the spaceship variables
	private double a2, v2;
	private int x1, y1;
	private int x2, y2;
	private int m;
	private String msg[]={"Start Game","", "Game Over"};//game messages
	private Rectangle ship1;
	private Rectangle ship2;
	private Rectangle asteroid;
	private boolean shipCollision=false;
	
	
	//Getters/Setters so that all variables of this class will not be accessed directly from another class
	public boolean isShipCollision() {
		return shipCollision;
	}

	public void setShipCollision(boolean shipCollision) {
		this.shipCollision = shipCollision;
	}

	public double getA1() {
		return a1;
	}

	public void setA1(double a1) {
		this.a1 = a1;
	}

	public double getV1() {
		return v1;
	}

	public void setV1(double v1) {
		this.v1 = v1;
	}

	public double getA2() {
		return a2;
	}

	public void setA2(double a2) {
		this.a2 = a2;
	}

	public double getV2() {
		return v2;
	}

	public void setV2(double v2) {
		this.v2 = v2;
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public SpaceShips(){
		super.setLayout(new BorderLayout());
		super.setPreferredSize(new Dimension(750, 500));
		m=0;
		startup();
		tm.start();//start Timer
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		// load the images
		for( int i= 1; i <= 16; i++ ){
			ss1[ i-1 ] = new ImageIcon( getClass().getResource("../images/1" + i + ".png" ));
			ss2[ i-1 ] = new ImageIcon( getClass().getResource("../images/2" + i + ".png" ));
		}
		asteroidPic= new ImageIcon( getClass().getResource("../images/asteroidpic.jpg" ));
	}

	public void startup(){//initialize variables for spaceships
		shipCollision=false;
		 a1=0; v1=0;
		 a2=0; v2=0;
		 x1= 325; y1= 405;
		 x2= 325; y2= 450;

	}
	
	public void paintComponent (Graphics g){
		super.paintComponent(g);
		ship1= new Rectangle(x1, y1, 43, 43);
		ship2= new Rectangle(x2, y2, 43, 43);
		g.setColor( Color.black ); g.fillRect(0, 0, 750, 500); // racing space
		asteroid = new Rectangle(100, 100, 550, 300);// asteroid
		g.setColor( Color.white ); g.drawLine(375, 300, 375, 500); // starting line
		asteroidPic.paintIcon(this, g, asteroid.x, asteroid.y);//paint images of the objects in the racing arena
		ss1[ currentImage1 ].paintIcon( this, g, x1, y1 );
		ss2[ currentImage2 ].paintIcon( this, g, x2, y2 );
		Font font = new Font("Arial", Font.PLAIN, 48);//font for game messages
	    g.setFont(font);
		g.setColor( Color.red ); g.drawString(msg[m], 250, 250);
	}
	
	private void collisionDetect(){
		//collision with asteroid and spaceship using the Rectangles
		if(ship1.intersects(asteroid)){
			v1=0;
		}
		if(ship2.intersects(asteroid)){
			v2=0;
		}		
		if(ship1.intersects(ship2)){
			shipCollision=true;
			
		}
	}
	
	public void actionPerformed(ActionEvent e){//called every 50ms (timer)
		if(SpaceGame.isHost){//
			x1 = x1 + (int)(v1*Math.cos(2*Math.PI*a1/360.0));//calculate new coordinates
			y1 = y1 - (int)(v1*Math.sin(2*Math.PI*a1/360.0));
			if(x1 < 0 ){x1= 0;}  if(x1 > 700 ){x1= 700;}//check for collision with borders of arena
			if(y1 < 0 ){y1= 0;}  if(y1 > 450 ){y1= 450;}
			if(SpaceGame.isConnected()){
				String result= String.format("%3d %3d %.2f %.2f", x1,y1,a1,v1);
				SpaceGame.sendString(result);//send the coordinates, angle and velocity
			}
		}else{
			x2 = x2 + (int)(v2*Math.cos(2*Math.PI*a2/360.0));
			y2 = y2 - (int)(v2*Math.sin(2*Math.PI*a2/360.0));
			if(x2 < 0 ){x2= 0;}  if(x2 > 700 ){x2= 700;}
			if(y2 < 0 ){y2= 0;}  if(y2 > 450 ){y2= 450;}
			if(SpaceGame.isConnected()){
				String result= String.format("%3d %3d %.2f %.2f", x2,y2,a2,v2);
				SpaceGame.sendString(result);
			}
		}
		currentImage1 = (int)(a1/22.5);//calculate the new image index
		currentImage2 = (int)(a2/22.5);
		if(SpaceGame.isConnected())collisionDetect();//check for collisions
		repaint();//call paintConmponent
	}
	
	public void keyPressed(KeyEvent e){
		int c = e.getKeyCode();
		if(SpaceGame.isHost){//keys for controling the spaceship
			if (c==KeyEvent.VK_RIGHT){a1 -= deltaAngle; if(a1<0) a1 +=360.0;}//calculate the new angle
			if (c==KeyEvent.VK_LEFT) {a1 += deltaAngle; if(a1>=360.0) a1 -=360.0;}
			if (c==KeyEvent.VK_UP)  {v1 += 10.0; if(v1>100) v1=100.0;}
			if (c==KeyEvent.VK_DOWN){v1 -= 10.0; if(v1<0.0)   v1=0.0;}
		}else{
			if (c==KeyEvent.VK_RIGHT){a2 -= deltaAngle; if(a2<0) a2 +=360.0;}
			if (c==KeyEvent.VK_LEFT) {a2 += deltaAngle; if(a2>=360.0) a2 -=360.0;}
			if (c==KeyEvent.VK_UP)  {v2 += 10.0; if(v2>100) v2=100.0;}
			if (c==KeyEvent.VK_DOWN){v2 -= 10.0; if(v2<0.0) v2=0.0;}
		}
		
	}

	public void keyReleased(KeyEvent arg0){}
	public void keyTyped(KeyEvent arg0) {}

}
