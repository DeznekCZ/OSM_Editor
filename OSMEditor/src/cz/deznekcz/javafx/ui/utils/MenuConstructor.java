package cz.deznekcz.javafx.ui.utils;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;

public class MenuConstructor {

	private Menu menu;
	private MenuConstructor parent;
	private MenuBarConstuctor menuBarConstuctor;

	public MenuConstructor(String string, MenuConstructor parent, MenuBarConstuctor menuBarConstuctor) {
		this.menu = new Menu(string);
		this.parent = parent;
		this.menuBarConstuctor = menuBarConstuctor;
		
		if (parent != null) {
			this.parent.menu.getItems().add(this.menu);
		}
	}
	
	public MenuConstructor close() {
		if (this.parent != null) {
			return this.parent;
		}
		else{
			return this;
		}
	}

	public MenuConstructor menu(String string) {
		return new MenuConstructor(string, this, menuBarConstuctor);
	}

	public ItemConstructor item(String string) {
		return new ItemConstructor(string, this, menuBarConstuctor);
	}

	public MenuConstructor separator() {
		this.menu.getItems().add(new SeparatorMenuItem());
		return this;
	}
	
	public MenuBar toMenuBar() {
		return this.menuBarConstuctor.toMenuBar();
	}

	public Menu getMenu() {
		return menu;
	}

	public MenuBarConstuctor next() {
		return menuBarConstuctor;
	}
}
