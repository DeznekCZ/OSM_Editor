package cz.deznekcz.javafx.ui.utils;

import javafx.scene.control.MenuBar;

public class MenuBarConstuctor {

	private MenuBar menuBar;
	
	public MenuBarConstuctor() {
		menuBar = new MenuBar();
	}
	
	public MenuBar toMenuBar() {
		return this.menuBar;
	}

	public MenuConstructor menu(String string) {
		MenuConstructor mc = new MenuConstructor(string, null, this);
		this.menuBar.getMenus().add(mc.getMenu());
		return mc;
	}

}
