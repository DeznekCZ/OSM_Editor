package cz.deznekcz.csl.osmeditor.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

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
		private ObservableStringValue statusInfo;
		public Property<TaskStatus> taskStatus;
		public StringProperty taskName;
		public StringProperty taskMessage;

		public Runner(String name, StringProperty statusInfo) {
			if (!Platform.isFxApplicationThread())
				throw new IllegalAccessError("Runner must be started from FXApplication thread!");
				
			this.name = name;
			this.tasks = new LinkedList<>();
			this.keepRunning = true;

			this.taskName = new SimpleStringProperty();
			this.taskMessage = new SimpleStringProperty();
			this.taskStatus = new SimpleObjectProperty<>(TaskStatus.WAITING);
			
			this.statusInfo = Bindings.concat(
						taskStatus,
						
						Bindings.when(taskName.isEmpty())
							.then("")
							.otherwise(Bindings.concat(" (",	taskName, ")")),
							
						Bindings.when(taskMessage.isEmpty())
							.then("")
							.otherwise(Bindings.concat(": ", taskMessage))
					);
			statusInfo.bind(this.statusInfo);
			
			this.thread = new Thread(this, this.name);
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
					setName(taskToRun.getName());
					setMessage(null);
					setStatus(TaskStatus.RUNNING);
					taskToRun.run();
				}
			}
		}

		public void setName(String name) {
			if (Platform.isFxApplicationThread())
				taskName.setValue(name);
			else
				Platform.runLater(() -> taskName.setValue(name));
		}

		public void setMessage(String message) {
			if (Platform.isFxApplicationThread())
				taskMessage.setValue(message);
			else
				Platform.runLater(() -> taskMessage.setValue(message));
		}

		public void setStatus(TaskStatus status) {
			if (Platform.isFxApplicationThread())
				taskStatus.setValue(status);
			else
				Platform.runLater(() -> taskStatus.setValue(status));
		}

		public synchronized void stop() {
			keepRunning = false;
		}

		public synchronized void newTask(String taskname, Consumer<TaskInfo> action) {
			tasks.add(new TaskInfo(taskname, action, this));
		}
	}

	private String message;
	private TaskStatus status;
	private String name;
	private Consumer<TaskInfo> action;
	private Runner manager;
	
	public TaskInfo(String name, Consumer<TaskInfo> action, TaskInfo.Runner manager) {
		this.name = name;
		this.message = "";
		this.status = TaskStatus.WAITING;
		this.action = action;
		this.manager = manager;
	}
	
	public String getName() {
		return name;
	}

	public synchronized void setMessage(String message) {
		this.message = message;
		
		if (Platform.isFxApplicationThread())
			manager.taskMessage.setValue(message);
		else
			Platform.runLater(() -> manager.taskMessage.setValue(message));
	}

	public synchronized void setStatus(TaskStatus status) {
		this.status = status;
		
		if (Platform.isFxApplicationThread())
			manager.taskStatus.setValue(status);
		else
			Platform.runLater(() -> manager.taskStatus.setValue(status));
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
