/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 *
 * @author Administrator
 */
public class FileProcessor {

    // 二维数组纵向合并  
    private static String[][] unite(String[][] content1, String[][] content2) {
        String[][] newArrey = new String[][]{};
        List<String[]> list = new ArrayList<String[]>();
        list.addAll(Arrays.<String[]>asList(content1));
        list.addAll(Arrays.<String[]>asList(content2));
        return list.toArray(newArrey);
    }
    public static String configBaseDir;
    public static String outputDirectory;

    public static String[][] parseXls(String filePath, int sheetNum) throws IOException, BiffException {
        String[][] finalContents = FileProcessor.parseXls(filePath, sheetNum, true);
        return finalContents;
    }

    public static WorkbookSettings getWorkbookSettings() {
        return FileProcessor.getWorkbookSettings("ISO-8859-1");
    }

    public static WorkbookSettings getWorkbookSettings(String encoding) {
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setEncoding(encoding); //关键代码，解决乱码
        return workbookSettings;
    }

    /**
     * 替换
     *
     * @param sheet
     * @param i
     * @param j
     * @return
     */
    public static String getTmpContent(Sheet sheet, int i, int j) {
        Cell cells = sheet.getCell(i, j);
        String finalContents;
        if (cells.getType() == CellType.DATE) {//对日期数据进行特殊处理，如果不处理的话 默认24h制会变成12小时制
            finalContents = FileProcessor.formatTime(sheet.getCell(i, j));
        } else {
            String tmpContent = sheet.getCell(i, j).getContents();
            if (!isNumeric(tmpContent)) {
                tmpContent = tmpContent.replace("\\'", "\'");//防止单引号已经被转义了 如果没有这一步骤的话 被转义的单引号就会出现问题
                tmpContent = tmpContent.replace("\'", "\\'");
            }
            finalContents = tmpContent;
        }
        return finalContents;
    }

    /**
     * 判断是否为 number类型
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0;) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化时间
     *
     * @param formatCell
     * @return
     */
    public static String formatTime(Cell formatCell) {
        java.util.Date date = null;
        DateCell dateCell = (DateCell) formatCell;
        date = dateCell.getDate();
        //long time = (date.getTime() / 1000) - 60 * 60 * 8;
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        //date.setTime(time * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(gmtZone);
        return formatter.format(date);

    }

