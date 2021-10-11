package cz.deznekcz.javafx.ui.utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.stage.Stage;

public class ItemConstructor {

	private MenuItem item;
	private MenuConstructor parent;
	private MenuBarConstuctor menuBarConstuctor;

	public ItemConstructor(String string, MenuConstructor parent, MenuBarConstuctor menuBarConstuctor) {
		this.item = new MenuItem(string);
		this.parent = parent;
		this.menuBarConstuctor = menuBarConstuctor;
		
		if (parent != null) {
			this.parent.getMenu().getItems().add(this.item);
		}
	}

	public ItemConstructor combination(KeyCode code, Modifier...modifiers) {
		this.item.setAccelerator(new KeyCodeCombination(code, modifiers));
		return this;
	}
	
	public ItemConstructor action(EventHandler<ActionEvent> action) {
		this.item.setOnAction(action);
		return this;
	}
	
	public MenuBar toMenuBar() {
		return this.menuBarConstuctor.toMenuBar();
	}
	
	public MenuConstructor close() {
		return parent;
	}
}