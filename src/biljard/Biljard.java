//Java projekt 1: Uppgift betyg 5
// Karin Johansson, Hanna Langels, STS2b, HT16

// Biljardspel för två spelare. Spelet går ut på att först få ned alla sina bollar i hålen, den spelare
// som uppnår detta först vinner! Råkar man skjuta ned den vita bollen får den andra spelaren placera ut
// den var den vill på bordet. Stöter man ned en boll får den spelaren fortsätta. Stöter man ned motspelarens 
// boll får man även då fortsätta, men motspelaren får poäng. 

// Må bäste spelare vinna!


package biljard;

import static biljard.Biljard.startButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Karin Johansson, Hanna Langels, HT16
 */
public class Biljard {
    final static int UPDATE_FREQUENCY = 100;    // Global constant: fps, ie times per second to simulate
    static Table table;
    static StartButton startButton;
    static JFrame frame;
    static SidePanel sidePanel;
   
    public static void main(String[] args) {
        Biljard biljard= new Biljard();
        
    }
    
    Biljard(){
        frame = new JFrame("Biljard");          
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        table = new Table();
        startButton = new StartButton();
        
        sidePanel = new SidePanel();
        
        
        
        Container pane = frame.getContentPane();
        
        pane.setLayout(new BorderLayout());
        pane.add(table, BorderLayout.CENTER);
        pane.add(sidePanel, BorderLayout.SOUTH);
        pane.add(startButton, BorderLayout.NORTH);

        frame.add(table);  
        frame.pack();
        frame.setVisible(true);
        
    }
}
class SidePanel extends JPanel {
    JLabel turn;
    JLabel player1;
    JLabel player2;
    private final int FONT_SIZE_PLAYER = 20;
    private final int FONT_SIZE_SCORE = 30;

    SidePanel(){
        setBackground(Color.WHITE);
        setLayout(new FlowLayout(1,25,25));
        turn = new JLabel(Biljard.table.PLAYER1 + "s turn!");
        player1 = new JLabel(Biljard.table.PLAYER1 + ": " + Biljard.table.getPlayer1Points() + " p");
        player1.setForeground(Color.RED);
        player2 = new JLabel(Biljard.table.PLAYER2 + ": " + Biljard.table.getPlayer2Points() + " p");
        player2.setForeground(Color.BLUE);
        turn.setForeground(Color.BLACK);
        Font scoreFont = new Font("Verdana", Font.BOLD, FONT_SIZE_SCORE);
        Font playerFont = new Font("Verdana", Font.BOLD, FONT_SIZE_PLAYER);
        turn.setFont(scoreFont);
        player1.setFont(playerFont);
        player2.setFont(playerFont);
        
        
        
        add(player1);
        add(turn);
        add(player2);
        
        
        setPreferredSize(new Dimension(400,80));
        

    }
}
    
    class Coord {

    double x, y;

    Coord(double xCoord, double yCoord) {
        x = xCoord;
        y = yCoord;
    }
    
    Coord(MouseEvent event) {                   // Create a Coord from a mouse event
        x = event.getX();
        y = event.getY();
    }

    static final Coord ZERO = new Coord(0,0);
    
    double magnitude() {                        
        return Math.sqrt(x * x + y * y);
    }

    Coord norm() {                              // norm: a normalised vector at the same direction
        return new Coord(x / magnitude(), y / magnitude());
    }

    void increase(Coord c) {           
        x += c.x;
        y += c.y;
    }
    
    void decrease(Coord c) {
        x -= c.x;
        y -= c.y;
    }
    
    static double scal(Coord a, Coord b) {      // scalar product
        return a.x * b.x + a.y * b.y;
    } 
    
    static Coord sub(Coord a, Coord b) {        
        return new Coord(a.x - b.x, a.y - b.y);
    }

    static Coord mul(double k, Coord c) {       // multiplication by a constant
        return new Coord(k * c.x, k * c.y);
    }

    static double distance(Coord a, Coord b) {
        return Coord.sub(a, b).magnitude();
    }
    
    static void paintLine(Graphics2D graph2D, Coord a, Coord b){  // paint line between points
        graph2D.setColor(Color.black);
        graph2D.drawLine((int)a.x, (int)a.y, (int)b.x, (int)b.y);
    }
    
    void changeCoord(double xCoord, double yCoord){
        x = xCoord;
        y = yCoord;
    }
    
}

