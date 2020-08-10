/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileExplo;

import Frame.CopyFrm;
import Frame.FilePropertiesFrm;
import Frame.FolderPropertiesFrm;
import Frame.MoveFrm;
import Frame.RenameFrm;
import MyFileTools.FileTool;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author admin
 */
public final class MyPopup extends JPopupMenu {

    private final Form frm;
    private final JTree tree;
    private final JList list;
    private boolean mode;
    private final JMenuItem delete;
    private final JMenuItem rename;
    private final JMenuItem copy;
    private final JMenuItem properties;
    private final JMenuItem move;
    private final ExecutorService executor;

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public MyPopup(JTree tree, JList list, Form frm) {
        this.tree = tree;
        this.frm = frm;
        this.list = list;
        this.executor = Executors.newFixedThreadPool(1);
        delete = new JMenuItem("Delete");
        rename = new JMenuItem("Rename");
        copy = new JMenuItem("Copy");
        move = new JMenuItem("Move");
        properties = new JMenuItem("Properties");
        control();

        this.add(delete);
        this.add(rename);
        this.add(move);
        this.add(copy);
        this.add(properties);
        this.add(new JSeparator());
    }

    public void control() {
        btnDelete();
        btnRename();
        btnCopy();
        btnProperties();
        btnMove();
    }

