package drawshapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.io.PrintWriter;
import java.util.Stack;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame
{
    public enum ShapeType {
        SQUARE,
        CIRCLE,
        RECTANGLE,
        TRIANGLE,
    }
    
    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;
    private Color color = Color.RED;
    private Point startDrag;
    private int distance = 30;
    private Stack<Scene> undoStack = new Stack<Scene>();

    public DrawShapes(int width, int height)
    {
        setTitle("Draw Shapes!");
        scene=new Scene();
        undoStack.add(scene.copy());
        
        // create our canvas, add to this frame's content pane
        shapePanel = new DrawShapesPanel(width,height,scene);
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocation(100,100);
        
        // Add key and mouse listeners to our canvas
        initializeMouseListener();
        initializeKeyListener();
        
        // initialize the menu options
        initializeMenu();

        // Handle closing the window.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    private void initializeMouseListener()
    {
        MouseAdapter a = new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e)
            {
                undoStack.add(scene.copy());
                System.out.printf("Mouse cliked at (%d, %d)\n", e.getX(), e.getY());
                
                if (e.getButton()==MouseEvent.BUTTON1) { 
                    if (shapeType == ShapeType.SQUARE) {
                        scene.addShape(new Square(color, 
                                e.getX(), 
                                e.getY(),
                                100));
                    } else if (shapeType == ShapeType.CIRCLE){
                        scene.addShape(new Circle(color,
                                e.getPoint(),
                                100));
                    } else if (shapeType == ShapeType.RECTANGLE) {
                        scene.addShape(new Rectangle(
                                e.getPoint(),
                                100, 
                                200,
                                color));
                    } else if(shapeType==ShapeType.TRIANGLE){
                        scene.addShape(new Triangle(
                                color,
                                e.getPoint(),
                                100,
                                100));
                    }
                    
                } else if (e.getButton()==MouseEvent.BUTTON2) {
                    // apparently this is middle click
                } else if (e.getButton()==MouseEvent.BUTTON3){
                    // right right-click
                    Point p = e.getPoint();
                    System.out.printf("Right click is (%d, %d)\n", p.x, p.y);
                    List<IShape> selected = scene.select(p);
                    if (selected.size() > 0){
                        for (IShape s : selected){
                            s.setSelected(true);
                        }
                    } else {
                        for (IShape s : scene){
                            s.setSelected(false);
                        }
                    }
                    System.out.printf("Select %d shapes\n", selected.size());
                }
                
            }
            
            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(MouseEvent e)
            {
                System.out.printf("mouse pressed at (%d, %d)\n", e.getX(), e.getY());
                scene.startDrag(e.getPoint());
                
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(MouseEvent e)
            {
                System.out.printf("mouse released at (%d, %d)\n", e.getX(), e.getY());
                scene.stopDrag();
                repaint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.printf("mouse drag! (%d, %d)\n", e.getX(), e.getY());
                scene.updateSelectRect(e.getPoint());
                repaint();
            }

            //shrinking and growing
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                System.out.printf("mouse scroll!");
                int factor = e.getWheelRotation()+100;
                scene.resize(factor);
            }
            
        };
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseListener(a);
        undoStack.add(scene);
    }
    
    /**
     * Initialize the menu options
     */
    private void initializeMenu()
    {
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // file menu
        JMenu fileMenu=new JMenu("File");
        menuBar.add(fileMenu);
        // load
        JMenuItem loadItem = new JMenuItem("Load");
        fileMenu.add(loadItem);
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    try{
                        scene.loadFromFile(selectedFile);
                        repaint();
                    }catch(IOException ex){
                        System.out.println("ex");
                    }
                    System.out.println("load from " +selectedFile.getAbsolutePath());
                    //TODO: load scene from file
                    
                }
            }
        });
        // save
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                // int returnValue = jfc.showOpenDialog(null);
                int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("save to " +selectedFile.getAbsolutePath());
                    //TODO: save scene to file
                    String text = scene.toString();
                    try(PrintWriter out = new PrintWriter(selectedFile)){
                        out.println(text);
                    }catch(IOException ex) {
                        //TODO:
                    }
                    
                }
            }
        });
        fileMenu.addSeparator();
        // edit
        JMenuItem itemExit = new JMenuItem ("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                System.exit(0);
            }
        });

        // color menu
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);

        // red color
        JMenuItem redColorItem= new JMenuItem ("Red");
        colorMenu.add(redColorItem);
        redColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to red
                color = Color.RED;
            }
        });
        
        
        // blue color
        JMenuItem blueColorItem = new JMenuItem ("Blue");
        colorMenu.add(blueColorItem);
        blueColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to blue
                color = Color.BLUE;
            }
        });

        JMenuItem greenColorItem = new JMenuItem ("Green");
        colorMenu.add(greenColorItem);
        greenColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to blue
                color = Color.GREEN;
            }
        });
        
        // shape menu
        JMenu shapeMenu = new JMenu("Shape");
        menuBar.add(shapeMenu);
        
        // square
        JMenuItem squareItem = new JMenuItem("Square");
        shapeMenu.add(squareItem);
        squareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Square");
                shapeType = ShapeType.SQUARE;
            }
        });
        
        // circle
        JMenuItem circleItem = new JMenuItem("Circle");
        shapeMenu.add(circleItem);
        circleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Circle");
                shapeType = ShapeType.CIRCLE;
            }
        });

        // rectangle
        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        shapeMenu.add(rectangleItem);
        rectangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rectangle");
                shapeType = ShapeType.RECTANGLE;
            }
        });

        // triangle
        JMenuItem triangleItem = new JMenuItem("Triangle");
        shapeMenu.add(triangleItem);
        triangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Triangle");
                shapeType = ShapeType.TRIANGLE;
            }
        });
        
        
        // operation mode menu
        JMenu operationModeMenu=new JMenu("Operation");
        menuBar.add(operationModeMenu);
        
        // draw option
        JMenuItem drawItem=new JMenuItem("Resize");
        operationModeMenu.add(drawItem);
        drawItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
            }
        });
        
        // select option
        JMenuItem selectItem=new JMenuItem("Move");
        operationModeMenu.add(selectItem);
        selectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
            }
        });
        

        // set the menu bar for this frame
        this.setJMenuBar(menuBar);
    }
    
    /**
     * Initialize the keyboard listener.
     */
    private void initializeKeyListener()
    {
        //we use up and down arrows for resizing
        //we also add to the undo stack so we can undo our size changes
        shapePanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                System.out.println("key typed: " + e.getKeyChar());
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP) {
                    System.out.println("Up arrow pressed");
                    scene.resize(5);
                    repaint();
                    undoStack.add(scene.copy());
                } else if (key == KeyEvent.VK_DOWN) {
                    System.out.println("Down arrow pressed");
                    scene.resize(-5);
                    repaint();
                    undoStack.add(scene.copy());
                }
            }
            public void keyReleased(KeyEvent e) {
                // TODO: implement this method if you need it
            }
            public void keyTyped(KeyEvent e) {
                // TODO: implement this method if you need it
                char k = e.getKeyChar();
                if(k=='w'){
                    scene.moveSelected(0, -distance);
                }
                if(k=='s'){
                    scene.moveSelected(0, distance);
                }
                if(k=='a'){
                    scene.moveSelected(-distance, 0);
                }
                if(k=='d'){
                    scene.moveSelected(distance, 0);
                }
                repaint();
                if(k=='z'){
                    if(!undoStack.isEmpty()){
                        
                        scene.update(undoStack.peek());
                        repaint();
                        undoStack.pop();//removes current scene
                    }else{
                        System.out.println("undo stack empty");
    
                    }
                }
                             
            }
           
        });
    }
    
    /**
     * @param args
     */

    
    public static void main(String[] args)
    {
        DrawShapes shapes=new DrawShapes(700, 600);
        shapes.setVisible(true);
    }

}
