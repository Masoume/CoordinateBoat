package sami.service.pathplanning.testing;

import sami.environment.Continuous3DEnvironment;
import sami.environment.EnvironmentModel;
import sami.path.Path;
import sami.path.Waypoint2D;
import sami.path.Waypoints2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class ViewerP extends JPanel {

    private final EnvironmentModel em;
    private final Path path;

    public ViewerP(EnvironmentModel em, Path path) {
        this.em = em;
        this.path = path;

        setPreferredSize(new Dimension(500, 500));
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        double width = (double) getWidth();
        double height = (double) getHeight();

        double cells = 300;
        
        if (em instanceof Continuous3DEnvironment) {
            Continuous3DEnvironment ce = (Continuous3DEnvironment) em;
            double edx = ce.getWidth() / cells;
            double edy = ce.getLength() / cells;
                       
            double vdx = width / cells;
            double vdy = height / cells;
                        
            // System.out.println("edy = " + edy + " " + ce.getLength() + " " + cells + " " + vdy);
            
            double wpdx = width / ce.getWidth();
            double wpdy = height / ce.getLength();
            
            // System.out.println(wpdx +" " + wpdy);
            
            double z = 0.0;
            // @todo sliders
            float upperCost = 10.0f;
            float lowerCost = 0.0f;
            
            for (int i = 0; i < cells; i++) {
                for (int j = 0; j < cells; j++) {
                    // @todo z axis
                    double c = ce.getPointCost((i-1) * edx, (j-1) * edy, z);
                    // if (i == 20 && j == 20) System.out.println("c = "  + c + " " + i + " " + j + " " + (i-1) * edx + " " + (j-1) * edy);
                    
                    c = Math.max(lowerCost, Math.min(upperCost, c));
                    c /= (upperCost - lowerCost);
                    
                    // @todo Scale
                    g2.setColor(new Color((float)c, 0.0f, 0.0f));
                    
                    g2.fillRect((int)(i * vdx), (int)(height - j * vdy), (int)Math.ceil(vdx), (int)Math.ceil(vdy));
                }
            }
            
            if (path instanceof Waypoints2D) {
                g2.setColor(Color.yellow);
                Waypoints2D wpp = (Waypoints2D)path;
                int px = -1, py = 1;
                final int wpSize = 6;
                for (Waypoint2D waypoint2D : wpp.getWps()) {
                    g2.fillOval((int)(waypoint2D.x * wpdx - wpSize/2), (int)(height - waypoint2D.y * wpdy - wpSize/2), wpSize, wpSize);
                    if (px >= 0) {
                        g2.drawLine((int)(waypoint2D.x * wpdx), (int)(height - waypoint2D.y * wpdy), (int)(px * wpdx), (int)(height - py * wpdy));                        
                    }
                    px = waypoint2D.x;
                    py = waypoint2D.y;
                }
            }                        

        }

    }
}