/**
 * ****************************************************************************************
 * Table
 *
 * The table has some constants and instance variables relating to the graphics and
 * the balls. When simulating the balls it starts a timer
 * which fires UPDATE_FREQUENCY times per second. Each time the timer is
 * activated one step of the simulation is performed. The table reacts to
 * events to accomplish repaints and to stop or start the timer.
 *
 */
class Table extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    
    final String PLAYER1 = "Hanna";
    final String PLAYER2 = "Karin";
    
    private final int   WIDTH           = 700;
    private final int   HEIGHT          = 400;
    private final int   WALL_THICKNESS  = 20;
    private final int   NUMBER_OF_BALLS = 9;
    private final int   NUMBER_OF_HOLES = 6;
    private int         pointsPlayer1   = 0;
    private int         pointsPlayer2   = 0;
    private final Color COLOR           = new Color(0,153,76);
    private final Color WALL_COLOR      = new Color(102,51,0);
    Ball  ball1, ball2, ball3, ball4, ball5, ball6, ball7, ball8, ball9;
    private Hole hole1, hole2, hole3, hole4, hole5, hole6;
    private int lastPlayer1Score,lastPlayer2Score;
    private double whiteXpos, whiteYpos;
    
        
    final Timer simulationTimer;
    Ball[] balls = new Ball[NUMBER_OF_BALLS];
    Hole[] holes = new Hole[NUMBER_OF_HOLES];

    
    Table() {
        
        setPreferredSize(new Dimension(WIDTH + 2 * WALL_THICKNESS,
                                       HEIGHT + 2 * WALL_THICKNESS));
        createInitialBalls();
        createHoles();
        
        addMouseListener(this);
        addMouseMotionListener(this);

        simulationTimer = new Timer((int) (1000.0 / Biljard.UPDATE_FREQUENCY), this);
    }

    private void createHoles(){
        hole1 = new Hole(new Coord(WALL_THICKNESS,WALL_THICKNESS),this);
        hole2 = new Hole(new Coord(WIDTH/2 + WALL_THICKNESS,WALL_THICKNESS),this);
        hole3 = new Hole(new Coord(WIDTH+WALL_THICKNESS,WALL_THICKNESS),this);
        hole4 = new Hole(new Coord(WALL_THICKNESS,HEIGHT + WALL_THICKNESS),this);
        hole5 = new Hole(new Coord(WIDTH/2 + WALL_THICKNESS,HEIGHT + WALL_THICKNESS),this);
        hole6 = new Hole(new Coord(WIDTH+WALL_THICKNESS,HEIGHT + WALL_THICKNESS),this);
        
        holes[0] = hole1;
        holes[1] = hole2;
        holes[2] = hole3;
        holes[3] = hole4;
        holes[4] = hole5;
        holes[5] = hole6;
    }
    
    private void createInitialBalls(){
        final Coord whiteInitialPosition = new Coord(WIDTH/4, HEIGHT/2);
        final Coord initialPosition2 = new Coord(500, 175);
        final Coord initialPosition3 = new Coord(550,175);
        final Coord initialPosition4 = new Coord(600,175);
        final Coord initialPosition5 = new Coord(650,175);
        final Coord initialPosition6 = new Coord(500,225);
        final Coord initialPosition7 = new Coord(550,225);
        final Coord initialPosition8 = new Coord(600,225);
        final Coord initialPosition9 = new Coord(650,225);
        
        ball1 = new Ball(whiteInitialPosition,this, Color.WHITE);
        ball2 = new Ball(initialPosition2, this, Color.RED);
        ball3 = new Ball(initialPosition3, this, Color.RED);
        ball4 = new Ball(initialPosition4, this, Color.RED);
        ball5 = new Ball(initialPosition5, this, Color.RED);
        ball6 = new Ball(initialPosition6, this, Color.BLUE);
        ball7 = new Ball(initialPosition7, this, Color.BLUE);
        ball8 = new Ball(initialPosition8, this, Color.BLUE);
        ball9 = new Ball(initialPosition9, this, Color.BLUE);
        
        balls[0] = ball1;
        balls[1] = ball2;
        balls[2] = ball3;
        balls[3] = ball4;
        balls[4] = ball5;
        balls[5] = ball6;
        balls[6] = ball7;
        balls[7] = ball8;
        balls[8] = ball9;
    }
    
    public void actionPerformed(ActionEvent e) {        // Timer event
        
        for (Ball ball:balls){
                ball.move(balls,holes);
                repaint();
        }
       
        if (isBallsStill()){
            simulationTimer.stop();
            if(lastPlayer1Score == getPlayer1Points() && 
                lastPlayer2Score == getPlayer2Points() && 
                    (whiteXpos != ball1.getPosition().x || whiteYpos != ball1.getPosition().y) ){
                updateTurn();
            }
        }
    }
    
    void updateTurn(){
        if(Biljard.sidePanel.turn.getText().equals(Biljard.table.PLAYER1 + "s turn!")){
                    Biljard.sidePanel.turn.setText(Biljard.table.PLAYER2 + "s turn!");
                }
                else{
                    Biljard.sidePanel.turn.setText(Biljard.table.PLAYER1 + "s turn!");
                }
    }
    
    boolean isBallsStill(){
        return (!ball1.isMoving() && 
            !ball2.isMoving() && 
            !ball3.isMoving() &&
            !ball4.isMoving() &&
            !ball5.isMoving() &&
            !ball6.isMoving() &&
            !ball7.isMoving() &&
            !ball8.isMoving() &&
            !ball9.isMoving()); 
    }
    
    boolean isCollidingAny(Coord position){
        for (Ball ball:balls){
            if(Coord.distance(position, ball.getPosition()) <= (ball.RADIUS*1.5)){
                return true;
            }
        
        }  for (Hole hole:holes){
            if(Coord.distance(position, hole.getPosition()) <= (ball1.RADIUS*2)){
                return true;
            }   
        }
        return false;
    }
    
    public void mousePressed(MouseEvent event) {
        Coord mousePosition = new Coord(event);
        ball1.setAimPosition(mousePosition);
        repaint();                       //  To show aiming line
        
    }

    public void mouseReleased(MouseEvent e) {
        ball1.shoot();
        if (!simulationTimer.isRunning()) {
            simulationTimer.start();
            lastPlayer1Score = getPlayer1Points();
            lastPlayer2Score = getPlayer2Points();
            whiteXpos = ball1.getPosition().x;
            whiteYpos = ball1.getPosition().y;
        }
    }

    public void mouseDragged(MouseEvent event) {
        if(isBallsStill()){
            Coord mousePosition = new Coord(event);
            ball1.updateAimPosition(mousePosition);
            repaint();
            
        } 
    }

    public void mouseClicked(MouseEvent e) {
       Coord mousePosition = new Coord(e);
        if(ball1.getPosition().x == 0 && !isCollidingAny(mousePosition)){
            ball1.COLOR = Color.WHITE;
            updateTurn();
            ball1.setWhitePosition(mousePosition);
            repaint();
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // This makes the graphics smoother
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2D.setColor(WALL_COLOR);
        g2D.fillRect(0, 0, WIDTH + 2 * WALL_THICKNESS, HEIGHT + 2 * WALL_THICKNESS);

        g2D.setColor(COLOR);
        g2D.fillRect(WALL_THICKNESS, WALL_THICKNESS, WIDTH, HEIGHT);
        for (Hole hole:holes){
            hole.paint(g2D);
        }
        for (Ball ball:balls){
            ball.paint(g2D);
        }
    }
    
    int getWallThickness(){return WALL_THICKNESS;}
    
    int getPlayer1Points(){
        
           for (int i=0; i< balls.length; i++){
                   if (balls[i].COLOR == Color.RED && balls[i].out){
                       pointsPlayer1 ++;
                   }
                   if (pointsPlayer1 == 4){
                       Biljard.sidePanel.turn.setText(Biljard.table.PLAYER1 + " won!");
                   }
                   
           }return pointsPlayer1;
    }
    
    
    int getPlayer2Points(){
       
       for (int i=0; i< balls.length; i++){ 
           if (balls[i].COLOR == Color.BLUE && balls[i].out){
                       pointsPlayer2 ++;
            }
           if (pointsPlayer2 == 4){
                    Biljard.sidePanel.turn.setText(Biljard.table.PLAYER2 + " won!");
           }
           }return pointsPlayer2;
    }
    
}  // end class Table

