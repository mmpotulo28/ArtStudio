import java.awt.Color;

public class Circle extends Shape{
    
    public Circle(int x, int y, int w , int h, Color c , int f){
        super(x, y ,w, h ,c);
        if(f == 1){
            isFilled = true;
        }
    }
}
