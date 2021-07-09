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

import java.util.Comparator;
import java.util.List;

/**
 * FSTableViewPresentation contains methods to work with TableViews
 * beyond controller. Represents list of FSObjects as Table with 4 columns
 * and ImageView icons for names depends on isDirectory field.
 */
//TODO: Засунуть его в какой-нибудь подходящий пакедж.
public class FSTableViewPresentation {
    public static void updateTable(TableView tw, List<FSObject> rList){
        ObservableList<FSObject> lst = FXCollections.observableArrayList(rList);
        TableColumn so = (TableColumn) tw.getSortOrder().get(0);
        tw.setItems(lst);
        tw.getSortOrder().add(so);
        tw.refresh();
    }

    public static void init(TableView tw){
        TableColumn nameCol = (TableColumn) tw.getColumns().get(0);
        TableColumn typeCol = (TableColumn) tw.getColumns().get(1);
        TableColumn sizeCol = (TableColumn) tw.getColumns().get(2);

        nameCol.setCellValueFactory(new PropertyValueFactory<FSObject, FSObject>("object"));
        typeCol.setCellValueFactory(new PropertyValueFactory<FSObject, FSObject>("object"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<FSObject, FSObject>("object"));

        nameCol.setComparator(new NameComparator());
        sizeCol.setComparator(new SizeComparator());
        typeCol.setComparator(new TypeComparator());

        nameCol.setSortType(TableColumn.SortType.ASCENDING);
        sizeCol.setCellFactory(cell -> new TableCell<FSObject, FSObject>(){
            @Override
            protected void updateItem(FSObject obj, boolean b) {
                if(obj == null || b) {
                    setGraphic(null);
                } else {
                    if(obj != null){
                        Label l = new Label();
                        if(!obj.isDirectory()){
                            l.setText(String.valueOf(obj.getSize()));
                        } else {
                            l.setText("");
                        }
                        setGraphic(l);
                    }
                }
            }
        });

        nameCol.setCellFactory(cell -> new TableCell<FSObject, FSObject>(){
            @Override
            protected void updateItem(FSObject obj, boolean b) {
                if(obj == null || b) {
                    setGraphic(null);
                } else {
                    if(obj != null){
                        Label l = new Label(obj.getName());
                        ImageView iv;
                        if (obj.isDirectory()) {
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
        });

        typeCol.setCellFactory(cell -> new TableCell<FSObject, FSObject>() {
            @Override
            protected void updateItem(FSObject obj, boolean b) {
                if (obj == null || b) {
                    setGraphic(null);
                } else {
                    String type = obj.getType();
                    Label l = new Label(type == null? "" : type);
                    setGraphic(l);
                }
            }
        });
        tw.getSortOrder().add(nameCol);
    }

    private static class NameComparator implements Comparator<FSObject> {
        @Override
        public int compare(FSObject o1, FSObject o2) {
            if(o1.isDirectory() && !o2.isDirectory()){
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()){
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    }

    private static class SizeComparator implements Comparator<FSObject>{
        @Override
        public int compare(FSObject o1, FSObject o2) {
            if(o1.isDirectory() && o2.isDirectory()){
                return o1.getName().compareTo(o2.getName());
            } else if(o1.isDirectory() && !o2.isDirectory()){
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()){
                return 1;
            } else {
                return Long.compare(o1.getSize(), o2.getSize());
            }
        }
    }

    private static class TypeComparator implements Comparator<FSObject>{
        @Override
        public int compare(FSObject o1, FSObject o2) {
            if(o1.isDirectory() && o2.isDirectory()){
                return o1.getName().compareTo(o2.getName());
            } else if(o1.isDirectory() && !o2.isDirectory()){
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()){
                return 1;
            } else {
                if(o1.getType() == null){
                    return -1;
                } else if (o2.getType() == null){
                    return 1;
                } else {
                    return o1.getType().compareTo(o2.getType());
                }
            }
        }
    }
}