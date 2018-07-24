package com.evtape.schedule.util;


import com.evtape.schedule.domain.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by holmes1214 on 28/08/2017.
 */
public class PictureUtil {

    static final int MARGIN = 80;
    static final int SPACE_SIZE_BASE = 40;
    static final int FONT_SIZE_BASE = 32;
    static final int HOUR_BASE = SPACE_SIZE_BASE * 4;
    static final int[] VERTICALS = new int[]{MARGIN, SPACE_SIZE_BASE * 10, SPACE_SIZE_BASE * 3, SPACE_SIZE_BASE * 12, SPACE_SIZE_BASE * 8, SPACE_SIZE_BASE * 8, HOUR_BASE * 24};
    static final String[] TITLES = new String[]{"日期", "星期", "工作时间", "班次", "工作岗位"};
    static final String[] PERIODS = new String[]{"0100", "0200", "0300", "0400", "0500", "0600", "0700", "0800", "0900", "1000", "1100", "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300", "0000"};

    public static void createUserSchedulePicture(Map<User, List<ScheduleInfo>> scheduleMap, Map<Integer, DutyClass> shiftMap,
                                                 Map<Integer, List<ScheduleWorkflow>> workflowMap, Map<Integer, List<ScheduleWorkflowContent>> contentMap, OutputStream ouputStream) throws Exception {
        //生成压缩包
        ZipOutputStream zip = new ZipOutputStream(ouputStream);

        for (User user : scheduleMap.keySet()) {

            BufferedImage image = createUserImage(user, scheduleMap.get(user), shiftMap, workflowMap, contentMap);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", bs);

            ZipEntry entry = new ZipEntry(user.getPhoneNumber() + user.getUserName() + ".jpg");
            zip.putNextEntry(entry);
            bs.writeTo(zip);
            bs.close();
        }
        zip.flush();
        zip.close();
    }

    private static BufferedImage createUserImage(User user, List<ScheduleInfo> scheduleInfos, Map<Integer, DutyClass> shiftMap,
                                                 Map<Integer, List<ScheduleWorkflow>> workflowMap, Map<Integer, List<ScheduleWorkflowContent>> contentMap) {
        //左右留边，日期，星期，班次，时间，位置，工作流程
        int width = MARGIN;
        for (int i = 0; i < VERTICALS.length; i++) {
            width += VERTICALS[i];
        }
        //上下留边，姓名，间距，标题，每日数据
        int height = MARGIN * 2 + SPACE_SIZE_BASE + SPACE_SIZE_BASE + SPACE_SIZE_BASE + SPACE_SIZE_BASE * 2 * scheduleInfos.size();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.black);

        drawUserName(graphics, user.getUserName());

        drawTable(graphics, scheduleInfos.size());

        drawTitle(graphics);

