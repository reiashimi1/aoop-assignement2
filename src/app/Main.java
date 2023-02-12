package app;

import javafx.application.Application;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.geometry.Insets;


import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import domain.collections.StarCollection;
import domain.galaxy.Star;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;




public class Main extends Application {
   public void start(Stage stage)throws Exception {
	
	      Menu fileMenu = new Menu("File");
	      Menu starMenu = new Menu("Star");
	      Menu speciesMenu = new Menu("Species");
	      Menu GraphsMenu = new Menu("Graphs");
	      Menu HelpMenu = new Menu("Help");
	      
	      Menu new1 = new Menu("New");
	      Menu load = new Menu("Load");
	      MenuItem exit = new MenuItem("Exit");
	      
	      MenuItem show = new MenuItem("Show Stars");
	      MenuItem create = new MenuItem("Create Star");
	      MenuItem add = new MenuItem("Add Star");
	      
	      MenuItem show1 = new MenuItem("Show Species");
	      MenuItem create1 = new MenuItem("Create Species");
	      
	      MenuItem pie = new MenuItem("Pie chart");
	      MenuItem bar = new MenuItem("Bar chart");
	      MenuItem line = new MenuItem("Line chart");
	      
	      MenuItem about = new MenuItem("About");
	      
	      MenuItem sub1 = new MenuItem("Load Star");
	      MenuItem sub2 = new MenuItem("Load Species");
	      
	      MenuItem sub3 = new MenuItem("New Star");
	      MenuItem sub4 = new MenuItem("New Species");
	    
	      load.getItems().addAll(sub1, sub2);
	      new1.getItems().addAll(sub3, sub4);
	    
	      SeparatorMenuItem sep = new SeparatorMenuItem();
	     
	      fileMenu.getItems().addAll(new1, load, sep, exit);
	      starMenu.getItems().addAll(show, create,add);
	      speciesMenu.getItems().addAll(show1, create1);
	      GraphsMenu.getItems().addAll(pie, bar,line);
	      HelpMenu.getItems().addAll(about);
	     
	      MenuBar menuBar = new MenuBar(fileMenu,starMenu,speciesMenu, GraphsMenu,HelpMenu);
	      menuBar.setTranslateX(200);
	      menuBar.setTranslateY(20);
	     
	      Group root = new Group(menuBar);
	      Scene scene = new Scene(root, 595, 200, Color.BEIGE);
	      stage.setTitle("Menu");
	      stage.setScene(scene);
	      stage.show();
	      
	      
Dialog<String> dialog = new Dialog<String>();
dialog.setTitle("About");
ButtonType type = new ButtonType("Close", ButtonData.OK_DONE);
dialog.setContentText("Information about the app.");
dialog.getDialogPane().getButtonTypes().add(type);
	     
	      about.setOnAction(e -> {
	         dialog.showAndWait();
	      });
	     
	   
   

	         

	          sub1.setOnAction(new EventHandler<ActionEvent>() {
	              @Override
	              public void handle(ActionEvent event) {
	                  FileChooser fileChooser = new FileChooser();
	                  fileChooser.setTitle("Open CSV File");
	                  File file = fileChooser.showOpenDialog(stage);
	                  if (file != null) {
	                      try {
	                          BufferedReader br = new BufferedReader(new FileReader(file));
	                          String line;
	                          while ((line = br.readLine()) != null) {
	                              String[] values = line.split(",");
	                              System.out.println(line);
	                          }
	                          br.close();
	                      } catch (IOException e) {
	                          e.printStackTrace();
	                      }
	                  }
	              }
	          });
	          
	          add.setOnAction(new EventHandler<ActionEvent>() {
	              @Override
	              public void handle(ActionEvent event) {
	            	  
	              }
	          });
	          
	          
	          show.setOnAction(new EventHandler<ActionEvent>() {
	              @Override
	              public void handle(ActionEvent event) {
	            	
	              }
	          });
      
	          
	          

	      }
	  
	      
   
	      
   
   public static void main(String args[]){
      launch(args);
   }
}


