package com.pae.cloudstorage.client;

import com.pae.cloudstorage.common.FSObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * FSTableViewPresentation contains methods to work with TableViews
 * beyond controller. Represents list of FSObjects as Table with 3 columns
 * and ImageView icons for names depends on isDirectory field.
 */
//TODO: Засунуть его в какой-нибудь подходящий пакедж.
public class FSTableViewPresentation {
    public static void updateTable(TableView tw, List<FSObject> rList){
        ObservableList<FSObject> lst = FXCollections.observableArrayList(rList);
        TableColumn nameCol = (TableColumn) tw.getColumns().get(0);
        TableColumn typeCol = (TableColumn) tw.getColumns().get(1);
        TableColumn sizeCol = (TableColumn) tw.getColumns().get(2);
        nameCol.setCellValueFactory(new PropertyValueFactory<FSObject, String>("name"));
        typeCol.setCellValueFactory(new PropertyValueFactory<FSObject, String>("type"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<FSObject, Long>("size"));

        sizeCol.setCellFactory(cell -> {
            return new TableCell<FSObject, Long>(){
                @Override
                protected void updateItem(Long size, boolean b) {
                    if(size == null || b) {
                        setGraphic(null);
                    } else {
                        FSObject tr = getTableRow().getItem();
                        if(tr != null){
                            if(!tr.isDirectory()){
                                Label l = new Label(size.toString());
                                setGraphic(l);
                            }
                        }
                    }
                }
            };
        });

        nameCol.setCellFactory(cell -> {
            return new TableCell<FSObject, String>(){
                @Override
                protected void updateItem(String name, boolean b) {
                    if(name == null || b) {
                        setGraphic(null);
                    } else {
                        FSObject tr = getTableRow().getItem();
                        if(tr != null){
                            Label l = new Label();
                            l.setText(name);
                            ImageView iv;
                            if (tr.isDirectory()) {
                                iv = new ImageView(new Image(getClass().getResourceAsStream("/icons/folder.png")));
                            } else {
                                iv = new ImageView(new Image(getClass().getResourceAsStream("/icons/file.png")));
                            }
                            iv.setFitHeight(18);
                            iv.setFitWidth(18);
                            l.setGraphic(iv);
                            setGraphic(l);
                        }
                    }
                }
            };
        });
        tw.setItems(lst);
        tw.refresh();
    }
}