/**
 * ****************************************************************************************
 * Ball:
 *
 * The ball has instance variables relating to its graphics and game state:
 * position, velocity, and the position from which a shot is aimed (if any).
 * 
 */
class Ball {

    Color  COLOR;
    boolean out;
    
    private final int    BORDER_THICKNESS    = 2;
    double               RADIUS              = 15;
    private final double DIAMETER            = 2 * RADIUS;
    private final double FRICTION            = 0.015;   // its friction constant (normed for 100 updates/second)
    private final double FRICTION_PER_UPDATE =          // friction applied each simulation step
                          1.0 - Math.pow(1.0 - FRICTION,       // don't ask - I no longer remember how I got to this
                                         100.0 / Biljard.UPDATE_FREQUENCY);           
    private Coord position;
    private Coord velocity;
    private Coord aimPosition;              // if aiming for a shot, ow null
    
    private final Table theTable;
    

    Ball(Coord initialPosition, Table table, Color color){
        theTable = table;
        position = initialPosition;
        velocity = new Coord(0, 0);
        COLOR = color;
        out = false;
                                  
    }

    private boolean isAiming() {
        return aimPosition != null;
    }
    
    boolean isMoving() {                                // if moving too slow I am deemed to have stopped
        return velocity.magnitude() > FRICTION_PER_UPDATE;
    }
    
