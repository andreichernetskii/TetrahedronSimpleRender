import shapes.Triangle;
import shapes.TripleMatrix;
import shapes.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
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

                double heading = Math.toRadians( headingSlider.getValue() );
                TripleMatrix headingTransform = new TripleMatrix( new double[] {
                        Math.cos( heading ), 0, Math.sin( heading ),
                        0, 1, 0,
                        -Math.sin( heading ), 0, Math.cos( heading )
                    }
                );

                double pitch = Math.toRadians( pitchSlider.getValue() );
                TripleMatrix pitchTransform = new TripleMatrix( new double[] {
                        1, 0, 0,
                        0, Math.cos( pitch ), Math.sin( pitch ),
                        0, -Math.sin( pitch ), Math.cos( pitch )
                    }
                );

                TripleMatrix transform = headingTransform.multiply( pitchTransform );


                graphics2D.translate( getWidth() / 2, getHeight() / 2 );
                graphics2D.setColor( Color.WHITE );

                for ( Triangle triangle : tetrahedron ) {
                    Vertex vertex1 = transform.transform( triangle.getVertex1() );
                    Vertex vertex2 = transform.transform( triangle.getVertex2() );
                    Vertex vertex3 = transform.transform( triangle.getVertex3() );

                    Path2D path = new Path2D.Double();
                    path.moveTo( vertex1.getX(), vertex1.getY() );
                    path.lineTo( vertex2.getX(), vertex2.getY() );
                    path.lineTo( vertex3.getX(), vertex3.getY() );
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

        headingSlider.addChangeListener( e -> renderPanel.repaint() );
        pitchSlider.addChangeListener( e -> renderPanel.repaint() );

        frame.setSize( 400, 400 );
        frame.setVisible( true );
    }


}
