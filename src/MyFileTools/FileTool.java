/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyFileTools;

import FileExplo.MyFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class FileTool {

    public int[] getFolderContain(File dir) {
        int[] i = new int[2];
        if (dir == null) {
            return null;
        }
        for (File file : dir.listFiles()) {
            try {
                if (file.isFile()) {
                    i[0] += 1;
                } else if (!file.getName().endsWith("$RECYCLE.BIN")) {
                    i[1]++;
                    int[] tem = getFolderContain(file);
                    i[1] += tem[1];
                    i[0] += tem[0];
                }
            } catch (Exception e) {
                continue;
            }
        }
        return i;
    }

    public long[] getFolderProperties(File dir) {
        long[] i = new long[3];
        if (dir == null) {
            return null;
        }
        for (File file : dir.listFiles()) {
            try {
                if (file.isFile()) {
                    i[0] += 1;
                    i[2] += file.length();
                } else if (!file.getName().endsWith("$RECYCLE.BIN")) {
                    i[1]++;
                    long[] tem = getFolderProperties(file);
                    i[1] += tem[1];
                    i[0] += tem[0];
                    i[2] += tem[2];
                }
            } catch (Exception e) {
                continue;
            }
        }
        return i;
    }

    public boolean delete(File deleteFile) {

        File[] allFile = deleteFile.listFiles();
        if (allFile != null) {
            for (File fileNode : allFile) {
                delete(fileNode);
            }
        }
        return deleteFile.delete();
    }

    public MyFile move(MyFile fSrc, MyFile fDes, String fname, int i) {
        System.out.println(fSrc.getAbsolutePath() + fDes.getAbsolutePath() + fname);
        if (fSrc.getAbsolutePath().equals(fDes.getAbsolutePath() + fname)) {
            System.out.println("Same");
            return fSrc;
        }
        String ext = "";
        String num = "";
        if (fname.matches(".*\\.[a-z]{3}")) {
            ext = fname.substring(fname.lastIndexOf("."), fname.length());
            fname = fname.substring(0, fname.lastIndexOf("."));

        }
        if (fname.matches(".+\\-\\s\\([0-9]{1,3}\\)\\.*[a-z]*")) {
            num = fname.substring(fname.lastIndexOf("-"), fname.lastIndexOf(")") + 1);
            fname = fname.substring(0, fname.lastIndexOf("-"));
        }

        System.out.println(fname + " ext" + ext + "num " + num);
        if (new File(fDes.getAbsolutePath() + fname + num + ext).exists()) {
            System.out.println("existed");
            if (i > 0) {
                fname = fname + num.substring(0, num.indexOf("(") + 1) + ++i + ")" + ext;
                return move(fSrc, fDes, fname, i);
            } else {
                fname += "- (" + ++i + ")" + ext;
                return move(fSrc, fDes, fname, i);
            }
        } else {
            try {
                fname += num + ext;
                Path src = Paths.get(fSrc.getAbsolutePath());
                Path des = Paths.get(fDes.getAbsolutePath() + "\\" + fname);
                System.out.println("des " + fDes.getAbsolutePath() + "\\"  + fname);

                Path f = Files.copy(src, des, StandardCopyOption.REPLACE_EXISTING);
                //list all file in folder

                if (fSrc.getFile().isDirectory()) {
                    System.out.println(fSrc.getAbsolutePath() + fSrc.getFile().isDirectory());
                    File[] fileInFolder = src.toFile().listFiles();
                    if (fileInFolder != null) {
                        for (File file1 : fileInFolder) {
                            MyFile myFile1 = new FileTool().move(new MyFile(file1), new MyFile(new File(fDes.getAbsolutePath() + "\\" + fname)), file1.getName(), 0);
                            new FileTool().delete(file1);
                        }
                    }
                }
                new FileTool().delete(src.toFile());
                System.out.println("Move to new des " + des.toString());
                return new MyFile(f.toFile());
            } catch (IOException ex) {
                Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    public MyFile copy(MyFile fSrc, MyFile fDes, String fname, int i) {
        String ext = "";
        String num = "";
        if (fname.matches(".*\\.[a-z]{3}")) {
            ext = fname.substring(fname.lastIndexOf("."), fname.length());
            fname = fname.substring(0, fname.lastIndexOf("."));

        }
        if (fname.matches(".+\\-\\s\\([0-9]{1,3}\\)\\.*[a-z]*")) {
            num = fname.substring(fname.lastIndexOf("-"), fname.lastIndexOf(")") + 1);
            fname = fname.substring(0, fname.lastIndexOf("-"));
        }

        System.out.println(fname + " ext" + ext + "num " + num);
        if (new File(fDes.getAbsolutePath() + fname + num + ext).exists()) {
            System.out.println("existed");
            if (i > 0) {
                fname = fname + num.substring(0, num.indexOf("(") + 1) + ++i + ")" + ext;
                return copy(fSrc, fDes, fname, i);
            } else {
                fname += "- (" + ++i + ")" + ext;
                return copy(fSrc, fDes, fname, i);
            }
        } else {
            try {
                fname += num + ext;
                Path src = Paths.get(fSrc.getAbsolutePath());
                Path des = Paths.get(fDes.getAbsolutePath() + "\\" + fname);
                //System.out.println("des " + fDes.getAbsolutePath() + "\\" + fname);
                Path f = Files.copy(src, des, StandardCopyOption.REPLACE_EXISTING);
                //list all file in folder
                File[] fileInFolder = fSrc.getFile().listFiles();
                if (fileInFolder != null) {
                    for (File file1 : fileInFolder) {
                        if (file1.isDirectory()) {
                            MyFile myFile1 = new FileTool().copy(new MyFile(file1), new MyFile(des.toFile()), file1.getName(), 0);
                        } else {
                            Path fileSrcPath = Paths.get(file1.getAbsolutePath());
                            Path fileDesPath = Paths.get(des.toAbsolutePath().toString() + "\\" + file1.getName());
                            Files.copy(fileSrcPath, fileDesPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

                System.out.println("Copy to new des " + des.toString());
                return new MyFile(f.toFile());
            } catch (IOException ex) {
                Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

    }

    public boolean rename(MyFile fSrc, String newName, int i) {
        if (fSrc.toString().equals(newName)) {
            return true;
        }
        Path src = Paths.get(fSrc.getAbsolutePath());
        String abPath = fSrc.getAbsolutePath().substring(0, fSrc.getAbsolutePath().lastIndexOf("\\")) + "\\" + newName;
        System.out.println("new path" + abPath);
        File newFile = new File(abPath);
        if (newFile.exists()) {
            System.out.println("existed");
            if (i > 0) {
                newName = newName.substring(0, newName.lastIndexOf("(")) + "(" + ++i + ")";
                return rename(fSrc, newName, i);
            } else {
                newName += " - (" + ++i + ")";
                return rename(fSrc, newName, i);
            }

        } else {
            try {
                Files.move(src, src.resolveSibling(newName));
                fSrc.setFile(newFile);
                System.out.println("atributte file path " + fSrc.getAbsolutePath());
                return true;
            } catch (IOException ex) {

                System.out.println(ex);
                Logger.getLogger(MyFile.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

    }
}