    void btnMove() {
        MoveFrm frm = new MoveFrm();
        move.addActionListener((e) -> {
            String title = "Move ";
            if (mode) {
                title += getFileFromMainTree().getAbsolutePath();
            } else {
                MyFile file = (MyFile) list.getSelectedValue();
                title += file.getAbsolutePath();
            }
            frm.setTitle(title);
            frm.getTreDisplay().setModel(tree.getModel());
            frm.setVisible(true);

        });
        frm.getBtnCancel().addActionListener((e) -> {
            frm.dispose();
        });
        frm.getBtnMove().addActionListener((e) -> {
            JTree copTree = frm.getTreDisplay();
            DefaultMutableTreeNode selectedNode = null;
            if (mode) {
                selectedNode = (DefaultMutableTreeNode) copTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                    return;
                }

                MyFile src = getFileFromMainTree();
                MyFile des = (MyFile) selectedNode.getUserObject();
                System.out.println(des);
                MyFile newFile = new FileTool().move(src, des, src.toString(), 0);
                System.out.println(newFile.getAbsolutePath());
                if (newFile != null) {
                    //delete old node
                    DefaultMutableTreeNode mainSelectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.removeNodeFromParent(mainSelectedNode);
                    DefaultComboBoxModel dcbm = (DefaultComboBoxModel) list.getModel();
                    dcbm.removeAllElements();
                    //add new node
                    DefaultMutableTreeNode copyNode = new DefaultMutableTreeNode(newFile);
                    selectedNode.add(copyNode);
                    executor.execute(new FileFinder(newFile.getFile(), copyNode));
                    model.reload(selectedNode);
                    System.out.println("success");
                    frm.dispose();
                } else {
                    System.out.println("failed");
                }
            } else {
                selectedNode = (DefaultMutableTreeNode) copTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                    return;
                }
                MyFile src = (MyFile) list.getSelectedValue();
                MyFile des = (MyFile) selectedNode.getUserObject();
                System.out.println(des);
                MyFile newFile = new FileTool().move(src, des, src.toString(), 0);
                if (newFile == null) {
                    System.out.println("Failed");
                } else if (newFile != src) {
                    DefaultComboBoxModel model = (DefaultComboBoxModel) list.getModel();
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        model.removeElementAt(selectedIndex);
                    }

                    System.out.println("Success");
                    frm.dispose();
                } else {
                    frm.dispose();
                }
            }

        });
    }

    void btnProperties() {
        properties.addActionListener((e) -> {
            if (mode) {

                MyFile file = getFileFromMainTree();
                if (file == null) {
                    return;
                }
                long [] i = new FileTool().getFolderProperties(file.getFile());
                FolderPropertiesFrm frm = new FolderPropertiesFrm();
                frm.getLblPath().setText(file.getAbsolutePath());
                frm.getLblContains().setText(i[0] + " Files - " + i[1] + " Folders");
                DecimalFormat df = new DecimalFormat("###,###.###");
                
                frm.getLblSize().setText(df.format(i[2]) + " Bytes");
                frm.setVisible(true);
                
            } else {
                MyFile file = (MyFile) list.getSelectedValue();
                if (file == null) {
                    return;
                }
                String fname = file.toString();
                FilePropertiesFrm frm = new FilePropertiesFrm();
                frm.getLblExtension().setText(fname.substring(fname.lastIndexOf("."), fname.length()));
                frm.getLblName().setText(fname);
                DecimalFormat df = new DecimalFormat("###,###.###");
                frm.getLblSize().setText(df.format(file.getFile().length()) + " Bytes");
                frm.getLblLocation().setText(file.getAbsolutePath());
                frm.setVisible(true);
                
            }
        });
    }

    void btnCopy() {
        CopyFrm frm = new CopyFrm();

        copy.addActionListener((e) -> {
            frm.setTitle("Copy");
            MyFile fileNode = getFileFromMainTree();
            frm.getTxtInput().setText(fileNode.toString());
            frm.getTreDisplay().setModel(tree.getModel());
            frm.setVisible(true);
        });

        frm.getBtnCancel().addActionListener((e) -> {
            frm.dispose();
        });
        
        //Btn Save CopyFrm
        frm.getBtnSave().addActionListener((e) -> {
            String fName = frm.getTxtInput().getText();
            JTree copTree = frm.getTreDisplay();
            DefaultMutableTreeNode selectedNode = null;
            if (mode) {
                selectedNode = (DefaultMutableTreeNode) copTree.getLastSelectedPathComponent();
                if ((!fName.equals(null) || fName != "") && selectedNode != null) {
                    System.out.println("OK" + selectedNode.getUserObject().toString());
                    MyFile desNode;
                    if (selectedNode.isRoot()) {
                        return;
                    }
                    try {
                        desNode = (MyFile) selectedNode.getUserObject();
                    } catch (Exception ex) {
                        System.out.println(ex);
                        return;
                    }
                    MyFile srcNode = getFileFromMainTree();
                    String copyFileName = frm.getTxtInput().getText();
                    if (!isCheckedFileName(copyFileName)) {
                        JOptionPane.showMessageDialog(frm, "Invalid name");
                        return;
                    }
                    MyFile copyFile = new FileTool().copy(srcNode, desNode, copyFileName, 0);
                    System.out.println(copyFile.getAbsolutePath());
                    if (copyFile != null) {
                        System.out.println(selectedNode.getUserObject());
                        DefaultMutableTreeNode copyNode = new DefaultMutableTreeNode(copyFile);
                        selectedNode.add(copyNode);
                        executor.execute(new FileFinder(copyFile.getFile(), copyNode));
                        //tree.setModel(copTree.getModel());
                        DefaultTreeModel dftm = (DefaultTreeModel) tree.getModel();
                        dftm.reload(selectedNode);
                        System.out.println("success");
                         frm.dispose();
                    } else {
                        System.out.println("failed");
                    }
                }
            } else {
                selectedNode = (DefaultMutableTreeNode) copTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                    return;
                }
                MyFile src = (MyFile) list.getSelectedValue();
                MyFile des = (MyFile) selectedNode.getUserObject();
                System.out.println(des);
                MyFile newFile = new FileTool().copy(src, des, src.toString(), 0);
                if (newFile == null) {
                    System.out.println("Failed");
                } else {
                    DefaultComboBoxModel model = (DefaultComboBoxModel) list.getModel();

                    model.addElement(newFile);

                    System.out.println("Success");
                    frm.dispose();
                }
            }

        });
    }

    void btnDelete() {

        delete.addActionListener((e) -> {
            if (mode) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile fileNode = getFileFromMainTree();
                if (new FileTool().delete(fileNode.getFile())) {
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.removeNodeFromParent(selectedNode);
                    DefaultComboBoxModel dcbm = (DefaultComboBoxModel) list.getModel();
                    dcbm.removeAllElements();
                }

            } else {
                MyFile fileNode = (MyFile) list.getSelectedValue();

                if (fileNode == null) {
                    return;
                }
                if (new FileTool().delete(fileNode.getFile())) {
                    DefaultComboBoxModel model = (DefaultComboBoxModel) list.getModel();
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        model.removeElementAt(selectedIndex);
                    }
                    System.out.println("Success");
                } else {
                    System.out.println("Failed");
                }

                
            }

        });
    }

    void btnRename() {
        RenameFrm reFrm = new RenameFrm();
        reFrm.getBtnOK().addActionListener((e) -> {

            MyFile fileNode = null;
            if (mode) {
                fileNode = getFileFromMainTree();
            } else {
                fileNode = (MyFile) list.getSelectedValue();
            }
            if (fileNode == null) {
                return;
            }
            String reName = reFrm.getTxtInput().getText();

            if (!isCheckedFileName(reName)) {
                JOptionPane.showMessageDialog(frm, "Invalid name");
                return;
            }
            if (mode) {
                boolean success = new FileTool().rename(fileNode, reName, 0);
                if (success) {
                    System.out.println("Rename success");
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.reload((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
                    reFrm.dispose();
                } else {
                    System.out.println("Rename failed");
                }
            } else {

            }
        });
        rename.addActionListener((e) -> {
            if (mode) {
                MyFile fileNode = getFileFromMainTree();
                if (fileNode == null) {
                    return;
                }
                reFrm.getTxtInput().setText(fileNode.toString());
                reFrm.setVisible(true);

            }
        });
    }

    private MyFile getFileFromMainTree() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        MyFile fileNode;
        if (selectedNode.isRoot()) {
            return null;
        }
        try {
            fileNode = (MyFile) selectedNode.getUserObject();
            return fileNode;
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }

    }

    public boolean isCheckedFileName(String fName) {
        boolean ok = !fName.matches(".*[\\<\\>\\:\"\\/\\\\|\\?\\*]+.*") && !fName.matches("\\w*\\W*\\s");
        System.out.println(ok);
        return ok;
    }
}