    /**
     * 获得指定worksheet名称的顺序
     *
     * @param filePath
     * @param desSheetName
     * @return String
     * @throws IOException
     * @throws BiffException
     */
    public static int getSheetIndexBySheetName(String filePath, String desSheetName) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        String[] sheetNames = workbook.getSheetNames();
        int sheetIndex = 0;
        int sheetNamesLen = sheetNames.length;
        for (int i = 0; i < sheetNamesLen; i++) {
            String sheetName = sheetNames[i];
            if (sheetName.equals(desSheetName)) {
                sheetIndex = i;
                break;
            }
        }
        return sheetIndex;
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static HashMap getFileParseRuleConfigInfo(String filePath) {
        HashMap<String, HashMap> configHashMap = new HashMap<String, HashMap>();
        try {
            int configSheetIndex = getSheetIndexBySheetName(filePath, "parseRuleCfg");
            String[][] configInfoString = parseXls(filePath, configSheetIndex, true);
            int rowCount = configInfoString.length;
            for (int row = 1; row < rowCount; row++) {
                String[] rowInfo = configInfoString[row];
                if (!rowInfo[0].isEmpty()) {
                    String sheetName = rowInfo[0];
                    String cfgKey = rowInfo[1];
                    String cfgValue = rowInfo[2];
                    HashMap cfgInfo;
                    if (configHashMap.containsKey(sheetName)) {
                        cfgInfo = configHashMap.get(sheetName);
                    } else {
                        cfgInfo = new HashMap();
                    }
                    cfgInfo.put(cfgKey, cfgValue);
                    configHashMap.put(sheetName, cfgInfo);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(configHashMap.toString());
        return configHashMap;
    }

    /**
     * 获得指定worksheet的名称
     *
     * @param filePath
     * @param sheetIndex
     * @return String
     * @throws IOException
     * @throws BiffException
     */
    public static String getSheetNameBySheetIndex(String filePath, int sheetIndex) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        String[] sheetNames = workbook.getSheetNames();
        String sheetName = sheetNames[sheetIndex];
        return sheetName;
    }

    /**
     * 获得指定excel文件的worksheet数量
     *
     * @param filePath
     * @return int
     * @throws IOException
     * @throws BiffException
     */
    public static int getSheetNumber(String filePath) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        int sheetNumber = workbook.getNumberOfSheets();
        return sheetNumber;
    }

    /**
     * 加载setting文件
     *
     * @param file_path
     */
    public static void loadSetting(String file_path) {
        File f = new File(file_path);
        if (f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis;
            try {
                fis = new FileInputStream(file_path);
                try {
                    prop.load(fis);
                } catch (IOException ex) {
                    Logger.getLogger(TranslateJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    showMessageDialogMessage(ex);
                }
                if (!prop.getProperty("configBaseDir", "").isEmpty()) {
                    try {
                        configBaseDir = new String(prop.getProperty("configBaseDir").getBytes("ISO-8859-1"), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(TranslateJFrame.class.getName()).log(Level.SEVERE, null, ex);
                        showMessageDialogMessage(ex);
                    }

                }
                if (!prop.getProperty("outputDirectory", "").isEmpty()) {
                    try {
                        outputDirectory = new String(prop.getProperty("outputDirectory").getBytes("ISO-8859-1"), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        showMessageDialogMessage(ex);
                        Logger.getLogger(TranslateJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TranslateJFrame.class.getName()).log(Level.SEVERE, null, ex);
                showMessageDialogMessage(ex);
            }
        }
    }

    /**
     * 解析excel文件
     *
     * @param filePath 文件路径
     * @param sheetNum work sheet 索引
     * @param reverse 是否需要reverse所得二维数组
     * @return array[][]
     * @throws IOException
     * @throws BiffException
     */
    public static String[][] parseXls(String filePath, int sheetNum, boolean reverse) throws IOException, BiffException {
        //通过Workbook的静态方法getWorkbook选取Excel文件
        WorkbookSettings workbookSettings = getWorkbookSettings();
        Workbook workbook = Workbook.getWorkbook(new File(filePath), workbookSettings);
        //通过Workbook的getSheet方法选择第一个工作簿（从0开始）
        Sheet sheet = workbook.getSheet(sheetNum);
        int rows = sheet.getRows();
        int cols = sheet.getColumns();
        Cell cells[][] = new Cell[cols][rows];
        String[][] finalContents;
        if (reverse) {
            finalContents = new String[rows][cols];
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    finalContents[j][i] = getTmpContent(sheet, i, j);
                }
            }
        } else {
            finalContents = new String[cols][rows];
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    finalContents[i][j] = getTmpContent(sheet, i, j);
                }
            }
        }

        workbook.close();
        return finalContents;
    }

    /**
     * 显示错误信息
     *
     * @param ex
     */
    public static void showMessageDialogMessage(Exception ex) {
        String exMsg = ex.toString();
        JOptionPane.showMessageDialog(null, exMsg, "错误信息提示", JOptionPane.ERROR_MESSAGE);
    }

    public static int[] getSheetIndexArrayWithoutParseRule(String configFilePath) throws IOException, BiffException {
        int sheetCount = getSheetNumber(configFilePath);
        int parseRuleSheetIndex = getSheetIndexBySheetName(configFilePath, "parseRuleCfg");
        int sheetMax = sheetCount - 1;
        int[] indexArray = new int[sheetMax];
        for (int index = 0, indexArrayIndex = 0; index < sheetMax; index++) {
            if (index != parseRuleSheetIndex) {
                indexArray[indexArrayIndex++] = index;
            }
        }
        return indexArray;
    }

}
