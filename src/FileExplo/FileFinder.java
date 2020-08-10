/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileExplo;

import java.io.File;
import java.io.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author admin
 */
public class FileFinder implements Runnable {

    File rootFile;
    private DefaultMutableTreeNode root;

    public FileFinder(File rootFile, DefaultMutableTreeNode root) {
        this.rootFile = rootFile;
        this.root = root;
    }

    public void addChild(File rootFile, DefaultMutableTreeNode node) {
        File[] files = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (files == null) {
            return;
        }
        for (File file : files) {
            DefaultMutableTreeNode dftn = new DefaultMutableTreeNode(new MyFile(file));
            node.add(dftn);
            addChild(file, dftn);

        }
    }

    @Override
    public void run() {
        addChild(rootFile, root);
        System.out.println( " Done");
    }
}
