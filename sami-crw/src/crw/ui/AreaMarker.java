/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.ui;


import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;

import sami.area.Area2D;

/**
 *
 * @author Masoume
 */
public class AreaMarker extends SurfacePolygon{
    
    private final Polygon area;
    
    public AreaMarker(Polygon polygon, ShapeAttributes sa) {
        super(sa);
        this.area = polygon;
        
    }
    
}
