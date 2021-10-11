package cz.deznekcz.csl.osmeditor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMConfig;
import cz.deznekcz.csl.osmeditor.data.OSMRelationInfo;
import cz.deznekcz.csl.osmeditor.data.OSMRender;
import cz.deznekcz.csl.osmeditor.ui.OSMNodeInfo;
import cz.deznekcz.csl.osmeditor.ui.OSMWayInfo;
import cz.deznekcz.csl.osmeditor.ui.ZoomableScrollPane;
import cz.deznekcz.csl.osmeditor.util.TaskInfo;
import cz.deznekcz.csl.osmeditor.util.TaskInfo.Runner;
import cz.deznekcz.csl.osmeditor.util.TaskStatus;
import cz.deznekcz.javafx.ui.utils.MenuBarConstuctor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class OSMEditor extends Application {
	private static final String PROP_PATH = System.getProperty("APPDATA") + "\\OSMEditor\\lastrun.cfg";
	private static final String PROP_KEY_MAP_FILE = "map";
	private static final String PROP_KEY_CONF = "render";
	private static final String PROP_KEY_OSM_LOC = "osmloc";
	private static final String PROP_KEY_PNG_LOC = "pngloc";
	
	private Stage primaryStage;
	private Properties properties;
	
	private OSM osmData;
	private Runner taskThread;
	private OSMConfig config;
	private Canvas canvas;

	public static void main(String[] args) {
		OSMEditor.launch(args);
	}

	public OSMEditor() {};
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		properties = new Properties();
		try {
			properties.load(new FileInputStream(PROP_PATH));
		} catch (Exception e) {
			// No older instance was running
			// Default properties
			properties.put(PROP_KEY_CONF, System.getProperty("APPDATA") + "\\OSMEditor\\render.cfg");
			config = new OSMConfig();
		}
		
		this.primaryStage = primaryStage;
		BorderPane borderFrame = new BorderPane();
		
		canvas = new Canvas(9000, 9000);
		ScrollPane sp = new ZoomableScrollPane(canvas);
		sp.setPrefViewportHeight(900);
		sp.setPrefViewportWidth(900);
		
		borderFrame.setTop(menuBar());
		borderFrame.setCenter(sp);
		
		Scene scene = new Scene(borderFrame);
		
		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(500);
		
		primaryStage.setMaxHeight(900);
		primaryStage.setMaxWidth(1000);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("OSM Editor");
		primaryStage.show();
		primaryStage.setOnCloseRequest(this::closing);

		System.out.println(sp.getHeight());
		System.out.println(sp.getWidth());
		
		taskThread = new TaskInfo.Runner("Tasks");
		if (new File(properties.getProperty(PROP_KEY_CONF)).exists()) {
			taskThread.newTask("Load render config", (info) -> loadConfig(info,new File(properties.getProperty(PROP_KEY_CONF))));
		} else {
			config = new OSMConfig();
		}
		
		if (properties.containsKey(PROP_KEY_MAP_FILE)) {
			taskThread.newTask("Load map", (info) -> {
				loadMap(info,new File(properties.getProperty(PROP_KEY_MAP_FILE)));
			});
		}
	}

	private MenuBar menuBar() {
		return new MenuBarConstuctor()
			.menu("&File")
				.item("&Open")
					.combination(KeyCode.O, KeyCombination.CONTROL_DOWN)
					.action(this::openFile)
					.close()
				.separator()
				.item("&Exit")
					.combination(KeyCode.F14, KeyCombination.ALT_DOWN)
					.action(this::exit)
					.close()
				.next()
			.menu("&Map")
				.item("Export as PNG")
					.combination(KeyCode.E, KeyCombination.CONTROL_DOWN)
					.action(this::exportPNG)
			.toMenuBar();
	}
	
	private void closing(WindowEvent windowevent) {
		windowevent.consume();
		exit(null);
	}

	private void exit(ActionEvent actionevent) {
		try {
			new File(PROP_PATH).getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(PROP_PATH);
			
			properties.store(fos, "OSM Editor session data");
			
			fos.close();
			
		} catch (IOException e) {
			Alert d = new Alert(AlertType.ERROR);
			d.setTitle("Exiting failed");
			d.setHeaderText(null);
			d.setContentText(
					"Information about last session can not be stored."
					+ "\nFile can not be created:"
					+ "\n  " + PROP_PATH);
		}
		
		taskThread.stop();
		Platform.exit();
	}
	
	private void openFile(ActionEvent actionevent) {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("OSM map file (*.osm)", "*.osm"));
		if (properties.contains(PROP_KEY_OSM_LOC))
			fc.setInitialDirectory(new File(properties.getProperty(PROP_KEY_OSM_LOC)));
		
		File osmDataFile = fc.showOpenDialog(this.primaryStage);
		if (osmDataFile != null) {
			properties.setProperty(PROP_KEY_OSM_LOC, osmDataFile.getParentFile().getAbsolutePath());
			taskThread.newTask("Load map", (info) -> {
				loadMap(info,osmDataFile);
			});
		}
	}

	private void setRender(Canvas canvas, OSMRender dataToRender) {
		var gc = canvas.getGraphicsContext2D();
		var canvasBounds = canvas.getLayoutBounds();
		
		gc.clearRect(0, 0, 9000, 9000);
		
		for (int i = dataToRender.getLowLayerIndex(); i <= dataToRender.getTopLayerIndex(); i++) {
			var layer = dataToRender.getWayLayer(i);
			
			if (layer != null)
				layer.forEach(n -> n.getPainters()
						.forEach(p -> p.consume(gc, osmData, canvasBounds)));
		}
		
		dataToRender.getNodeLayer()
			.forEach(n -> n.getPainters()
					.forEach(p -> p.consume(gc, osmData, canvasBounds)));

		dataToRender.getRelationLayer()
			.forEach(n -> n.getPainters()
					.forEach(p -> p.consume(gc, osmData, canvasBounds)));
	}

	private void loadMap(TaskInfo info, File osmDataFile) {
		info.setMessage("Map is loading");
		osmData = OSM.Load(osmDataFile);
		
		info.setMessage("Map is drawing");
		
		info.setMessage("Map is loaded");
		info.setStatus(TaskStatus.SUCCESS);
		properties.setProperty(PROP_KEY_MAP_FILE, osmDataFile.getAbsolutePath());

		final OSMRender render = dataToRender(info, this.config);
		Platform.runLater(() -> setRender(canvas, render));
	}

	private void loadConfig(TaskInfo info, File file) {
		// TODO config loading now will be stubbed (mocked)
	}
	
	private void exportPNG(ActionEvent actionevent) {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Portable bitmap file (*.png)", "*.png"));
		if (properties.contains(PROP_KEY_PNG_LOC))
			fc.setInitialDirectory(new File(properties.getProperty(PROP_KEY_PNG_LOC)));
		
		File osmDataFile = fc.showSaveDialog(this.primaryStage);
		if (osmDataFile != null) {
			properties.setProperty(PROP_KEY_PNG_LOC, osmDataFile.getParentFile().getAbsolutePath());
			// complex export settings dialog
			
			taskThread.newTask("Creating PNG file", (info) -> {
				OSMConfig config = this.config;
				OSMRender render = dataToRender(info, config);
				renderToPNG(info, render, osmDataFile);
			});
		}
	}

	private OSMRender dataToRender(TaskInfo info, OSMConfig config) {
		OSMRender render = new OSMRender();

		osmData.getNodes().forEach(
				(id, node) -> {
					OSMNodeInfo nodeInfo = config.getInfo(node);
					if (nodeInfo != null)
						render.getNodeLayer().add(nodeInfo);
				}
			);
		
		osmData.getWays().forEach(
				(id, node) -> {
					OSMWayInfo wayInfo = config.getInfo(node);
					if (wayInfo != null)
						render.getWayLayer(wayInfo.getLayer()).add(wayInfo);
				}
			);

		osmData.getRelations().forEach(
				(id, node) -> {
					OSMRelationInfo wayInfo = config.getInfo(node);
					if (wayInfo != null)
						render.getRelationLayer().add(wayInfo);
				}
			);
		
		return render;
	}

	private void renderToPNG(TaskInfo info, OSMRender render, File file) {
		info.setMessage("Generating image");
		
		var snapshot = new TaskInfo.Wait<WritableImage>();
		Platform.runLater(()->{
			setRender(canvas, render);
			WritableImage wim = new WritableImage(9000,9000);
			var params = new SnapshotParameters();
			params.setFill(Color.TRANSPARENT);
			canvas.snapshot(params, wim);
			snapshot.setValue(wim);
		});
		
		try {
			snapshot.waitForValue();
			BufferedImage buffer = new BufferedImage(9000,9000,BufferedImage.TYPE_4BYTE_ABGR);
			BufferedImage image = SwingFXUtils.fromFXImage(snapshot.getValue(), buffer);
			
            ImageIO.write(image, "png", file);

        	info.setStatus(TaskStatus.SUCCESS);
        } catch (Exception s) {
        	s.printStackTrace(System.err);
        	info.setMessage(s.getLocalizedMessage());
        	info.setStatus(TaskStatus.FAIL);
        }
	}
}
