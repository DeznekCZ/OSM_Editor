package cz.deznekcz.javafx.ui.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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

	public ItemConstructor<MenuItem> item(String string) {
		return new ItemConstructor<MenuItem>(string, this, menuBarConstuctor, MenuItem::new);
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
	
	public MenuConstructor edit(Consumer<Menu> edit) {
		edit.accept(this.menu);
		return this;
	}

	public <T extends MenuItem> ItemConstructor<T> item(String string, Supplier<T> type) {
		return new ItemConstructor<T>(string, this, menuBarConstuctor, type);
	}
}
