package com.evtape.schedule.support.service;

import com.ducetech.app.model.ScheduleInfoTemplate;
import com.ducetech.app.model.ShiftModel;
import com.ducetech.app.model.ShiftSetting;
import com.ducetech.app.support.domain.PersonPair;
import com.ducetech.app.support.domain.PersonalDuty;
import com.ducetech.app.support.domain.Task;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleCalculator {
    public static final int WEEK_DAYS = 7;
    public static final int DAY_MINUTES = 24 * 60;
    private static final int THRESHOLD = 6 * 60;
    private static Logger logger = LoggerFactory.getLogger(ScheduleCalculator.class);


    public static List<ScheduleInfoTemplate> calculate(List<ShiftSetting> shifts, ShiftModel model) {
        int taskCountPerDay = 0;
        int totalHours = 0;
        for (int i = 0; i < shifts.size(); i++) {
            ShiftSetting shift = shifts.get(i);
            taskCountPerDay += shift.getShiftNum();
            totalHours += (shift.getTotalAt() * shift.getShiftNum());
        }
        int workerCount = taskCountPerDay * WEEK_DAYS / (WEEK_DAYS - model.getMinWeeklyRest()) + 1;
        int count = totalHours * WEEK_DAYS / model.getMaxWeeklyReason() + 1;
        workerCount = Math.max(workerCount, count);
        while (true){
            try{
                return calculate(shifts, workerCount, model);
            }catch (Exception e){
                workerCount++;
            }
        }
    }

    /**
     * 计算排班
     *
     * @param count
     * @param model
     * @param shifts
     * @return int
     */
    public static List<ScheduleInfoTemplate> calculate(List<ShiftSetting> shifts, int count, ShiftModel model) {
        LinkedHashMap<Integer, PersonalDuty> prMap = new LinkedHashMap<>();
        initPriorityQueue(shifts, count, model, prMap);
        List<String> relShiftIds = new ArrayList<>();
        for (ShiftSetting s :
                shifts) {
            if (s.getRelevanceId() != null) {
                relShiftIds.add(s.getRelevanceId());
            }
        }

        List<Task> taskList = initShifts(shifts);
        int index = 0;
        boolean forward = true;
        while (index < taskList.size() && index >= 0) {
            Task task = taskList.get(index);
            if (forward) {
                forward = calcForward(task, prMap, model);
            } else {
                forward = calcBackward(task, taskList, prMap, index);
            }
            if (forward) {
                ++index;
            } else {
                --index;
            }
        }
        if (relShiftIds.size() > 0) {
            adjustRelevance(taskList, prMap);
        }
        adjustWorkingHours(taskList, prMap,shifts);
        adjustCycling(taskList, prMap, shifts, relShiftIds);
        adjustDensity(taskList, prMap);
        printPersonalDuty(taskList, prMap);
        return trans(taskList, model);
    }

    private static void adjustDensity(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap) {
    }


    private static List<ScheduleInfoTemplate> trans(List<Task> taskList, ShiftModel model) {
        List<ScheduleInfoTemplate> result = new LinkedList<>();
        for (Task t :
                taskList) {
            ScheduleInfoTemplate template = new ScheduleInfoTemplate();
            template.setModelId(model.getModelId());
            template.setShiftId(t.shift.getShiftId());
            template.setShiftName(t.shift.getShiftName());
            template.setShiftMinutes(t.shift.getTotalAt());
            template.setShiftColor(t.shift.getShiftColor());
            template.setOrderIndex(t.userId * WEEK_DAYS + t.day);
            result.add(template);
        }
        return result;
    }


    /**
     * 无权有向图求最大路径，DFS
     *
     * @param taskList
     * @param prMap
     * @param relShifts
     */
    private static void adjustCycling(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap, List<ShiftSetting> shifts, List<String> relShifts) {
        for (Task t :
                taskList) {
            t.parent = null;
            t.relevance = null;
        }

        final Map<String, List<String>> availableMap = new HashMap<>();
        Map<String, List<Integer>> resourceMap = new HashMap<>();
        Map<Integer, String> beginMap = new HashMap<>();
        Map<Integer, String> endMap = new HashMap<>();
        availableMap.put(null, new LinkedList<String>());
        for (ShiftSetting s1 :
                shifts) {
            if (judgeConnect(null, s1, relShifts)) {
                availableMap.get(null).add(s1.getShiftId());
            }
        }
        for (ShiftSetting s :
                shifts) {
            ArrayList<String> aList = new ArrayList<>();
            availableMap.put(s.getShiftId(), aList);
            if (judgeConnect(s, null, relShifts)) {
                aList.add(null);
            }
            for (ShiftSetting s1 :
                    shifts) {
                if (judgeConnect(s, s1, relShifts)) {
                    aList.add(s1.getShiftId());
                }
            }
        }
        for (String key : availableMap.keySet()) {
            Collections.sort(availableMap.get(key), new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return availableMap.get(o1).size() - availableMap.get(o2).size();
                }
            });
        }

        int start = -1;
        for (int i = 0; i < prMap.size(); ++i) {
            PersonalDuty p = prMap.get(i);
            Task task = p.workingMap.get(0);
            Task task2 = p.workingMap.get(6);
            if (start == -1 && task == null) {
                start = i;
            }
            String shiftId = task == null ? null : task.shift.getShiftId();
            String shiftId2 = task2 == null ? null : task2.shift.getShiftId();
            if (!resourceMap.containsKey(shiftId)) {
                resourceMap.put(shiftId, new LinkedList<Integer>());
            }
            beginMap.put(i, shiftId);
            endMap.put(i, shiftId2);
            resourceMap.get(shiftId).add(i);
        }

        //TODO connect all the relevant tasks

        long l = System.currentTimeMillis();
        Stack<Integer> circuit = new Stack<>();
        boolean b = checkCircuitDFS(circuit, availableMap, resourceMap, beginMap, endMap, start, prMap.size());
        logger.info("DFS cost: {}", System.currentTimeMillis() - l);


        if (b) {
            LinkedHashMap<Integer, PersonalDuty> cloned = ObjectUtils.clone(prMap);
            prMap.clear();
            for (int i = 0; i < circuit.size(); i++) {
                PersonalDuty p = new PersonalDuty();
                prMap.put(i, p);
                p.userId = i;
                int user = circuit.get(i);
                PersonalDuty pd = cloned.get(user);
                List<Task> tasks = new ArrayList<>(pd.workingMap.values());
                for (Task t :
                        tasks) {
                    unsetTask(taskList, cloned, t);
                    setTask(p, t);
                }
            }
        } else {
            throw new IllegalStateException("不能找到合法循环");
        }

    }

    /**
     * DFS求最长路径，效率太低
     *
     * @param circuit
     * @param aMap
     * @param current
     * @return
     */
    private static boolean checkCircuitDFS(Stack<Integer> circuit, Map<String, List<String>> aMap, Map<String, List<Integer>> rMap,
                                           Map<Integer, String> bMap, Map<Integer, String> eMap, Integer current, Integer max) {
        circuit.push(current);
        String endShift = eMap.get(current);
        String beginShift = bMap.get(current);
        rMap.get(beginShift).remove(current);
        List<String> available = aMap.get(endShift);
        for (String begin : available) {
            List<Integer> resources = rMap.get(begin);

            if (resources.size() > 0) {
                Integer next = resources.get(0);
                boolean r = checkCircuitDFS(circuit, aMap, rMap, bMap, eMap, next, max);
                if (r) {
                    return r;
                } else {
                    continue;
                }
            } else {
                if (circuit.size() == max) {
                    return true;
                }
            }
        }
        circuit.pop();
        rMap.get(beginShift).add(current);
        return false;
    }

    private static boolean judgeConnect(ShiftSetting s1, ShiftSetting s2, List<String> relShifts) {
        if (s1 == null) {
            if (s2 != null && !relShifts.contains(s2.getShiftId())) {
                return true;
            }
        } else {
            if (s2 == null) {
                if (s1.getRelevanceId() == null) {
                    return true;
                }
            } else {

                if (s1.getRelevanceId() != null) {
                    if (s1.getRelevanceId().equals(s2.getShiftId())) {
                        return true;
                    }
                } else if (s1.getEndAt() + s1.getIntervalAt() - 24 * 60 <= s2.getStartAt()
                        && !relShifts.contains(s2.getShiftId())) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 调整关联班次次数，要求最多和最少次数相差不超过1次.计算所有人的关联班次次数，找出最多和最少次数的用户，依次匹配是否能够把多夜班的夜班调整到少的人身上
     * 要求调整的两个班不能有夜班和下夜。
     *
     * @param taskList 任务列表
     * @param prMap    用户
     */
    private static void adjustRelevance(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap) {
        while (true) {
            Map<Integer, List<PersonalDuty>> countMap = new HashMap<>();
            for (PersonalDuty p :
                    prMap.values()) {
                int count = 0;
                for (int i = 0; i < WEEK_DAYS; i++) {
                    if (p.hasWork(i) && p.workingMap.get(i).shift.getRelevanceId() != null) {
                        count++;
                    }
                }
                if (!countMap.containsKey(count)) {
                    countMap.put(count, new ArrayList<PersonalDuty>());
                }
                countMap.get(count).add(p);
            }
            if (countMap.size() <= 2) {
                return;
            }
            int max = 0, min = Integer.MAX_VALUE;
            for (Integer c :
                    countMap.keySet()) {
                if (max < c) {
                    max = c;
                }
                if (min > c) {
                    min = c;
                }
            }
            List<PersonalDuty> pMax = countMap.get(max);
            List<PersonalDuty> pMin = countMap.get(min);
            Map<PersonalDuty, Map<PersonalDuty, int[]>> solutionMap = new HashMap<>();
            Set<PersonalDuty> sourceSet = new HashSet<>();
            for (PersonalDuty pm : pMax) {
                for (PersonalDuty pn :
                        pMin) {
                    PersonalDuty pf = pMax.size() > pMin.size() ? pn : pm;
                    PersonalDuty pl = pMax.size() > pMin.size() ? pm : pn;
                    if (!solutionMap.containsKey(pf)) {
                        solutionMap.put(pf, new HashMap<PersonalDuty, int[]>());
                    }
                    int[] target = checkRelevanceChangeable(pm, pn);
                    if (target != null) {
                        solutionMap.get(pf).put(pl, target);
                        sourceSet.add(pl);
                    }
                }
            }
            for (Map<PersonalDuty, int[]> p :
                    solutionMap.values()) {
                if (p.size() == 0) {
                    throw new IllegalStateException("不能协调所有关联班次");
                }
            }
            if (sourceSet.size() < solutionMap.size()) {
                throw new IllegalStateException("不能协调所有关联班次");
            }
            List<PersonPair> result = getPersonPairs(solutionMap, sourceSet);
            for (PersonPair pair :
                    result) {
                changePair(taskList, prMap, pair);
                logger.info("change task: {} --> {}", pair.pm, pair.pn);
                printPersonalDuty(taskList, prMap);
            }
            logger.info("after adjust relevance");
        }
    }

    public static void changePair(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap, PersonPair pair) {
        List<Task> list1 = new ArrayList<>(), list2 = new ArrayList<>();
        for (int i = pair.range[0]; i < pair.range[1]; i++) {
            Task t1 = pair.pm.workingMap.get(i);
            unsetToList(taskList, prMap, list1, t1);
            Task t2 = pair.pn.workingMap.get(i);
            unsetToList(taskList, prMap, list2, t2);
        }
        setTasks(pair.pm, list2);
        setTasks(pair.pn, list1);
    }

    private static void unsetToList(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap, List<Task> list, Task t) {
        if (t != null) {
            list.add(t);
            if (t.relevance != null) {
                unsetTask(taskList, prMap, t.relevance);
            } else {
                unsetTask(taskList, prMap, t);
            }
        }
    }

    private static List<PersonPair> getPersonPairs(final Map<PersonalDuty, Map<PersonalDuty, int[]>> solutionMap, Set<PersonalDuty> sourceSet) {
        List<PersonPair> result = new LinkedList<>();
        while (solutionMap.size() > 0) {
            PersonalDuty pd = null, pv = null;
            final Map<PersonalDuty, Integer> referenceMap = new HashMap<>();
            for (PersonalDuty p :
                    solutionMap.keySet()) {
                if (solutionMap.get(p).size() == 1) {
                    pd = p;
                    pv = solutionMap.get(pd).keySet().iterator().next();
                    break;
                }
                for (PersonalDuty v : solutionMap.get(p).keySet()) {
                    if (!referenceMap.containsKey(v)) {
                        referenceMap.put(v, 1);
                    } else {
                        referenceMap.put(v, referenceMap.get(v) + 1);
                    }
                }
            }
            if (pd == null) {
                List<PersonalDuty> keys = new ArrayList<>(solutionMap.keySet());
                Collections.sort(keys, new Comparator<PersonalDuty>() {
                    @Override
                    public int compare(PersonalDuty o1, PersonalDuty o2) {
                        return solutionMap.get(o2).size() - solutionMap.get(o1).size();
                    }
                });
                pd = keys.get(0);
                List<PersonalDuty> values = new ArrayList<>(solutionMap.get(pd).keySet());
                Collections.sort(values, new Comparator<PersonalDuty>() {
                    @Override
                    public int compare(PersonalDuty o1, PersonalDuty o2) {
                        return referenceMap.get(o1) - referenceMap.get(o2);
                    }
                });
                pv = values.get(0);
            }
            if (pd == null) {
                throw new IllegalArgumentException("排班错误");
            }
            PersonPair pair = new PersonPair();
            pair.pm = pd;
            pair.pn = pv;
            pair.range = solutionMap.get(pd).get(pv);
            result.add(pair);
            solutionMap.remove(pd);
            for (PersonalDuty p :
                    solutionMap.keySet()) {
                solutionMap.get(p).remove(pv);
            }
        }
        return result;
    }

    private static void setTasks(PersonalDuty p, List<Task> list) {
        for (Task t :
                list) {
            setTask(p, t);
        }
    }

    private static int[] checkRelevanceChangeable(PersonalDuty pMax, PersonalDuty p) {
        for (int i = 2; i < WEEK_DAYS; i++) {
            for (int j = 0; j <= WEEK_DAYS - i; j++) {
                int start = j, end = j + i;
                int c1 = checkRelevanceCount(pMax, start, end);
                int c2 = checkRelevanceCount(p, start, end);
                if (c1 - c2 != 1) {
                    continue;
                }
                int total1 = checkRestCount(pMax, 0, 7);
                int total2 = checkRestCount(p, 0, 7);
                c1 = checkRestCount(pMax, start, end);
                c2 = checkRestCount(p, start, end);
                if (total1 - total2 != c1 - c2) {
                    continue;
                }
                boolean b = checkConnectible(pMax, p, start, end);
                if (b) {
                    return new int[]{start, end};
                }
            }
        }
        return null;
    }

    private static int checkRestCount(PersonalDuty p, int start, int end) {
        int c = 0;
        for (int i = start; i < end; i++) {
            if (p.workingMap.get(i) == null) {
                c++;
            }
        }
        return c;
    }


    private static int checkRelevanceCount(PersonalDuty p, int start, int end) {
        Task first = p.workingMap.get(start);
        if (first != null && first.parent != null) {
            return -1;
        }
        Task last = p.workingMap.get(end - 1);
        if (last != null && last.shift.getRelevanceId() != null) {
            return -1;
        }
        int c = 0;
        for (int i = start; i < end; i++) {
            Task task = p.workingMap.get(i);
            if (task != null && task.shift.getRelevanceId() != null) {
                if (i + 1 < end) {
                    Task t = p.workingMap.get(i + 1);
                    if (t != null && t.shift.getShiftId().equals(task.shift.getRelevanceId())) {
                        c++;
                    }
                }
            }
        }
        return c;
    }

    private static boolean checkConnectible(PersonalDuty pMax, PersonalDuty p, int start, int end) {
        if (start > 0) {
            Task prefix1 = pMax.workingMap.get(start - 1);
            Task prefix2 = p.workingMap.get(start - 1);
            Task first1 = pMax.workingMap.get(start);
            Task first2 = p.workingMap.get(start);
            if (!isConnectible(prefix1, first2) || !isConnectible(prefix2, first1)) {
                return false;
            }
        }
        if (end <= WEEK_DAYS) {
            Task suffix1 = pMax.workingMap.get(end);
            Task suffix2 = p.workingMap.get(end);
            Task last1 = pMax.workingMap.get(end - 1);
            Task last2 = p.workingMap.get(end - 1);
            if (!isConnectible(last1, suffix2) || !isConnectible(last2, suffix1)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isConnectible(Task t1, Task t2) {
        if (t1 == null && t2 == null) {
            return false;
        }
        if (t1 != null && t2 != null) {
            int a = t1.shift.getEndAt() + t1.shift.getIntervalAt();
            int b = t2.shift.getStartAt();
            return (a - DAY_MINUTES) < b;
        }
        return true;
    }


    private static void adjustWorkingHours(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap,List<ShiftSetting> settings) {
        final List<PersonalDuty> list = new ArrayList<>(prMap.values());
        boolean loop = true, lazy = true;
        List<Integer> distances=new ArrayList<>();
        int dist=Integer.MAX_VALUE;
        for (ShiftSetting s :
                settings) {
            dist=Math.min(dist,s.getTotalAt());
        }
        dist=Math.max(THRESHOLD,dist);
        while (loop) {
            Collections.sort(list, new Comparator<PersonalDuty>() {
                @Override
                public int compare(PersonalDuty o1, PersonalDuty o2) {
                    return o1.total - o2.total;
                }
            });
            PersonalDuty laziest = list.get(0);
            PersonalDuty busiest = list.get(list.size() - 1);
            int distance = busiest.total - laziest.total;
            distances.add(distance);
            checkDistances(distances,prMap.size());
            if (distance > dist) {
                logger.debug("total check --distance, distance: {}", distance);
                if (lazy) {
                    loop = adjustBusiestHours(busiest, list, taskList, prMap);
                    if (loop) {
                        lazy = false;
                    } else {
                        loop = adjustLaziestHours(laziest, list, taskList, prMap);
                        if(!loop){
                            throw new IllegalStateException("can not fix working hours");
                        }
                    }
                } else {
                    loop = adjustLaziestHours(laziest, list, taskList, prMap);
                    if (loop) {
                        lazy = true;
                    } else {
                        loop = adjustBusiestHours(busiest, list, taskList, prMap);
                        if(!loop){
                            throw new IllegalStateException("can not fix working hours");
                        }
                    }
                }
            } else {
                return ;
            }
        }
    }

    private static void checkDistances(List<Integer> distances,int c) {
        if(distances.size()<c/2){
            return ;
        }
        int count=0,cursor=distances.get(distances.size()-1);
        for(int i=distances.size()-2;i>=0;i--){
            if (distances.get(i)==cursor){
                count++;
            }
        }
        if (count>c/2){
            throw new IllegalStateException("can not fix distances");
        }
    }

    /**
     * 调整总工时，要求最高工时和最低工时不超过<tt>ScheduleCalculator.THRESHOLD<tt>, 如果超过范围，则找到工作时间最高的人，和最低的人
     * 交换二人的班次，将工时多的交换给总工时少的人。
     *
     * @param busiest
     * @param list
     * @param taskList
     * @param prMap
     */
    private static boolean adjustBusiestHours(PersonalDuty busiest, List<PersonalDuty> list, List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap) {
        for (int i = 0; i <= list.size() / 2; i++) {
            PersonalDuty laziest = list.get(i);
            List<PersonPair> pairs = checkChangeableDays(busiest, laziest);
            Collections.sort(pairs, new Comparator<PersonPair>() {
                @Override
                public int compare(PersonPair o1, PersonPair o2) {
                    return (o1.pmHours - o1.pnHours) - (o2.pmHours - o2.pnHours);
                }
            });
            if (pairs.size() > 0) {
                changePair(taskList, prMap, pairs.get(0));
                return true;
            }
        }
        return false;
    }

    private static boolean adjustLaziestHours(PersonalDuty laziest, List<PersonalDuty> list, List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap) {
        for (int i = list.size() - 1; i >= list.size() / 2; i--) {
            PersonalDuty busiest = list.get(i);
            List<PersonPair> pairs = checkChangeableDays(busiest, laziest);
            Collections.sort(pairs, new Comparator<PersonPair>() {
                @Override
                public int compare(PersonPair o1, PersonPair o2) {
                    return (o1.pmHours - o1.pnHours) - (o2.pmHours - o2.pnHours);
                }
            });
            if (pairs.size() > 0) {
                changePair(taskList, prMap, pairs.get(0));
                return true;
            }
        }
        return false;
    }

    private static List<PersonPair> checkChangeableDays(PersonalDuty pb, PersonalDuty pl) {
        List<PersonPair> result = new ArrayList<>();
        for (int i = 1; i < WEEK_DAYS; i++) {
            for (int j = 0; j < WEEK_DAYS; j++) {
                int start = j, end = j + i;
                if (end>=WEEK_DAYS){
                    continue;
                }
                int c1 = checkRelevanceCount(pl, start, end);
                int c2 = checkRelevanceCount(pb, start, end);
                if (c1 != c2) {
                    continue;
                }
                c1 = checkRestCount(pl, start, end);
                c2 = checkRestCount(pb, start, end);
                if (c1 != c2) {
                    continue;
                }
                c1 = calcWorkingMinutes(pb, start, end);
                c2 = calcWorkingMinutes(pl, start, end);
                if (c1 <= c2) {
                    continue;
                }
                boolean b = checkConnectible(pl, pb, start, end);
                if (b) {
                    PersonPair r = new PersonPair();
                    r.pm = pb;
                    r.pn = pl;
                    r.pmHours = c1;
                    r.pmHours = c2;
                    r.range = new int[]{start, end};
                    result.add(r);
                }
            }
        }
        return result;
    }

    private static int calcWorkingMinutes(PersonalDuty pb, int start, int end) {
        int r = 0;
        for (int i = start; i < end; i++) {
            Task task = pb.workingMap.get(i);
            if (task != null) {
                r += task.shift.getTotalAt();
            }
        }
        return r;
    }

    /**
     * 判断任务方向
     *
     * @param task
     * @return
     */
    private static boolean judgeDirection(Task task) {
        if (!task.userAvailable.isEmpty()) {
            PersonalDuty pd = task.userAvailable.poll();
            setTask(pd, task);
            return true;
        }
        return false;
    }

    /**
     * 任务回退
     *
     * @param task
     * @param taskList
     * @param prMap
     * @param index
     * @return
     */
    private static boolean calcBackward(Task task, List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap, int index) {
        if (task.parent != null) {
            return false;
        }
        clearUserAvailable(task, taskList, prMap.get(task.userId), index);
        if (task.relevance != null) {
            unsetTask(taskList, prMap, task.relevance);
        } else {
            unsetTask(taskList, prMap, task);
        }
        return judgeDirection(task);
    }

    /**
     * 清除任务可选用户列表
     *
     * @param task
     * @param taskList
     * @param p
     * @param index
     */
    private static void clearUserAvailable(Task task, List<Task> taskList, PersonalDuty p, int index) {
        for (int i = index - 1; i >= 0; i--) {
            Task t = taskList.get(index);
            if (sameShift(t, task)) {
                t.userAvailable.remove(p);
            } else {
                break;
            }
        }
    }

    /**
     * 判断任务是否为同一班次
     *
     * @param t1
     * @param t2
     * @return
     */
    private static boolean sameShift(Task t1, Task t2) {
        ShiftSetting shift1 = t1.shift;
        ShiftSetting shift2 = t2.shift;
        return t1.day == t2.day && shift1.getShiftId().equals(shift2.getShiftId());
    }


    /**
     * 任务列表向前计算
     *
     * @param task
     * @param prMap
     * @param position
     * @return
     */
    private static boolean calcForward(Task task, LinkedHashMap<Integer, PersonalDuty> prMap, ShiftModel position) {
        if (task.parent != null) {
            return true;
        }
        task.userAvailable.clear();
        for (PersonalDuty p :
                prMap.values()) {
            if (isAvailable(p, position, task, prMap)) {
                task.userAvailable.add(p);
            }
        }
        return judgeDirection(task);
    }

    /**
     * 打印排班情况
     *
     * @param taskStack
     * @param prMap
     */
    private static void printPersonalDuty(List<Task> taskStack, LinkedHashMap<Integer, PersonalDuty> prMap) {
        Map<Integer, List<Task>> tasks = getUserTasks(taskStack);
        int length = WEEK_DAYS + 2;
        String[][] table = new String[tasks.size() + 1][];
        table[0] = new String[length];
        for (int i = 0; i < length - 1; i++) {
            switch (i % 8) {
                case 0:
                    table[0][i] = "合";
                    break;
                case 1:
                    table[0][i] = "一";
                    break;
                case 2:
                    table[0][i] = "二";
                    break;
                case 3:
                    table[0][i] = "三";
                    break;
                case 4:
                    table[0][i] = "四";
                    break;
                case 5:
                    table[0][i] = "五";
                    break;
                case 6:
                    table[0][i] = "六";
                    break;
                case 7:
                    table[0][i] = "日";
                    break;

                default:
                    break;
            }
        }
        int i = 0;
        for (int user = 0; user < prMap.size(); user++) {
            List<Task> list = tasks.get(user);
            table[i + 1] = new String[length];
            table[i + 1][0] = user + "";
            for (int j = 0; j < list.size(); j++) {
                int d = list.get(j).day;
                int t = d / 7;
                int r = d % 7;
                table[i + 1][t * 8 + r + 1] = list.get(j).shift.getShiftName();
            }
            i++;
        }
        for (i = 0; i < 1; i++) {
            int n = 0;
            for (int id = 0; id < prMap.size(); id++) {
                table[n + 1][(i + 1) * 8] = prMap.get(id).total / 60 + "";
                n++;
            }
        }
        for (String[] array : table) {
            StringBuilder log = new StringBuilder();
            for (String string : array) {
                log.append((string == null ? "休" : string) + "   ");
            }
            logger.debug(log.toString());
        }
    }

    /**
     * @param taskStack
     * @return
     */
    private static Map<Integer, List<Task>> getUserTasks(List<Task> taskStack) {
        Map<Integer, List<Task>> pMap = new HashMap<>();
        for (Task task : taskStack) {
            if (task.userId == null) {
                continue;
            }
            if (!pMap.containsKey(task.userId)) {
                pMap.put(task.userId, new LinkedList<Task>());
            }
            pMap.get(task.userId).add(task);
        }
        return pMap;
    }


    /**
     * 分配任务到用户
     *
     * @param p
     * @param t
     */
    private static void setTask(PersonalDuty p, Task t) {
//        logger.debug("set task days:" + t + "," + p);
        if (t.userId != null) {
            if (t.userId != p.userId) {
                throw new IllegalArgumentException("同一班次排了多人");
            } else {
                return;
            }
        }
        t.userId = p.userId;
        p.available = t.priAfter;
        p.addWorkingDays(t.day, t);
        if (t.relevance != null) {
            setTask(p, t.relevance);
        }
    }

    /**
     * 释放用户任务
     *
     * @param taskList
     * @param prMap
     * @param t
     */
    private static void unsetTask(List<Task> taskList, LinkedHashMap<Integer, PersonalDuty> prMap, Task t) {
//        logger.debug("unset task days:" + t);
        if (t.userId == null) {
            return;
        }
        PersonalDuty p = prMap.get(t.userId);
        t.userId = null;
        Task prev = null;
        for (Task e : taskList) {
            if (p.userId.equals(e.userId)) {
                prev = e;
            }
            if (e == t) {
                break;
            }
        }
        p.available = prev == null ? 0 : prev.priAfter;
        p.removeWorkingDays(t.day);
        if (t.parent != null) {
            unsetTask(taskList, prMap, t.parent);
        }
    }

    /**
     * 判断用户是否可以分配当前任务
     *
     * @param peek
     * @param position
     * @param t
     * @param prMap
     * @return
     */
    private static boolean isAvailable(PersonalDuty peek, ShiftModel position, Task t, LinkedHashMap<Integer, PersonalDuty> prMap) {

        int pri = t.priBefore;
        if (pri < peek.available) {
//            logger.debug("unavailable");
            return false;
        }

        if (peek.hasWork(t.day)) {
//            logger.debug("has work " + t.day);
            return false;
        }
        if (t.relevance != null && peek.hasWork(t.day + 1)) {
//            logger.debug("has work " + t.day);
            return false;
        }

        return true;
    }

    /**
     * 处理班次关系，生成任务列表
     *
     * @param shifts
     * @return
     */
    private static List<Task> initShifts(List<ShiftSetting> shifts) {
        Map<String, ShiftSetting> shiftMap = new HashMap<>();
        Set<String> relShiftSet = new HashSet<>();
        for (ShiftSetting s : shifts) {
            shiftMap.put(s.getShiftId(), s);
            if (s.getRelevanceId() != null) {
                relShiftSet.add(s.getRelevanceId());
            }
        }
        Collections.sort(shifts, new Comparator<ShiftSetting>() {
            @Override
            public int compare(ShiftSetting o1, ShiftSetting o2) {
                return o2.getTotalAt() - o1.getTotalAt();
            }
        });
        List<Task> PersonalDuty = new LinkedList<>();
        for (int d = 0; d < WEEK_DAYS; d++) {
            for (ShiftSetting s : shifts) {
                if (relShiftSet.contains(s.getShiftId()) && d != 0) {
                    continue;
                }
                for (int i = 0; i < s.getShiftNum(); i++) {
                    Task t = new Task();
                    t.shift = s;
                    t.day = d;
                    t.priBefore = d * DAY_MINUTES + s.getStartAt();
                    t.priAfter = d * DAY_MINUTES + s.getEndAt() + s.getIntervalAt();
                    PersonalDuty.add(t);
                    if (s.getRelevanceId() != null && d != WEEK_DAYS - 1) {
                        ShiftSetting s2 = shiftMap.get(s.getRelevanceId());
                        Task t2 = new Task();
                        t2.shift = s2;
                        t2.day = d + 1;
                        t2.priBefore = t2.day * DAY_MINUTES + s2.getStartAt();
                        t2.priAfter = t2.day * DAY_MINUTES + s2.getEndAt() + s.getIntervalAt();
                        t.relevance = t2;
                        t2.parent = t;
                        PersonalDuty.add(t2);
                    }
                }
            }
        }
        return PersonalDuty;
    }

    /**
     * 生成用户列表
     *
     * @param shifts
     * @param post
     * @param prMap
     */
    public static void initPriorityQueue(List<ShiftSetting> shifts, int count, ShiftModel post,
                                         LinkedHashMap<Integer, PersonalDuty> prMap) {
        for (int i = 0; i < count; i++) {
            PersonalDuty d = new PersonalDuty();
            d.userId = i;
            prMap.put(d.userId, d);
        }
    }

    public static void main(String[] args) throws ParseException {
        List<ShiftSetting> shifts = new ArrayList<>();
        ShiftSetting s = new ShiftSetting();
        s.setStartAt(0);
        s.setEndAt(540);
        s.setTotalAt(540);
        s.setIntervalAt(12 * 60);
        s.setShiftName("下");
        s.setShiftId("下");
        s.setShiftNum(2);
        shifts.add(s);

        s = new ShiftSetting();
        s.setStartAt(420);
        s.setEndAt(1020);
        s.setTotalAt(600);
        s.setIntervalAt(12 * 60);
        s.setShiftName("白");
        s.setShiftId("白");
        s.setShiftNum(2);
        shifts.add(s);

        s = new ShiftSetting();
        s.setStartAt(420);
        s.setEndAt(960);
        s.setTotalAt(540);
        s.setIntervalAt(12 * 60);
        s.setShiftId("中");
        s.setShiftName("中");
        s.setShiftNum(1);
        shifts.add(s);

        s = new ShiftSetting();
        s.setStartAt(960);
        s.setEndAt(1260);
        s.setTotalAt(300);
        s.setIntervalAt(12 * 60);
        s.setShiftName("小");
        s.setShiftId("小");
        s.setShiftNum(2);
        shifts.add(s);

        s = new ShiftSetting();
        s.setStartAt(1020);
        s.setEndAt(1440);
        s.setTotalAt(420);
        s.setIntervalAt(0);
        s.setRelevanceId("下");
        s.setShiftId("夜");
        s.setShiftName("夜");
        s.setShiftNum(2);
        shifts.add(s);
        ShiftModel position = new ShiftModel();
        position.setMinWeeklyRest(2);
        position.setMaxWeeklyRest(3);
        position.setMinWeeklyReason(35 * 60);
        position.setMaxWeeklyReason(41 * 60);

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        ScheduleCalculator.calculate(shifts, 13, position);

    }

}
