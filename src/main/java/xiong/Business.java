package xiong;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by johnson on 25/05/2017.
 */
public class Business {
    private final JTextArea printPnl;
    private StringBuilder builder = new StringBuilder();
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private Map<String, Dto> dtoMap = new HashMap<>();
    private File resultDir;
    private File tmpDocFile;
    public static final Logger logger = LoggerFactory.getLogger(Business.class);

    public Business(JTextArea printPnl) {
        this.printPnl = printPnl;
        String dir = System.getProperty("usr.dir");
        resultDir = new File(dir, "result");
        tmpDocFile = new File(dir, "tmp.docx");
        if (!resultDir.exists()) {
            resultDir.mkdir();
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public void readFiels(File[] selectedFiles) {
        printPnl.setText("");
        dtoMap.clear();
        if (selectedFiles.length < 1) {
            return;
        }
        for (File f : selectedFiles) {
            builder.append(f.getName()).append(",");
        }
        printMsg(builder.toString());
        try {
            String company = null, companyCode = null;
            for (File f : selectedFiles) {
                FileInputStream fin = null;
                try {
                    fin = new FileInputStream(f);
                    Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(fin));
                    int sheetNum = workbook.getNumberOfSheets();
                    for (int sheetIndex = 0; sheetIndex < sheetNum; sheetIndex++) {
                        Sheet sheet = workbook.getSheetAt(sheetIndex);
                        for (int rowIndex = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                            logger.debug("读取第{}行", rowIndex);
                            Row row = sheet.getRow(rowIndex);
                            if (row == null) {
                                continue;
                            }
                            String value;
                            if (isNullOrEmpty(getCellValue(row, 2))) {
                                value = getCellValue(row, 0);
                                if (isNullOrEmpty(value)) {
                                    continue;
                                }
                                if (value.contains("单位名称")) {
                                    company = getCellValue(row, 1);
                                }
                                value = getCellValue(row, 5);
                                if (value.contains("组织机构代码")) {
                                    if (Strings.isNotBlank(value)) {
                                        companyCode = value.substring(value.indexOf("：") + 1);
                                    }
                                }
                            } else {
                                value = getCellValue(row, 0);
                                if ("序号".equals(value)) {
                                    continue;
                                }
                                value = getCellValue(row, 2);
                                if (value.length() != 15 && value.length() != 18) {
                                    logger.debug("{}身份证长度不满足15，18位", value);
                                    continue;
                                }
                                Dto dto = dtoMap.get(value);
                                if (dto == null) {
                                    dto = new Dto();
                                    dto.setCompany(company);
                                    dto.setCompanyCode(companyCode);
                                    dto.setIdnum(value);
                                    dto.setUsername(getCellValue(row, 1));
                                    dtoMap.put(value, dto);
                                }
                                WorkRecord workrecord = new WorkRecord();
                                workrecord.setStartTime(getCellValue(row, 3));
                                workrecord.setEndTime(getCellValue(row, 4));
                                workrecord.setCompany(getCellValue(row, 5));
                                workrecord.setReplay(!"否".equals(getCellValue(row, 6).trim()));
                                workrecord.setPrincipal(Integer.parseInt(getCellValue(row, 7)));
                                dto.addWorkRecord(workrecord);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                } finally {
                    if (fin != null) {
                        try {
                            fin.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            printMsg("读入成功");
        } catch (Exception e) {
            printMsg(e.getMessage());
            printMsg("读入失败");
        }
        writeWord(dtoMap);
    }

    private void copyFile(File source, File dest) throws IOException {
        FileChannel sourceFileChannel = null, destChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            sourceFileChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceFileChannel, 0, sourceFileChannel.size());
        } finally {
            if (sourceFileChannel != null) {
                sourceFileChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    private void resetReplacementHolder(Map<String, String> replacementMap, Dto dto) {
        replacementMap.put("start_time", "");
        replacementMap.put("end_time", "");
        replacementMap.put("company_record", "");
        replacementMap.put("replay", "");
        replacementMap.put("principal", "");

    }

    private void writeWord(Map<String, Dto> dtoMap) {
        int count = 0;
        Map<String, String> replacementMap = new HashMap<>();
        for (Map.Entry<String, Dto> entry : dtoMap.entrySet()) {
            logger.debug("写入第{}个文件", ++count);
            Dto value = entry.getValue();
            File targetFile = new File(resultDir, "个人缴费退还申请表(" + value.getUsername() + " " + value.getIdnum() + ").docx");
            InputStream is = null;
            try {
                copyFile(tmpDocFile, targetFile);
                is = new FileInputStream(targetFile);
                XWPFDocument doc = new XWPFDocument(is);
                List<XWPFTable> tables = doc.getTables();
                XWPFTable xwpfTable = tables.get(0);
                replacementMap.put("company", value.getCompany());
                replacementMap.put("username", value.getUsername());
                replacementMap.put("idnum", value.getIdnum());
                replacementMap.put("sum", value.getSum() + "");
                List<WorkRecord> workRecordList = value.getWorkRecordList();
                xwpfTable.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> {
                        String text = cell.getText();
                        if (text.contains("${start_time}")) {
                            if(workRecordList.isEmpty()){
                                resetReplacementHolder(replacementMap, value);
                            }else{
                                WorkRecord record = workRecordList.remove(0);
                                replacementMap.put("start_time", record.getStartTime());
                                replacementMap.put("end_time", record.getEndTime());
                                replacementMap.put("company_record", record.getCompany());
                                replacementMap.put("replay", record.isReplay() ? "是" : "否");
                                replacementMap.put("principal", record.getPrincipal() + "");
                            }
                        }
                        replacePlaceholdersInParagraph(cell.getParagraphArray(0), replacementMap);
                    });
                });
                OutputStream os = new FileOutputStream(targetFile);
                doc.write(os);
                os.close();
            } catch (Exception e) {
                printMsg("写入失败");
                logger.error(e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        printMsg(dtoMap.size() + "个文件成功生成");
    }

    private void replacePlaceholdersInParagraph(XWPFParagraph paragraph,
                                                Map<String, String> replacementMap) {
        String text = paragraph.getText();

        // Word splits up a single piece of text into multiple runs in tables, so let's just delete those and create a single run instead.
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }

        paragraph.createRun().setText(
                StrSubstitutor.replace(text, replacementMap));
    }


    private String getCellValue(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            return "";
        }
        cell.setCellType(CellType.STRING);
        return cell.getRichStringCellValue().getString();
    }

    private void printMsg(String msg) {
        builder.setLength(0);
        builder.append(format.format(new Date(System.currentTimeMillis()))).append(": ");
        builder.append(msg);
        builder.append("\n");
        printPnl.append(builder.toString());
        builder.setLength(0);
    }

    public void openResultDir() {
        try {
            Desktop.getDesktop().open(resultDir);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public void clearResultDir() {
        deleteFile(resultDir);
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
        } else {
            file.delete();
        }
    }
}
