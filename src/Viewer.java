import shapes.Triangle;
import shapes.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Viewer {
    public static void main( String[] args ) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout( new BorderLayout() );

        // horizontal slider
        JSlider headingSlider = new JSlider( 0, 360, 180 );
        pane.add( headingSlider, BorderLayout.SOUTH );

        // vertical slider
        JSlider pitchSlider = new JSlider( SwingConstants.VERTICAL, -90, 90, 0 );
        pane.add( pitchSlider, BorderLayout.EAST );

        // display panel
        JPanel renderPanel = new JPanel() {
            public void paintComponent( Graphics graphics ) {
                Graphics2D graphics2D = ( Graphics2D ) graphics;
                graphics2D.setColor( Color.BLACK );
                graphics2D.fillRect( 0, 0, getWidth(), getHeight() );

                List<Triangle> tetrahedron = createTetrahedron();

                graphics2D.translate( getWidth() / 2, getHeight() / 2 );
                graphics2D.setColor( Color.WHITE );

                for ( Triangle triangle : tetrahedron ) {
                    Path2D path = new Path2D.Double();
                    path.moveTo( triangle.getVertex1().getX(), triangle.getVertex1().getY() );
                    path.lineTo( triangle.getVertex2().getX(), triangle.getVertex2().getY() );
                    path.lineTo( triangle.getVertex3().getX(), triangle.getVertex3().getY() );
                    path.closePath();
                    graphics2D.draw( path );
                }
            }

            private List<Triangle> createTetrahedron() {
                return new ArrayList<>(
                        Arrays.asList(
                                new Triangle(
                                        new Vertex( 100, 100, 100 ),
                                        new Vertex( -100, -100, 100 ),
                                        new Vertex( -100, 100, -100 ),
                                        Color.WHITE ),
                                new Triangle(
                                        new Vertex( 100, 100, 100 ),
                                        new Vertex( -100, -100, 100 ),
                                        new Vertex( 100, -100, -100 ),
                                        Color.RED ),
                                new Triangle(
                                        new Vertex( -100, 100, -100 ),
                                        new Vertex( 100, -100, -100 ),
                                        new Vertex( 100, 100, 100 ),
                                        Color.GREEN ),
                                new Triangle(
                                        new Vertex( -100, 100, -100 ),
                                        new Vertex( 100, -100, -100 ),
                                        new Vertex( -100, -100, 100 ),
                                        Color.BLUE )
                        )
                );
            }
        };
        pane.add( renderPanel, BorderLayout.CENTER );

        frame.setSize( 400, 400 );
        frame.setVisible( true );
    }


}
