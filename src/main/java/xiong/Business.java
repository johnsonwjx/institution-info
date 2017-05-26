package xiong;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        tmpDocFile = new File(dir, "tmp.doc");
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
                                    company = value.substring(value.indexOf("：") + 1);
                                }
                                value = getCellValue(row, 3);
                                if (value.contains("组织机构代码")) {
                                    companyCode = value.substring(value.indexOf("：") + 1);
                                }
                            } else {
                                value = getCellValue(row, 0);
                                if ("序号".equals(value)) {
                                    continue;
                                }

                                value = getCellValue(row, 2);
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

    private void writeWord(Map<String, Dto> dtoMap) {
        int count = 0;
        for (Map.Entry<String, Dto> entry : dtoMap.entrySet()) {
            logger.debug("写入第{}个doc", count++);
            Dto value = entry.getValue();
            File targetFile = new File(resultDir, "事业单位在职和退休人员信息表(" + value.getUsername() + " " + value.getIdnum() + ").doc");
            InputStream is = null;
            try {
                copyFile(tmpDocFile, targetFile);
                is = new FileInputStream(targetFile);
                HWPFDocument doc = new HWPFDocument(is);
                Range rang = doc.getRange();
                rang.replaceText("${company}", value.getCompany());
                rang.replaceText("${username}", value.getUsername());
                rang.replaceText("${idnum}", value.getIdnum());
                List<WorkRecord> workRecordList = value.getWorkRecordList();
                int index = 15;
                for (WorkRecord record : workRecordList) {
                    rang.getParagraph(index).replaceText("${start_time}", record.getStartTime());
                    rang.getParagraph(index + 1).replaceText("${end_time}", record.getEndTime());
                    rang.getParagraph(index + 2).replaceText("${company_record}", record.getCompany());
                    index += 5;
                }
                clearPlaceHoler(rang);
                doc.write(targetFile);
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
        printMsg(dtoMap.size() + "个doc文件成功生成");
    }

    private void clearPlaceHoler(Range rang) {
        rang.replaceText("${start_time}", "");
        rang.replaceText("${end_time}", "");
        rang.replaceText("${company_record}", "");
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
