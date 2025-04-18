package drawshapes;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;


public class Triangle extends AbstractShape {

    protected int base;
    protected int height;

	public Triangle(Color color, Point center, int base, int height) {
		super(new Point(center.x, center.y));
        setBoundingBox(center.x+base/2, center.x-base/2, center.y+height/2, center.y-height/2);
        this.color=color;
        this.base=base;
        this.height=height;
	}

	// Implementing the draw method from IShape
	@Override
	public void draw(Graphics g) {
        //We create two arrays of integers to define the triangle
        int bottomCornerY = getAnchorPoint().y+height/2;
        int topCornerY = getAnchorPoint().y-height/2;
        int leftCornerX = getAnchorPoint().x+base/2;
        int rightCornerX = getAnchorPoint().x-base/2;
        int topCornerX = getAnchorPoint().x;
        
        
        int[] yPoints = {topCornerY,bottomCornerY,bottomCornerY};
        int[] xPoints = {topCornerX,leftCornerX,rightCornerX};
		if (isSelected()){
            g.setColor(this.color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    @Override
	public void setAnchorPoint(Point point) {
		
	}

    public String toString() {
        return String.format("TRIANGLE %d %d %d %s %s", 
                getAnchorPoint().x,
                getAnchorPoint().y,
                base,
                height,
                Util.colorToString(getColor()),
                selected);
    }

    @Override
    public Triangle copy(){
        return new Triangle(color, anchorPoint, base, height);
    }

    @Override
    public void expand(int factor){
        base += factor;
        height += factor;
    }

}
