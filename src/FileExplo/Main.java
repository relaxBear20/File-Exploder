/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileExplo;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;
import javax.swing.tree.DefaultTreeModel;


/**
 *
 * @author admin
 */
public class Main {

    private final Form frm;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private final JTree tree;
    private final JList list;
    private final MyPopup myPopup;

    void control() {
        init();
        frm.setVisible(true);
        //fileTrack("F:\\");

    }

    void treeSelectedNode() {
        tree.getSelectionModel().addTreeSelectionListener((e) -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot()) {
                return;
            }
            MyFile fileNode;

            try {
                fileNode = (MyFile) selectedNode.getUserObject();
            } catch (Exception ex) {
                System.out.println(ex);
                return;
            }

            File[] files = fileNode.getFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });
            if (files == null) {
                return;
            }
            DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
            for (File file1 : files) {
                dcbm.addElement(new MyFile(file1));
            }
            frm.getLstCon().setModel(dcbm);

        });
    }

    void listClickPopupMenu() {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                check(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                check(e);
            }

            public void check(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopup.setMode(false);
                    list.setSelectedIndex(list.locationToIndex(e.getPoint()));
                    myPopup.show(list, e.getX(), e.getY());
                }
            }
        });
    }

    void treeClickPopupMenu() {

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selRow = tree.getPathForLocation(e.getX(), e.getY());
                    
                    tree.setSelectionPath(selRow);
                    if (row > -1) {
                        tree.setSelectionRow(row);
                    }
                    
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tree.getLastSelectedPathComponent() != null &&tree.getLastSelectedPathComponent() != root) {
                   check(e); 
                }
                
            }

            public void check(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopup.setMode(true);
                    myPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    void init() {
        //init tree
        root = new DefaultMutableTreeNode("All File");
        treeModel = new DefaultTreeModel(root);

        tree.setModel(treeModel);
        DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
        dtcr.setLeafIcon(dtcr.getDefaultClosedIcon());
        tree.setCellRenderer(dtcr);

        treeSelectedNode();
        treeClickPopupMenu();
        listClickPopupMenu();
        //add node
        
        File[] drives = File.listRoots();
        ExecutorService executor = Executors.newFixedThreadPool(drives.length);
        for (File drive : drives) {
            DefaultMutableTreeNode driveRoot = new DefaultMutableTreeNode(new MyFile(drive));
            executor.execute(new FileFinder(drive, driveRoot));
            root.add(driveRoot);
            //treeModel.reload();
            //System.out.println("Done");
        }
    }

    public Main(Form frm) {
        this.frm = frm;
        this.tree = frm.getTreeFile();
        this.list = frm.getLstCon();
        this.myPopup = new MyPopup(tree, list, frm);
    }

    public static void main(String[] args) {
        new Main(new Form()).control();
    }

}
