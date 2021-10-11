package cz.deznekcz.csl.osmeditor.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class TaskInfo {

	public static class Wait<T> {
		private T value;
		
		public synchronized T getValue() {
			return value;
		}
		
		public synchronized void setValue(T value) {
			this.value = value;
		}

		public void waitForValue() {
			try {
				while (null == getValue())
					Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static class Runner implements Runnable {

		private String name;
		private Thread thread;
		private boolean keepRunning;
		private Queue<TaskInfo> tasks;

		public Runner(String name) {
			this.name = name;
			this.tasks = new LinkedList<>();
			this.keepRunning = true;
			this.thread = new Thread(this, name);
			this.thread.start();
		}

		@Override
		public void run() {
			
			while(true) {
				TaskInfo taskToRun = null;
				
				synchronized (this) {
					if (keepRunning) {
						try {
							taskToRun = tasks.poll();
							
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else {
						break;
					}
				}
				
				if (taskToRun != null) {
					final TaskInfo ti = taskToRun;
					Platform.runLater(() -> {
						Alert dialog = new Alert(AlertType.INFORMATION);
						ti.dialog = dialog;
						dialog.setHeaderText(null);
						dialog.setContentText(ti.getMessage());
						dialog.setTitle(ti.name + ": " + ti.getStatus());
						dialog.show();
					});
					taskToRun.run();
				}
			}
		}

		public synchronized void stop() {
			keepRunning = false;
		}

		public synchronized void newTask(String taskname, Consumer<TaskInfo> action) {
			tasks.add(new TaskInfo(taskname, action));
		}
	}

	private String message;
	private TaskStatus status;
	private Alert dialog;
	private String name;
	private Consumer<TaskInfo> action;
	
	public TaskInfo(String name, Consumer<TaskInfo> action) {
		this.name = name;
		this.message = "";
		this.status = TaskStatus.WAITING;
		this.action = action;
	}

	public synchronized void setMessage(String message) {
		this.message = message;
		
		if (dialog != null) {
			Platform.runLater(() -> dialog.setContentText(message));
		}
	}

	public synchronized void setStatus(TaskStatus status) {
		this.status = status;
		if (dialog != null) {
			Platform.runLater(() -> dialog.setTitle(name + ": " + status));
		}
	}

	public synchronized String getMessage() {
		return message;
	}
	
	public synchronized TaskStatus getStatus() {
		return status;
	}
	
	public void run() {
		this.setStatus(TaskStatus.RUNNING);
		this.action.accept(this);
	}
}