    boolean isCollidingX(){
        if(position.x >= (theTable.getWidth() - theTable.getWallThickness() - RADIUS) || position.x <= (theTable.getWallThickness()+ RADIUS)){
            return true;
        
        }
        return false;
    
    }
    boolean isCollidingY(){
        if(position.y >= (theTable.getHeight() - theTable.getWallThickness()- RADIUS) || position.y <= (theTable.getWallThickness()+ RADIUS)){
            return true;
        }
        return false;
    }
    
     boolean isCollidingBall(Ball ball){
        return ((distanceBetween(ball)<=DIAMETER) && isRollingAgainst(ball));
    }
     
    boolean isCollidingHole(Hole hole){
      return (Coord.distance(this.position,hole.getPosition())<=(DIAMETER));
    }
    
    boolean isRollingAgainst(Ball ball){
        double deltaX = this.position.x - ball.position.x;
        double deltaY = this.position.y - ball.position.y;
        double difVelX = this.velocity.x - ball.velocity.x;
        double difVelY = this.velocity.y - ball.velocity.y;
        
                return ((deltaX)*(difVelX)+(deltaY)*(difVelY) < 0);
    } 
    
    double distanceBetween(Ball ball){
        double deltaX = position.x - ball.position.x;
        double deltaY = position.y - ball.position.y;
        double distance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        //System.out.println(distance);
        return distance;
    }
    

    void shoot() {
        if (isAiming()) {
            Coord aimingVector = Coord.sub(position, aimPosition);
            velocity = Coord.mul(Math.sqrt(15.0 * aimingVector.magnitude() / Biljard.UPDATE_FREQUENCY),
                                 aimingVector.norm());  // don't ask - determined by experimentation
            aimPosition = null;
        }
    }
  
    void setAimPosition(Coord grabPosition) {
        if (Coord.distance(position, grabPosition) <= RADIUS) {
            aimPosition = grabPosition;
        }
    }
    
    void updateAimPosition(Coord newPosition) {
        if (isAiming()){
            aimPosition = newPosition;
        }
    }
    
    void setWhitePosition(Coord newPosition){
        position = newPosition;
    }

    void move(Ball[] otherBalls, Hole[] theHoles) {
         if (isMoving()) {  
            wallCollision();
            ballCollision(otherBalls);
            holeCollision(theHoles);
            position.increase(velocity);      
            velocity.decrease(Coord.mul(FRICTION_PER_UPDATE, velocity.norm()));
            
        }   
    }
    
    void wallCollision(){
        
        if  (isCollidingX()){
            velocity.x = -velocity.x;
        }
        if (isCollidingY()){
            velocity.y = -velocity.y;
        }   
    }
    
    void ballCollision(Ball[] otherBalls){
        for (int i = 0; i < otherBalls.length; i++){ 
            if (otherBalls[i] != this && isCollidingBall(otherBalls[i])){
                //System.out.println("bollkrock");
                calcVelocityAndBounce(otherBalls[i]);
            }
        }
    }
    
