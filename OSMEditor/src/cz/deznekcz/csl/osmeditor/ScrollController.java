package cz.deznekcz.csl.osmeditor;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class ScrollController {
	
	private ScrollPane sp;

	public ScrollController(ScrollPane sp) {
		this.sp = sp;
		
		sp.setOnMousePressed(this::start);
		sp.setOnMouseReleased(this::end);
		sp.addEventFilter(ScrollEvent.SCROLL, this::scroll);
	}

	public void start(MouseEvent event) {
		sp.setPannable(event.getButton() == MouseButton.PRIMARY);	
	}

	public void end(MouseEvent event) {
		sp.setPannable(false);
	}
	
	public void scroll(ScrollEvent event) {
		var node = sp.getContent();
		node.setScaleX(event.getDeltaY()/300 + node.getScaleX());
		node.setScaleY(event.getDeltaY()/300 + node.getScaleY());
		event.consume();
	}
}
