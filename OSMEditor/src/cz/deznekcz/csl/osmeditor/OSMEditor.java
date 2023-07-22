package cz.deznekcz.csl.osmeditor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.BoundingBox;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class OSMEditor extends Application {
	private static final String PROP_PATH = System.getenv("APPDATA") + "\\OSMEditor\\lastrun.cfg";
	private static final String PROP_KEY_MAP_FILE = "map";
	private static final String PROP_KEY_CONF = "render";
	private static final String PROP_KEY_OSM_LOC = "osmloc";
	private static final String PROP_KEY_PNG_LOC = "pngloc";
	private static final String PROP_KEY_PROPOSAL_RENDER = "proposal-render";
	
	private Stage primaryStage;
	private Properties properties;
	
	private OSM osmData;
	private Runner taskThread;
	private OSMConfig config;
//	private Canvas canvas;
	private ScrollPane view;
	private boolean renderProposal;
	private Menu proposalsList;
	private Label statusBar;
	private ImageView[] image;
	private TilePane tiles;

	public static void main(String[] args) {
		launch(args);
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
			properties.put(PROP_KEY_CONF, System.getenv("APPDATA") + "\\OSMEditor\\render.cfg");
			config = new OSMConfig();
			renderProposal = false;
		}
		
		this.primaryStage = primaryStage;
		BorderPane borderFrame = new BorderPane();
		
		image = new ImageView[4];
		for (int i = 0; i < image.length; i++)
			image[i] = new ImageView();
		tiles = new TilePane(0, 0, image);
		tiles.setPrefColumns(2);
		tiles.setPrefRows(2);
		view = new ZoomableScrollPane(new Pane(tiles));
		view.setPrefViewportHeight(900);
		view.setPrefViewportWidth(900);
		statusBar = new Label();
		statusBar.setMinHeight(15);
		
		borderFrame.setTop(menuBar());
		borderFrame.setCenter(view);
		borderFrame.setBottom(statusBar);
		
		Scene scene = new Scene(borderFrame);
		
		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(500);
		
		primaryStage.setMaxHeight(900);
		primaryStage.setMaxWidth(1000);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("OSM Editor");
		primaryStage.show();
		primaryStage.setOnCloseRequest(this::closing);

		System.out.println(view.getHeight());
		System.out.println(view.getWidth());
		
		taskThread = new TaskInfo.Runner("Tasks", statusBar.textProperty());
		if (new File(properties.getProperty(PROP_KEY_CONF)).exists()) {
			taskThread.newTask("Load render config", (info) -> loadConfig(info,new File(properties.getProperty(PROP_KEY_CONF))));
		} else {
			config = new OSMConfig();
		}
		
		if ("true".equals(properties.getProperty(PROP_KEY_PROPOSAL_RENDER)))
			renderProposal = true;
		
		if (properties.containsKey(PROP_KEY_MAP_FILE)) {
			taskThread.newTask("Load map", (info) -> {
				loadMap(info,new File(properties.getProperty(PROP_KEY_MAP_FILE)));
			});
		}
	}

	private MenuBar menuBar() {
		return new MenuBarConstuctor()
			.menu("_File")
				.item("_Open")
					.combination(KeyCode.O, KeyCombination.CONTROL_DOWN)
					.action(this::openFile)
					.close()
				.separator()
				.item("_Exit")
					.combination(KeyCode.F14, KeyCombination.ALT_DOWN)
					.action(this::exit)
					.close()
				.next()
			.menu("_Config")
				.menu("_Proposals")
					.edit(menu -> this.proposalsList = menu)
					.item("_Render", CheckMenuItem::new)
						.combination(KeyCode.P, KeyCombination.CONTROL_DOWN)
						.edit(prt -> prt.selectedProperty().addListener(this::renderProposalChange))
						.close()
					.separator()
					.close()
				.next()
			.menu("_Map")
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
		if (properties.containsKey(PROP_KEY_OSM_LOC))
			fc.setInitialDirectory(new File(properties.getProperty(PROP_KEY_OSM_LOC)));
		
		File osmDataFile = fc.showOpenDialog(this.primaryStage);
		if (osmDataFile != null) {
			properties.setProperty(PROP_KEY_OSM_LOC, osmDataFile.getParentFile().getAbsolutePath());
			taskThread.newTask("Load map", (info) -> {
				loadMap(info,osmDataFile);
			});
		}
	}

	private void setRender(/*Canvas canvas, */OSMRender dataToRender) {
//		var gc = canvas.getGraphicsContext2D();
//		
//		gc.clearRect(0, 0, 9000, 9000);
//		
		var drawers = new ArrayList<Painter>(); 
		
		for (int i = dataToRender.getLowLayerIndex(); i <= dataToRender.getTopLayerIndex(); i++) {
			var layer = dataToRender.getWayLayer(i);
			
			if (layer != null)
				layer.forEach(n -> n.getPainters().forEach(drawers::add));
		}
		
		dataToRender.getNodeLayer()
			.forEach(n -> n.getPainters().forEach(drawers::add));

		dataToRender.getRelationLayer()
			.forEach(n -> n.getPainters().forEach(drawers::add));

		var canvasBounds = new BoundingBox(0, 0, 9000, 9000);
		var params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		
		var c = new Canvas(4500, 4500);
		var gc = c.getGraphicsContext2D();
		
		for (int i = 0; i < 4; i++) {
			var wiml = new WritableImage(4500, 4500);
			var cutX = (i & 0x1) * 4500;
			var cutY = (i & 0x2) * 2250;
			
			gc.clearRect(0, 0, 9000, 9000);
			gc.translate(-cutX, -cutY);
			drawers.forEach((action) -> {
				action.consume(gc, osmData, canvasBounds, true);
			});
			drawers.forEach((action) -> {
				action.consume(gc, osmData, canvasBounds, false);
			});
			gc.setTransform(new Affine());
			
			wiml = c.snapshot(params, wiml);

			image[i].setImage(wiml);
		}
		
//		image.setImage(wim);
	}

	private void loadMap(TaskInfo info, File osmDataFile) {
		info.setMessage("Map is loading");
		osmData = OSM.Load(osmDataFile);
		
		info.setMessage("Map is drawing");
		
		properties.setProperty(PROP_KEY_MAP_FILE, osmDataFile.getAbsolutePath());

		final OSMRender render = dataToRender(info, this.config);
		Platform.runLater(() -> {
			setRender(/*canvas, */render);
			info.setMessage("Map is loaded");
			info.setStatus(TaskStatus.SUCCESS);
		});
	}

	private void loadConfig(TaskInfo info, File file) {
		// TODO config loading now will be stubbed (mocked)
	}
	
	private void exportPNG(ActionEvent actionevent) {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Portable bitmap file (*.png)", "*.png"));
		if (properties.containsKey(PROP_KEY_PNG_LOC))
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

		var proposalList = new ArrayList<String>();
		
		osmData.getNodes().forEach(
				(id, node) -> {
					OSMNodeInfo nodeInfo = config.getInfo(node);
					if (nodeInfo != null && (!node.isProposal() || proposalList.contains(node.getProposeType())))
						render.getNodeLayer().add(nodeInfo);
				}
			);
		
		osmData.getWays().forEach(
				(id, node) -> {
					OSMWayInfo wayInfo = config.getInfo(node);
					if (wayInfo != null && (!node.isProposal() || proposalList.contains(node.getProposeType())))
						render.getWayLayer(wayInfo.getLayer()).add(wayInfo);
				}
			);

		osmData.getRelations().forEach(
				(id, node) -> {
					OSMRelationInfo wayInfo = config.getInfo(node);
					if (wayInfo != null && (!node.isProposal() || proposalList.contains(node.getProposeType())))
						render.getRelationLayer().add(wayInfo);
				}
			);
		
		return render;
	}

	private void renderToPNG(TaskInfo info, OSMRender render, File file) {
		info.setMessage("Generating image");
		
		try {
			var snapshot = new TaskInfo.Wait<WritableImage>();
			Platform.runLater(()->{
				WritableImage wim = new WritableImage(9000,9000);
				tiles.snapshot(new SnapshotParameters(), wim);
				snapshot.setValue(wim);
			});
			
			snapshot.waitForValue();
			
			BufferedImage bufferImage = new BufferedImage(9000,9000,BufferedImage.TYPE_4BYTE_ABGR);
			BufferedImage image = SwingFXUtils.fromFXImage(snapshot.getValue(), bufferImage);
			
            ImageIO.write(image, "png", file);

        	info.setStatus(TaskStatus.SUCCESS);
        } catch (Exception s) {
        	s.printStackTrace(System.err);
        	info.setMessage(s.getLocalizedMessage());
        	info.setStatus(TaskStatus.FAIL);
        }
	}
	
	private void renderProposalChange(ObservableValue<? extends Boolean> o, boolean l, boolean n) {
		if (this.renderProposal && !n) {
			this.renderProposal = false;
			
			taskThread.newTask("Render new configuration", info -> {
				final OSMRender render = dataToRender(info, this.config);
				Platform.runLater(() -> setRender(/*canvas, */render));
			});
		}
	}
}
