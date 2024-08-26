import shapes.Triangle;
import shapes.TripleMatrix;
import shapes.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        JPanel renderPanel = createJPanel( headingSlider, pitchSlider );

        pane.add( renderPanel, BorderLayout.CENTER );

        headingSlider.addChangeListener( e -> renderPanel.repaint() );
        pitchSlider.addChangeListener( e -> renderPanel.repaint() );

        frame.setSize( 400, 400 );
        frame.setVisible( true );
    }

    private static JPanel createJPanel( JSlider headingSlider, JSlider pitchSlider ) {
        return new JPanel() {
            @Override
            public void paintComponent( Graphics graphics ) {
                Graphics2D graphics2D = initializeGraphics( graphics );

                List<Triangle> tetrahedron = createTetrahedron();

                TripleMatrix transform = createTransformMatrix( headingSlider, pitchSlider );

                BufferedImage img = new BufferedImage( getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB );

                double[] zBuffer = new double[ img.getWidth() * img.getHeight() ];

                Arrays.fill( zBuffer, Double.NEGATIVE_INFINITY );

                for ( Triangle triangle : tetrahedron ) {
                    Vertex vertex1 = transform.transform( triangle.getVertex1() );
                    Vertex vertex2 = transform.transform( triangle.getVertex2() );
                    Vertex vertex3 = transform.transform( triangle.getVertex3() );

                    // manual translations
                    setManualTranslationsFor( vertex1 );
                    setManualTranslationsFor( vertex2 );
                    setManualTranslationsFor( vertex3 );

                    Vertex ab = new Vertex(
                            vertex2.getX() - vertex1.getX(), vertex2.getY() - vertex1.getY(), vertex2.getZ() - vertex1.getZ()
                    );
                    Vertex ac = new Vertex(
                            vertex3.getX() - vertex1.getX(), vertex3.getY() - vertex1.getY(), vertex3.getZ() - vertex1.getZ()
                    );
                    Vertex norm = new Vertex(
                            ab.getY() * ac.getZ() - ab.getZ() * ac.getY(),
                            ab.getZ() * ac.getX() - ab.getX() * ac.getZ(),
                            ab.getX() * ac.getY() - ab.getY() * ac.getX()
                    );

                    double normalLength = Math.sqrt( norm.getX() * norm.getX() + norm.getY() * norm.getY() + norm.getZ() * norm.getZ() );

                    norm.setX( norm.getX() / normalLength );
                    norm.setY( norm.getY() / normalLength );
                    norm.setZ( norm.getZ() / normalLength );

                    // angle between triangle normal and light direction
                    double angleCos = Math.abs( norm.getZ() );


                    // computing rectangular bounds for triangle
                    int minX = ( int ) Math.max(
                            0,
                            Math.ceil( Math.min( vertex1.getX(), Math.min( vertex2.getX(), vertex3.getX() ) ) )
                    );
                    int maxX = ( int ) Math.min(
                            img.getWidth() - 1,
                            Math.floor( Math.max( vertex1.getX(), Math.max( vertex2.getX(), vertex3.getX() ) ) )
                    );
                    int minY = ( int ) Math.max(
                            0,
                            Math.ceil( Math.min( vertex1.getY(), Math.min( vertex2.getY(), vertex3.getY() ) ) )
                    );
                    int maxY = ( int ) Math.min(
                            img.getHeight() - 1,
                            Math.floor( Math.max( vertex1.getY(), Math.max( vertex2.getY(), vertex3.getY() ) ) )
                    );

                    double triangleArea =
                            ( vertex1.getY() - vertex3.getY() ) * ( vertex2.getX() - vertex3.getX() ) +
                                    ( vertex2.getY() - vertex3.getY() ) * ( vertex3.getX() - vertex1.getX() );

                    for ( int y = minY; y <= maxY; y++ ) {
                        for ( int x = minX; x <= maxX; x++ ) {
                            double b1 =
                                    ( ( y - vertex3.getY() ) * ( vertex2.getX() - vertex3.getX() ) +
                                            ( vertex2.getY() - vertex3.getY() ) * ( vertex3.getX() - x ) ) / triangleArea;
                            double b2 =
                                    ( ( y - vertex1.getY() ) * ( vertex3.getX() - vertex1.getX() ) +
                                            ( vertex3.getY() - vertex1.getY() ) * ( vertex1.getX() - x ) ) / triangleArea;
                            double b3 =
                                    ( ( y - vertex2.getY() ) * ( vertex1.getX() - vertex2.getX() ) +
                                            ( vertex1.getY() - vertex2.getY() ) * ( vertex2.getX() - x ) ) / triangleArea;

                            if ( b1 >= 0
                                    && b1 <= 1
                                    && b2 >= 0
                                    && b2 <= 1
                                    && b3 >= 0
                                    && b3 <= 1 ) {

                                double depth = b1 * vertex1.getZ() + b2 * vertex2.getZ() + b3 * vertex3.getZ();
                                int zIndex = y * img.getWidth() + x;

                                if ( zBuffer[ zIndex ] < depth ) {
                                    img.setRGB( x, y, getShade( triangle.getColor(), angleCos ).getRGB() );
                                    zBuffer[ zIndex ] = depth;
                                }
                            }
                        }
                    }
                    graphics2D.drawImage( img, 0, 0, null );
                }
            }

            private TripleMatrix createTransformMatrix( JSlider headingSlider, JSlider pitchSlider ) {
                double heading = Math.toRadians( headingSlider.getValue() );
                double pitch = Math.toRadians( pitchSlider.getValue() );

                TripleMatrix headingTransform = createHeadingTransform( heading );
                TripleMatrix pitchTransform = createPitchTransform( pitch );

                return headingTransform.multiply( pitchTransform );
            }

            private TripleMatrix createPitchTransform( double pitch ) {
                return new TripleMatrix( new double[]{
                        1, 0, 0,
                        0, Math.cos( pitch ), Math.sin( pitch ),
                        0, -Math.sin( pitch ), Math.cos( pitch )
                   }
                );
            }

            private TripleMatrix createHeadingTransform( double heading ) {
                return new TripleMatrix( new double[]{
                        Math.cos( heading ), 0, Math.sin( heading ),
                        0, 1, 0,
                        -Math.sin( heading ), 0, Math.cos( heading )
                    }
                );
            }

            private Graphics2D initializeGraphics( Graphics graphics ) {
                Graphics2D graphics2D = ( Graphics2D ) graphics;

                graphics2D.setColor( Color.BLACK );
                graphics2D.fillRect( 0, 0, getWidth(), getHeight() );

                return graphics2D;
            }

            private void setManualTranslationsFor( Vertex vertex ) {
                vertex.setX( vertex.getX() + ( double ) getWidth() / 2 );
                vertex.setY( vertex.getY() + ( double ) getHeight() / 2 );
            }
        };
    }

    // shading effect
    public static Color getShade( Color color, double shade ) {
        double redLinear = Math.pow( color.getRed(), 2.4 ) * shade;
        double greenLinear = Math.pow( color.getGreen(), 2.4 ) * shade;
        double blueLinear = Math.pow( color.getBlue(), 2.4 ) * shade;

        int red = ( int ) Math.pow( redLinear, 1 / 2.4 );
        int green = ( int ) Math.pow( greenLinear, 1 / 2.4 );
        int blue = ( int ) Math.pow( blueLinear, 1 / 2.4 );

        return new Color( red, green, blue );
    }

    public static List<Triangle> createTetrahedron() {
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
}
