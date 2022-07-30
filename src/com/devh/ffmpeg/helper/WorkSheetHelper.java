package com.devh.ffmpeg.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Pattern;

public class WorkSheetHelper {
    private final String[] extensions = {
            "mkv", "mp4", "avi", "wmv"
    };
    private final String[] timelineDelimiters = {
            "[[[[", "]]]]"
    };
    private final String finalBatchscript;

    public WorkSheetHelper() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        this.finalBatchscript = sdf.format(new Date()) + ".bat";
    }

    private File workDirectory;
    private File resultDirectory;

    public void setWorkDirectory(String path) {
        this.workDirectory = new File(path);
    }
    public void setResultDirectory(String path) {
        this.resultDirectory = new File(path);
    }

    public void work() {
        if(!this.resultDirectory.exists()) {
            this.resultDirectory.mkdirs();
        }

        /* 1. listUp & sort */
        File[] workList = listUp();
        Arrays.sort(workList, Comparator.comparingLong(File::lastModified));

        /* 2. create worksheet */
        for(File f : workList) {
            createWorkSheet(f);
        }
    }

    private File[] listUp() {
        return workDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean isFile = pathname.isFile();
                final String fileName = pathname.getName();
                String[] fileNameTokens = fileName.split(Pattern.quote("."));
                final String extension = fileNameTokens[fileNameTokens.length - 1];
                return isFile &&
                        Arrays.asList(extensions).contains(extension) &&
                        fileName.contains(timelineDelimiters[0]) &&
                        fileName.contains(timelineDelimiters[1]);
            }
        });
    }

    private void createWorkSheet(File file) {
        final String fileName = file.getName();
        final String[] fileNameSplitsByDot = fileName.split(Pattern.quote("."));
        final String fileAbsolutePath = file.getAbsolutePath();
        final int idxTimelineStart = fileName.indexOf(timelineDelimiters[0]);
        final int idxTimelineEnd   = fileName.indexOf(timelineDelimiters[1]);
        final String pureFileName = fileName.substring(0, idxTimelineStart);
        final String pureFileExtension = fileNameSplitsByDot[fileNameSplitsByDot.length - 1];
        final String timeline = fileName.substring(idxTimelineStart+4, idxTimelineEnd);

        File worksheet = new File(workDirectory.getAbsolutePath() + File.separator + pureFileName.trim() + ".txt");
        StringBuffer sbWork = new StringBuffer();

        String[] timelineTokens = timeline.split(",");

        for (String t : timelineTokens) {
            if (t.trim().length() == 0) {
                continue;
            }

            appendFile(sbWork, fileAbsolutePath);
            appendLineSeparator(sbWork);

            String[] points = t.split("-");
            if (t.startsWith("-")) {
                appendInPoint(sbWork, "00:00:00");
                appendLineSeparator(sbWork);
                appendOutPoint(sbWork, convertTime(points[1].trim()));
                appendLineSeparator(sbWork);
            } else if (t.endsWith("-")) {
                appendInPoint(sbWork, convertTime(points[0].trim()));
            } else {
                appendInPoint(sbWork, convertTime(points[0].trim()));
                appendLineSeparator(sbWork);
                appendOutPoint(sbWork, convertTime(points[1].trim()));
                appendLineSeparator(sbWork);
            }
        }

        try (FileWriter fw = new FileWriter(worksheet)) {
            fw.write(sbWork.toString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter(new File(this.workDirectory + File.separator + finalBatchscript), true)) {
            // ffmpeg -f concat -safe 0 -i "Muse - Live at Reading Festival 2017 (Full Set)   [[[[46 37-55 16, 1 4 32-1 56 54, 2 0 3-2 33 47]]]].mp4.txt" -c copy "Muse - Live at Reading Festival 2017 (Full Set).mp4"
            fw.write(String.format("ffmpeg -f concat -safe 0 -i \"%s\" -c copy \"%s.%s\"", worksheet.getAbsolutePath(), this.resultDirectory.getAbsolutePath() + File.separator + pureFileName.trim(), pureFileExtension.trim()));
            fw.write(System.lineSeparator());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String convertTime(String t) {
        String[] timeTokens = t.split(" ");
        if(timeTokens.length == 1) {
            /* 초 */
            return String.format("00:00:%s", pad(timeTokens[0]));
        } else if(timeTokens.length == 2) {
            /* 분 초 */
            return String.format("00:%s:%s", pad(timeTokens[0]), pad(timeTokens[1]));
        } else {
            /* 시 분 초 */
            return String.format("%s:%s:%s", pad(timeTokens[0]), pad(timeTokens[1]), pad(timeTokens[2]));
        }
    }
    private String pad(String timeToken) {
        return timeToken.length() == 1 ? "0"+timeToken : timeToken;
    }
    private void appendFile(StringBuffer sb, String path) {
        sb.append("file '").append(path).append("'");
    }
    private void appendInPoint(StringBuffer sb, String inPoint) {
        sb.append("inpoint ").append(inPoint);
    }
    private void appendOutPoint(StringBuffer sb, String outPoint) {
        sb.append("outpoint ").append(outPoint);
    }
    private void appendLineSeparator(StringBuffer sb) {
        sb.append(System.lineSeparator());
    }
}
