package edu.umd.cs.piccolo.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.nodes.PLens;

/**
 * This example shows one way to create and use lens's in Piccolo.
 */
public class LensExample extends PFrame {

	public LensExample() {
		this(null);
	}
	
	public LensExample(PCanvas aCanvas) {
		super("LensExample", false, aCanvas);
	}
	
	public void initialize() {
		PRoot root = getCanvas().getRoot();
		PCamera camera = getCanvas().getCamera();		
		PLayer mainLayer = getCanvas().getLayer();	// viewed by the PCanvas camera, the lens is added to this layer.
		PLayer sharedLayer = new PLayer();			// viewed by both the lens camera and the PCanvas camera
		final PLayer lensOnlyLayer = new PLayer();	// viewed by only the lens camera
		
		root.addChild(lensOnlyLayer);
		root.addChild(sharedLayer);
		camera.addLayer(0, sharedLayer);
		
		final PLens lens = new PLens();
		lens.setBounds(10, 10, 100, 130);
		lens.addLayer(0, lensOnlyLayer);		
		lens.addLayer(1, sharedLayer);
		mainLayer.addChild(lens);
		PBoundsHandle.addBoundsHandlesTo(lens);
		
		// Create an event handler that draws squiggles on the first layer of the bottom
		// most camera.
		PDragSequenceEventHandler squiggleEventHandler = new PDragSequenceEventHandler() {
			protected PPath squiggle;
			public void startDrag(PInputEvent e) {
				super.startDrag(e);
				Point2D p = e.getPosition();
				squiggle = new PPath();
				squiggle.moveTo((float)p.getX(), (float)p.getY());
				
				// add squiggles to the first layer of the bottom camera. In the case of the
				// lens these squiggles will be added to the layer that is only visible by the lens,
				// In the case of the canvas camera the squiggles will be added to the shared layer
				// viewed by both the canvas camera and the lens.
				e.getCamera().getLayer(0).addChild(squiggle);
			}
			
			public void drag(PInputEvent e) {
				super.drag(e);				
				updateSquiggle(e);
			}
		
			public void endDrag(PInputEvent e) {
				super.endDrag(e);
				updateSquiggle(e);
				squiggle = null;
			}	
				
			public void updateSquiggle(PInputEvent aEvent) {
				Point2D p = aEvent.getPosition();
				squiggle.lineTo((float)p.getX(), (float)p.getY());
			}
		};
		
		// add the squiggle event handler to both the lens and the
		// canvas camera.
		lens.getCamera().addInputEventListener(squiggleEventHandler);
		camera.addInputEventListener(squiggleEventHandler);

		// make sure that the event handler consumes events so that it doesn't
		// conflic with other event handlers or with itself (since its added to two
		// event sources).
		squiggleEventHandler.getEventFilter().setMarksAcceptedEventsAsHandled(true);
		
		// remove default event handlers, not really nessessary since the squiggleEventHandler
		// consumes everything anyway, but still good to do.
		getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
		getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler());
		
		// create a node that is viewed both by the main camera and by the
		// lens. Note that in its paint method it checks to see which camera
		// is painting it, and if its the lens uses a different color.
		PNode sharedNode = new PNode() {
			protected void paint(PPaintContext paintContext) {
				if (paintContext.getCamera() == lens.getCamera()) {
					Graphics2D g2 = paintContext.getGraphics();
					g2.setPaint(Color.RED);
					g2.fill(getBoundsReference());
				} else {
					super.paint(paintContext);
				}
			}
		};		
		sharedNode.setPaint(Color.GREEN);
		sharedNode.setBounds(0, 0, 100, 200);
		sharedNode.translate(200, 200);
		sharedLayer.addChild(sharedNode);
		
		PText label = new PText("Move the lens \n (by dragging title bar) over the green rectangle, and it will appear red. press and drag the mouse on the canvas and it will draw squiggles. press and drag the mouse over the lens and drag squiggles that are only visible through the lens.");
		label.setConstrainWidthToTextWidth(false);
		label.setConstrainHeightToTextHeight(false);
		label.setBounds(200, 100, 200, 200);
			
		sharedLayer.addChild(label);				
	}
		
	public static void main(String[] args) {
		new LensExample();
	}	
}
