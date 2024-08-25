package shapes;

public class TripleMatrix {
    double[] values;

    public TripleMatrix( double[] values ) {
        this.values = values;
    }

    public TripleMatrix multiply( TripleMatrix otherMatrix ) {
        double[] result = new double[ 9 ];

        for ( int row = 0; row < 3; row++ ) {
            for ( int column = 0; column < 3; column++ ) {
                for ( int i = 0; i < 3; i++ ) {
                    result[ row * 3 + column ] += this.values[ row * 3 + i ] * otherMatrix.values[ i * 3 + column ];
                }
            }
        }

        return new TripleMatrix( result );
    }

    public Vertex transform( Vertex in ) {
        return new Vertex(
                in.getX() * values[0] + in.getY() * values[3] + in.getZ() * values[6],
                in.getX() * values[1] + in.getY() * values[4] + in.getZ() * values[7],
                in.getX() * values[2] + in.getY() * values[5] + in.getZ() * values[8]
        );
    }
}