    void holeCollision(Hole[] holes){
        for(Hole hole:holes){
            if(isCollidingHole(hole)){
                velocity.x = 0;
                velocity.y = 0;
                position.x = 0;
                position.y = 0;
                out = true;
                updateScore();
                COLOR = Color.BLACK;
            } 
        }
    }
    void updateScore(){
        Biljard.sidePanel.player1.setText(theTable.PLAYER1 + ": " + theTable.getPlayer1Points() + " p");
        Biljard.sidePanel.player2.setText(theTable.PLAYER2 + ": " + theTable.getPlayer2Points() + " p");
    }
    
    
    // paint: to draw the ball, first draw a black ball
    // and then a smaller ball of proper color inside
    // this gives a nice thick border
    void paint(Graphics2D g2D) {
        g2D.setColor(Color.black);
        g2D.fillOval(
                (int) (position.x - RADIUS + 0.5),
                (int) (position.y - RADIUS + 0.5),
                (int) DIAMETER,
                (int) DIAMETER);
        g2D.setColor(COLOR);
        g2D.fillOval(
                (int) (position.x - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (position.y - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS));
        if (isAiming()) {
            paintAimingLine(g2D);
        }
    }
    
    private void paintAimingLine(Graphics2D graph2D) {
            Coord.paintLine(
                    graph2D,
                    aimPosition, 
                    Coord.sub(Coord.mul(2, position), aimPosition)
                           );
    } 
  
   
    public void calcVelocityAndBounce(Ball ball) {
        double x_ball = ball.velocity.x - calcImpulse(ball)*calcUnityVectorX(ball);
        double y_ball = ball.velocity.y - calcImpulse(ball)*calcUnityVectorY(ball);
        double x_this = this.velocity.x + calcImpulse(ball)*calcUnityVectorX(ball);
        double y_this = this.velocity.y + calcImpulse(ball)*calcUnityVectorY(ball);
        
        ball.velocity.changeCoord(x_ball, y_ball);
        this.velocity.changeCoord(x_this, y_this);
    }

    public double calcImpulse(Ball ball) {
        double dx = calcUnityVectorX(ball);
        double dy = calcUnityVectorY(ball);
        double impulse = ball.velocity.x * dx + ball.velocity.y * dy - 
                        (this.velocity.x * dx + this.velocity.y * dy);
        return impulse;
    }

    public double calcUnityVectorX(Ball ball) {
        double distance = Math.sqrt(Math.pow((this.position.x - ball.position.x), 2)
                + Math.pow((this.position.y - ball.position.y), 2));
        return (this.position.x - ball.position.x) / distance;
    }

    public double calcUnityVectorY(Ball ball) {
        double distance = Math.sqrt(Math.pow((this.position.x - ball.position.x), 2)
                + Math.pow((this.position.y - ball.position.y), 2));
        return (this.position.y - ball.position.y) / distance;
    }
    
    Coord getPosition(){return position;}
    
    
    
    // end  class Ball
} 

class Hole{
    private final Color  COLOR               = Color.BLACK;
    private final int    BORDER_THICKNESS    = 2;
    private final double RADIUS              = 30;
    private final double DIAMETER            = 2 * RADIUS;
                
    private final Coord POSITION;
    private final Table theTable;
    
    Hole(Coord position, Table table){
        POSITION = position;
        theTable = table;
    }
        
   
    void paint(Graphics2D g2D) {
        g2D.setColor(new Color(102,51,0));
        g2D.fillOval(
                (int) (POSITION.x - RADIUS + 0.5),
                (int) (POSITION.y - RADIUS + 0.5),
                (int) DIAMETER,
                (int) DIAMETER);
        g2D.setColor(COLOR);
        g2D.fillOval(
                (int) (POSITION.x - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (POSITION.y - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS));
    }
    
    Coord getPosition(){return POSITION;}
    Color getColor(){return COLOR;}
}

class StartButton extends JButton implements ActionListener {
    
    
    
    StartButton(){
        setText("New game");
        addActionListener(this);
        
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) { 
        
        Biljard.frame.dispose();
        Biljard biljard = new Biljard();
                
      
         
        
    }
}


    