        for (int i = 0; i < scheduleInfos.size(); i++) {
            ScheduleInfo info = scheduleInfos.get(i);
            drawDailyContent(graphics, i, info, shiftMap.get(info.getDutyClassId()));
            drawTimeLine(graphics, i, info, workflowMap.get(info.getDutyClassId()), contentMap);
        }
        return image;
    }

    private static void drawUserName(Graphics graphics, String userName) {
        setFont(graphics, 2);
        graphics.drawString(userName, MARGIN, MARGIN + SPACE_SIZE_BASE * 2);
    }

    private static void drawTable(Graphics graphics, int size) {
        int left = 0;
        int top = MARGIN + SPACE_SIZE_BASE * 3;

        int bottom = top + size * SPACE_SIZE_BASE * 2 + SPACE_SIZE_BASE;
        //加粗
        graphics.drawLine(MARGIN - 1, top, MARGIN - 1, bottom);
        graphics.drawLine(MARGIN - 2, top, MARGIN - 2, bottom);
        //表格竖线
        for (int i = 0; i < VERTICALS.length; i++) {
            left += VERTICALS[i];
            graphics.drawLine(left, top, left, bottom);
        }
        //加粗
        graphics.drawLine(left + 1, top, left + 1, bottom);
        graphics.drawLine(left + 2, top, left + 2, bottom);
        //顶上横线
        graphics.drawLine(MARGIN, top, left, top);
        graphics.drawLine(MARGIN, top - 1, left, top - 1);
        graphics.drawLine(MARGIN, top - 2, left, top - 2);
        for (int i = 0; i <= size; i++) {
            int t = top + SPACE_SIZE_BASE + i * SPACE_SIZE_BASE * 2;
            //时间表横线
            graphics.drawLine(left - VERTICALS[VERTICALS.length - 1], t - SPACE_SIZE_BASE, left, t - SPACE_SIZE_BASE);
            //表格横线
            graphics.drawLine(MARGIN, t - 1, left, t - 1);
            graphics.drawLine(MARGIN, t, left, t);
            graphics.drawLine(MARGIN, t + 1, left, t + 1);
        }
        //时间表小时线
        for (int i = 0; i < 24; i++) {
            int x = left - (i + 1) * HOUR_BASE;
            graphics.drawLine(x, top, x, top + SPACE_SIZE_BASE);
        }
        //时间表分钟线
        for (int i = left; i > left - VERTICALS[VERTICALS.length - 1]; i -= SPACE_SIZE_BASE / 2) {
            graphics.drawLine(i, top + SPACE_SIZE_BASE, i, bottom);
        }
    }

    private static void drawTitle(Graphics graphics) {
        setFont(graphics, 1);
        int left = 0;
        int top = MARGIN + SPACE_SIZE_BASE * 2 + SPACE_SIZE_BASE + SPACE_SIZE_BASE - (SPACE_SIZE_BASE - FONT_SIZE_BASE) / 2;

        for (int i = 0; i < VERTICALS.length - 2; i++) {
            left += VERTICALS[i];
            int width = VERTICALS[i + 1];
            drawString(graphics, TITLES[i], left, top, width);
        }
        left += VERTICALS[VERTICALS.length - 2] + SPACE_SIZE_BASE;
        for (int i = 0; i < PERIODS.length; i++) {
            int width = HOUR_BASE;
            drawString(graphics, PERIODS[i], left, top, width);
            left += width;
        }
    }

    private static void drawDailyContent(Graphics graphics, int number, ScheduleInfo info, DutyClass dutyClass) {
        setFont(graphics, 1);
        graphics.setColor(Color.black);
        int x = 1;
        int left = MARGIN;
        int head = MARGIN + SPACE_SIZE_BASE * 4 + SPACE_SIZE_BASE * 2 * (number + 1);
        int top = head - (SPACE_SIZE_BASE - FONT_SIZE_BASE) / 2;

        String text = info.getDateStr();
        int width = VERTICALS[x++];
        drawString(graphics, text, left, top, width);

        text = info.getScheduleWeek();
        left += VERTICALS[x - 1];
        width = VERTICALS[x++];
        drawString(graphics, text, left, top, width);

        left += VERTICALS[x - 1];
        width = VERTICALS[x++];
        if (dutyClass != null) {
            String start = getTimeText(dutyClass.getStartTime());
            String end = getTimeText(dutyClass.getEndTime());
            text = start + "-" + end;
            drawString(graphics, text, left, top, width);
        }

        text = info.getDutyName();
        left += VERTICALS[x - 1];
        width = VERTICALS[x++];


        if (dutyClass != null && dutyClass.getDutyCode() != null) {
            text += "(" + dutyClass.getDutyCode() + ")";
        }
        drawString(graphics, text, left, top, width);


        if (info.getDutyCode() != null && info.getWorkflowCode() != null) {
            text = info.getDutyCode();
            text += info.getWorkflowCode();
            left += VERTICALS[x - 1];
            width = VERTICALS[x++];

            int x1 = left, y1 = head - SPACE_SIZE_BASE * 2;
            if (dutyClass != null && dutyClass.getClassColor() != null) {
                graphics.setColor(getColor(dutyClass.getClassColor()));
                graphics.fillRect(x1 + 2, y1 + 2, SPACE_SIZE_BASE * 8 - 4, SPACE_SIZE_BASE * 2 - 4);
            }

            drawString(graphics, text, left, top, width);
        }
    }

    private static void drawTimeLine(Graphics graphics, int number, ScheduleInfo info, List<ScheduleWorkflow> workflows, Map<Integer, List<ScheduleWorkflowContent>> contentMap) {
        if (workflows == null) {
            return;
        }
        Integer shiftId = info.getDutyClassId();
        List<ScheduleWorkflowContent> contents = null;
        for (ScheduleWorkflow w :
                workflows) {
            if (w.getCode()!=null&&w.getClassId().equals(shiftId) &&  w.getCode().equals(info.getWorkflowCode())) {
                contents = contentMap.get(w.getId());
            }
        }
        if (contents != null) {
            int leftBase = 0;
            for (int i = 0; i < VERTICALS.length - 1; i++) {
                leftBase += VERTICALS[i];
            }
            setFont(graphics, 0.75);
            for (ScheduleWorkflowContent c :
                    contents) {
                int head = MARGIN + SPACE_SIZE_BASE * 4 + SPACE_SIZE_BASE * 2 * number + c.getLineNumber() * SPACE_SIZE_BASE;
                int top = head + SPACE_SIZE_BASE - (SPACE_SIZE_BASE - FONT_SIZE_BASE) / 2;
                int left = leftBase + c.getStartTime() / 10 * HOUR_BASE / 6;
                int width = (c.getEndTime() - c.getStartTime()) / 10 * HOUR_BASE / 6;
                graphics.setColor(getColor(c.getColor()));
                graphics.fillRect(left + 1, head + 1, width - 2, SPACE_SIZE_BASE - 2);
                drawString(graphics, c.getContent(), left, top, width);
            }
        }
    }


    private static void setFont(Graphics graphics, double ratio) {
        Font font = new Font("汉仪中隶书简", Font.BOLD, (int) (FONT_SIZE_BASE * ratio));
        graphics.setFont(font);
    }

    private static void drawString(Graphics graphics, String text, int left, int top, int width) {
        graphics.setColor(Color.black);
        int padding = Math.max((width - text.length() * SPACE_SIZE_BASE) / 2, 0);
        graphics.drawString(text, left + padding, top);
    }

    private static Color getColor(String shiftColor) {
        if (shiftColor==null){
            return Color.WHITE;
        }
        if (shiftColor.startsWith("rgb")){
            String color = shiftColor.substring(5, shiftColor.length() - 1);
            String[] split = color.split(",");
            return new Color(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
        }else {
            Integer c = 16777215;
            try{
                c=Integer.parseInt(shiftColor, 16);
            }catch (Exception e){
                return Color.WHITE;
            }
            return new Color(c >>> 16, c >>> 8 & 0xff, c & 0xff);
        }

    }

    private static String getTimeText(int time) {
        int hour = time / 60;
        int minute = time % 60;
        String h = null, m = null;
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = "" + hour;
        }
        if (minute < 10) {
            m = "0" + minute;
        } else {
            m = "" + minute;
        }
        return h + ":" + m;
    }

}
